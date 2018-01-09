package biblio.Controler;

import android.util.Log;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import static android.content.ContentValues.TAG;

public class Listener implements OnTouchListener, OnClickListener{
	private Controller control;
	private Var var;
	private boolean deplacementEnCours = false;
	private boolean deplacementFait = false;
	private int positionBoutonX = 0;
	private int positionBoutonY = 0;
	private int positionInitBoutonX = 0;
	private int positionInitBoutonY = 0;
	private int positionRelativeBoutonX = 0;
	private int positionRelativeBoutonY = 0;
	private int tailleDroite = 0;
	private int tailleBas = 0;

	public Listener(Controller control, Var var){
		this.control = control;
		this.var = var;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// On regarde si c'est un déplacement
		switch(event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN: // L'utilisateur vient d'appuyer
				Log.d(TAG, "onTouch: Appuie");
				deplacementEnCours = true;
				deplacementFait = false;
				positionRelativeBoutonX = this.var.pR.x;
				positionRelativeBoutonY = this.var.pR.y;
				positionInitBoutonX = (int)event.getRawX();
				positionInitBoutonY = (int)event.getRawY();
				break;

			case MotionEvent.ACTION_UP: // L'utilisateur vient de relâcher
				Log.d(TAG, "onTouch: Relâchement");
				deplacementEnCours = false;
				// Si l'utilisateur a juste cliqué on le prend en compte
				if(!deplacementFait)
					this.onClick(v);
				break;

			case MotionEvent.ACTION_MOVE: // L'utilisateur bouge
				// On vérifie qu'il a déjà bougé suffisemment
				if(deplacementFait) {
					this.positionBoutonX = this.positionRelativeBoutonX+((int)event.getRawX()-this.positionInitBoutonX);
					this.positionBoutonY = this.positionRelativeBoutonY+((int)event.getRawY()-this.positionInitBoutonY);
					// On bouge le layout
					Log.d(TAG, "onTouch: Déplacement de (" + event.getRawX() + "|" + event.getRawY() + ")");
					this.var.pR.x = this.positionBoutonX;
					this.var.pR.y = this.positionBoutonY;
					this.var.windowManager.updateViewLayout(this.var.vueRangee, this.var.pR);
				}
				// Sinon on regarde si on bouge suffisemment
				else if(Math.abs((int)event.getRawX()-this.positionInitBoutonX)>70 || Math.abs((int)event.getRawY()-this.positionInitBoutonY)>70){
					// On indique qu'on a bougé (et on active le mode de déplacement)
					deplacementFait = true;

					// On déplace le layout
					Log.d(TAG, "onTouch: Déplacement de (" + event.getRawX() + "|" + event.getRawY() + ")");
					this.var.pR.x = this.positionBoutonX;
					this.var.pR.y = this.positionBoutonY;
					this.var.windowManager.updateViewLayout(this.var.vueRangee, this.var.pR);
				}
				// Sinon on sauvegarde notre déplacement
				else{
					this.positionBoutonX = this.positionRelativeBoutonX+((int)event.getRawX()-this.positionInitBoutonX);
					this.positionBoutonY = this.positionRelativeBoutonY+((int)event.getRawY()-this.positionInitBoutonY);
				}
				break;

			default:
				break;
		}
		return true;
	}

	@Override // Gère les boutons
	public void onClick(View v) {
		// On extrait le numéro du bouton
		String id = v.getContentDescription().toString();
		int num = Integer.parseInt(id);

		// On appelle le contrôleur
		this.control.setBoutonAppuie(num);
	}

	// gère les cases à cocher
	public void onSelect(View v) {
		// On extrait le numéro de la case à cocher
		String id = v.getContentDescription().toString();
		int num = Integer.parseInt(id);

		// On appelle le contrôleur
		this.control.appuieCaseCoche(num, ((CheckBox)v).isChecked());
	}

	// Gère les seekbar
	public void onChangeValue(View v) {
		// On extrait le numéro de la case à cocher
		String id = v.getContentDescription().toString();
		int num = Integer.parseInt(id);

		// On appelle le contrôleur
		this.control.changerValeurConfig(num, ((SeekBar)v).getProgress());
	}
}