package org.predictor.main.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.predictor.entity.TecInd_II;
import org.predictor.entity.TecInd_III;
import org.predictor.dto.TechnicalIndicator;
import org.predictor.entity.DailyStock;
import org.predictor.entity.DataTmp;
import org.predictor.entity.Forex;
import org.predictor.entity.TecInd;
import org.predictor.utils.Costants;
import org.predictor.utils.TipoEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class StockPricePredictorContinuousBase {
	
	protected static final Logger log = LoggerFactory.getLogger(StockPricePredictorContinuousBase.class);
	
	protected static EntityManagerFactory emf;
	protected static EntityManager em;
	
//	protected static final String persistenceUnitName = Costants.JPA_SERVICE;
//	private static final String persistenceUnitName = Costants.JPA_SERVICE_ORA;
//	private static final String persistenceUnitName = Costants.JPA_SERVICE_ORA_SFERA;
	private static final String persistenceUnitName = Costants.JPA_SERVICE_ORA_XE;
	
	protected String alphavantageUrl = Costants.alphavantageUrl;
	protected String alphavantageBBANDSUrl = Costants.alphavantageBBANDSUrl;
	protected String alphavantageMACDUrl = Costants.alphavantageMACDUrl;
	protected String alphavantageSTOCHUrl = Costants.alphavantageSTOCHUrl;
	protected String alphavantageAROONUrl = Costants.alphavantageAROONUrl;
	protected String alphavantageEMAUrl = Costants.alphavantageEMAUrl;
	protected String alphavantageSMAUrl = Costants.alphavantageSMAUrl;
	protected String dataSetDir = Costants.dataSetDirConvLstm + "technical_indicator_";
	protected String dataSetForexDir = Costants.dataSetDirConvLstm + "fx_daily_";
//	protected String dataSetDir = Costants.dataSetDirConvLstm + "daily_";
	
	public int epochs = Costants.epochs;
	public int miniBatchSize = 2;
	public int VECTOR_SIZE = Costants.VECTOR_SIZE;
	public int timesteps = Costants.timesteps;
	public int predictLength = Costants.outputLength;
	public double learningRate = Costants.learningRate;
	public double rmsDecay = Costants.rmsDecay;
	public Double l2 =  Costants.l2;
	public Double l1 =  Costants.l1;
	public int truncatedBPTT = Costants.truncatedBPTT;
	
	protected String data = Costants.DATE_FORMAT.format(new Date(System.currentTimeMillis()));
	protected String lastData = Costants.DATE_FORMAT.format(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)));
	protected long inizio = System.currentTimeMillis();
	protected double bestScore = 100;
	protected int bestEpoch = 0;
	protected static final DecimalFormat f = new DecimalFormat("####0.00000000");
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("HH'h' mm'm' ss's'");
	
	protected final static int CLOSE = 1;
	protected final static int LOW = 2;
	protected final static int HIGH = 3;
	
	protected static long intervallo = System.currentTimeMillis();
	protected static int count = 1;
	
	/**

UPDATE DAILY_STOCK SET DAY=TO_NUMBER(TO_CHAR(DATA, 'd'));

--INSERT INTO DAILY_STOCK_ADJUSTED 
SELECT DATA_ORA,SYMBOL,
CASE WHEN T.CLOSE IS NULL THEN AVG(case when T.CLOSE<>0 then T.CLOSE end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.CLOSE END AS CLOSE,
CASE WHEN T.OPEN IS NULL THEN AVG(case when T.OPEN<>0 then T.OPEN end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.OPEN END AS OPEN,
CASE WHEN T.HIGH IS NULL THEN AVG(case when T.HIGH<>0 then T.HIGH end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.HIGH END AS HIGH,
CASE WHEN T.LOW IS NULL THEN AVG(case when T.LOW<>0 then T.LOW end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.LOW END AS LOW,
CASE WHEN T.BB_MIDDLE IS NULL THEN AVG(case when T.BB_MIDDLE<>0 then T.BB_MIDDLE end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.BB_MIDDLE END AS BB_MIDDLE,
CASE WHEN T.BB_UPPER IS NULL THEN AVG(case when T.BB_UPPER<>0 then T.BB_UPPER end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.BB_UPPER END AS BB_UPPER,
CASE WHEN T.BB_LOWER IS NULL THEN AVG(case when T.BB_LOWER<>0 then T.BB_LOWER end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.BB_LOWER END AS BB_LOWER,
CASE WHEN T.EMA IS NULL THEN AVG(case when T.EMA<>0 then T.EMA end) OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) ELSE T.EMA END AS EMA,
LUN,MAR,MER,GIO,VEN,MASK,DAY,15 AS INTERVAL_MIN,DATA
FROM (
SELECT TO_CHAR(d.DATA,'YYYY-MM-DD HH24:MI') DATA_ORA, 'US.MI' SYMBOL,
sy.CLOSE,sy.OPEN,sy.HIGH,sy.LOW,
bb.VALUE_I BB_MIDDLE,bb.VALUE_III BB_UPPER,bb.VALUE_II BB_LOWER,ema.VALUE EMA,
CASE WHEN d.DAY=1 THEN 1 ELSE 0 END AS LUN,
CASE WHEN d.DAY=2 THEN 1 ELSE 0 END AS MAR,
CASE WHEN d.DAY=3 THEN 1 ELSE 0 END AS MER,
CASE WHEN d.DAY=4 THEN 1 ELSE 0 END AS GIO,
CASE WHEN d.DAY=5 THEN 1 ELSE 0 END AS VEN,
CASE WHEN sy.CLOSE=sy.OPEN AND sy.CLOSE=sy.HIGH AND sy.CLOSE=sy.LOW AND sy.CLOSE=0 THEN 0 ELSE 1 END AS MASK,
d.DAY,d.DATA
FROM DATA_TMP d
LEFT OUTER JOIN DAILY_STOCK sy ON sy.DATA=d.DATA AND sy.SYMBOL='US.MI' AND sy.CATEGORIA=6 AND sy.INTERVAL_MIN=15--AND sy.SYMBOL NOT IN ('BDB.MI','BRI.MI')
LEFT OUTER JOIN TEC_IND_III bb ON bb.DATA=sy.DATA AND  bb.SYMBOL=sy.SYMBOL AND  bb.IND_TYPE='BBANDS' AND bb.INTERVAL_MIN=15
LEFT OUTER JOIN TEC_IND ema    ON ema.DATA=sy.DATA AND ema.SYMBOL=sy.SYMBOL AND ema.IND_TYPE='EMA' AND ema.INTERVAL_MIN=15
WHERE d.DATA>=(SELECT MIN(d.DATA) FROM DATA_TMP d WHERE d.INTERVAL_MIN=15 AND TO_NUMBER(TO_CHAR(d.DATA, 'HH24'))=11 AND TO_NUMBER(TO_CHAR(d.DATA, 'MI'))=15 AND d.DATA>TO_DATE('2018-11-30','YYYY-MM-DD') AND d.DAY=5)
AND d.INTERVAL_MIN=15
ORDER BY d.DATA ASC) T;
		
		
--INSERT INTO DATA_TMP
SELECT DATA,DAY FROM (
SELECT (TO_DATE('2019-01-01','YYYY-MM-DD') - level ) AS DATA, TO_CHAR((TO_DATE('2019-01-01','YYYY-MM-DD') - level ), 'd') AS DAY
FROM DUAL
CONNECT BY LEVEL <= (TO_DATE('2019-01-01','YYYY-MM-DD') - TO_DATE('2001-01-01','YYYY-MM-DD')) order by DATA ASC)
WHERE DAY NOT IN (6,7);

--media fra il precendete e il successivo per i valori mancanti
SELECT TRUNC(T.c_mavg,4), T.* FROM (
SELECT s.*,
   AVG(case when VOLUME<>0 then VOLUME end) OVER (PARTITION BY SYMBOL ORDER BY DATA 
   ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING) AS c_mavg
   FROM DAILY_STOCK s
   WHERE s.SYMBOL='US.MI' 
   AND s.DATA>TO_DATE('2018-10-26 09','YYYY-MM-DD HH24')) T;

SELECT * FROM DAILY_STOCK;
SELECT * FROM DAILY_STOCK_ADJUSTED;
SELECT * FROM DATA_TMP;
SELECT * FROM TEC_IND;
SELECT * FROM TEC_IND_III; 
	 
	 */
	private String INSERT_DAILY_STOCK_ADJUSTED = 
			"INSERT INTO DAILY_STOCK_ADJUSTED " +
			"SELECT DATA_ORA,SYMBOL, " +
			"CASE WHEN T.CLOSE IS NULL THEN AVG(case when T.CLOSE<>0 then T.CLOSE end) 				OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.CLOSE END AS CLOSE, " +
			"CASE WHEN T.OPEN IS NULL THEN AVG(case when T.OPEN<>0 then T.OPEN end) 				OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.OPEN END AS OPEN, " +
			"CASE WHEN T.HIGH IS NULL THEN AVG(case when T.HIGH<>0 then T.HIGH end) 				OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.HIGH END AS HIGH, " +
			"CASE WHEN T.LOW IS NULL THEN AVG(case when T.LOW<>0 then T.LOW end) 					OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.LOW END AS LOW, " +
			"CASE WHEN T.BB_MIDDLE IS NULL THEN AVG(case when T.BB_MIDDLE<>0 then T.BB_MIDDLE end) 	OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.BB_MIDDLE END AS BB_MIDDLE, " +
			"CASE WHEN T.BB_UPPER IS NULL THEN AVG(case when T.BB_UPPER<>0 then T.BB_UPPER end) 	OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.BB_UPPER END AS BB_UPPER, " +
			"CASE WHEN T.BB_LOWER IS NULL THEN AVG(case when T.BB_LOWER<>0 then T.BB_LOWER end) 	OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.BB_LOWER END AS BB_LOWER, " +
			"CASE WHEN T.EMA IS NULL THEN AVG(case when T.EMA<>0 then T.EMA end) 					OVER (PARTITION BY T.SYMBOL ORDER BY DATA ROWS BETWEEN 4 PRECEDING AND 4 FOLLOWING) ELSE T.EMA END AS EMA, " +
			"LUN,MAR,MER,GIO,VEN,MASK,DAY,5 AS INTERVAL_MIN,DATA " +
			"FROM ( " +
			"SELECT TO_CHAR(d.DATA,'YYYY-MM-DD HH24:MI') DATA_ORA, ? SYMBOL, " +
			"sy.CLOSE,sy.OPEN,sy.HIGH,sy.LOW, " +
			"bb.VALUE_I BB_MIDDLE,bb.VALUE_III BB_UPPER,bb.VALUE_II BB_LOWER,ema.VALUE EMA, " +
			"CASE WHEN d.DAY=1 THEN 1 ELSE 0 END AS LUN, " +
			"CASE WHEN d.DAY=2 THEN 1 ELSE 0 END AS MAR, " +
			"CASE WHEN d.DAY=3 THEN 1 ELSE 0 END AS MER, " +
			"CASE WHEN d.DAY=4 THEN 1 ELSE 0 END AS GIO, " +
			"CASE WHEN d.DAY=5 THEN 1 ELSE 0 END AS VEN, " +
			"CASE WHEN sy.CLOSE=sy.OPEN AND sy.CLOSE=sy.HIGH AND sy.CLOSE=sy.LOW AND sy.CLOSE=0 THEN 0 ELSE 1 END AS MASK, " +
			"d.DAY,d.DATA " +
			"FROM DATA_TMP d " +
			"LEFT OUTER JOIN DAILY_STOCK sy ON sy.DATA=d.DATA AND sy.SYMBOL=? AND sy.CATEGORIA=6 AND sy.INTERVAL_MIN=5 " +
			"LEFT OUTER JOIN TEC_IND_III bb ON bb.DATA=sy.DATA AND  bb.SYMBOL=sy.SYMBOL AND  bb.IND_TYPE='BBANDS' AND bb.INTERVAL_MIN=5 " +
			"LEFT OUTER JOIN TEC_IND ema    ON ema.DATA=sy.DATA AND ema.SYMBOL=sy.SYMBOL AND ema.IND_TYPE='EMA' AND ema.INTERVAL_MIN=5 " +
			"WHERE d.DATA>TO_DATE('2019-01-04','YYYY-MM-DD') " +
			"AND d.INTERVAL_MIN=5 " +
			"ORDER BY d.DATA ASC) T";
	
	public static void main(String[]args) throws Exception {
//		String[] symbols = {"CHL.MI","S24.MI","RN.MI","SRI.MI","ELC.MI","DIA.MI","ACO.MI","IKG.MI","MON.MI","PQ.MI","TES.MI","A2A.MI","REC.MI","ENEL.MI"};
//		String[] symbols = {"SRI.MI","TES.MI","IF.MI","DIA.MI","REC.MI","ELC.MI"};
		//https://finance.yahoo.com/
//		String[] symbols = {"FTSEMIB.MI","^GDAXI","^HSI","^N225","^DJI","^FTSE","^FCHI","^JKSE","^KLSE","^MXX","^MERV","NDX"};
		//indice di volatilit�
//		String[] symbols = {"^FCHI","^JKSE","^KLSE","^MXX","^MERV","NDX"};
		//valori pi� completi
//		String[] symbols = {"^GDAXI","^FTSE","^HSI","^N225","^DJI","^VIX","CL","EURUSD"}; //close EURUSD al posto del volume di ^VIX
//		String[] symbols = {"^GDAXI","^FTSE","^HSI","^N225","^DJI","^VIX"};// borse: cat 2
//		String[] symbols = {"CL"}; // materie prime: cat 5
		
//		String[] symbols = {"FBK.MI","UBI.MI","BMED.MI","BAMI.MI","IF.MI","BPSO.MI","CVAL.MI","AZM.MI","PRO.MI"}; 
//		String[] symbols = {"BMPS.MI","G.MI","CASS.MI","CE.MI","ISP.MI","MB.MI","US.MI"};
//		String[] symbols = {"BMPS.MI","G.MI","CASS.MI","ISP.MI","MB.MI","US.MI","FBK.MI","UBI.MI","BMED.MI","BAMI.MI","CVAL.MI","AZM.MI","US.MI"}; 
		String[] symbols = {"^FTSE"};
//		String[] symbols = {"^DJI","^FCHI","^GDAXI","^HSI","^N225","^STOXX50E","^FTSE","^VIX"};
		//ETF
//		String[] symbols = {"XLE","PSI","VIOV","FXZ","RTM","SLY","QTEC","SMH","ONEQ","PRF","SLYV"};
		Integer timePeriod = 2;
		String bbUrl  = new TechnicalIndicator(TipoEnum.BBDS, 			   TipoEnum.DAILY, timePeriod, 2, 2, TipoEnum.CLOSE).getUrl();
		String emaUrl = new TechnicalIndicator(TipoEnum.EMA,  			   TipoEnum.DAILY, timePeriod,       TipoEnum.CLOSE).getUrl();
		String rsiUrl = new TechnicalIndicator(TipoEnum.RSI,  			   TipoEnum.DAILY, timePeriod,       TipoEnum.CLOSE).getUrl();
		String tsdUrl = new TechnicalIndicator(TipoEnum.TIME_SERIES_DAILY, TipoEnum.DAILY,    		 		 TipoEnum.CLOSE).getUrl();
		
		StockPricePredictorContinuousBase util = new StockPricePredictorContinuousBase();
//		util.creaDailyStockDataSet(symbols, 100000, 2, tsdUrl, 24);//scarica csv, inserimento db | arrotondamento volume, es: /10000
//		util.creaTecIndDataSet(symbols, "EMA", emaUrl, timePeriod);
//		util.creaTecIndDataSet(symbols, "RSI", rsiUrl, timePeriod);
//		util.creaTecInd_III_IndDataSet(symbols, "BBANDS", bbUrl, timePeriod);
//		util.creaTecInd_III_IndDataSet(symbols, "MACD", Costants.alphavantageMACDUrl, 60);
//		util.creaForexDataSet("GBP", "EUR", Costants.alphavantageForex_GBP_EUR_Url, 24);
//		util.creaForexDataSet("GBP", "USD", Costants.alphavantageForex_GBP_USD_Url, 24);
//		util.creaDatasetCsv();
		util.creaAnalyzeDatasetCsv();
//		util.insertDay(2000,Calendar.JANUARY,3, 2019,Calendar.MARCH,30,  24);//15-OCT-18	11-JAN-19 
//		util.insertDaylyStockAdjusted(symbols);
	}
	
	protected void insertDaylyStockAdjusted(String[] symbols) throws Exception {
		
		log.info("begin");
		begin();
		
		Query queryHeader = em.createNativeQuery(INSERT_DAILY_STOCK_ADJUSTED);
		
		for(String symbol: symbols) {
			queryHeader.setParameter(1, symbol);
			queryHeader.setParameter(2, symbol);
			
			queryHeader.executeUpdate();
			log.info("insertDaylyStockAdjusted {} ", symbol);
		}
		
		log.info("commitClose");
		commitClose();
	}
	
	protected void creaDatasetCsv() throws Exception {
		
		log.info("begin");
		begin();
		
		Query queryHeader = em.createNativeQuery("SELECT column_name FROM USER_TAB_COLUMNS WHERE table_name='DAILY_STOCK_VIEW'");
		Object[] resultRecord = queryHeader.getResultList().toArray();
		String[] headerRecord = new String[resultRecord.length];
		for(int i=0; i<resultRecord.length; i++) headerRecord[i] = (String)resultRecord[i];
		
		Query query = em.createNativeQuery("SELECT * FROM DAILY_STOCK_VIEW");
		List<Object[]> result = query.getResultList();
		List<String> list = new ArrayList<String>();
		 
		for(Object[] obj:result) {
			String str = String.valueOf(obj[0])  + "," + String.valueOf(obj[1]).trim()
					 	 + "," + obj[2]  + "," + obj[3]  + "," + obj[4]  + "," + obj[5]  + "," + obj[6]
					 	 + "," + obj[7]  + "," + obj[8]  + "," + obj[9]  + "," + obj[10] + "," + obj[11]
					 	 + "," + obj[12] + "," + obj[13] + "," + obj[14] + "," + obj[15] + "," + obj[16]				
					 	 + "," + obj[17] + "," + obj[18] + "," + obj[19] + "," + obj[20] + "," + obj[21]
					 	 + "," + obj[22]// + "," + obj[23] + "," + obj[24] + "," + obj[25] + "," + obj[26]
//					 	 + "," + obj[27] + "," + obj[28] + "," + obj[29] + "," + obj[30] + "," + obj[31]
//					 	 + "," + obj[32] + "," + obj[33] + "," + obj[34] + "," + obj[35] + "," + obj[36]
								 ;
			list.add(str);
		}
		
		String csvFile = Costants.dataSetDirConvLstm + "prices-split-adjusted_tmp.csv";
//		String csvFile = "src/main/resources/analize_data/analyze-prices-split-adjusted.csv";
		writeCsv(csvFile, list, headerRecord);
		log.info("createDataSetCsv {} ", csvFile);
		
		log.info("commitClose");
		commitClose();
	}
	
protected void creaAnalyzeDatasetCsv() throws Exception {
		
		log.info("begin");
		begin();
		
		Query queryHeader = em.createNativeQuery("SELECT column_name FROM USER_TAB_COLUMNS WHERE table_name='ANALIZE_VIEW'");
		Object[] resultRecord = queryHeader.getResultList().toArray();
		String[] headerRecord = new String[resultRecord.length];
		for(int i=0; i<resultRecord.length; i++) headerRecord[i] = (String)resultRecord[i];
		
		Query query = em.createNativeQuery("SELECT * FROM ANALIZE_VIEW WHERE INTERVAL_MIN=2");
		List<Object[]> result = query.getResultList();
		List<String> list = new ArrayList<String>();
		
		for(Object[] obj:result) {
			String str = (String)obj[0]  + "," + obj[1] 
					 	 + "," + obj[2]  + "," + obj[3]  + "," + obj[4]  + "," + obj[5]  + "," + obj[6]
					 	 + "," + obj[7]  + "," + obj[8]  + "," + obj[9]  + "," + obj[10] + "," + obj[11]
//					 	 + "," + obj[12] + "," + obj[13] + "," + obj[14] + "," + obj[15]// + "," + obj[16]				
//					 	 + "," + obj[17] + "," + obj[18] + "," + obj[19] + "," + obj[20] + "," + obj[21]
//					 	 + "," + obj[22] + "," + obj[23] + "," + obj[24] + "," + obj[25] + "," + obj[26]
//					 	 + "," + obj[27] + "," + obj[28] + "," + obj[29] + "," + obj[30] + "," + obj[31]
//					 	 + "," + obj[32] + "," + obj[33] + "," + obj[34] + "," + obj[35] + "," + obj[36]
								 ;
			list.add(str);
		}
		
		String csvFile = "src/main/resources/analize_data/analyze-prices-split-adjusted.csv";
		writeCsv(csvFile, list, headerRecord);
		log.info("creaAnalyzeDatasetCsv {} ", csvFile);
		
		log.info("commitClose");
		commitClose();
	}
	
	protected void creaDailyStockDataSet(String[] symbols, double arrotondamentoVolume, int categoria, String alphavantageUrl, int intervalMin) throws Exception {
		log.info("creaConvLstmDataSet...");
		StockPricePredictorContinuousBase util = new StockPricePredictorContinuousBase();
		
		log.info("begin");
		util.begin();
		
		for(String symbol: symbols) {
			//controlla se non sia superato il limite di 5 richieste al minuto
			util.checkIntervallo(count);

			//scarica il csv
			util.downLoadCsv(symbol, alphavantageUrl);

			//inserisce nella tabella
			/* controllare se necessario arrotondamento volume, es: /10000
				- 1	Titoli azionari
				- 2	Indici di borsa
				- 3	ETF
				- 4	Valute 
				- 5 Materie prime */
			util.insertStockData(symbol, arrotondamentoVolume, categoria, intervalMin);
		}
		
		log.info("commitClose");
		util.commitClose();
	}
	
	protected void creaForexDataSet(String from_symbol, String to_symbol, String alphavantageUrl, int intervalMin) throws Exception {
		log.info("creaConvLstmDataSet...");
		StockPricePredictorContinuousBase util = new StockPricePredictorContinuousBase();
		
		log.info("begin");
		util.begin();
		
			//controlla se non sia superato il limite di 5 richieste al minuto
			util.checkIntervallo(count);

			//scarica il csv - fx_daily_GBP_EUR
			util.downLoadCsvForex(from_symbol, to_symbol, alphavantageUrl);

			//inserisce nella tabella
			/* controllare se necessario arrotondamento volume, es: /10000
				- 1	Titoli azionari
				- 2	Indici di borsa
				- 3	ETF
				- 4	Valute 
				- 5 Materie prime */
			util.insertForexData(from_symbol, to_symbol, intervalMin);
		
		log.info("commitClose");
		util.commitClose();
	}
	
	/**
	 * Il Moving Average Convergence/Divergence (MACD, ossia convergenza e divergenza di medie mobili).
	 * Fu sviluppato da Gerald Appel e basa la sua costruzione su medie mobili esponenziali.
	 * 
	 * @param symbols
	 * @throws Exception
	 */
	protected void creaTecInd_III_IndDataSet(String[] symbols, String indType, String alphavantageTecIndUrl, int intervalMin) throws Exception {
		log.info("creaTecInd_III_IndDataSet...");
		
		log.info("begin");
		begin();

		for(String symbol: symbols) {
			//controlla se non sia superato il limite di 5 richieste al minuto
			checkIntervallo(count);

			//scaricare il csv
			downLoadCsv(symbol, alphavantageTecIndUrl);

			//inserire nella tabella
			insertTecInd_III_Ind(symbol, indType, intervalMin);
		}
		
		log.info("commitClose");
		commitClose();
	}
	
	protected void creaTecInd_II_IndDataSet(String[] symbols, String indType, String alphavantageTecIndUrl, int intervalMin) throws Exception {
		log.info("creaTecInd_II_IndDataSet...");
		
		log.info("begin");
		begin();

		for(String symbol: symbols) {
			//controlla se non sia superato il limite di 5 richieste al minuto
			checkIntervallo(count);

			//scaricare il csv
			downLoadCsv(symbol, alphavantageTecIndUrl);

			//inserire nella tabella
			insertTecInd_II_Ind(symbol, indType, intervalMin);
		}
		
		log.info("commitClose");
		commitClose();
	}
	
	protected void creaTecIndDataSet(String[] symbols, String indType, String alphavantageTecIndUrl, int intervalMin) throws Exception {
		log.info("creaTecIndDataSet...");
		
		log.info("begin");
		begin();

		for(String symbol: symbols) {
			//controlla se non sia superato il limite di 5 richieste al minuto
			checkIntervallo(count);

			//scaricare il csv
			downLoadCsv(symbol, alphavantageTecIndUrl);

			//inserire nella tabella
			insertTecInd(symbol, indType, intervalMin);
		}
		
		log.info("commitClose");
		commitClose();
	}
	
	protected void checkIntervallo(int count) throws Exception {
		long t = System.currentTimeMillis();
		log.info("count {} t {}", count, (t - intervallo)/1000);
		if(count > 4) {
			while(t - intervallo < 1000 * 70) {
				long sleep = (1000 * 70) - (t - intervallo);
				log.info("Thread.sleep( {}s ) - t-intervallo {}s", sleep/1000, (t - intervallo)/1000);
				Thread.sleep(sleep);
				t = System.currentTimeMillis();
			}
			this.intervallo = t;
			this.count = 1;
		}else {
			this.count++;
		}
	}
	
	protected void runEvaluation(MultiLayerNetwork net, int curEpoch, double score, File locationToSaveBest) {
		Thread thread = new Thread(){
			public void run(){
				//salva best model
				saveBestModel(net, curEpoch, score, locationToSaveBest);
				//stampa score
				printScore(net, curEpoch, score);
			}
		};
		thread.start();
	}
	
	protected void saveBestModel(MultiLayerNetwork bestModel, int curEpoch, double score, File locationToSaveBest) {
		if(score < bestScore && curEpoch > 100 || curEpoch == epochs-1) {
			log.info("Saving best model...");
			try {
				if(locationToSaveBest.exists()) locationToSaveBest.delete();
				ModelSerializer.writeModel(bestModel, locationToSaveBest, true);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	protected void printScore(MultiLayerNetwork net, int curEpoch, double score) {
		if(score < bestScore) {
			log.info("New Best Epoch " + curEpoch + " - score " + f.format(score) + " | Old Epoch " + bestEpoch + " - score " +  f.format(bestScore) + " | " + sdf.format(new Date((System.currentTimeMillis() - inizio) - (1000*60*60))));
			bestEpoch = curEpoch;
			bestScore = score;
		}else {
			log.info("Epoch " + curEpoch + " - score " + f.format(score) + " | Best Epoch " + bestEpoch + " - score " +  f.format(bestScore) + " | " + sdf.format(new Date((System.currentTimeMillis() - inizio) - (1000*60*60))));
		}
	}
	
	//scaricare il csv
	public void downLoadCsv(String symbol, String alphavantageUrl) throws Exception {
		log.info("downLoadCsv {}...", symbol);
		
		// And as before now you can use URL and URLConnection
		log.info(alphavantageUrl + symbol);
//		System.setProperty("http.proxyHost", "10.18.101.7");
//		System.setProperty("http.proxyPort", "80");
		URLConnection connection = new URL(alphavantageUrl + symbol).openConnection();
		InputStream initialStream = connection.getInputStream();

		File targetFile = new File(dataSetDir + symbol + ".csv");
//		if(targetFile.exists())targetFile.delete();
		OutputStream outStream = new FileOutputStream(targetFile);

		byte[] buffer = new byte[8 * 1024];
		int bytesRead = 0;
		while ((bytesRead = initialStream.read(buffer)) != -1)  outStream.write(buffer, 0, bytesRead);

		initialStream.close();
		outStream.close();
	}
	
	//scaricare il csv forex
		public void downLoadCsvForex(String from_symbol, String to_symbol, String alphavantageUrl) throws Exception {
			log.info("downLoadCsv {}_{}...", from_symbol, to_symbol);
			
			// And as before now you can use URL and URLConnection
			log.info(alphavantageUrl);
//			System.setProperty("http.proxyHost", "10.18.101.7");
//			System.setProperty("http.proxyPort", "80");
			URLConnection connection = new URL(alphavantageUrl).openConnection();
			InputStream initialStream = connection.getInputStream();

			File targetFile = new File(dataSetForexDir + from_symbol + "_" + to_symbol + ".csv");
//			if(targetFile.exists())targetFile.delete();
			OutputStream outStream = new FileOutputStream(targetFile);

			byte[] buffer = new byte[8 * 1024];
			int bytesRead = 0;
			while ((bytesRead = initialStream.read(buffer)) != -1)  outStream.write(buffer, 0, bytesRead);

			initialStream.close();
			outStream.close();
		}
	
	//inserire nella tabella x predizione
	public void insertStockData(String symbol, double arrotondamentoVolume, int categoria, int intervalMin) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
		
		em.createNativeQuery( Costants.ALTER_SESSION_NLS_DATE_FORMAT ).executeUpdate();
		
		@SuppressWarnings("resource")
		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
		DailyStock dailyStock = null;
		for(int i=0; i<list.size(); i++) {
			String[] row = list.get(i);
				dailyStock = new DailyStock(Costants.DATE_FORMAT.parse(row[0]),row[0], symbol, Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), Double.parseDouble(row[5])/arrotondamentoVolume, categoria, intervalMin);
				em.persist(dailyStock);
		}
		em.flush();
		log.info("insert insertStockData {}", symbol);
	}
	
	//inserire nella tabella x predizione
	public void insertForexData(String from_symbol, String to_symbol, int intervalMin) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
		
		em.createNativeQuery( Costants.ALTER_SESSION_NLS_DATE_FORMAT ).executeUpdate();
		
		@SuppressWarnings("resource")
		List<String[]> list = new CSVReader(new FileReader(dataSetForexDir + from_symbol + "_" + to_symbol + ".csv"),',','"',1).readAll();
		Forex forex = null;
		for(int i=0; i<list.size(); i++) {
			String[] row = list.get(i);
//			 Forex(Date data, String timestamp, String from_symbol, String to_symbol, double open, double close,
//						double low, double high, int intervalMin)
			forex = new Forex(Costants.DATE_FORMAT.parse(row[0]),row[0], from_symbol, to_symbol, Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), intervalMin);
				em.persist(forex);
		}
		em.flush();
		log.info("insert insertForexData {}", from_symbol + "_" + to_symbol);
	}
	
	public void insertTecInd_III_Ind(String symbol, String indType, int intervalMin) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
		
		em.createNativeQuery( Costants.ALTER_SESSION_NLS_DATE_FORMAT ).executeUpdate();
		
		@SuppressWarnings("resource")
		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
		for(int i=0; i<list.size(); i++) {
			String[] row = list.get(i);
			em.persist(new TecInd_III(Costants.DATE_FORMAT.parse(row[0]),row[0], symbol, indType, Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), intervalMin));
		}
		em.flush();
		log.info("insert insertTecInd_III_Ind {} {}", indType, symbol);
	}
	
	
	public void insertTecInd_II_Ind(String symbol, String indType, int intervalMin) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
		@SuppressWarnings("resource")
		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
		for(int i=0; i<list.size(); i++) {
			String[] row = list.get(i);
			em.persist(new TecInd_II(Costants.DATE_FORMAT_IND.parse(row[0]),row[0], symbol, indType, Double.parseDouble(row[1]), Double.parseDouble(row[2]), intervalMin));
		}
		em.flush();
		log.info("insert insertTecInd_II_Ind {} {}", indType, symbol);
	}
	
	public void insertTecInd(String symbol, String indType, int intervalMin) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
		
		em.createNativeQuery( Costants.ALTER_SESSION_NLS_DATE_FORMAT ).executeUpdate();
		
		@SuppressWarnings("resource")
		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
		for(int i=0; i<list.size(); i++) {
			String[] row = list.get(i);
			em.persist(new TecInd(Costants.DATE_FORMAT.parse(row[0]),row[0], symbol, indType, Double.parseDouble(row[1]), intervalMin));
		}
		em.flush();
		log.info("insert insertTecInd {} {}", indType, symbol);
	}
	
//	public void insertStockDataFit(String symbol) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
//		@SuppressWarnings("resource")
//		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
//		
//		for(String[] row: list) em.persist(new DailyStock(Costants.DATE_FORMAT.parse(row[0]),row[0], symbol, Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), Double.parseDouble(row[5]), 1));
//		em.flush();
//		log.info("insert insertStockDataGpu {}", symbol);
//	}
	
	//inserire nella tabella x addestramento
//	public void insertStockDataTraining(String symbol) throws FileNotFoundException, IOException, NumberFormatException, ParseException {
//		@SuppressWarnings("resource")
//		List<String[]> list = new CSVReader(new FileReader(dataSetDir + symbol + ".csv"),',','"',1).readAll();
//		
//		for(String[] row: list) em.persist(new DailyStock(Costants.DATE_FORMAT.parse(row[0]),row[0], symbol, Double.parseDouble(row[1]), Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), Double.parseDouble(row[5]), 1));
//		em.flush();
//		log.info("insert insertStockData {}", symbol);
//	}
	
	//creare il dataset csv con gli ultimi 141 valori
	public void createDataSetCsv(String csvFile) throws FileNotFoundException, IOException {
//		TypedQuery<StockDataGpu> query = em.createNamedQuery(StockDataGpu.FIND_ALL_NO_ZERO, StockDataGpu.class);
		TypedQuery<DailyStock> query = em.createNamedQuery(DailyStock.FIND_ALL, DailyStock.class);
		String[] headerRecord = {"date", "symbol", "open", "close","low","high","volume"};
		writeCsv(csvFile, query.getResultList(), headerRecord);
		log.info("createDataSetCsv {} ", csvFile);
	}
	
	
	protected void begin(){
		emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}
	
	protected void commitClose(){
		em.getTransaction().commit();
		em.close();
		emf.close();
		
		//clean hsqldb
		cleanHsqldb();
	}
	
	
	public void cleanHsqldb() {
		File dataSetDirScript = new File("src/main/resources/data/fin_predictor.script");
		if(dataSetDirScript.exists())dataSetDirScript.delete();
		File dataSetDirLog = new File("src/main/resources/data/fin_predictor.log");
		if(dataSetDirLog.exists())dataSetDirLog.delete();
		File dataSetDiroProperties = new File("src/main/resources/data/fin_predictor.properties");
		if(dataSetDiroProperties.exists())dataSetDiroProperties.delete();
	}
	
	public void cleanPrevisioni() {
		File previsioni = new File("src/main/resources/dataset_continuous/previsioni.csv");
		if(previsioni.exists())previsioni.delete();
		File adjusted = new File("src/main/resources/dataset_continuous/prices-split-adjusted.csv");
		if(adjusted.exists())adjusted.delete();
	}
	
	protected <T> void writeCsv(String csvFile, List<T> dataSet, String[] headerRecord) throws IOException {
		try (
				Writer writer = Files.newBufferedWriter(Paths.get(csvFile));
				CSVWriter csvWriter = new CSVWriter(writer,
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);
				) {
			//date,symbol,open,close,low,high,volume
			csvWriter.writeNext(headerRecord);

			for(T sd: dataSet) { csvWriter.writeNext(sd.toString().split(",")); }
		}
	}
	
	public void insertDay(int yyyyFrom, int mmFrom, int ddFrom, int yyyyTo, int mmTo, int ddTo, int intervalMin) {
		log.info("insertDays da {}-{}-{} a {}-{}-{}, intervalMin: {}",  yyyyFrom,  mmFrom,  ddFrom,  yyyyTo,  mmTo,  ddTo, intervalMin);
		
		log.info("begin");
		begin();
		
		em.createNativeQuery( Costants.ALTER_SESSION_NLS_DATE_FORMAT ).executeUpdate();//DD-MON-RRRR HH24:MI
		
		
		//2018-11-26, 2019-01-19
		GregorianCalendar dataFrom = new GregorianCalendar(yyyyFrom,mmFrom,ddFrom-1);
		GregorianCalendar dataTo = new GregorianCalendar(yyyyTo,mmTo,ddTo);
		
		while(dataFrom.getTimeInMillis() < dataTo.getTimeInMillis()) {
			dataFrom.set(Calendar.HOUR_OF_DAY, 00);
			dataFrom.set(Calendar.MINUTE, 00);
			dataFrom.set(Calendar.SECOND, 0);
			
			dataFrom.add(Calendar.DAY_OF_MONTH, 1);
			if(dataFrom.get(Calendar.DAY_OF_WEEK) == 1 || dataFrom.get(Calendar.DAY_OF_WEEK) == 7) continue;
			
			/* 60 minuti */
//			while(dataFrom.get(Calendar.HOUR_OF_DAY) < 11 ) {
//				dataFrom.add(Calendar.MINUTE, intervalMin);
//				DataTmp t = new DataTmp(dataFrom.getTime(), dataFrom.get(Calendar.DAY_OF_WEEK) - 1, 5);
//				em.persist(t);
//				System.out.println(t);
//			}
			
			/* 5 minuti */
//			while((100*dataFrom.get(Calendar.HOUR_OF_DAY))+dataFrom.get(Calendar.MINUTE) < 1125 ) {
//				dataFrom.add(Calendar.MINUTE, intervalMin);
//				DataTmp t = new DataTmp(dataFrom.getTime(), dataFrom.get(Calendar.DAY_OF_WEEK) - 1, intervalMin);
//				em.persist(t);
//				System.out.println(t);
//			}
			
			/* 1 giorno */
			DataTmp t = new DataTmp(dataFrom.getTime(), dataFrom.get(Calendar.DAY_OF_WEEK) - 1, intervalMin);
			em.persist(t);
			System.out.println(t);
			em.flush();
		}
		
		log.info("commitClose");
		commitClose();
	}

}
