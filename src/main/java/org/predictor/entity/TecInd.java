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
@Table(name="TEC_IND")
public class TecInd implements Serializable {
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
	
	@Column(name="VALUE")
    private double value; // volume 
	
	@Column(name="INTERVAL_MIN")
    private int intervalMin;

	public TecInd() {}

	public TecInd(Date data, String timestamp, String symbol, String indType, double value, int intervalMin) {
		this.data = data;
		this.timestamp = timestamp;
		this.symbol = symbol;
		this.indType = indType;
		this.value = value;
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

	public String getIndType() {
		return indType;
	}

	public void setIndType(String indType) {
		this.indType = indType;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getIntervalMin() {
		return intervalMin;
	}

	public void setIntervalMin(int intervalMin) {
		this.intervalMin = intervalMin;
	}
}
