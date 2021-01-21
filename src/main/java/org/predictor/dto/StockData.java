package org.predictor.dto;

public class StockData {

	//22 caratteristiche - close=label
	private String stockName;
	private String date;
	private double open;
	private double high;
	private double low;
	private double label;
	private double closeRef1;
	private double closeRef2;
	private double closeVX;
	private double closeDJ;
	private double closeFS;
	private double closeHS;
	private double closeNK;
	private double pctChange;
	private double ma7;
	private double ma21;
	private double ema26;
	private double ema12;
	private double MACD;
	private double sd20;
	private double upperBand;
	private double owerBand;
	private double ema;
	private double momentum;
	private int annoMeseGiorno;
	
	public StockData() {
		super();
	}

	public StockData(String stockName, String date, double open, double high, double low, double label, double closeRef1,
			double closeRef2, double closeVX, double closeDJ, double closeFS, double closeHS, double closeNK,
			double pctChange, double ma7, double ma21, double ema26, double ema12, double mACD, double sd20,
			double upperBand, double owerBand, double ema, double momentum) {
		super();
		this.stockName = stockName;
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.label = label;
		this.closeRef1 = closeRef1;
		this.closeRef2 = closeRef2;
		this.closeVX = closeVX;
		this.closeDJ = closeDJ;
		this.closeFS = closeFS;
		this.closeHS = closeHS;
		this.closeNK = closeNK;
		this.pctChange = pctChange;
		this.ma7 = ma7;
		this.ma21 = ma21;
		this.ema26 = ema26;
		this.ema12 = ema12;
		this.MACD = mACD;
		this.sd20 = sd20;
		this.upperBand = upperBand;
		this.owerBand = owerBand;
		this.ema = ema;
		this.momentum = momentum;
		this.annoMeseGiorno = Integer.parseInt(date.replaceAll("-", ""));
	}

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getLabel() {
		return label;
	}

	public void setLabel(double label) {
		this.label = label;
	}

	public double getCloseRef1() {
		return closeRef1;
	}

	public void setCloseRef1(double closeRef1) {
		this.closeRef1 = closeRef1;
	}

	public double getCloseRef2() {
		return closeRef2;
	}

	public void setCloseRef2(double closeRef2) {
		this.closeRef2 = closeRef2;
	}

	public double getCloseVX() {
		return closeVX;
	}

	public void setCloseVX(double closeVX) {
		this.closeVX = closeVX;
	}

	public double getCloseDJ() {
		return closeDJ;
	}

	public void setCloseDJ(double closeDJ) {
		this.closeDJ = closeDJ;
	}

	public double getCloseFS() {
		return closeFS;
	}

	public void setCloseFS(double closeFS) {
		this.closeFS = closeFS;
	}

	public double getCloseHS() {
		return closeHS;
	}

	public void setCloseHS(double closeHS) {
		this.closeHS = closeHS;
	}

	public double getCloseNK() {
		return closeNK;
	}

	public void setCloseNK(double closeNK) {
		this.closeNK = closeNK;
	}

	public double getPctChange() {
		return pctChange;
	}

	public void setPctChange(double pctChange) {
		this.pctChange = pctChange;
	}

	public double getMa7() {
		return ma7;
	}

	public void setMa7(double ma7) {
		this.ma7 = ma7;
	}

	public double getMa21() {
		return ma21;
	}

	public void setMa21(double ma21) {
		this.ma21 = ma21;
	}

	public double getEma26() {
		return ema26;
	}

	public void setEma26(double ema26) {
		this.ema26 = ema26;
	}

	public double getEma12() {
		return ema12;
	}

	public void setEma12(double ema12) {
		this.ema12 = ema12;
	}

	public double getMACD() {
		return MACD;
	}

	public void setMACD(double mACD) {
		MACD = mACD;
	}

	public double getSd20() {
		return sd20;
	}

	public void setSd20(double sd20) {
		this.sd20 = sd20;
	}

	public double getUpperBand() {
		return upperBand;
	}

	public void setUpperBand(double upperBand) {
		this.upperBand = upperBand;
	}

	public double getOwerBand() {
		return owerBand;
	}

	public void setOwerBand(double owerBand) {
		this.owerBand = owerBand;
	}

	public double getEma() {
		return ema;
	}

	public void setEma(double ema) {
		this.ema = ema;
	}

	public double getMomentum() {
		return momentum;
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public int getAnnoMeseGiorno() {
		return annoMeseGiorno;
	}

	public void setAnnoMeseGiorno(int annoMeseGiorno) {
		this.annoMeseGiorno = annoMeseGiorno;
	}

	@Override
	public String toString() {
		return "Item [\tstockName=" + stockName + ", \tdate=" + date + ", \topen=" + open + ", \thigh=" + high + ", \tlow=" + low
				+ ", \tlabel=" + label + ", \tcloseRef1=" + closeRef1 + ", \tcloseRef2=" + closeRef2 + ", \tcloseVX=" + closeVX
				+ ", \tcloseDJ=" + closeDJ + ", \tcloseFS=" + closeFS + ", \tcloseHS=" + closeHS + ", \tcloseNK=" + closeNK
				+ ", \tpctChnge=" + pctChange + ", \tma7=" + ma7 + ", \tma21=" + ma21 + ", \tema26=" + ema26 + ", \tema12="
				+ ema12 + ", \tMACD=" + MACD + ", \tsd20=" + sd20 + ", \tupperBand=" + upperBand + ", \towerBand=" + owerBand
				+ ", \tema=" + ema + ", \tmomentum=" + momentum + ", \tannoMeseGiorno=" + annoMeseGiorno + "]";
	}

}
