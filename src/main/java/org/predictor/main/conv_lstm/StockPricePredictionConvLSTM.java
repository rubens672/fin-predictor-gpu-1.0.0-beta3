package org.predictor.main.conv_lstm;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.lossfunctions.impl.LossMSE;
import org.predictor.model.RecurrentNets;
import org.predictor.utils.Costants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StockPricePredictionConvLSTM {

	private static final Logger log = LoggerFactory.getLogger(StockPricePredictionConvLSTM.class);

	private DecimalFormatSymbols decimalFormatSymbols_Locale_US =   new DecimalFormatSymbols(Locale.US);
	private final DecimalFormat f   = new DecimalFormat("####0.000000", decimalFormatSymbols_Locale_US);
	private final DecimalFormat fr  = new DecimalFormat("####0.0000",   decimalFormatSymbols_Locale_US);
	private final DecimalFormat frp = new DecimalFormat("####0.00",     decimalFormatSymbols_Locale_US);
	
	private int 	V_HEIGHT = 2;			//7; //in questo caso il numero dei titoli x il numero dei valori (4 -> o,c,h,l)
	private int 	V_WIDTH = 4;

	private int 	epochs 		  = 10;
	private double 	learningRate  = 0.005;
	private int 	timesteps 	  = 102;
	private int 	numStep 	  = 11;
	private int 	miniBatchSize = 103;//(10 * numStep) - (numStep-1);
	private int 	VECTOR_SIZE   = 13;
	private int 	outputLength  = 1;

	private String locationToSaveModelPath = "src/main/resources/model_convLstm/StockPricePredictorConvLSTM.zip";
	private String datasetConvLstm 		   = "dataset_convLstm/prices-split-adjusted.csv";

	public void execute() throws Exception {
		String file = new ClassPathResource(datasetConvLstm).getFile().getAbsolutePath();
//		String[] symbols = {"G.MI","CASS.MI","CE.MI","ISP.MI","MB.MI","BMPS.MI"};//"BMPS.MI",
//		String[] symbols = {"BMPS.MI","G.MI","CASS.MI","CE.MI","ISP.MI","MB.MI","FBK.MI","UBI.MI","BMED.MI","BAMI.MI","IF.MI","BPSO.MI","CVAL.MI","AZM.MI","PRO.MI"};
//		String[] symbols = {"BMPS.MI","G.MI","CASS.MI","ISP.MI","MB.MI","FBK.MI","UBI.MI","BMED.MI","BAMI.MI","CVAL.MI","AZM.MI"}; 
		String[] symbols = {"US.MI"};
		String evalSymbol = "US.MI";

		int numIter = symbols.length;
		int count = 1;

		List<StockDataSetConvLSTMIterator> iterators = new ArrayList<StockDataSetConvLSTMIterator>();
		for(String symbol: symbols) {
			log.info("Create {} train iterator...", symbol);
			StockDataSetConvLSTMIterator iterator  = new StockDataSetConvLSTMIterator(count, file, symbol, miniBatchSize, VECTOR_SIZE, timesteps, outputLength, false);

			log.info("NormalizerStandardize {} training data...", symbol);
			NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
			normalizer.fitLabel(true);
			normalizer.fit(iterator);
			iterator.reset();
			iterator.setPreProcessor(normalizer);

			iterators.add(iterator);
			count++;
		}


		log.info("Create {} tests iterator...", evalSymbol);
		StockDataSetConvLSTMIterator tests  = new StockDataSetConvLSTMIterator(1, file, evalSymbol, miniBatchSize, VECTOR_SIZE, timesteps, outputLength, false);

		NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
		normalizer.fitLabel(true);
		normalizer.fit(tests);
		tests.reset();
		tests.setPreProcessor(normalizer);

		log.info("build model");
		MultiLayerNetwork net = new RecurrentNets().buildLstmNetworks(VECTOR_SIZE, outputLength, learningRate, 0, 0, 0, timesteps);


		INDArray weightsArray = Nd4j.create(new double[]{1.0, 0.66, 0.66, 0.66});
		LossMSE lossMSE = new LossMSE(weightsArray);

		log.info("Training...");
		for (int curEpoch = 0; curEpoch < epochs; curEpoch++) {
			System.out.println();
			System.out.print("[INFO] 2019-01-20 11:14:07,128 StockPricePredictionConvLSTM - ----------------------|    Epoch " + curEpoch + "    |");
			for(StockDataSetConvLSTMIterator trainData: iterators) {
				System.out.print("--");
				while(trainData.hasNext()) {
					DataSet dataSet = trainData.next(); 
					net.fit(dataSet);
				}
				net.rnnClearPreviousState();
				trainData.reset();
			}
			
			System.out.println();
			RegressionEvaluation evaluation = net.evaluateRegression(tests);
			evaluation.setColumnNames(tests.getHeaderRecord().subList(0, outputLength));
			String meanSquaredError     = f.format(evaluation.meanSquaredError(0));
			String meanAbsoluteError    = f.format(evaluation.meanAbsoluteError(0));
			String rootMeanSquaredError = f.format(evaluation.rootMeanSquaredError(0));
			String relativeSquaredError = f.format(evaluation.relativeSquaredError(0));
			String correlationR2 		= f.format(evaluation.correlationR2(0));
			tests.reset();
			
			log.info("score       MSE         MAE         RMSE        RSE         R^2");
			log.info("{}    {}    {}    {}    {}    {}",
					f.format(net.score()),
					meanSquaredError,
					meanAbsoluteError,
					rootMeanSquaredError,
					relativeSquaredError,
					correlationR2);
		}


		log.info("Saving model...");
		File f = new File(locationToSaveModelPath);
		if(f.exists())f.delete();
		ModelSerializer.writeModel(net, locationToSaveModelPath, false);

		log.info("Evaluate Plot model...");
		evaluatePlot(evalSymbol, false);

		log.info("done...");
	}


	public void evaluatePlot(String evalSymbol, boolean singoloBatch) throws Exception {
		if(singoloBatch) {
			log.info("evaluateSingoloBatch...");
			evaluateSingoloBatch(evalSymbol);
		}else {
			log.info("evaluateMultiBatch...");
			evaluateMultiBatch(evalSymbol);
		}
	}


	public void evaluateMultiBatch(String evalSymbol) throws Exception {
		log.info("Load model...");
		MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(locationToSaveModelPath);

		int timesteps = 102;
		int numStep = 6;
//		int miniBatchSize = (10 * numStep) - (numStep-1);
		int miniBatchSize = 103;
		frp.setPositivePrefix("+");

		String file = new ClassPathResource(datasetConvLstm).getFile().getAbsolutePath();
		StockDataSetConvLSTMIterator tests  = new StockDataSetConvLSTMIterator(1, file, "US.MI", miniBatchSize, VECTOR_SIZE, timesteps, outputLength,false);
		StockDataSetConvLSTMIterator testsControllo  = new StockDataSetConvLSTMIterator(1, file, "US.MI", miniBatchSize, VECTOR_SIZE, timesteps, outputLength,true);
		
		
		log.info("NormalizerMinMaxScaler {} training data...", evalSymbol);
		NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
		normalizer.fitLabel(true);
		normalizer.fit(tests);
		tests.setPreProcessor(normalizer);
		
		int totalMiniBatch = tests.totalMiniBatch();
		

		int predictLength = Math.min(40, totalMiniBatch-1);
		int miniBatchTest = Math.min(140, tests.totalExamples());
		
		log.info("miniBatchSize  {}", miniBatchSize);
		log.info("totalMiniBatch {}", totalMiniBatch);
		log.info("predictLength  {}", predictLength);
		log.info("miniBatchTest  {}", miniBatchTest);
		
		double[] predictsCLOSE = new double[predictLength];
		double[] actualsCLOSE  = new double[predictLength];
		

		
		for(int i=0; i<predictLength; i++) {
			
			/* minibatch */
			DataSet next  = tests.next();
			INDArray features = next.getFeatures();
			INDArray labelsAct = next.getLabels();
			
			DataSet nextControllo = testsControllo.next();
			INDArray featuresControllo = nextControllo.getFeatures();
			INDArray labelsActControllo = nextControllo.getLabels();
			
			Long rowslong = features.shape()[0];
			int rows = rowslong.intValue();
			
			/* rnnTimeStep */
			INDArray predict = net.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.interval(0,VECTOR_SIZE), NDArrayIndex.all()));
			
			normalizer.revertLabels(predict);
			normalizer.revertLabels(labelsAct);

			predictsCLOSE[i] =   predict.getDouble(rows-1, 0, timesteps - 1) * 10;
			actualsCLOSE [i] = labelsAct.getDouble(rows-1, 0, timesteps - 1) * 10;
			
			
			Integer dataI   = featuresControllo.getInt(rows-1, VECTOR_SIZE + 0, timesteps - 1);
			Integer oraMinI = featuresControllo.getInt(rows-1, VECTOR_SIZE + 1, timesteps - 1);
			Integer dayI    = featuresControllo.getInt(rows-1, VECTOR_SIZE + 2, timesteps - 1);
			String start = sdf(dataI) + " " + sdfHHMM(oraMinI) + " " + dayI;
			
			Integer dataL   = labelsActControllo.getInt(rows-1, outputLength + 0, timesteps - 1);
			Integer oraMinL = labelsActControllo.getInt(rows-1, outputLength + 1, timesteps - 1);
			Integer dayL    = labelsActControllo.getInt(rows-1, outputLength + 2, timesteps - 1);
			String startL = sdf(dataL) + " " + sdfHHMM(oraMinL) + " " + dayL;
			

			double percAct  = i == 0 ? 0 : ( actualsCLOSE[i] -  actualsCLOSE[i-1]) /  actualsCLOSE[i-1] * 100;
			double percPred = i == 0 ? 0 : (predictsCLOSE[i] - predictsCLOSE[i-1]) / predictsCLOSE[i-1] * 100;

			log.info("{}{} | {} -> {}  actuals {} {}% - predicts {} {}%", i<10?" ":"", i, start, startL, fr.format(actualsCLOSE[i]), frp.format(percAct), fr.format(predictsCLOSE[i]), frp.format(percPred) );

			net.rnnClearPreviousState();
		}
		
		log.info("Plot...");
		plot(predictsCLOSE, actualsCLOSE, "CLOSE", 1);
	}

	@SuppressWarnings("unused")
	public void evaluateSingoloBatch(String evalSymbol) throws Exception {
		log.info("Load model...");
		MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(locationToSaveModelPath);

		int timesteps = 9 * 9;
		int numStep = 22;
		int miniBatchSize = 102;
		frp.setPositivePrefix("+");

		String file = new ClassPathResource(datasetConvLstm).getFile().getAbsolutePath();
		StockDataSetConvLSTMIterator tests  = new StockDataSetConvLSTMIterator(1, file, evalSymbol, miniBatchSize, VECTOR_SIZE, timesteps, outputLength,true);
		
		log.info("NormalizerMinMaxScaler {} training data...", evalSymbol);
		NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
		normalizer.fitLabel(true);
		normalizer.fit(tests);
		tests.setPreProcessor(normalizer);

		int totalMiniBatch = tests.totalMiniBatch();
		int predictLength = Math.min(40, totalMiniBatch-1);
		int miniBatchTest = Math.min(140, tests.totalExamples());
		
		log.info("miniBatchSize  {}", miniBatchSize);
		log.info("totalMiniBatch {}", totalMiniBatch);
		log.info("predictLength  {}", predictLength);
		log.info("miniBatchTest  {}", miniBatchTest);
		
		double[] predictsCLOSE = new double[predictLength];
		double[] actualsCLOSE  = new double[predictLength];
		

		for(int j=0; j<predictLength; j++) {
			DataSet next  = tests.next();
			INDArray features = next.getFeatures();
			
			/* init rnn */
			for(int i=0; i<timesteps; i++) {
				net.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.interval(0,13), NDArrayIndex.interval(i,i+1)));
			}
			
			
		}
		

		

		for(int i=0; i<35; i++) {
			DataSet next  = tests.next();
			INDArray features = next.getFeatures();
			INDArray labelsAct = next.getLabels();
			
			INDArray predict = net.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.interval(0,13), NDArrayIndex.all()));
			
			Long rowsLong = features.shape()[0];
			int rows = rowsLong.intValue();
			
			normalizer.revertLabels(predict);
			normalizer.revertLabels(labelsAct);
			normalizer.revertFeatures(features);
			

			predictsCLOSE[i] =   predict.getDouble(rows-1, 0, timesteps - 1) * 10;
			actualsCLOSE [i] = labelsAct.getDouble(rows-1, 0, timesteps - 1) * 10;
			
			
			Integer dataI   = features.getInt(rows-1, VECTOR_SIZE + 0, timesteps - 1);
			Integer oraMinI = features.getInt(rows-1, VECTOR_SIZE + 1, timesteps - 1);
			Integer dayI    = features.getInt(rows-1, VECTOR_SIZE + 2, timesteps - 1);
			String start = sdf(dataI) + " " + sdfHHMM(oraMinI) + " " + dayI;
			
			Integer dataL   = labelsAct.getInt(rows-1, outputLength + 0, timesteps - 1);
			Integer oraMinL = labelsAct.getInt(rows-1, outputLength + 1, timesteps - 1);
			Integer dayL    = labelsAct.getInt(rows-1, outputLength + 2, timesteps - 1);
			String startL = sdf(dataL) + " " + sdfHHMM(oraMinL) + " " + dayL;
			

			double percAct  = i == 0 ? 0 : ( actualsCLOSE[i] -  actualsCLOSE[i-1]) /  actualsCLOSE[i-1] * 100;
			double percPred = i == 0 ? 0 : (predictsCLOSE[i] - predictsCLOSE[i-1]) / predictsCLOSE[i-1] * 100;

			log.info("{}{} | {} -> {}  actuals {} {}% - predicts {} {}%", i<10?" ":"", i, start, startL, fr.format(actualsCLOSE[i]), frp.format(percAct), fr.format(predictsCLOSE[i]), frp.format(percPred) );

			net.rnnClearPreviousState();
		}
		
	}

	public static void plot(double[] predicts, double[] actuals, String name, int j) {
		double[] index = new double[predicts.length];
		for (int i = 0; i < predicts.length; i++) index[i] = i;
		int min = minValue(predicts, actuals);
		int max = maxValue(predicts, actuals);
		final XYSeriesCollection dataSet = new XYSeriesCollection();
		addSeries(dataSet, index, predicts, "Predicts");
		addSeries(dataSet, index, actuals, "Actuals");
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"Prediction Result " + j, // chart title
				"Index", // x axis label
				name, // y axis label
				dataSet, // data
				PlotOrientation.VERTICAL,
				true, // include legend
				true, // tooltips
				false // urls
				);
		XYPlot xyPlot = chart.getXYPlot();
		// X-axis
		final NumberAxis domainAxis = (NumberAxis) xyPlot.getDomainAxis();
		domainAxis.setRange((int) index[0], (int) (index[index.length - 1]));
		domainAxis.setTickUnit(new NumberTickUnit(1));
		domainAxis.setVerticalTickLabels(true);
		// Y-axis
		final NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
		rangeAxis.setRange(min, max);
		rangeAxis.setTickUnit(new NumberTickUnit(1));
		final ChartPanel panel = new ChartPanel(chart);
		final JFrame f = new JFrame();
		f.add(panel);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
	private static void addSeries (final XYSeriesCollection dataSet, double[] x, double[] y, final String label){
		final XYSeries s = new XYSeries(label);
		for( int j = 0; j < x.length; j++ ) s.add(x[j], y[j]);
		dataSet.addSeries(s);
	}
	private static int minValue (double[] predicts, double[] actuals) {
		double min = Integer.MAX_VALUE;
		for (int i = 0; i < predicts.length; i++) {
			if (min > predicts[i]) min = predicts[i];
			if (min > actuals[i]) min = actuals[i];
		}
		return (int) (min * 0.98);
	}
	private static int maxValue (double[] predicts, double[] actuals) {
		double max = Integer.MIN_VALUE;
		for (int i = 0; i < predicts.length; i++) {
			if (max < predicts[i]) max = predicts[i];
			if (max < actuals[i]) max = actuals[i];
		}
		return (int) (max * 1.02);
	}
	private String sdfHHMM(int hhmm) {
		String tmp = String.valueOf(hhmm);
		tmp = tmp.length()<4?"0"+tmp:tmp;
		return tmp.substring(0, 2) + ":" + tmp.substring(2);
	}
	private String sdf(int data) {
		String tmp = String.valueOf(data);
		for(int i=tmp.length();i<6;i++) tmp = "0" + tmp;
		return "20" + tmp.substring(0, 2) + "-" + tmp.substring(2, 4) + "-" + tmp.substring(4);
	}
	static {
		
		log.info("ND4J Data Type Setting: {}", Nd4j.dataType());
		//backend initialization
		log.info("cuda backend initialization...");
		CudaEnvironment.getInstance().getConfiguration()
		.allowMultiGPU(true)// key option enabled
		.setMaximumDeviceCache(8L * 1024L * 1024L * 1024L)// we're allowing larger memory caches
		.allowCrossDeviceAccess(true);// cross-device access is used for faster model averaging over pcie
	}

	public static void main(String[] args) {
		long inizio = System.currentTimeMillis();
		try {
			StockPricePredictionConvLSTM spp = new StockPricePredictionConvLSTM();
			spp.execute();

			log.info("{}", new SimpleDateFormat("HH'h' mm'm' ss's'").format(new Date((System.currentTimeMillis() - inizio) - (1000*60*60))));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
