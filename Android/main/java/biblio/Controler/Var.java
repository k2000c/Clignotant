package biblio.Controler;

import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import biblio.Model.*;
import biblio.View.*;
import com.iut.kitt.k2000.MainActivity;

/**
 * Regroupe un ensemble de variables utilisés par le système
 */
public class Var {

	public Bluetooth bt;
	public Ecran ecran;
	public Controller control;
	public Capteur capteur;
	public Parametre params;
	public MainActivity main;
	public WindowManager windowManager;
	public WindowManager.LayoutParams p;
	public WindowManager.LayoutParams pR;
	public View vuePrincipale;
	public View vueRangee;
	public int vueActuelle = 0; //
	public boolean couperBt = true; // Indique si on doit couper le bt à l'arrêt du logiciel
	public boolean bulle = false; // Indique si l'appli doit se trouver sous forme de fenêtre flotante
	public boolean fenetreTransparente = false; // Indique si la fenêtre doit être transparente (ne peut pas l'être si elle n'est pas flottante)
	public int valeurTransparence = 255; // Opacité de la fenêtre (de 0 à 255)
	public int valeurFeux = 5; // Puissance des feux (de 0 à 5)
	public boolean allumerFeux = false; // Indique si le logiciel doit allumer les feux automatiquement
	public int valeurAllumageFeux = 0; // En dessous de quelle luminosité devons-nous allumer les feux ?
	public int rotationMinimum = 90; // Degrès de rotation minimum que le guidon doit avoir pour activer la chute


	/**
	 * Crée un regroupement de variables, pour le système
	 */
	public Var(Bluetooth bt, Ecran ecran, Capteur capteur, Parametre params, MainActivity main) {
		this.bt = bt;
		this.ecran = ecran;
		this.control = null;
		this.capteur = capteur;
		this.params = params;
		this.main = main;
	}

	public Bluetooth getBluetooth(){
		return this.bt;
	}

	public Ecran getEcran(){
		return this.ecran;
	}
	
	public Controller getControl(){
		return this.control;
	}
	
	public Capteur getCapteur(){
		return this.capteur;
	}
	
	public Parametre getParams(){
		return this.params;
	}

	public void setBluetooth(Bluetooth bt){
		this.bt = bt;
	}

	public void setEcran(Ecran ecran){
		this.ecran = ecran;
	}
	
	public void setControl(Controller control){
		this.control = control;
	}
	
	public void setCapteur(Capteur capteur){
		this.capteur = capteur;
	}
	
	public void setParams(Parametre params){
		this.params = params;
	}
}