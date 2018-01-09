#include <SoftwareSerial.h>
#include <EEPROM.h>
#define DEBUG
SoftwareSerial bluetooth(10, 11); // RX, TX

#define latchPin 2                          //Pin connected to ST_CP of 74HC595
#define clockPin 3                          //Pin connected to SH_CP of 74HC595
#define dataPin 4                           //Pin connected to DS of 74HC595
#define pwmFeux 6                           //pin de gestion du pwm des feux
#define offPin 12                           //pin d'extinction
#define LED 13                              //pin de led témoin de connection

#define delaiPriseEtat 3                    //temps en secondes entre 2 vérifications de connexion
#define tempsDeconnexion 6                  //temps en secondes à partir duquel on se considère déconnecté
#define tempsReconnexion 2                  //temps en secondes durant lequel on tente de se reconnecter 
unsigned long tempsReponse = 0;             //temps depuis la dernière réponse
unsigned long tempsEnvoiReconnexion = 0;    //temps depuis le dernier envoi de reconnection
unsigned long tempsEnvoieBatterie = 0;      //temps depuis le dernier envoi du niveau de batterie
unsigned long tempsK2000 = 0;               //temps depuis la dernière animation K2000 sur les feux de position
unsigned long millisLed = 0;                //temps depuis la dernière animation de lumière témoin sur le bouton
byte tempsArret = 0;                        // temps en secondes avant d'éteindre le système si aucun appareil n'est connecté

byte commande[4];                           // Buffer qui stock la dernière commande reçue

boolean connecte = false;                   // Indique si l'application est connectée et authentifiée

unsigned long millisDernierClignotement;    // Gestion du temps pour le clignotement
bool sensClignotement;                      // Indique si c'est le moment d'éteindre ou d'allumer les LEDs
#define millisDecalage 20                   // Temps (en ms) entre l'allumage/l'arrêt de chaque LED dans l'animation
#define millisRAZ 400                       // Temps (en ms) entre le démarrage et l'arrêt d'une LED & vice-versa
bool ledAllumee[3][8];                      // Indique si la LED est allumée
byte clignoAllume[2];                       // Indique si le clignotant est allumée
boolean fCirculation = false;               // Indique si les feux de circulation sont allumés
byte puissancePWM = 128;                    // Indique la puissance des feux de circulation
boolean feuxStopOn = false;                 // indique si les feux de stop sont allumés
bool ledChange;                             // Indique si une LED a changé d'état (ça ne sert à rien d'actualiser la sortie si rien n'a changé)
bool led = false;                           // Etat actuel de la LED d'allumage

byte niveauBatterie = 1;                    // Niveau de batterie, de 1 à 10

byte animK2000 = 1;                         // LED actuellement allumée pour l'animation k2000
bool sensAnimK2000 = false;                 // Sens de l'animation k2000


/**
 * Initialisation de la carte et des modules
 */
void setup() {
  // Lecture du temps d'arrêt
  switch(EEPROM.read(10)){
    case 0: // 30 secondes
      tempsArret = 30;
      break;

    case 1: // 1 minute
      tempsArret = 60;
      break;

    case 2: // 2 minutes
      tempsArret = 120;
      break;

    case 3: // 5 minutes
      tempsArret = 300;
      break;

    default: // 10 minutes
      tempsArret = 600;
      break;
  }
  // On lit la puissance des PWM
  puissancePWM = EEPROM.read(11);
  analogWrite(pwmFeux, 42);
  // Initialisation des pins
  pinMode(offPin, OUTPUT);
  digitalWrite(offPin, LOW);
  pinMode(pwmFeux, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(dataPin, OUTPUT);
  pinMode(latchPin, OUTPUT);
  // On éteint toutes les LEDs
  digitalWrite(latchPin, LOW);
  // Feux stop
  digitalWrite(dataPin, LOW);
  for(int j=0; j<8; j++){
    digitalWrite(clockPin, LOW);
    digitalWrite(clockPin, HIGH);
  }
  // Clignos
  digitalWrite(dataPin, HIGH);
  for(int i=0; i<2; i++){
    for(int j=0; j<8; j++){
      ledAllumee[i][j] = false;
    digitalWrite(clockPin, LOW);
    digitalWrite(clockPin, HIGH);
    }
  }
  // Feux
  digitalWrite(dataPin, LOW);
  for(int j=0; j<8; j++){
    digitalWrite(clockPin, LOW);
    digitalWrite(clockPin, HIGH);
  }
  ledChange = true;
  digitalWrite(latchPin, HIGH);
  //initialisation communication série
  Serial.begin(115200);
  //initialisation communication BlueTooth
  bluetooth.begin(38400);
  Serial.println("Demarrage termine");
  digitalWrite(LED, HIGH);
}

/**
 * Boucle principale
 */
void loop() {
  // Gestion de la LED d'allumage
  ledAllumage();
  // Gestion du clignotement
  cligno();
  // On récupère les derniers messages
  traitementSerial();
  // On traite la dernière commande reçue
  int dc = derniereCommande();
  #ifdef DEBUG
  if(dc != 0){
    Serial.print("Reception de ");
    Serial.println(dc);
  }
  #endif
  if (connecte){ // On vérifie que l'application est authentifiée
    // Si on a reçu quelque chose, on remet à 0 le watchdog
    if(dc != 0)
      tempsReponse = millis();
    switch(dc){
      case 10: // Eteindre cligno droit
        eteindreClignos();
        send(1,0);
      break;

      case 11: // Allumer cligno droit
        allumerCligno(0);
        send(1,1);
      break;

      case 20: // Eteindre cligno gauche
        eteindreClignos();
        send(2,0);
      break;

      case 21: // Allumer cligno gauche
        allumerCligno(1);
        send(2,1);
      break;

      case 30: // Eteindre warnings
        eteindreClignos();
        send(3,0);
      break;

      case 31: // Allumer warnings
        clignoAllume[0]=2;
        clignoAllume[1]=2;
        send(3,1);
      break;

      case 40: // Eteindre feu de stop
        feuxStopOn = false;
        ledChange = true;
        send(4, 0);
      break;

      case 41:  // Allumer feu de stop
        feuxStopOn = true;
        ledChange = true;
        send(4, 1);
      break;

      case 50: // Eteindre feux de position
        fCirculation = false;
        ledChange = true;
        send(5,0);
      break;

      case 51: // Allumer feux de position
        fCirculation = true;
        ledChange = true;
        send(5,1);
      break;

      case 60: // Changement de la puissance des feux
      case 61:
      case 62:
      case 63:
      case 64: 
        EEPROM.write(11, (dc-59)*42);
        puissancePWM = EEPROM.read(11);
        analogWrite(pwmFeux, puissancePWM);
      break;

      case 80: // Arrêt du module et déconnexion
        Serial.println("Arret du module");
        digitalWrite(offPin, HIGH);
        eteindreClignos();
        for(int i=0; i<8; i++){
          ledAllumee[0][i] = false;
          ledAllumee[1][i] = false;
        }
        cligno();
        connecte = false;
        // On bloque le système
        while(true){
          digitalWrite(LED, HIGH);
          delay(500);
          digitalWrite(LED, LOW);
          delay(500);
        }
      break;

      case 91:
      case 92:
      case 93:
      case 94:
      case 95: // Changement du temps d'arrêt
        // Enregistrement du temps d'arrêt
        EEPROM.write(10, dc-91);
        // Mise à jour du temps d'arrêt
        switch(EEPROM.read(10)){
          case 0: // 30 secondes
            tempsArret = 30;
            break;

          case 1: // 1 minute
            tempsArret = 60;
            break;

          case 2: // 2 minutes
            tempsArret = 120;
            break;

          case 3: // 5 minutes
            tempsArret = 300;
            break;

          default: // 10 minutes
            tempsArret = 600;
            break;
        }
      break;

      default:
      break;
    }
  }
  // Si l'application n'est pas encore authentifiée, on regarde si elle s'authentifie
  else if(dc == 99){
    Serial.println("telephone authentifie");
    connecte = true;
    send(9, 0); //on envoi un message au téléphone, témoignant de la connection
    //on initialise les variables de temps
    tempsEnvoiReconnexion = millis();
    tempsReponse = millis();
    tempsEnvoieBatterie = millis() - 59000;
    ledChange = true; // On met à 0 les LEDs
    analogWrite(pwmFeux, puissancePWM); //on active la masse pwm à la fréquence définie pour les feux
  }
  // Si le téléphone ne répond plus, on considère qu'il est déconnecté
  if (millis() - tempsReponse > (long)tempsDeconnexion*1000 && connecte){
    connecte = false;
    analogWrite(pwmFeux, 42); //on active la masse pwm à 20% de la puissance max
    Serial.println("Deconnexion du telephone (ping timeout)");
  }
  // Au bout d'un certain temps sans connexion, on éteint le module
  if (millis() - tempsReponse > (long)tempsArret*1000){
    Serial.println("Arret du module (mise en veille auto)");
    digitalWrite(offPin, HIGH);
    //on éteind tous les feux
    eteindreClignos();
    for(int i=0; i<8; i++){
      ledAllumee[0][i] = false;
      ledAllumee[1][i] = false;
    }
    cligno();
    // On bloque le système
    while(true){
      digitalWrite(LED, HIGH);
      delay(500);
      digitalWrite(LED, LOW);
      delay(500);
    }
  }
  // On envoie un ping
  if (millis() - tempsReponse > (long) tempsReconnexion*1000 && connecte && millis() - tempsEnvoiReconnexion > (long) tempsReconnexion*1000){
    tempsEnvoiReconnexion = millis();
    #ifdef DEBUG
    Serial.println("Envoie ping");
    #endif
    send(7,0);
  }
  // On envoie le niveau de batterie toutes les minutes
  if (millis() - tempsEnvoieBatterie > 60000 && connecte){
    tempsEnvoieBatterie = millis();
    #ifdef DEBUG
    Serial.println("Envoie niveau batterie");
    #endif
    byte bat = niveauBatterie;
    if(bat<1)
      bat=1;
    if(bat>9)
      bat = 0;
    send(8,bat);
  }
  // On met à jour l'animation K2000
  majK2000();
  // On vérifie la batterie
  majBat();
}

/** Mesure la référence interne à 1.1 volts */
unsigned int analogReadReference() {
  /* Elimine toutes charges résiduelles */
  ADMUX = 0x4F;
  delayMicroseconds(5);

  /* Sélectionne la référence interne à 1.1 volts comme point de mesure, avec comme limite haute VCC */
  ADMUX = 0x4E;
  delayMicroseconds(200);

  /* Active le convertisseur analogique -> numérique */
  ADCSRA |= (1 << ADEN);

  /* Lance une conversion analogique -> numérique */
  ADCSRA |= (1 << ADSC);

  /* Attend la fin de la conversion */
  while(ADCSRA & (1 << ADSC));

  /* Récupère le résultat de la conversion */
  return ADCL | (ADCH << 8);
}
/**
 * met à jour la valeur de la tension de la batterie, et éteind le module si cette dernière est inférieure à 3,7V (20% de la charge)
 */
void majBat(){
  unsigned int raw_ref = analogReadReference();
  float tension = (((analogRead(0)*1.1)/raw_ref)*2);
  // Si la tension est plus basse que 3.7v, on éteint
  /*if(tension<=3.7){
    Serial.println("Arret du module (batterie faible)");
    digitalWrite(offPin, HIGH);
    eteindreClignos();
    for(int i=0; i<8; i++){
      ledAllumee[0][i] = false;
      ledAllumee[1][i] = false;
    }
    cligno();
    // On bloque le système
    while(true){
      digitalWrite(LED, HIGH);
      delay(500);
      digitalWrite(LED, LOW);
      delay(500);
    }
  }*/

  // On détermine le poucentage de batteries
  niveauBatterie = 0;
  if(tension < 4.10)
    niveauBatterie = 9;
  if(tension < 3.97)
    niveauBatterie = 8;
  if(tension < 3.92)
    niveauBatterie = 7;
  if(tension < 3.87)
    niveauBatterie = 6;
  if(tension < 3.84)
    niveauBatterie = 5;
  if(tension < 3.82)
    niveauBatterie = 4;
  if(tension < 3.79)
    niveauBatterie = 3;
  if(tension < 3.75)
    niveauBatterie = 2;
  if(tension < 3.71)
    niveauBatterie = 1;
}

/**
 * gère l'animation de la led témoin sur le bouton d'allumage
 */
void ledAllumage(){
  // Toutes les 2 secondes, on remet le compteur à 0
  if(millis()-millisLed>=2000)
    millisLed = millis();
  // Si on est pas connecté et que la LED n'est pas allumée, on l'allume
  if(!connecte && !led){
    digitalWrite(LED, HIGH);
    led = true;
  }
  // Si on est connecté, on fait clignoter 2 coups toutes les secondes
  else if(connecte){
    if(millis()-millisLed>=200){
      if(led)
        digitalWrite(LED, LOW);
      led = false;
    }
    else if(millis()-millisLed>=150){
      if(!led)
        digitalWrite(LED, HIGH);
      led = true;
    }
    else if(millis()-millisLed>=50){
      if(led)
        digitalWrite(LED, LOW);
      led = false;
    }
    else if(!led){
      digitalWrite(LED, HIGH);
      led = true;
    }
  }

}

/**
 * Extrait la dernière commande reçue sous forme d'un nombre à deux chiffres (Commande + paramètre)
 * @return La commande extraite ou 0 s'il n'y en a pas
 */
byte derniereCommande(){
  if(commande[3]){
    commande[3] = 0;
    return (commande[0]-48)*10+commande[1]-48;
  }
  return 0;
}

/**
 * Traite les deux ports séries
 * Enregistre la commande reçue du module Bluetooth
 * Achemine les messages du port USB vers le module Bluetooth
 */
void traitementSerial(){
  // Réception depuis le bluetooth
  while(bluetooth.available() && commande[3]!=1){
    commande[0] = commande[1];
    commande[1] = commande[2];
    commande[2] = bluetooth.read();
    // Si c'est un 'k', on indique qu'une commande est disponible
    if(commande[2] == 107)
      if(commande[0]>47 && commande[0]<58 && commande[1]>47 && commande[1]<58)
        commande[3] = 1;
  }

  // Pont du port série vers le bluetooth
  while(Serial.available())
    bluetooth.write(Serial.read());
}

/**
 * Envoie une commande à l'application via le module bluetooth
 * @param commande  commande à envoyer
 * @param parametre paramètre de la commande à envoyer
 */
void send(char commande, char parametre){
  if(commande > 9)
    commande  = 9;
  if(parametre > 9)
    parametre = 9;
    bluetooth.write(48+commande);
    bluetooth.write(48+parametre);
    bluetooth.write(107);
    #ifdef DEBUG
      Serial.print("Envoie de ");
      Serial.write((char)(48+commande));
      Serial.write((char)(48+parametre));
      Serial.write('\n');
    #endif
}
/**
 * gère les changements d'état des clignotants. on met à jour les variables contenant l'état des leds, puis on les écrit sur les 74HC595
 */
void cligno(){
  // Si on est pas connecté, on quitte
  if(!connecte)
    return;
  // On met à jour tous les clignotants de gauche
  if(clignoAllume[0] != 2)
    for(int i=0; i<8; i++)
      majCligno(0, i);

  // On met à jour tous les clignotants de droite
  if(clignoAllume[1] != 2)
    for(int i=0; i<8; i++)
      majCligno(1, i);

  // On met à jour les warnings
  majWarning();

  // Si le timer est à plus d'une seconde, on le remet à 0 et on change de sens (allumer/eteindre)
  if(millis()-millisDernierClignotement > millisRAZ){
    sensClignotement = !sensClignotement;
    millisDernierClignotement = millis();
  }

  // Si une LED a changé, on l'écrit dans la sortie série
  if(ledChange){
    // On indique qu'on inscrit de nouvelles valeurs
    digitalWrite(latchPin, LOW);

    // Feux stop
    for(int j=0; j<8; j++){
      digitalWrite(clockPin, LOW);
      //digitalWrite(dataPin, feuxStopOn);
      digitalWrite(dataPin, fCirculation);
      digitalWrite(clockPin, HIGH);
    }

    // Pour chaque cligno
    for(int j=0; j<2; j++){
      #ifdef DEBUG
      Serial.print("Led ");
      Serial.print(j);
      Serial.print(" : [");
      #endif

      // On gère les clignos
      for(int i=0; i<8; i++){
        #ifdef DEBUG
        Serial.print(ledAllumee[j][i]);
        Serial.print("|");
        #endif
        //départ du cycle, on met l'horloge à l'état bas
        digitalWrite(clockPin, LOW);
        //on met le bit de donnée courant en place
        digitalWrite(dataPin, !ledAllumee[j][7-i]);
        //enfin on remet l'horloge à l'état haut pour faire prendre en compte ce dernier et finir le cycle
        digitalWrite(clockPin, HIGH);
      } //et on boucle 8 fois pour faire de même sur chaque bit de l'octet d'ordre
      #ifdef DEBUG
      Serial.println("]");
      #endif
    }
    // Pour chaque LED des feux
    for(int i=0; i<8; i++){
        //départ du cycle, on met l'horloge à l'état bas
        digitalWrite(clockPin, LOW);
        //on met le bit de donnée courant en place
        digitalWrite(dataPin, fCirculation);
        //enfin on remet l'horloge à l'état haut pour faire prendre en compte ce dernier et finir le cycle
        digitalWrite(clockPin, HIGH);
    }
    // On indique que les valeurs sont inscrites
    digitalWrite(latchPin, HIGH);
  }
  // On remet à 0 pour le prochain tour
  ledChange = false;
}

/**
 * @brief      Met à jour l'allumage des LEDs pour les warnings
 */
void majWarning(){
  // Si les warnings doivent être mis à jour, on le fait
  if(ledAllumee[0][0] == !sensClignotement && clignoAllume[0] == 2){
    for(int i=0; i<8; i++){
      ledAllumee[0][i] = sensClignotement;
      ledAllumee[1][i] = sensClignotement;
    }
    ledChange = true;
  }
}

/**
 * @brief      Met à jour l'allumage des LEDs pour les clignos
 *
 * @param[in]  sens   Le clignotant (gauche ou droite)
 * @param[in]  noLed  Le numéro de la LED sur le clignotant (0-7)
 */
void majCligno(int sens, int noLed){
  // Si la LED est éteinte, on regarde si on doit l'allumer
  if(!ledAllumee[sens][noLed]){
    // Si le clignotant est allumé
    if(clignoAllume[sens] == 1){
      // Si c'est à notre tour de nous allumer, dans l'animation
      if((millis()-millisDernierClignotement > (millisDecalage*noLed)) && !sensClignotement){
        // On allume la LED
        ledAllumee[sens][noLed] = true;
        ledChange = true;
      }
    }
  }
  // Si la LED est allumée, on regarde si on doit l'éteindre
  else{
    // Si c'est à notre tour de nous éteindre, dans l'animation
    if((millis()-millisDernierClignotement > (millisDecalage*noLed)) && sensClignotement && clignoAllume[sens] != 2){
      // On éteint la LED
      ledAllumee[sens][noLed] = false;
      ledChange = true;
    }
  }
}

/**
 * gère l'animation K2000 des feux de position tant que le module est déconnecté
 */
void majK2000(){
  // On regarde si on doit jouer l'animation (non connecté)
  if(!connecte && millis() - tempsK2000 > 50){
    tempsK2000 = millis();
    // On regarde si on doit changer de sens
    if(animK2000 == 1 || animK2000 == 128)
      sensAnimK2000 = !sensAnimK2000;
    // Selon le sens, on change la LED allumée
    if(sensAnimK2000)
      animK2000*=2;
    else
      animK2000/=2;
    // On met à jour les LEDs
    digitalWrite(latchPin, LOW);
    // Faux arrière
    digitalWrite(dataPin, false);
    for(int i=0; i<8; i++){
        digitalWrite(clockPin, LOW);
        digitalWrite(clockPin, HIGH);
    }
    // Clignos
    digitalWrite(dataPin, true);
    for(int i=0; i<16; i++){
        digitalWrite(clockPin, LOW);
        digitalWrite(clockPin, HIGH);
    }
    for(int i=0; i<8; i++){
        digitalWrite(clockPin, LOW);
        Serial.print(((animK2000>>i)&1)?"*":"-");
        digitalWrite(dataPin, ((animK2000>>i)&1));
        digitalWrite(clockPin, HIGH);
    }
    Serial.println("");
    digitalWrite(latchPin, HIGH);
  }
}

/**
 * @brief      Lance la procédure d'allumage du clignotant choisi, et arrête l'autre
 *
 * @param[in]  sens  Le sens du clignotant (gauche/droite)
 */
void allumerCligno(int sens){
  // On éteint toutes les LEDs
  for(int i=0; i<16; i++){
    *(((bool*)&ledAllumee)+i) = 0;
  }
  // On allume le clignotant choisi et on éteint l'autre
  clignoAllume[0] = sens;
  clignoAllume[1] = !sens;

  // On remet le compteur de clignotement à 0 pour que ça commence tout de suite à s'allumer
  sensClignotement = 0;
  millisDernierClignotement = millis();

  // On actualise la sortie
  ledChange = true;
}

/**
 * @brief      Lance la procédure d'arrêt des clignotants
 */
void eteindreClignos(){
  // On éteint les clignotants
  clignoAllume[0] = 0;
  clignoAllume[1] = 0;

  // On remet le compteur de clignotement à 0 pour que ça commence tout de suite à s'éteindre
  sensClignotement = 1;
  millisDernierClignotement = millis();

  // On actualise la sortie
  ledChange = true;
}

