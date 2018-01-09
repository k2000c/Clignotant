package biblio.Model;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import biblio.Controler.Var;

/**
 * Gère tous les capteurs du téléphone
 */
public class Capteur implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorLumina;
	private Sensor sensorInclinaison;
	private Var var;
    private float valeurLumina;

	/**
	 * Crée un objet de type capteurs, permettant d'accéder aux différents capteurs du téléphone si disponibles
	 */
	public Capteur(SensorManager sens, Var var) {
		this.var = var;
        sensorManager = sens;
        sensorLumina = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.onResume();
	}

	/**
	 * Récupère et retourne la valeur du capteur de liminosité exterieure
	 * @return La valeur du capteur (0-255)
	 */
	public int getLuminositeAmbiante() {
		int ret = 255;
        // On vérifie que le capteur est présent
        if(sensorLumina != null){
            //ret = (int)((valeurLumina*255)/sensorLumina.getMaximumRange());
			ret = (int)valeurLumina;
        }
        return ret;
	}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Mettre à jour la valeur de notre capteur
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT: // Luminosité
				if(this.var.control != null)
					if(this.var.bt.getEtatConnexion() == 2){
						valeurLumina = event.values[0];
						Log.d("lumina", "Value : " + valeurLumina + " = " + this.getLuminositeAmbiante());

						// Si la luminosité est trop faible, on allume les feux
						if (this.var.valeurFeux > this.getLuminositeAmbiante() && !this.var.control.getValeurBoutonFeux() && this.var.allumerFeux) {
							this.var.bt.envoieCommande(5, 1);
						}

						// Si on est en mode de configuration, on met à jour la valeur
						if(this.var.vueActuelle == 3){
							this.var.main.majValeursConfig();
						}
					}
                break;
        }
    }

    /**
     * A faire lors de l'arrêt de l'application
     */
    public void onPause() {
        // désenregistrer notre écoute des capteurs
        sensorManager.unregisterListener(this, sensorLumina);
    }

    /**
     * A faire lors de la reprise
     */
    public void onResume() {
        // enregistrer notre écoute des capteurs
        sensorManager.registerListener(this, sensorLumina, SensorManager.SENSOR_DELAY_GAME);
    }

	/**
	 * Récupère et retourne l'inclinaison actuelle du vélo
	 * @return Valeur du capteur
	 */
	public int getInclinaison() {
		// TODO - implement Capteur.getInclinaison
		throw new UnsupportedOperationException();
	}

}