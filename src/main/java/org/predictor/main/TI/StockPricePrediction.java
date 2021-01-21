package org.predictor.main.TI;

import javafx.util.Pair;

import org.deeplearning4j.api.storage.StatsStorageRouter;
import org.deeplearning4j.api.storage.impl.RemoteUIStatsStorageRouter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Created by zhanghao on 26/7/17.
 * Modified by zhanghao on 28/9/17.
 * @author ZHANG HAO
 */
public class StockPricePrediction {

	private static final Logger log = LoggerFactory.getLogger(StockPricePrediction.class);

	private static int exampleLength = 88;//22; // time series length, assume 22 working days per month
	private static String symbol = "JP";
	private static int batchSize = 89; // mini-batch size
	private static double splitRatio = 0.96; // 90% for training, 10% for testing

	public static void main (String[] args) throws IOException {
		String file = new ClassPathResource("dataset_convLstm/dataset_TI_JP.csv").getFile().getAbsolutePath();
		int epochs = 200; // training epochs

		log.info("Create dataSet iterator...");
		StockDataSetIterator iterator = new StockDataSetIterator(file, symbol, batchSize, exampleLength, splitRatio);

		log.info("NormalizerStandardize {} training data...", symbol);
		NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
		normalizer.fitLabel(true);
		normalizer.fit(iterator);
		iterator.reset();
		iterator.setPreProcessor(normalizer);
		//		iterator.setPreProcessor(null);

		log.info("Load test dataset...");
		List<DataSet> test = iterator.getTestDataSet();

		log.info("Create iterator Test...");
		String fileTest = new ClassPathResource("dataset_convLstm/dataset_TI_JP.csv").getFile().getAbsolutePath();
		StockDataSetIterator iteratorTest = new StockDataSetIterator(fileTest, "JP", batchSize, exampleLength, splitRatio);
		
		log.info("NormalizerStandardize {} training data...", "JP");
		NormalizerMinMaxScaler normalizerTest = new NormalizerMinMaxScaler(0, 1);
		normalizerTest.fitLabel(true);
		normalizerTest.fit(iteratorTest);
		iteratorTest.reset();
		iteratorTest.setPreProcessor(normalizerTest);

		log.info("Build lstm networks...");
		MultiLayerNetwork net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

		DecimalFormatSymbols decimalFormatSymbols_Locale_US =   new DecimalFormatSymbols(Locale.US);
		DecimalFormat f   = new DecimalFormat("####0.000000", decimalFormatSymbols_Locale_US);

		log.info("Training...");
		for (int i = 0; i < epochs; i++) {

			while (iterator.hasNext()) {
				net.fit(iterator.next()); // fit model using mini-batch data
				System.out.print(".");
			}
			log.info("epoch {} - score {}",i, net.score());
			iterator.reset(); // reset iterator
			net.rnnClearPreviousState(); // clear previous state

			//			log.info("------------|    Evaluate Regression at epoch {}    |------------", curEpoch);
			RegressionEvaluation evaluation = net.evaluateRegression(iteratorTest);
			evaluation.setColumnNames(Arrays.asList(new String[]{"Open","Close","Low","High","VX"}));
			String meanSquaredError     = f.format(evaluation.meanSquaredError(0));
			String meanAbsoluteError    = f.format(evaluation.meanAbsoluteError(0));
			String rootMeanSquaredError = f.format(evaluation.rootMeanSquaredError(0));
			String relativeSquaredError = f.format(evaluation.relativeSquaredError(0));
			String correlationR2 		= f.format(evaluation.correlationR2(0));
			iteratorTest.reset();

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
		File locationToSave = new File("src/main/resources/StockPriceLSTM".concat(".zip"));
		// saveUpdater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this to train your network more in the future
		ModelSerializer.writeModel(net, locationToSave, true);

		log.info("Load model...");
		net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

		log.info("Testing...");

		//        predictPriceOneAhead(net, test, iterator.min, iterator.max);
		predictPriceOneAheadII(net);

		log.info("Done...");
	}


	private static void predictPriceOneAheadII (MultiLayerNetwork net) throws IOException {
		String fileTest = new ClassPathResource("dataset_convLstm/dataset_TI_JP.csv").getFile().getAbsolutePath();
		StockDataSetIterator iteratorTest = new StockDataSetIterator(fileTest, "JP", 10, exampleLength, splitRatio);
		NormalizerMinMaxScaler normalizerTest = new NormalizerMinMaxScaler(0, 1);
		normalizerTest.fitLabel(true);
		normalizerTest.fit(iteratorTest);
		iteratorTest.setPreProcessor(normalizerTest);
		
		int count = 0;
		while (iteratorTest.hasNext()) {
			count++;
			iteratorTest.next();
		}
		iteratorTest.reset();
		log.info("iteratorTest count {}", count);

		double[] predicts = new double[count];
		double[] actuals = new double[count];

		for(int i=0; i<count; i++) {
			DataSet next = iteratorTest.next();
			
//			NormalizerMinMaxScaler normalizerTest = new NormalizerMinMaxScaler(0, 1);
//			normalizerTest.fitLabel(true);
//			normalizerTest.fit(next);
			
			INDArray features = next.getFeatures();
			INDArray labelsAct = next.getLabels();

			INDArray predict = net.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all()));

			Long rowsLong = features.shape()[0];
			int rows = rowsLong.intValue();

			normalizerTest.revertLabels(predict);
			normalizerTest.revertLabels(labelsAct);
//			normalizerTest.revertFeatures(features);

			predicts[i] =   predict.getDouble(rows-1, 0, exampleLength - 1);
			actuals [i] = labelsAct.getDouble(rows-1, 0, exampleLength - 1);
			
			net.rnnClearPreviousState();
		}
		
		log.info("Print out Predictions and Actual Values...");
		log.info("Predict,Actual");
		for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "," + actuals[i]);
		log.info("Plot...");
		PlotUtil.plot(predicts, actuals, symbol);
	}

	/** Predict one feature of a stock one-day ahead */
	private static void predictPriceOneAhead (MultiLayerNetwork net, List<DataSet> testData, double min, double max) {
		double[] predicts = new double[testData.size()];
		double[] actuals = new double[testData.size()];

		NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
		preProcessor.fitLabel(true);

		for(DataSet ds:testData) {
			preProcessor.fit(ds);
		}

		for (int i = 0; i < testData.size(); i++) {
			DataSet dataSet = testData.get(i);

			//        	NormalizerMinMaxScaler preProcessor = new NormalizerMinMaxScaler();
			//        	preProcessor.fitLabel(false);
			//    		preProcessor.fit(dataSet);

			INDArray input = dataSet.getFeatures();
			INDArray label = dataSet.getLabels();

			preProcessor.transform(input);

			INDArray pred = net.rnnTimeStep(input);
			//        	preProcessor.revertLabels(label);

			double mi = preProcessor.getLabelMin().getDouble(0);
			double ma = preProcessor.getLabelMax().getDouble(0);

			long[] inputShape = input.shape();	//[66, 22]
			long[] labelShape = label.shape();	//[1]
			long[] predShape = pred.shape();	//[66, 1]


			predicts[i] = pred.getDouble(exampleLength - 1);
			actuals[i] = label.getDouble(0);
		}
		log.info("Print out Predictions and Actual Values...");
		log.info("Predict,Actual");
		for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "," + actuals[i]);
		log.info("Plot...");
		PlotUtil.plot(predicts, actuals, symbol);
	}


	/** Predict all the features (open, close, low, high prices and volume) of a stock one-day ahead */
	private static void predictAllCategories (MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData) {
		INDArray[] predicts = new INDArray[testData.size()];
		INDArray[] actuals = new INDArray[testData.size()];
		for (int i = 0; i < testData.size(); i++) {
			predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getRow(exampleLength - 1);
			actuals[i] = testData.get(i).getValue();
		}
		log.info("Print out Predictions and Actual Values...");
		log.info("Predict\tActual");
		for (int i = 0; i < predicts.length; i++) log.info(predicts[i] + "\t" + actuals[i]);
		log.info("Plot...");
		for (int n = 0; n < 5; n++) {
			double[] pred = new double[predicts.length];
			double[] actu = new double[actuals.length];
			for (int i = 0; i < predicts.length; i++) {
				pred[i] = predicts[i].getDouble(n);
				actu[i] = actuals[i].getDouble(n);
			}
			String name;
			switch (n) {
			case 0: name = "Stock OPEN Price"; break;
			case 1: name = "Stock CLOSE Price"; break;
			case 2: name = "Stock LOW Price"; break;
			case 3: name = "Stock HIGH Price"; break;
			case 4: name = "Stock VOLUME Amount"; break;
			default: throw new NoSuchElementException();
			}
			PlotUtil.plot(pred, actu, name);
		}
	}

}
