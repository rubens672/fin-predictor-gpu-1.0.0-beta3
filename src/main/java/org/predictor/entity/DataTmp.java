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
@Table(name="DATA_TMP")
public class DataTmp  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID")
	private long id;
	
	@Column(name="DATA")
	@Temporal(TemporalType.DATE)
	private Date data;
	
	@Column(name="DAY")
	private int day;
	
	@Column(name="INTERVAL_MIN")
    private int intervalMin;

	public DataTmp() { }

	public DataTmp(Date data, int day, int intervalMin) {
		this.data = data;
		this.day = day;
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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	@Override
	public String toString() {
		return "DataTmp [data=" + data + ", day=" + day + "]";
	}

	public int getIntervalMin() {
		return intervalMin;
	}

	public void setIntervalMin(int intervalMin) {
		this.intervalMin = intervalMin;
	}
}
