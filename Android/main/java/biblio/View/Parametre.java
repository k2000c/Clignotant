package biblio.View;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import biblio.Controler.Var;

/**
 * Gère tous les paramètres de l'application
 */
public class Parametre {
	/**
	 * Nom du réseau bluetooth sur lequel se connecter
	 */
	private String nomReseau;
	private Var var;

	public void setVar(Var var){
		this.var = var;
	}

	public String getNomReseau() {
		return this.nomReseau;
	}

	public void setNomReseau(String nomReseau) {
		this.nomReseau = nomReseau;
	}

	/**
	 * Crée un groupe de paramètres
	 */
	public Parametre() {
	}

	/**
	 * Charge le fichier de config dans les variables
	 * @param file
	 */
	public void chargerConfig(String file) {
		Log.d("File", "Lecture");
		// On regarde si le fichier existe
		try {
			FileInputStream fis = this.var.main.openFileInput(file);

			// On charge
			String str = this.convertStreamToString(fis);
			fis.close();

			// On extrait les infos (Avec vérification !!)
			Log.d("File", str);
			String[] strs = str.split("-");
			int i=0;
			for(String s : strs){
				i++;
				switch(i){
					case 1: // CouperBT
						this.var.couperBt = s.equals("true");
						break;

					case 2: // Bulle
						this.var.bulle = s.equals("true");
						break;

					case 3: // Transparent
						this.var.fenetreTransparente = s.equals("true");
						break;

					case 4: // Niveau trans
						try {
							int var = Integer.parseInt(s);
							if (var == 0 && !s.equals("0") || var<0 || var>255)
								break;
							this.var.valeurTransparence = var;
						}catch(Exception e){
							Log.e("Lecture Fichier", e.toString());
						}
						break;

					case 5: // Puissance feux
						try {
							int var = Integer.parseInt(s);
							if (var == 0 && !s.equals("0") || var<0 || var>5)
								break;
							this.var.valeurFeux = var;
						}catch(Exception e){
							Log.e("Lecture Fichier", e.toString());
						}
						break;

					case 6: // AllumerFeux
						this.var.allumerFeux = s.equals("true");
						break;

					case 7: // ValeurAllumageFeux
						try {
							int var = Integer.parseInt(s);
							if (var == 0 && !s.equals("0") || var<0)
								break;
							this.var.valeurAllumageFeux = var;
						}catch(Exception e){
							Log.e("Lecture Fichier", e.toString());
						}
						break;

					case 8: // RotationMinimum
						try {
							int var = Integer.parseInt(s);
							if (var == 0 && !s.equals("0") || var<0 || var>360)
								break;
							this.var.rotationMinimum = var;
						}catch(Exception e){
							Log.e("Lecture Fichier", e.toString());
						}
						break;

					default:
						break;
				}
			}
		}catch(Exception e){
		}
	}

	/**
	 * Enregistre la configuration actuelle dans le fichier de config
	 * @param file
	 */
	public void sauverConfig(String file) {
		// On supprime le fichier
		try{
			this.var.main.deleteFile(file);
		}catch(Exception e){
		}

		// On crée le fichier et on enregistre la config
		try {
			Log.d("File", "Ecriture");
			FileOutputStream fos = this.var.main.openFileOutput(file, Context.MODE_PRIVATE);

			// On crée la chaine de caractère
			String str = ""
					+this.var.couperBt+"-"
					+this.var.bulle+"-"
					+this.var.fenetreTransparente+"-"
					+this.var.valeurTransparence+"-"
					+this.var.valeurFeux+"-"
					+this.var.allumerFeux+"-"
					+this.var.valeurAllumageFeux+"-"
					+this.var.rotationMinimum+"-";

			// On enregistre
			Log.d("File", str);
			fos.write(str.getBytes());
			fos.close();
		}catch(Exception e){
		}
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}