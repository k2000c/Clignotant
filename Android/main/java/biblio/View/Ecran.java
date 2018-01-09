package biblio.View;

import com.iut.kitt.k2000.MainActivity;

import biblio.Controler.Var;

public class Ecran {
	private Var var;

	/**
	 * Crée une instance d'écran
	 */
	public Ecran(Var var) {
		this.var = var;
	}

	public void setVar(Var var){
		this.var = var;
	}

	/**
	 * Change l'état d'un bouton pour indiquer à l'utilisateur l'état actuel du module
	 * @param numBouton Numéro du bouton à changer
	 * @param etatBouton Nouvel état du bouton
	 */
	public void changerEtatBouton(final int numBouton, final boolean etatBouton) {
        var.main.runOnUiThread(new Runnable() {
            public void run() {
				var.main.changerEtat(numBouton, etatBouton);
            }
        });
	}

	/**
	 * Affiche du texte à l'écran pour l'utilisateur
	 * @param s Texte à afficher à l'utilisateur
	 */
	public void afficherTexte(String s) {
		this.var.main.afficher(s);
	}

	/**
	 * Change l'opacité de l'application
	 * @param opacite Opacité à appliquer, de 0 à 255
	 */
	public void opacite(final int opacite) {
		this.var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.opacitePrincipale(opacite);
			}
		});
	}

	/**
	 * On met à jour la fenêtre de config
	 */
	public void majConfig() {
		if(var.vueActuelle == 3)
		this.var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.majValeursConfig();
			}
		});
	}

	/**
	 * Change le type de fenêtre : flotant ou transparent
	 * @param type Nouveau type à appliquer à la fenêtre
	 * 0 - Plein écran, non transparent
	 * autre - Flotant, transparent à x%
	 */
	public void changeType(int type) {
		// TODO - implement Ecran.changeType
		throw new UnsupportedOperationException();
	}

	/**
	 * Change le contenu de la fenêtre par les éléments principaux
	 */
	public void afficherFenetrePrincipale() {
		this.var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.chargerLayoutPrincipal();
			}
		});
	}

	/**
	 * Change le contenu de la fenêtre par les éléments principaux
	 */
	public void afficherPopupRangee() {
		this.var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.chargerLayoutPopupRangee();
			}
		});
	}

	/**
	 * Change le contenu de la fenêtre par les éléments de connexion
	 */
	public void afficherFenetreConnexion() {
		var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.chargerLayoutConnexion();
			}
		});
	}

	/**
	 * Change le contenu de la fenêtre par les éléments de l'écran de configuration
	 */
	public void afficherFenetreParametres() {
		var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.chargerLayoutConfiguration();
			}
		});
	}

	/**
	 * Affiche un message indiquant que la connexion est en train de s'établir
	 */
	public void afficherConnexionEnCours() {
		var.main.runOnUiThread(new Runnable() {
			public void run() {
				var.main.chargerLayoutTentativeConnexion();
			}
		});
	}
}