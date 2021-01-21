package org.predictor.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.predictor.utils.Costants;

@Entity
@Table(name="FOREX")
@NamedQueries({
	@NamedQuery(name= Forex.FIND_ALL, query="SELECT s FROM Forex s ORDER BY s.data ASC")
})
public class Forex implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FIND_ALL = "Forex.FIND_ALL";
	public static final String FIND_ALL_NO_ZERO = "Forex.FIND_ALL_NO_ZERO";
	public static final String SYMBOL = "symbol";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID")
	private long id;
	
	@Column(name="DATA")
	@Temporal(TemporalType.DATE)
	private Date data;
	
	@Column(name="TIMESTAMP")
	private String timestamp; // date
	
	@Column(name="FROM_SYMBOL")
	private String from_symbol;
	
	@Column(name="TO_SYMBOL")
	private String to_symbol;

	@Column(name="OPEN")
    private double open; // open price
	
	@Column(name="CLOSE")
    private double close; // close price
	
	@Column(name="LOW")
    private double low; // low price
	
	@Column(name="HIGH")
    private double high; // high price

	@Column(name="INTERVAL_MIN")
    private int intervalMin;
	
	public Forex() {
		
	}

	public Forex(Date data, String timestamp, String from_symbol, String to_symbol, double open, double close,
			double low, double high, int intervalMin) {
		this.data = data;
		this.timestamp = timestamp;
		this.from_symbol = from_symbol;
		this.to_symbol = to_symbol;
		this.open = open;
		this.close = close;
		this.low = low;
		this.high = high;
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

	public String getFrom_symbol() {
		return from_symbol;
	}

	public void setFrom_symbol(String from_symbol) {
		this.from_symbol = from_symbol;
	}

	public String getTo_symbol() {
		return to_symbol;
	}

	public void setTo_symbol(String to_symbol) {
		this.to_symbol = to_symbol;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public int getIntervalMin() {
		return intervalMin;
	}

	public void setIntervalMin(int intervalMin) {
		this.intervalMin = intervalMin;
	}
	
	@Override
	public String toString() {
		return Costants.DATE_FORMAT.format(data) + "," + from_symbol + "," + to_symbol + open + "," + close + "," + low + "," + high + "," ;
	}
}
