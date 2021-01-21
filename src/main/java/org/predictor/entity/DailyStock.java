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
@Table(name="DAILY_STOCK")
@NamedQueries({
	@NamedQuery(name= DailyStock.FIND_ALL, query="SELECT s FROM DailyStock s ORDER BY s.data ASC")
})
public class DailyStock implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FIND_ALL = "StockData.FIND_ALL";
	public static final String FIND_ALL_NO_ZERO = "StockData.FIND_ALL_NO_ZERO";
	public static final String SYMBOL = "symbol";
	
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

	@Column(name="OPEN")
    private double open; // open price
	
	@Column(name="CLOSE")
    private double close; // close price
	
	@Column(name="LOW")
    private double low; // low price
	
	@Column(name="HIGH")
    private double high; // high price
	
	@Column(name="VOLUME")
    private double volume; // volume 
	
	@Column(name="CATEGORIA")
    private int categoria;// 

	@Column(name="INTERVAL_MIN")
    private int intervalMin;
	
	@Transient
	private double label;

	public DailyStock(Date data, String timestamp, String symbol, double open, double high, double low, double close, double volume, int categoria, int intervalMin) {
		this.data = data;
		this.timestamp = timestamp;
		this.symbol = symbol;
		this.open = open;
		this.close = close;
		this.low = low;
		this.high = high;
		this.volume = volume;
		this.categoria = categoria;
		this.intervalMin = intervalMin;
	}

	public DailyStock() { }

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

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public int getCategoria() {
		return categoria;
	}

	public void setCategoria(int categoria) {
		this.categoria = categoria;
	}

	//date,symbol,open,close,low,high,volume
	@Override
	public String toString() {
		return Costants.DATE_FORMAT.format(data) + "," + symbol + "," + open + "," + close + "," + low + "," + high + "," + volume;
	}

	public int getIntervalMin() {
		return intervalMin;
	}

	public void setIntervalMin(int intervalMin) {
		this.intervalMin = intervalMin;
	}
	
}
