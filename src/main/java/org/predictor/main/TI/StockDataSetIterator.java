package org.predictor.main.TI;

import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVReader;
import javafx.util.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.predictor.dto.StockData;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhanghao on 26/7/17.
 * Modified by zhanghao on 28/9/17.
 * @author ZHANG HAO
 */
public class StockDataSetIterator implements DataSetIterator {

	private static final long serialVersionUID = 1L;

	private final int VECTOR_SIZE = 8; // number of features for a stock data
	private int miniBatchSize; // mini-batch size
	private int exampleLength = 88;//22; // default 22, say, 22 working days per month
	private int predictLength = 1; // default 1, say, one day ahead prediction

	/** preprocessor */
	private DataSetPreProcessor preProcessor;

	/** mini-batch offset */
	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

	/** stock dataset for training */
	private List<StockData> train;
	/** adjusted stock dataset for testing */
	@SuppressWarnings("restriction")
	private List<DataSet> test;

	public StockDataSetIterator (String filename, String symbol, int miniBatchSize, int exampleLength, double splitRatio) {
		List<StockData> stockDataList = readStockDataFromFile(filename, symbol);
		this.miniBatchSize = miniBatchSize;
		this.exampleLength = exampleLength;
		int split = (int) Math.round(stockDataList.size() * splitRatio);
		train = stockDataList.subList(0, split);
		test = generateTestDataSet(stockDataList.subList(split, stockDataList.size()));
		initializeOffsets();
	}

	/** initialize the mini-batch offsets */
	private void initializeOffsets () {
		exampleStartOffsets.clear();
		int window = exampleLength + predictLength;
		for (int i = 0; i < train.size() - window; i++) { exampleStartOffsets.add(i); }
	}

	@Override
	public DataSet next(int num) {
		if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();
		int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
		INDArray input = Nd4j.zeros(new int[] {actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
		INDArray label;
		label = Nd4j.zeros(new int[] {actualMiniBatchSize, predictLength, exampleLength}, 'f');

		for (int index = 0; index < actualMiniBatchSize; index++) {
			int startIdx = exampleStartOffsets.removeFirst();
			int endIdx = startIdx + exampleLength;
			StockData curData = train.get(startIdx);
			StockData nextData;
			for (int i = startIdx; i < endIdx; i++) {
				int c = i - startIdx;
				input.putScalar(new int[] {index, 0, c},  (curData.getOpen()));
				input.putScalar(new int[] {index, 1, c},  (curData.getLabel()));
				input.putScalar(new int[] {index, 2, c},  (curData.getLow()));
				input.putScalar(new int[] {index, 3, c},  (curData.getHigh()));
//				input.putScalar(new int[] {index, 4, c},  (curData.getCloseRef1()));
//				input.putScalar(new int[] {index, 5, c},  (curData.getCloseRef2()));
				input.putScalar(new int[] {index, 7, c},  (curData.getCloseVX()));
//				input.putScalar(new int[] {index, 7, c},  (curData.getCloseDJ()));
//				input.putScalar(new int[] {index, 8, c},  (curData.getCloseFS()));
//				input.putScalar(new int[] {index, 9, c},  (curData.getCloseHS()));
//				input.putScalar(new int[] {index, 10, c}, (curData.getCloseNK()));
//				input.putScalar(new int[] {index, 11, c}, (curData.getPctChange()));
//				input.putScalar(new int[] {index, 4, c}, (curData.getMa7()));
//				input.putScalar(new int[] {index, 5, c}, (curData.getMa21()));
//				input.putScalar(new int[] {index, 6, c}, (curData.getEma26()));
//				input.putScalar(new int[] {index, 7, c}, (curData.getEma12()));
//				input.putScalar(new int[] {index, 8, c}, (curData.getMACD()));
				input.putScalar(new int[] {index, 4, c}, (curData.getSd20()));
				input.putScalar(new int[] {index, 5, c}, (curData.getUpperBand()));
				input.putScalar(new int[] {index, 6, c}, (curData.getOwerBand()));
//				input.putScalar(new int[] {index, 12, c}, (curData.getEma()));
//				input.putScalar(new int[] {index, 13, c}, (curData.getMomentum()));

				nextData = train.get(i + 1);

				label.putScalar(new int[]{index, 0, c}, nextData.getLabel());

				curData = nextData;
			}
			if (exampleStartOffsets.size() == 0) break;
		}

		DataSet ds = new DataSet(input, label);
		if (preProcessor != null) { preProcessor.preProcess(ds); }
		
		return ds;
	}

	public List<DataSet> getTestDataSet(){
		return test;
	}

	public double min = Double.MAX_VALUE;
	public double max = Double.MIN_VALUE;

	@SuppressWarnings("restriction")
	private List<DataSet> generateTestDataSet (List<StockData> stockDataList) {
		int window = exampleLength + predictLength;
		List<DataSet> test = new ArrayList<>();
		for (int i = 0; i < stockDataList.size() - window; i++) {
			INDArray input = Nd4j.zeros(new int[] {exampleLength, VECTOR_SIZE}, 'f');
			for (int j = i; j < i + exampleLength; j++) {
				StockData stock = stockDataList.get(j);
				input.putScalar(new int[] {j - i, 0},  (stock.getOpen()));
				input.putScalar(new int[] {j - i, 1},  (stock.getLabel()));
				input.putScalar(new int[] {j - i, 2},  (stock.getLow()));
				input.putScalar(new int[] {j - i, 3},  (stock.getHigh()));
//				input.putScalar(new int[] {j - i, 4},  (stock.getCloseRef1()));
//				input.putScalar(new int[] {j - i, 5},  (stock.getCloseRef2()));
				input.putScalar(new int[] {j - i, 7},  (stock.getCloseVX()));
//				input.putScalar(new int[] {j - i, 7},  (stock.getCloseDJ()));
//				input.putScalar(new int[] {j - i, 8},  (stock.getCloseFS()));
//				input.putScalar(new int[] {j - i, 9},  (stock.getCloseHS()));
//				input.putScalar(new int[] {j - i, 10}, (stock.getCloseNK()));
//				input.putScalar(new int[] {j - i, 11}, (stock.getPctChange()));
//				input.putScalar(new int[] {j - i, 4}, (stock.getMa7()));
//				input.putScalar(new int[] {j - i, 5}, (stock.getMa21()));
//				input.putScalar(new int[] {j - i, 6}, (stock.getEma26()));
//				input.putScalar(new int[] {j - i, 7}, (stock.getEma12()));
//				input.putScalar(new int[] {j - i, 8}, (stock.getMACD()));
				input.putScalar(new int[] {j - i, 4}, (stock.getSd20()));
				input.putScalar(new int[] {j - i, 5}, (stock.getUpperBand()));
				input.putScalar(new int[] {j - i, 6}, (stock.getOwerBand()));
//				input.putScalar(new int[] {j - i, 12}, (stock.getEma()));
//				input.putScalar(new int[] {j - i, 13}, (stock.getMomentum()));
			}

			StockData nextData = stockDataList.get(i + exampleLength);
			INDArray label = Nd4j.zeros(new int[] {1}, 'f');

			double val = nextData.getLabel();
			if(val > max)max = val;
			if(val < min)min = val;
			
			label.putScalar(new int[] {0}, val);
			
			DataSet ds = new DataSet(input, label);
			
			test.add(ds);
		}
		return test;
	}


	public int totalExamples() { return train.size() - exampleLength - predictLength; }

	@Override public int inputColumns() { return VECTOR_SIZE; }

	@Override public int totalOutcomes() {
		return predictLength;
	}

	@Override public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) { this.preProcessor = dataSetPreProcessor; }

	@Override public DataSetPreProcessor getPreProcessor() { return this.preProcessor; }

	@Override public boolean resetSupported() { return false; }

	@Override public boolean asyncSupported() { return false; }

	@Override public void reset() { initializeOffsets(); }

	@Override public int batch() { return miniBatchSize; }

	public int cursor() { return totalExamples() - exampleStartOffsets.size(); }

	public int numExamples() { return totalExamples(); }

	@Override public List<String> getLabels() { throw new UnsupportedOperationException("Not Implemented"); }

	@Override public boolean hasNext() { return exampleStartOffsets.size() > 0; }

	@Override public DataSet next() { return next(miniBatchSize); }

	@SuppressWarnings("resource")
	public static List<StockData> readStockDataFromFile (String filename, String symbol) {
		List<StockData> stockDataList = new ArrayList<StockData>();
		try {
			List<String[]> list = new CSVReader(new FileReader(filename)).readAll(); // load all elements in a list

			//			String symbol, String date, double open, double high, double low, double label, double closeRef1,
			//			double closeRef2, double closeVX, double closeDJ, double closeFS, double closeHS, double closeNK,
			//			double pctChange, double ma7, double ma21, double ema26, double ema12, double mACD, double sd20,
			//			double upperBand, double owerBand, double ema, double momentum

			for(int i=1; i<list.size(); i++) {
				String[] row = list.get(i);

				StockData item = new StockData(symbol, row[0], Double.valueOf(round(row[1])), Double.valueOf(round(row[2])), Double.valueOf(round(row[3])), Double.valueOf(round(row[4])), Double.valueOf(row[5]),
						Double.valueOf(row[6]), Double.valueOf(row[7]), Double.valueOf(row[8]), Double.valueOf(row[9]), Double.valueOf(row[10]), Double.valueOf(row[11]),	
						Double.valueOf(row[12]), Double.valueOf(row[13]), Double.valueOf(row[14]), Double.valueOf(row[15]), Double.valueOf(row[16]), Double.valueOf(row[17]), Double.valueOf(row[18]),
						Double.valueOf(row[19]), Double.valueOf(row[20]), Double.valueOf(row[21]), Double.valueOf(row[22])
						);

				System.out.println(item);
				stockDataList.add(item);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("inizio: " + stockDataList.get(0).getDate());
		System.out.println("fine:   " + stockDataList.get(stockDataList.size()-1).getDate());
		System.out.println("totale: " + stockDataList.size());

		return stockDataList;
	}


	private static String round(String x) {
		int p = x.indexOf(".");
		String z = x.substring(p);
		String y = "";
		if(z.length() > 2) {
			y = x.substring(0, (x.indexOf(".")+3));
		}else {
			y = x;
		}
		return y;
	}

	public static void main(String[]args) {
		//TODO: arrotondare gli indici di borsa!!
		StockDataSetIterator.readStockDataFromFile("src/main/resources/dataset_convLstm/dataset_TI_GS.csv", "GS");
		//		String x = "86.7";
		//		int p = x.indexOf(".");
		//		String z = x.substring(p);
		//		String y = "";
		//		if(z.length() > 2) {
		//			y = x.substring(0, (x.indexOf(".")+3));
		//		}else {
		//			y = x;
		//		}
		//		System.out.println(x);
		//		System.out.println(y);
	}
}
