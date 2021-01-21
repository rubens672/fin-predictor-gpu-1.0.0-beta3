package org.predictor.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Montante ad interesse composto continuo o matematico. 
 * Gli interessi si sommano al capitale che li ha prodotti ad ogni istante. 
 * 
 */
public class Interessi {
	
	private static NumberFormat  f;
	
	static {
		f = DecimalFormat.getInstance(Locale.ITALIAN);
		f.setMaximumFractionDigits(2);
	}

	public static void main(String[] args) {
		
		double i = 0.02; //tasso di guadagno giornaliero 5%
		double c = 0.25; //imposte sul capital gain
		double ic = i * (1 - c); //tasso di guadagno giornaliero al netto delle imposte
		int t = 20; //numero di giorni
		int n = 12; //il montante al termine di n mesi
		double C = 50000.0; //capitale iniziale
		double M_mese = C * Math.pow((1 + ic), t); // montante
		double M_anno = C * Math.pow((1 + ic), t * n); // montante
		
		System.out.println("- calcolo del montante -");
		System.out.println("giorno\t" + f.format(C * i));
		System.out.println("mese\t" + f.format(M_mese));
		System.out.println("anno\t" + f.format(M_anno));
		
//		for(int j=2; j<13; j++) {
//			System.out.println(j + " mesi\t" + f.format(C * Math.pow((1 + ic), t * j)));
//		}
		
	}

}
