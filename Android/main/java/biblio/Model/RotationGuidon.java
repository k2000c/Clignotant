package biblio.Model;

/**
 * Gère tout le système de rotation du guidon, permettant de désactiver le clignotant automatiquement après avoir tourné
 */
public class RotationGuidon {

	/**
	 * Indique si le guidon a suffisemment tourné pour désactiver le clignotant lors du redressement
	 */
	private boolean rotationActive = false;
	private int ancienneValeurRotation;

	public boolean isRotationActive() {
		return this.rotationActive;
	}

	public void setRotationActive(boolean rotationActive) {
		this.rotationActive = rotationActive;
	}

	/**
	 * Indique si le vélo est en train de tourner suffisemment pour considérer que le clignotant peut être désactivé
	 * @param rotationMinimum Rotation relative minimum à avoir pour considérer que le vélo est en train de tourner
	 * @return true si le vélo est en train de tourner
	 */
	public boolean enTrainDeTourner(int rotationMinimum) {
		// TODO - implement RotationGuidon.enTrainDeTourner
		throw new UnsupportedOperationException();
	}

	/**
	 * Indique si le vélo a tourné, et remet le tout à 0
	 * @return true si le vélo vient de finir son virage
	 */
	public boolean aTourne() {
		// TODO - implement RotationGuidon.aTourne
		throw new UnsupportedOperationException();
	}

}