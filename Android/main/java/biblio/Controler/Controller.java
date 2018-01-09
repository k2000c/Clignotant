package biblio.Controler;

import android.util.Log;

/**
 * Contrôleur du logiciel
 */
public class Controller {

	/**
	 * Ensemble des variables utilisés par le logiciel (Le contrôleur en a besoin)
	 */
	private Var var;
	private static Controller instance = new Controller();
	private int dernierBoutonPresse = 0;
	private boolean warningsAllumes = false;
	private boolean clignosGaucheAllumes = false;
	private boolean clignosDroitAllumes = false;
	private boolean feuxAllumes = false;
	private long timeCligno = 0;
	private boolean etatCligno = false;

	/**
	 * Crée une instance unique du contrôleur
	 */
	private Controller() {
	}

	/**
	 * Retourne et initalise l'objet unique du contrôleur
	 * @param var Variables du système (Si null, n'est pas pris en compte)
	 * @return Retourne le contrôleur unique
	 */
	public static Controller getInstance(Var var) {
		if(var != null)
			instance.var = var;
		return instance;
	}

	/**
	 * Change la fenêtre pour afficher l'interface principale
	 */
	public void principale() {
		this.var.ecran.afficherFenetrePrincipale();
	}

	/**
	 * Affiche la fenêtre de connexion et gère toute la connexion
	 * Si la connexion a réussie, la configuration est automatiquement sauvegardée dans le fichier
	 */
	public void connexion() {
		this.var.ecran.afficherFenetreConnexion();
	}

	/**
	 * Change le contenu de la fenêtre par le menu de configuration
	 */
	public void parametres() {
		this.var.ecran.afficherFenetreParametres();
	}

	/**
	 * Gère la dernière action qui s'est passée à l'écran (clic, etc...)
	 */
	public void getActionEcran() {
		if(this.dernierBoutonPresse != 0){
			// Selon le bouton, on ne fait pas les mêmes actions
			switch(this.dernierBoutonPresse){
				case 1:
					// On récupère les valeurs de connexion
					this.var.getParams().setNomReseau(this.var.main.getNomReseau());
					// Tentative de connexion
					this.var.ecran.afficherConnexionEnCours();
					// Si la connexion a réussie, on change de fenêtre
					int ret = this.var.bt.connexion(this.var.getParams().getNomReseau());
					if(ret == 1){
						// On s'authentifie
						if(this.var.bt.authentification())
							// Si c'est bon, on est connecté
							this.principale();
						else{
							// Sinon on indique que le péripherique n'est pas reconnu
							this.var.main.afficher(this.var.getParams().getNomReseau()+" n'est pas le module désiré");
							this.connexion();
						}
					}
					else if(ret == 2){ // Pas de bt
						System.exit(0);
					}
					else{ // Sinon on redemande les infos de connexion
						this.var.main.afficher("La connexion à "+this.var.getParams().getNomReseau()+" n'a pas réussie");
						this.connexion();
					}
				break;

				case 2: // Quitter l'appli
				case 6:
				case 9:
				case 30:
				    this.var.bt.deconnexion();
					System.exit(0);
				break;

				case 7: // Rangement popup
					this.var.ecran.afficherPopupRangee();
				break;

				case 8: // Ouverture popup
					this.principale();
				break;

				case 3: // Cligno gauche
                    if(!this.warningsAllumes)
					    this.var.bt.envoieCommande(2, (clignosGaucheAllumes)?0:1);
				break;

				case 4: // Cligno droit
                    if(!this.warningsAllumes)
					    this.var.bt.envoieCommande(1, (clignosDroitAllumes)?0:1);
				break;

				case 5: // Warnings
					this.var.bt.envoieCommande(3, (warningsAllumes)?0:1);
				break;

				case 10: // Feux Arrières
					this.var.bt.envoieCommande(5, (feuxAllumes)?0:1);
				break;

				case 20: // Configuration
				case 31:
					this.var.ecran.afficherFenetreParametres();
				break;

				case 21: // Enregistrement de la configuration
					this.var.getParams().sauverConfig("config.txt");
					this.var.ecran.afficherFenetrePrincipale();
					this.var.ecran.afficherPopupRangee();
				break;

				case 40: // Luminosité ambiante
					this.var.valeurAllumageFeux = this.var.capteur.getLuminositeAmbiante();
					this.var.getEcran().majConfig();
				break;

				default:
					this.var.main.afficher("Le bouton n°"+this.dernierBoutonPresse+" a été appuyé");
				break;
			}
			this.dernierBoutonPresse = 0;
		}

		// On vérifie les commandes externes
		this.getCommande();

		// On vérifie la connexion
		if(this.var.bt.connexionPerdue())
			this.perteConnexion();

		// On met à jour l'interface s'il y a un clignotement en cours
		this.clignotement();
	}

	/**
	 * Gère les clignotements de chaque système
	 */
	private void clignotement(){
		// On regarde quel a été le dernier temps de changement
        if(this.time(this.timeCligno)>500) {
            this.timeCligno = this.time(0);
            this.etatCligno = !this.etatCligno;
            // On regarde qui doit être modifié
            if(warningsAllumes){
                this.var.ecran.changerEtatBouton(1, this.etatCligno);
                this.var.ecran.changerEtatBouton(2, this.etatCligno);
            }
            if(clignosDroitAllumes){
                this.var.ecran.changerEtatBouton(2, this.etatCligno);
            }
            if(clignosGaucheAllumes){
                this.var.ecran.changerEtatBouton(1, this.etatCligno);
            }
        }
	}

    // Retourne le nombe de millisecondes écoulés depuis le temps indiqué
    private long time(long time){
        return System.currentTimeMillis()-time;
    }

	/**
	 * Indique qu'un nouveau bouton vient d'être pressé. Il sera géré au prochain "scan" (On est pas sûr de pouvoir le traiter tout de suite)
	 * @param      noBouton  Numéro du bouton qui vient d'être pressé
	 */
	public void setBoutonAppuie(int noBouton){
		if(this.dernierBoutonPresse == 0)
			this.dernierBoutonPresse = noBouton;
	}

	/**
	 * Gère la dernière commande reçue
	 */
	public void getCommande() {
		int commande = this.var.bt.recoitCommande();
		switch(commande){
			case 0:
				break;

			case 11:
                this.timeCligno = 0;
                this.etatCligno = false;
				clignosDroitAllumes = true;
                clignosGaucheAllumes = false;
                this.var.ecran.changerEtatBouton(2, true);
                this.var.ecran.changerEtatBouton(1, false);
				break;

			case 10:
                this.timeCligno = 0;
                this.etatCligno = false;
				clignosDroitAllumes = false;
                this.var.ecran.changerEtatBouton(2, false);
				break;

			case 21:
                this.timeCligno = 0;
                this.etatCligno = false;
				clignosGaucheAllumes = true;
                clignosDroitAllumes = false;
                this.var.ecran.changerEtatBouton(1, true);
                this.var.ecran.changerEtatBouton(2, false);
				break;

			case 20:
                this.timeCligno = 0;
                this.etatCligno = false;
				clignosGaucheAllumes = false;
                this.var.ecran.changerEtatBouton(1, false);
				break;

			case 31:
                this.timeCligno = 0;
                this.etatCligno = false;
				warningsAllumes = true;
                clignosGaucheAllumes = false;
                clignosDroitAllumes = false;
                this.var.ecran.changerEtatBouton(0, true);
				break;

			case 30:
                this.timeCligno = 0;
                this.etatCligno = false;
				warningsAllumes = false;
                clignosGaucheAllumes = false;
                clignosDroitAllumes = false;
                this.var.ecran.changerEtatBouton(0, false);
                this.var.ecran.changerEtatBouton(1, false);
                this.var.ecran.changerEtatBouton(2, false);
				break;

			case 50:
				this.feuxAllumes = false;
				this.var.ecran.changerEtatBouton(3, false);
				break;

			case 51:
				this.feuxAllumes = true;
				this.var.ecran.changerEtatBouton(3, true);
				break;

            case 70: // Ping
				Log.d("rec", "Ping recu");
                this.var.bt.envoieCommande(7, 1);

			default:
				Log.e("Controller", "Commande reçue : "+commande);
				break;
		}
	}

	/**
	 * Test la connexion avec le module
	 * @return Retourne true si la connexion est toujours valide
	 */
	public boolean testerConnexion() {
		return this.var.bt.estConnecte();
	}

	/**
	 * Récupère les actions des capteurs et les traite.
	 * (Ne traite pas encore la décélération dans les montées, ni l'arrêt du clignotant après le virage)
	 */
	public void getActionCapteur() {
		// TODO - implement Controller.getActionCapteur
		throw new UnsupportedOperationException();
	}

	/**
	 * Affiche l'écran de perte de connexion, puis tente de se reconnecter tant que l'utilisateur n'a pas choisi d'autres actions
	 */
	public void perteConnexion() {
		Log.e("Controller", "Deconnexion");
		this.var.main.afficher("La connexion avec " + this.var.getParams().getNomReseau()+" a été perdue");
		this.var.ecran.afficherFenetreConnexion();
		this.var.bt.deconnexion();
	}

	public void appuieCaseCoche(int noCase, boolean etat){
		// selon la case
		switch(noCase){
			case 1: // Couper bt à l'arrêt
				this.var.couperBt = etat;
				break;

			case 2: // Bulle
				this.var.bulle = etat;
				if(!etat) {
					this.var.fenetreTransparente = false;
					this.var.getEcran().opacite(255);
				}
				this.var.getEcran().majConfig();
				break;

			case 3: // Transparence
				this.var.fenetreTransparente = etat;
				this.var.getEcran().opacite((etat)?this.var.valeurTransparence:255);
				if(etat)
					this.var.bulle = true;
				this.var.getEcran().majConfig();
				break;

			case 4: // Allumage feux
				this.var.allumerFeux = etat;
				break;

			default:
				break;
		}
	}

	public void changerValeurConfig(int noCase, int etat){
		// selon la case
		switch(noCase){
			case 1: // Transparence
				this.var.valeurTransparence = (etat+1)*42;
				this.var.getEcran().opacite(this.var.valeurTransparence);
				break;

			case 2: // Puissance feux
				this.var.valeurFeux = etat;
				break;

			default:
				break;
		}
	}

	public boolean getValeurBoutonFeux(){
		return this.feuxAllumes;
	}

}