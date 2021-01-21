package org.predictor.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Costants {
	
	/*configurazione ideale
	  epochs=200
	  learningRate=0.0005
	  lrSchedule=5000
	  miniBatchSize=164
	  timesteps=140 
	  truncatedBPTT=140
	  lstmLayerSize=46  
	  denseLayerSize=22
	  l2=1e-4
	  dropoutRatio=0.2
	  versione dl4j = 0.9.1; 
	  
	  Epoch 100 - score 0,00338413 | Old Epoch 98   - score 0,00356151 | 00h 06m 49s
	  Epoch 199 - score 0,00204169 | Best Epoch 178 - score 0,00200592 | 00h 13m 35s
	*/
	
	public static final int epochs = 200;
	public static final double learningRate = 0.0005;
	public static final int miniBatchSize=164,  timesteps=140, truncatedBPTT=140, lstmLayerSize=46,  denseLayerSize=22;
	public static final int lrSchedule = 5000;
	public static final int predictionLength = 9;
	public static final int VECTOR_SIZE = 6;
	public static final int outputLength = 3;
	public static final double l2 = 1e-4;
	public static final double l1 = 1e-4;
	
	public static final double rmsDecay = 0.001;
	public static final double splitRatio = 0.9;
	public static final int averagingFrequency = 11;
	public static final int examplesPerDataSetObject = 1;
	public static final int dataSetSize = 0;
	
//	public static final String versione = "1.0.0-beta2"; 
	public static final String versione = "0.9.1"; 
	public static final String dataSetDir = "src/main/resources/dataset/";
	public static final String statisticDir = "src/main/resources/statistic/";
	public static final String locationToSaveModelPath 	   = "src/main/resources/model/StockPricePredictorLSTM.zip";
	public static final String locationToSaveBestModelPath = "src/main/resources/model/StockPriceBestPredictor12LSTM.zip"; // -BEST-
	
	public static final String ftse = dataSetDir + "daily_^FTSE.csv";
	public static final String nikkei = dataSetDir + "daily_^N225.csv";
	public static final String dow = dataSetDir + "daily_DJI.csv";
	public static final String nsdq = dataSetDir + "daily_IXIC.csv";
	
	public static final String sam = dataSetDir + "daily_005930.KS.csv";
	public static final String apple = dataSetDir + "daily_AAPL.csv";
	public static final String msft = dataSetDir + "daily_MSFT.csv";
	
	public static final String samBands = dataSetDir + "technical_indicator_BBANDS_005930.KS.csv";
	public static final String appleBands = dataSetDir + "technical_indicator_BBANDS_AAPL.csv";
	public static final String msftBBands = dataSetDir + "technical_indicator_BBANDS_MSFT.csv";
	
	public static final String samEma = dataSetDir + "technical_indicator_EMA_005930.KS.csv";
	public static final String appleEma = dataSetDir + "technical_indicator_EMA_AAPL.csv";
	public static final String msftEma = dataSetDir + "technical_indicator_EMA_MSFT.csv";
	
	public static final String samSma = dataSetDir + "technical_indicator_SMA_005930.KS.csv";
	public static final String appleSma = dataSetDir + "technical_indicator_SMA_AAPL.csv";
	public static final String msftSma = dataSetDir + "technical_indicator_SMA_MSFT.csv";
	
	public static final String jsonFileMSFT = dataSetDir + "data_set_MSFT.json";
	public static final String jsonFileAAPL = dataSetDir + "data_set_AAPL.json";
	
	public static final String jsonFileMINMAXMSFT = dataSetDir + "data_set_MINMAX_MSFT.json";
	public static final String jsonFileMINMAXAAPL = dataSetDir + "data_set_MINMAX_AAPL.json";
	
	public static final String JPA_SERVICE = "JPAService";
	public static final String JPA_SERVICE_ORA = "JPAService_ORA";
	public static final String JPA_SERVICE_ORA_SFERA = "JPAService_ORA_SFERA";
	public static final String JPA_SERVICE_ORA_XE = "JPAService_ORA_XE";
	
	public static final String alphavantageUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&outputsize=full&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageEMAUrl = "https://www.alphavantage.co/query?function=EMA&interval=daily&time_period=60&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageBBANDSUrl = "https://www.alphavantage.co/query?function=BBANDS&interval=daily&time_period=60&series_type=close&nbdevup=3&nbdevdn=3&datatype=csv&apikey=PRIVATE_KEY&symbol=";
	
	public static final String alphavantageForex_GBP_EUR_Url = "https://www.alphavantage.co/query?function=FX_DAILY&outputsize=full&apikey=PRIVATE_KEY&datatype=csv&from_symbol=GBP&to_symbol=EUR";
	public static final String alphavantageForex_GBP_USD_Url = "https://www.alphavantage.co/query?function=FX_DAILY&outputsize=full&apikey=PRIVATE_KEY&datatype=csv&from_symbol=GBP&to_symbol=USD";
	
	public static final String alphavantageMACDUrl = "https://www.alphavantage.co/query?function=MACD&interval=60min&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	
	public static final String alphavantageSMAUrl = "https://www.alphavantage.co/query?function=SMA&interval=60min&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageRSIUrl = "https://www.alphavantage.co/query?function=RSI&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageADXUrl = "https://www.alphavantage.co/query?function=ADX&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageCCIUrl = "https://www.alphavantage.co/query?function=CCI&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageOBVUrl = "https://www.alphavantage.co/query?function=OBV&interval=daily&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageWMAUrl = "https://www.alphavantage.co/query?function=WMA&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageDEMAUrl = "https://www.alphavantage.co/query?function=DEMA&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageTEMAUrl = "https://www.alphavantage.co/query?function=TEMA&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageTRIMAUrl = "https://www.alphavantage.co/query?function=TRIMA&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageKAMAUrl = "https://www.alphavantage.co/query?function=KAMA&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageT3Url = "https://www.alphavantage.co/query?function=T3&interval=daily&time_period=10&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageMACDEXTUrl = "https://www.alphavantage.co/query?function=MACDEXT&interval=daily&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageSTOCHFUrl = "https://www.alphavantage.co/query?function=STOCHF&interval=daily&series_type=close&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageMAMAUrl = "https://www.alphavantage.co/query?function=MAMA&interval=daily&series_type=close&fastlimit=0.02&datatype=csv&apikey=PRIVATE_KEY&symbol=";
	public static final String alphavantageSTOCHUrl = "https://www.alphavantage.co/query?function=STOCH&interval=daily&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String alphavantageAROONUrl = "https://www.alphavantage.co/query?function=AROON&interval=daily&time_period=14&apikey=PRIVATE_KEY&datatype=csv&symbol=";
	public static final String dataSetDirContinuous = "src/main/resources/dataset_continuous/";
	public static final String dataSetDirConvLstm = "src/main/resources/dataset_convLstm/";
	public static final String split_adjusted_continuous = dataSetDirContinuous + "prices-split-adjusted.csv";
	public static final String locationToSaveContinuousBestModelPath = "src/main/resources/model_continuous/StockPriceBestPredictorLSTM_"; // -BEST-
	
	public static final String ALTER_SESSION_NLS_DATE_FORMAT = "alter session set nls_date_format='DD-MON-RRRR HH24:MI'";//'DD-MON-RRRR HH24:MI'
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATE_FORMAT_IND = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("####0.000",new DecimalFormatSymbols(Locale.ENGLISH));
	public static final DecimalFormat DECIMAL_FORMAT_00 = new DecimalFormat("####0.00",new DecimalFormatSymbols(Locale.ENGLISH));
}
