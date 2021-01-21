package org.predictor.utils;

import java.io.Serializable;

public enum TipoEnum implements Serializable{
	//  id	 label		  descrizione
	MSFT(1,  "MSFT", 	  "Microsoft"),
	AAPL(2,  "AAPL", 	  "Apple"),
	SAMS(3,  "005930.KS", "Samsung"),
	NSDQ(4,  "IXIC", 	  "Nasdaq"),
	DOWJ(5,  "DJI", 	  "Dow jones"),
	FTSE(6,  "^FTSE", 	  "Ftse 100"),
	NIKK(7,  "^N225", 	  "Nikkei"),
	 SMA(8,  "SMA", 	  "Media mobile semplice"),
	 EMA(9,  "EMA", 	  "Media mobile esponenziale"),
	BBDS(10, "BBANDS", 	  "Bande di Bollinger"), 
	LOCALE(11, "local[*]", "Locale"),
    YARN(12, "yarn-client", "Yarn"),
    MINMAX(13, "MinMaxScaler", "Normalizer Min Max Scaler"),
    STNDRD(14, "Standardize", "Normalizer Standardize"),
    CLOSE(15, "close", "Close"),
    DAILY(16, "daily", "Daily"),
    TIME_SERIES_DAILY(17, "TIME_SERIES_DAILY", "This API returns daily time series (date, daily open, daily high, daily low, daily close, daily volume) of the global equity specified, covering 20+ years of historical data."),
    FTSEMIB(18, "FTSEMIB.MI", "Borsa di Milano"),
    RSI(19, "RSI", "This API returns the relative strength index (RSI) values.");
	
	private int id;
	private String label;
	private String descrizione;
	
	TipoEnum(int i, String l, String d){
		id = i;
		label = l;
		descrizione = d;
	}

	public int id() {return id;}
	public String label() {return label;}
	public String descrizione() {return descrizione;}
	
    // converter that will be used later
    public static TipoEnum fromString(String code) {
        for(TipoEnum output : TipoEnum.values()) {
            if(output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }
        return null;
    }
}
