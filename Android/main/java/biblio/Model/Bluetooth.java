package biblio.Model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import android.os.Build;
import android.util.Log;

import biblio.Controler.Var;

import static java.lang.System.currentTimeMillis;

/**
 * Classe de gestion de la communication avec le module
 */
public class Bluetooth {
	private static Bluetooth instance = new Bluetooth();
	/**
	 * Nom du réseau bluetoot sur lequel se connecter
	 */
	private String nom;
	/**
	 * Dernière fois (en ms) que la connexion a été testée
	 * (La dernière fois que nous avons reçu un message du module)
	 */
	private long watchDog;
	/**
	 * Etat actuelle de la connexion
	 * 0 - Non connecté
	 * 1 - En cours de connexion
	 * 2 - Connecté
	 * 3 - Perte de connexion
	 */
	private int etatConnexion;

	/**
	 * Adaptateur physique bluetooth
	 */
	private BluetoothAdapter btAdaptateur;

	/**
	 * Connexion du module
	 */
	private BluetoothSocket btSock;
	private ConnectedThread btThread;

	/**
	 * Liste des appareils connus
	 */
	private Set<BluetoothDevice> devices;

	/**
	 * UUID
	 */
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	/**
	 * Dernière commande reçue
	 * cases :
	 * 0 = indique si un message est non-lu
	 * 1 = commande+paramètre
	 * 2 = Etat connexion -> vers Thread
	 * 3 = Etat connexion <- de Thread
	 */
	private int[] derniereCommande = new int[4];

	// Variables
	Var var;

	/**
	 * Crée un objet bluetooth singleton
	 */
	private Bluetooth() {
		this.btAdaptateur = BluetoothAdapter.getDefaultAdapter();
		if(this.btAdaptateur != null){
			// On active le bluetooth si besoin
			if(!btAdaptateur.isEnabled())
				btAdaptateur.enable();
			this.etatConnexion = 0;
			this.devices = btAdaptateur.getBondedDevices();
		}
	}

	public void setVar(Var var){
		this.var = var;
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				return null;
			}
		} else {
			try {
				return device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * Crée sous forme de texte, la liste des modules bluetooth
	 * @return     Une liste de string, contenant les modules existants
	 */
	public ArrayList<String> devices(){
		ArrayList<String> ret = new ArrayList<String>();
		for (BluetoothDevice blueDevice : devices)
			ret.add(blueDevice.getName());
		return ret;
	}

	/**
	 * Si le bluetooth est connecté à quelque chose, vérifie que ce quelque chose est bien le module attendu
	 * @return     true si c'est le bon module
	 */
	public boolean authentification(){
		return true; // DEBUG !!!
		/*if(this.btSock != null){
			this.btThread.start();
			Log.e("btConnect", "Authentification");
			// On envoie "99"
			if(this.send("99k")){
				String recu = "";
				// On attend 3 secondes maximum
				long timeDepart = currentTimeMillis();
				int message = 0;
				// On attend le message pendant 5 secondes
				while(currentTimeMillis() - timeDepart < 5000){
					message = this.recoitCommande();
					if(message != 0){
						// On imprime le message reçu
						Log.e("auth", "Message reçu : "+message);

						// On regarde si on a reçu "90"
						if(message == 90) {
							this.etatConnexion = 2;
							return true;
						}
					}
				}
				// On a pas réussi à se connecter
				derniereCommande[2]++; // On arrête la réception de messages
				try {
					btSock.close();
				} catch(Exception e) {
					Log.e("btConnect", e.toString());
				}
				return false;
			}
		}
		return false;*/
	}

	/**
	 * Tente de se connecter au module.
	 * Commence par se connecter au réseau bluetooth, puis s'authentifie pour certifier que nous sommes bien connecté au module et pas à autre chose.
	 * @param nom Nom du réseau bluetooth
	 * @return Retourne 1 si connecté, 0 si non connecté et 2 si aucun adaptateur n'est connecté
	 */
	public int connexion(String nom) {
		return 1; // DEBUG !!!
		// On vérifie qu'il y a un adaptateur
		/*if(this.btAdaptateur == null)
			return 2;
		else{
			// On active le bluetooth si besoin
			if(!btAdaptateur.isEnabled())
				btAdaptateur.enable();
			this.etatConnexion = 0;

			this.devices = btAdaptateur.getBondedDevices();

			// On récupère les périphériques et on lance la connexion
			for (BluetoothDevice blueDevice : devices){
				// Si le réseau a été trouvé, on tente de se connecter
				if(blueDevice.getName().equals(nom)){
					try{
						btSock = createBluetoothSocket(blueDevice);
						btThread = new ConnectedThread(btSock, derniereCommande);
					}
					catch(Exception e){
						Log.e("btConnect", e.toString());
						btSock = null;
						return 0;
					}
					// On tente de se connecter
					try {
						btSock.connect();// Tentative de connexion
						// Connexion réussie
						this.etatConnexion = 1;
					} catch (IOException e) {
						// Echec de la connexion
						Log.e("btConnect", e.toString());
						return 0;
					}
					return 1;
				}
			}
		}
		return 0;*/
	}

	/**
	 * Eteint le module bluetooth et se déconnecte du module
	 * @return Retourne true si le module s'est bien éteint avant la déconnexion
	 */
	public boolean deconnexion() {
		if(this.etatConnexion != 0) {
			eteindreModule();
			pause(1000);
			try {
				btSock.close();
			} catch(Exception e){
			} finally {
				btSock = null;
				derniereCommande[2]++;
			}
		}
		// On désactive le bluetooth si nécessaire
		if (this.var != null)
			if(this.var.couperBt)
				btAdaptateur.disable();
		this.etatConnexion = 0;
		return true;
	}

	/**
	 * Met le Thread en pause de Xms
	 * @param      ms    The milliseconds
	 */
	private void pause(long ms){
		long timeDepart = currentTimeMillis();
		while(currentTimeMillis() - timeDepart < ms);
	}

	/**
	 * Envoie une commande et son paramètre au module
	 * @param noCommande Numéro de la commande
	 * @param paramCommande Paramètre de la commande
	 * @return Retourne true si la commande a bien été envoyée (Il n'a pas forcément été bien reçu !!)
	 */
	public boolean envoieCommande(int noCommande, int paramCommande) {
		return this.send(noCommande+""+paramCommande+"k");
	}

	private boolean send(String texte){
		Log.d("Send", texte);
		if(this.btThread != null)
			return this.btThread.write(texte);
		return false;
	}

	/**
	 * Retourne la commande reçu, ainsi que son paramètre (dizaine = commande, unité = paramètre).
	 * Si rien n'a été reçu, retourne 0
	 * @return Retourne la commande si reçu, 0 sinon
	 */
	public int recoitCommande() {
		int ret = 0;
		if(derniereCommande[0]>0)
			ret =  derniereCommande[1];
		derniereCommande[0] = 0;
		return ret;
	}

	/**
	 * Indique si la connexion est toujours d'actualilté
	 * @return true si on est toujours connecté
	 */
	public boolean estConnecte(){
		// On demande à android l'état de la connexion bluetooth
		return this.btSock.isConnected();
	}

	/**
	 * Indique si la connexion est perdue
	 * @return true si on n'est plus connecté
	 */
	public boolean connexionPerdue(){
		// On vérifie qu'on est censé être connecté
		if(this.etatConnexion >= 2)
			// On demande à android l'état de la connexion bluetooth
			return (this.derniereCommande[3] == 3);
		return false;
	}

	public int getEtatConnexion(){
		return this.etatConnexion;
	}

	/**
	 * Allume le clignotant droit
	 */
	public void allumerClignotantDroit() {
		envoieCommande(1, 1);
	}

	/**
	 * Eteint le clignotant droit
	 */
	public void eteindreClignotantDroit() {
		envoieCommande(1, 0);
	}

	/**
	 * Allume le clignotant gauche
	 */
	public void allumerClignotantGauche() {
		envoieCommande(2, 1);
	}

	/**
	 * Eteint le clignotant gauche
	 */
	public void eteindreClignotantGauche() {
		envoieCommande(2, 0);
	}

	/**
	 * Allume les warnings
	 */
	public void allumerWarning() {
		envoieCommande(3, 1);
	}

	/**
	 * Eteint les warnings
	 */
	public void eteindreWarning() {
		envoieCommande(3, 0);
	}

	/**
	 * Allume les freins
	 */
	public void allumerFreins() {
		envoieCommande(4, 1);
	}

	/**
	 * Eteint les freins
	 */
	public void eteindreFreins() {
		envoieCommande(4, 0);
	}

	/**
	 * Allume les phares, au niveau définit auparavant
	 */
	public void allumerPhares() {
		envoieCommande(5, 1);
	}

	/**
	 * Eteint les phares
	 */
	public void eteindrePhares() {
		envoieCommande(5, 0);
	}

	/**
	 * Règle la luminosité des phares (ne les allume pas, les règle juste)
	 * @param lum Niveau de luminosité, de 0 à 9
	 */
	public void reglerLuminosite(int lum) {
		if(lum>9)
			lum = 9;
		envoieCommande(6, lum);
	}

	/**
	 * Eteint le module
	 */
	public void eteindreModule(){
		envoieCommande(8, 0);
	}

	/**
	 * Retourne l'instance unique de l'objet Bluetooth
	 * @return Retourne l'instance unique
	 */
	public static Bluetooth getInstance() {
		return Bluetooth.instance;
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
 		private final int[] bufferReception;

		ConnectedThread(BluetoothSocket socket, int[] buffer) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			this.bufferReception = buffer;
 
			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }
 
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
 
		public void run() {
			byte[] buffer = new byte[256];  // buffer store for the stream
			int bytes; // bytes returned from read()
			char buf1 = ' ', buf2 = ' ', buf3 = ' '; // Buffer temporaire le temps de recevoir un "\n"
			Log.e("btThread", "runLancé");
			int no = ++bufferReception[2];
			bufferReception[3] = 2;
 
			// Keep listening to the InputStream until an exception occurs
			while (no == bufferReception[2]) {
				if(bufferReception[0] != 1)
					try {
						// Read from the InputStream
						bytes = mmInStream.read(buffer, 0, 1);        // Get number of bytes and message in "buffer"
						if(bytes>0)
							Log.e("btThread", "Lecture de "+(char)buffer[0]);
						buf1 = buf2;
						buf2 = buf3;
						buf3 = (char)buffer[0];

						// Si c'est une fin de ligne, on extrait la commande
						if(buf3 == 'k'){
							Log.e("btThread", "\n trouvé, Lecture de "+buf1+" "+buf2);
							int result = buf1-'0';
							if(result<0 || result>9)
								continue;
							result*=10;
							result += buf2-'0';
							if(result<0 || result>99)
								continue;
							Log.e("btThread", "Message reçu : "+buf1+" & "+buf2+" => "+result);
							// La commande est conforme, on l'ajoute et on attend que le programme l'ai lue avant de lire les suivants
							bufferReception[1] = result;
							bufferReception[0] = 1;
						}

					} catch (IOException e) {
						break;
					}
			}
			Log.e("btThread", "runStoppé");
			bufferReception[3] = 3;
		}
 
		/* Call this from the main activity to send data to the remote device */
		boolean write(String message) {
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}

}