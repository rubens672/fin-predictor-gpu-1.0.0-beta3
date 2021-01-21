package org.predictor.dto;

import org.predictor.utils.TipoEnum;

/*
 * 
https://www.alphavantage.co/query?
function=BBANDS
&outputsize=full
&interval=daily
&time_period=60
&series_type=close
&nbdevup=3
&nbdevdn=3
&datatype=json
&apikey=05WFWKLKS2UURLUA
&symbol=FTSEMIB.MI

 */

public class TechnicalIndicator {

	private String url = "https://www.alphavantage.co/query?";
	private String outputsize = "full";
	private String function;
	private String interval;
	private String timePeriod;
	private String seriesType;
	private String nbdevup;
	private String nbdevdn;
	public static String datatype = "csv";
	private String apikey = "05WFWKLKS2UURLUA";
	private static String symbol = "";
	
	public String toString() {
		StringBuffer sb = new StringBuffer(url);
		sb.append("function=" + function);
		sb.append("&outputsize=" + outputsize);
		sb.append("&interval=" + interval);
		sb.append(timePeriod != null ? "&time_period=" + timePeriod : "");
		sb.append(seriesType != null ? "&series_type=" + seriesType : "");
		sb.append(nbdevup != null ? "&nbdevup=" + nbdevup : "");
		sb.append(nbdevdn != null ? "&nbdevdn=" + nbdevdn : "");
		sb.append("&datatype=" + datatype);
		sb.append("&apikey=" + apikey);
		sb.append("&symbol=" + symbol);
		
		return sb.toString();
	}
	
	/*
	 * Bande di Bollinger
	 */
	public TechnicalIndicator(TipoEnum function, TipoEnum interval, Integer timePeriod, Integer nbdevup, Integer nbdevdn, TipoEnum seriesType) {
		this.function = function.label();
		this.interval = interval.label();
		this.timePeriod = timePeriod.toString();
		this.nbdevup = nbdevup.toString();
		this.nbdevdn = nbdevdn.toString();
		this.seriesType = seriesType.label();
	}
	
	/*
	 * Indicatori vari
	 */
	public TechnicalIndicator(TipoEnum function, TipoEnum interval, Integer timePeriod, TipoEnum seriesType) {
		this.function = function.label();
		this.interval = interval.label();
		this.timePeriod = timePeriod.toString();
		this.seriesType = seriesType.label();
	}
	
	/*
	 * Titoli azionari
	 */
	public TechnicalIndicator(TipoEnum function, TipoEnum interval, TipoEnum seriesType) {
		this.function = function.label();
		this.interval = interval.label();
		this.seriesType = seriesType.label();
	}
	
	public String getUrl() {
		return toString();
	}
	
	public static void main(String[]args) {
		TechnicalIndicator.datatype = "json";
		TechnicalIndicator.symbol = TipoEnum.FTSE.label();
		
		TechnicalIndicator test1 = new TechnicalIndicator(TipoEnum.BBDS, 			  TipoEnum.DAILY, 60, 3, 3, TipoEnum.CLOSE);
		TechnicalIndicator test2 = new TechnicalIndicator(TipoEnum.EMA,  			  TipoEnum.DAILY, 60,       TipoEnum.CLOSE);
		TechnicalIndicator test3 = new TechnicalIndicator(TipoEnum.RSI,  			  TipoEnum.DAILY, 60,       TipoEnum.CLOSE);
		TechnicalIndicator test4 = new TechnicalIndicator(TipoEnum.TIME_SERIES_DAILY, TipoEnum.DAILY,    		TipoEnum.CLOSE);
		
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);
		System.out.println(test4);
	}

}
