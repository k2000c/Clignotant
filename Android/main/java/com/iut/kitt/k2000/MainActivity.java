package com.iut.kitt.k2000;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import biblio.Controler.Controller;
import biblio.Controler.Listener;
import biblio.Controler.Var;
import biblio.Model.Bluetooth;
import biblio.Model.Capteur;
import biblio.View.Ecran;
import biblio.View.Parametre;

import static android.R.attr.button;

public class MainActivity extends Activity {
    public Listener listener;
    public boolean quitter;
    public Bluetooth bt;
    public Ecran ecran;
    public Capteur capteur;
    public Parametre params;
    public Var var;
    public Controller control;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onPause() {
        this.capteur.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        this.capteur.onResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onDestroy(){
        this.quitter = true;
        this.bt.deconnexion();

        super.onDestroy();
    }

    public class Principal extends Thread {
        private MainActivity main;

        Principal(MainActivity main) {
            this.main = main;
        }

        public void run() {
            main.quitter = false;
            while (!main.quitter) {
                main.control.getActionEcran();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }

            // On quitte
            main.bt.deconnexion();
            System.exit(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connexion);

        // On crée les variables de la bibliothèque
        bt = Bluetooth.getInstance();
        ecran = new Ecran(null);
        params = new Parametre();
        var = new Var(bt, ecran, null, params, this);
        ecran.setVar(var);
        bt.setVar(var);
        params.setVar(var);
        params.chargerConfig("config.txt");
        capteur = new Capteur((SensorManager)getSystemService(SENSOR_SERVICE), var);
        control = Controller.getInstance(var);
        var.control = control;
        var.setCapteur(capteur);

        // On associe tous les boutons au listener principal
        this.listener = new Listener(control, var);

        // On charge la fenêtre de connexion
        control.connexion();

        // On lance la boucle principale
        Principal main = new Principal(this);
        main.start();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Change l'état du bouton demandé
     * @param bouton numéro du bouton à modifier
     * @param etat nouvel état
     */
    public void changerEtat(int bouton, boolean etat){
        if(this.var.vuePrincipale != null)
        if(this.var.vuePrincipale.findViewById(R.id.bt05) != null)
        switch(bouton){
            case 0: // warnings
                if(etat)
                    this.var.vuePrincipale.findViewById(R.id.bt05).setBackgroundColor(Color.rgb(255, 0, 0));
                else
                    this.var.vuePrincipale.findViewById(R.id.bt05).setBackgroundColor(Color.rgb(100, 0, 0));
                break;

            case 1: // cligno gauche
                if(etat)
                    this.var.vuePrincipale.findViewById(R.id.bt03).setBackgroundColor(Color.rgb(0, 255, 0));
                else
                    this.var.vuePrincipale.findViewById(R.id.bt03).setBackgroundColor(Color.rgb(0, 100, 0));
                break;

            case 2: // cligno droit
                if(etat)
                    this.var.vuePrincipale.findViewById(R.id.bt04).setBackgroundColor(Color.rgb(0, 255, 0));
                else
                    this.var.vuePrincipale.findViewById(R.id.bt04).setBackgroundColor(Color.rgb(0, 100, 0));
                break;

            case 3: // feux arrières
                if(etat)
                    this.var.vuePrincipale.findViewById(R.id.bt10).setBackgroundColor(Color.rgb(255, 255, 0));
                else
                    this.var.vuePrincipale.findViewById(R.id.bt10).setBackgroundColor(Color.rgb(100, 100, 0));
                break;

            case 4: // niveau Batterie
                if(etat)
                    this.var.vuePrincipale.findViewById(R.id.batterie).setBackgroundColor(Color.rgb(0, 100, 0));
                else
                    this.var.vuePrincipale.findViewById(R.id.batterie).setBackgroundColor(Color.rgb(100, 0, 0));
                break;

            default:
                break;
        }
    }

    /**
     * Charge le layout de connexion dans la fenêtre principale
     */
    public void chargerLayoutConnexion() {
        this.setContentView(R.layout.connexion);

        // On ferme la popup si besoin
        if(this.var.windowManager != null){
            switch(this.var.vueActuelle) {
                case 1:
                    this.var.windowManager.removeView(this.var.vuePrincipale);
                    break;

                case 2:
                    this.var.windowManager.removeView(this.var.vueRangee);
                    break;

                default:
                    break;
            }
            this.var.vueActuelle = 0;
        }
    }

    /**
     * Change la popup ou l'application par l'interface de configuration
     */
    public void chargerLayoutConfiguration() {
        this.setContentView(R.layout.configuration);

        // On ferme la popup si besoin
        if(this.var.windowManager != null){
            switch(this.var.vueActuelle) {
                case 1:
                    this.var.windowManager.removeView(this.var.vuePrincipale);
                    break;

                case 2:
                    this.var.windowManager.removeView(this.var.vueRangee);
                    break;

                default:
                    break;
            }
            this.var.vueActuelle = 0;
        }
        this.var.getEcran().opacite(this.var.valeurTransparence);

        // On ajoute des listeners aux seekbars
        final SeekBar sk1=(SeekBar) findViewById(R.id.seekBar);
        sk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                changerValeur(sk1);
            }
        });
        final SeekBar sk2=(SeekBar) findViewById(R.id.seekBar1);
        sk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                changerValeur(sk2);
            }
        });

        this.majValeursConfig();
        this.var.vueActuelle = 3;
    }

    /**
     * Met à jour les valeurs des paramètres
     */
    public void majValeursConfig() {
        // On modifie les valeurs affichées
        ((CheckBox)findViewById(R.id.cB1)).setChecked(var.couperBt);
        ((CheckBox)findViewById(R.id.cb4)).setChecked(var.bulle);
        ((CheckBox)findViewById(R.id.cb5)).setChecked(var.fenetreTransparente);
        ((CheckBox)findViewById(R.id.cb6)).setChecked(var.allumerFeux);
        ((SeekBar) findViewById(R.id.seekBar)).setProgress(var.valeurTransparence / 42 - 1);
        ((SeekBar) findViewById(R.id.seekBar1)).setProgress(var.valeurFeux);
        findViewById(R.id.valeurAllumageFeux).setSelected(false);
        ((EditText) findViewById(R.id.valeurAllumageFeux)).setText(String.valueOf(var.valeurAllumageFeux));
        ((Button)findViewById(R.id.bt40)).setText(String.valueOf(var.capteur.getLuminositeAmbiante()));
    }

    /**
     * Charge le layout indiquant que nous essayons actuellement de nous connecter
     */
    public void chargerLayoutTentativeConnexion() {
        this.setContentView(R.layout.tentativeconnexion);
        // On change le texte
        ((TextView) findViewById(R.id.nomReseauTentativeConnexion)).setText("Tentative de connexion à " + this.getNomReseau());
    }

    /**
     * Affiche une notification à l'écran
     * @param s Message à afficher dans la notification
     */
    public void afficher(final String s) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Charge la popup dépliée
     */
    public void chargerLayoutPrincipal() {
        // On crée le WM
        if (this.var.windowManager == null) {
            // On crée la vue
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            this.var.vuePrincipale = layoutInflater.inflate(R.layout.principal, null, false);
            this.var.vueRangee = layoutInflater.inflate(R.layout.popup_rangee, null, false);

            // Vue étendue
            this.var.p = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    // Display it on top of other application windows, but only for the current user
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible through any transparent parts
                    PixelFormat.TRANSLUCENT);
            this.var.p.gravity = Gravity.TOP | Gravity.RIGHT;
            this.var.p.x = 0;
            this.var.p.y = 0;

            // Vue rangée
            this.var.pR = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows, but only for the current user
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible through any transparent parts
                    PixelFormat.TRANSLUCENT);
            this.var.pR.gravity = Gravity.TOP | Gravity.LEFT;
            this.var.pR.x = 0;
            this.var.pR.y = 0;

            // On désactive les boutons de base
            this.var.ecran.changerEtatBouton(0, false);
            this.var.ecran.changerEtatBouton(1, false);
            this.var.ecran.changerEtatBouton(2, false);
            this.var.ecran.changerEtatBouton(3, false);
            this.var.ecran.changerEtatBouton(4, false);

            this.var.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        if(var.bulle) {
            this.setContentView(R.layout.activity_main);

            if (this.var.vueActuelle == 2)
                this.var.windowManager.removeView(this.var.vueRangee);

            this.var.vueActuelle = 1;
            this.var.windowManager.addView(this.var.vuePrincipale, this.var.p);
            this.var.windowManager.updateViewLayout(this.var.vuePrincipale, this.var.p);

        }
        else {
            this.setContentView(R.layout.principal);
            findViewById(R.id.bt07).setEnabled(false);
            findViewById(R.id.bt07).setVisibility(View.INVISIBLE);
            findViewById(R.id.bt31).setEnabled(true);
            findViewById(R.id.bt31).setVisibility(View.VISIBLE);
            if(!var.fenetreTransparente)
                findViewById(R.id.space).setVisibility(View.GONE);
        }
        this.var.getEcran().opacite(this.var.valeurTransparence);
    }

    /**
     * Change la popup par un simple bouton qu'on peut déplacer
     */
    public void chargerLayoutPopupRangee() {
        if(var.bulle) {
            if (var.vueActuelle == 1)
                this.var.windowManager.removeView(this.var.vuePrincipale);

            this.var.vueActuelle = 2;
            this.var.windowManager.addView(this.var.vueRangee, this.var.pR);
            this.var.windowManager.updateViewLayout(this.var.vueRangee, this.var.pR);
            this.var.vueRangee.findViewById(R.id.bt08).setOnTouchListener(this.listener);
        }
    }

    /**
     * Change l'opacité de la fenêtre principale
     */
    public void opacitePrincipale(int val){
        if(var.bulle) {
            View v = var.vuePrincipale.findViewById(R.id.mainlayout);
            if(var.fenetreTransparente) {
                // On récupère le layout
                float op = (float) val / 255;
                if (v != null) {
                    // On change son oppacité
                    v.setAlpha(op);
                }
            }
            else
                v.setAlpha(1);
        }
        View v = this.findViewById(R.id.mainlayout);
        if(var.fenetreTransparente) {
            // On récupère le layout
            if(val < 45)
                val = 45;
            float op = (float) (val-42) / 255;
            if (v != null) {
                // On change son oppacité
                v.setAlpha(op);
            }
        }
        else
            v.setAlpha(1);
    }

    /**
     * Ajoute le bouton sur lequel nous venons de cliquer, dans la file d'attente de traitement
     * @param v Le bouton cliqué
     */
    public void clickButton(View v) {
        this.listener.onClick(v);
    }

    /**
     * Traite la case à cocher qu'on vient de modifier
     * @param v Le bouton cliqué
     */
    public void caseCochee(View v) {
        this.listener.onSelect(v);
    }

    /**
     * Traite la seekbar qu'on vient de modifier
     * @param v Le bouton cliqué
     */
    public void changerValeur(View v) {
        this.listener.onChangeValue(v);
    }

    /**
     * Indique le nom du réseau bluetooth sur lequel nous devons nous connecter
     * @return nom du réseau BT
     */
    public String getNomReseau() {
        return "K2000";
    }
}
