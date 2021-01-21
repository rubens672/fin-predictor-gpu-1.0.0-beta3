package org.predictor.utils;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

public class Tipo implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String label;
	private String descrizione;

	public Tipo(int id, String label, String descrizione) {
		this.id = id;
		this.label = label;
		this.descrizione = descrizione;
	}

	public Tipo() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	@Override
	public String toString() {
		return "Tipo [id=" + id + ", label=" + label + ", descrizione=" + descrizione + "]";
	}
	
	
}
