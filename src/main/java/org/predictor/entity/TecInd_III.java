package org.predictor.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="TEC_IND_III")
public class TecInd_III implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID")
	private long id;
	
	@Column(name="DATA")
	@Temporal(TemporalType.TIMESTAMP)
	private Date data;
	
	@Column(name="TIMESTAMP")
	private String timestamp; // date
	
	@Column(name="SYMBOL")
	private String symbol;
	
	@Column(name="IND_TYPE")
	private String indType;
	
	@Column(name="VALUE_I")
    private double valI;
	
	@Column(name="VALUE_II")
    private double valII;
	
	@Column(name="VALUE_III")
    private double valIII;
	
	@Column(name="INTERVAL_MIN")
    private int intervalMin;

	public TecInd_III() {}

	public TecInd_III(Date data, String timestamp, String symbol, String indType, double valI, double valII, double valIII, int intervalMin) {
		this.data = data;
		this.timestamp = timestamp;
		this.symbol = symbol;
		this.indType = indType;
		this.valI = valI;
		this.valII = valII;
		this.valIII = valIII;
		this.intervalMin = intervalMin;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getValI() {
		return valI;
	}

	public void setValI(double valI) {
		this.valI = valI;
	}

	public double getValII() {
		return valII;
	}

	public void setValII(double valII) {
		this.valII = valII;
	}

	public double getValIII() {
		return valIII;
	}

	public void setValIII(double valIII) {
		this.valIII = valIII;
	}

	public String getIndType() {
		return indType;
	}

	public void setIndType(String indType) {
		this.indType = indType;
	}

	public int getIntervalMin() {
		return intervalMin;
	}

	public void setIntervalMin(int intervalMin) {
		this.intervalMin = intervalMin;
	}

}
