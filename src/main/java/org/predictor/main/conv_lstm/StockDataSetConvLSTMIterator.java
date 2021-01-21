package org.predictor.main.conv_lstm;


import com.opencsv.CSVReader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.predictor.utils.Costants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class StockDataSetConvLSTMIterator implements DataSetIterator {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(StockDataSetConvLSTMIterator.class);

	private int VECTOR_SIZE;// = 5; // number of features for a stock data
	private int miniBatchSize; // mini-batch size
	private int timesteps;// = 22; // default 22, say, 22 working days per month
	private int outputLength;

	/** minimal values of each feature in stock dataset */
	private double[] minArray;
	/** maximal values of each feature in stock dataset */
	private double[] maxArray;

	/** mini-batch offset */
	private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();
	private LinkedList<Integer> exampleStartOffsetsTmp = new LinkedList<>();

	/** stock dataset for training */
	private List<Double[]> train;

	private int id = 0;
	private String symbol;
	private boolean isTest;

	private DataSetPreProcessor preProcessor;
	private List<String> headerRecord;


	public StockDataSetConvLSTMIterator(int id, String filename, String symbol, int miniBatchSize, int VECTOR_SIZE, int timesteps, int outputLength, boolean isTest) {
		this.id = id;
		this.symbol = symbol;
		this.VECTOR_SIZE = VECTOR_SIZE;
		this.miniBatchSize = miniBatchSize;
		this.timesteps = timesteps;
		this.outputLength = outputLength;
		this.train = readStockDataFromFile(filename, symbol);
		this.isTest = isTest;
		initializeOffsets();
	}


	/** initialize the mini-batch offsets */
	private void initializeOffsets () {
		exampleStartOffsets.clear();
		int window = timesteps + 102;//Costants.predictionLength;
		for (int i = 0; i < train.size() - window; i++) { exampleStartOffsets.add(i); }
	}

	private void initializeOffsets (int start, int stop) {
		exampleStartOffsets.clear();
		int window = timesteps + 103;// Costants.predictionLength;
		for (int i = start; i < train.size() - window; i++) { exampleStartOffsets.add(i); }
	}

	int startIdx = 0;
	int miniBatch = 0;
	int startIdxTmp = -1;

	@Override
	public DataSet next(int num) {
		miniBatch++;
		if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();
		int testLabel = isTest ? 3 : 0;
		int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
		INDArray feautures = Nd4j.create(new int[] {actualMiniBatchSize, VECTOR_SIZE + testLabel,  timesteps}, 'f');
		INDArray labels    = Nd4j.zeros(new int[] {actualMiniBatchSize, outputLength + testLabel, timesteps}, 'f');

		INDArray feauturesMask = Nd4j.ones(actualMiniBatchSize, timesteps);
		INDArray labelsMask    = Nd4j.ones(actualMiniBatchSize, timesteps);

		int startIdx = 0;
		for (int index = 0; index < actualMiniBatchSize; index++) {
			startIdx = exampleStartOffsets.removeFirst();

			int endIdx = startIdx + timesteps;

			for (int i = startIdx; i < endIdx; i++) {
				int c = i - startIdx;

				Double[] curData  = train.get(i);
				Double[] nextData = train.get(i + 102);

				for(int d=0; d<VECTOR_SIZE; d++) feautures.putScalar(new int[] {index, d, c},  curData[d]);
				for(int o=0; o<outputLength; o++)   labels.putScalar(new int[] {index, o, c}, nextData[o]);

				//			feauturesMask.putScalar(new int[] {index, c},  curData[VECTOR_SIZE]);
				//				   labelsMask.putScalar(new int[] {index, c}, nextData[VECTOR_SIZE]);

				if(isTest) {

					Integer data   = curData[VECTOR_SIZE + 1].intValue();
					Integer oraMin = curData[VECTOR_SIZE + 2].intValue();
					Integer day    = curData[VECTOR_SIZE + 3].intValue();

					feautures.putScalar(new int[] {index, VECTOR_SIZE, 	   c}, data);
					feautures.putScalar(new int[] {index, VECTOR_SIZE + 1, c}, oraMin);
					feautures.putScalar(new int[] {index, VECTOR_SIZE + 2, c}, day);


					Integer dataN   = nextData[VECTOR_SIZE + 1].intValue();
					Integer oraMinN = nextData[VECTOR_SIZE + 2].intValue();
					Integer dayN    = nextData[VECTOR_SIZE + 3].intValue();

					labels.putScalar(new int[] {index, outputLength, 	 c}, dataN);
					labels.putScalar(new int[] {index, outputLength + 1, c}, oraMinN);
					labels.putScalar(new int[] {index, outputLength + 2, c}, dayN);

//					log.info("{} {} {} -> {} {} {}", sdf(data), sdfHHMM(oraMin), day, sdf(dataN), sdfHHMM(oraMinN), dayN);
				}

			}	 
//			log.info("---|   miniBatch {} - row {}   |---",miniBatch,index+1);
			if (exampleStartOffsets.size() == 0) break;
		}
		if (exampleStartOffsets.size() > 1) exampleStartOffsets.add(0, startIdx);
//		log.info("---|   miniBatch {} - rows {}  |---", miniBatch, feautures.shape()[0]);
		//DataSet ds = new DataSet(feautures, labels, feauturesMask, labelsMask);
		DataSet ds = new DataSet(feautures, labels);
		if (preProcessor != null) { preProcessor.preProcess(ds); }
		return  ds;
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

	public int totalExamples() { return train.size() - timesteps; }

	@Override public int inputColumns() { return VECTOR_SIZE; }

	@Override public int totalOutcomes() { return outputLength; }

	@Override public boolean resetSupported() { return true; }

	@Override public boolean asyncSupported() { return true; }

	@Override public void reset() { 
		initializeOffsets(); 
		miniBatch = 0;
		startIdxTmp = -1;
	}

	public int totalMiniBatch() { 
		int count = 0;
		while(hasNext()) {
			count++;
			next();
		}
		reset();
		return count; 
	}

	@Override public int batch() { return miniBatchSize; }

	public int cursor() { return totalExamples() - exampleStartOffsets.size(); }

	public int numExamples() { return totalExamples(); }

	@Override public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) { this.preProcessor = dataSetPreProcessor; }

	@Override public DataSetPreProcessor getPreProcessor() { return this.preProcessor; }

	@Override public List<String> getLabels() { return headerRecord.subList(0, outputLength); }

	@Override public boolean hasNext() { return exampleStartOffsets.size() > 0; }

	@Override public DataSet next() { return next(miniBatchSize); }

	public DataSet next(int start, int stop) {
		initializeOffsets(start, stop);
		return next(miniBatchSize);
	}

	@SuppressWarnings("resource")
	public List<Double[]> readStockDataFromFile (String filename, String symbol) {
		List<Double[]> stockDataList = new ArrayList<>();
		try {
			List<String[]> list = new CSVReader(new FileReader(filename)).readAll(); // load all elements in a list

			String[] hr = list.get(0);
			headerRecord = Arrays.asList(hr).subList(2, hr.length);

			for (String[] arr : list) {
				if (!arr[1].equals(symbol)) continue;

				Double[] nums = new Double[VECTOR_SIZE + 4];
				for (int i = 0; i < VECTOR_SIZE; i++) { 
					nums[i] = Double.valueOf(arr[i + 2]); 
				}
				nums[VECTOR_SIZE] = Double.valueOf(arr[VECTOR_SIZE + 2]); //mask
				//2018-10-30 11:00
				String[] dataOra = arr[0].split(" ");
				double data = Double.valueOf(dataOra[0].replaceAll("-", "").substring(2));
				double oraMin = Double.valueOf(dataOra[1].split(":")[0]+dataOra[1].split(":")[1]);
				double day = Double.valueOf(arr[VECTOR_SIZE + 3]); //day

				nums[VECTOR_SIZE + 1] = data;
				nums[VECTOR_SIZE + 2] = oraMin;
				nums[VECTOR_SIZE + 3] = day;

				stockDataList.add(nums);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//        log.info("paddingData");
		//        stockDataList = paddingData(stockDataList);
		//        stockDataList = variazioni(stockDataList);
		//        stockDataList = normalizeMinMax(stockDataList);

		return stockDataList;
	}

	private List<Double[]> paddingData(List<Double[]> input){
		for(int r=1; r<input.size(); r++) {
			Double[] row = input.get(r);

			for(int i=0; i<9; i++) {
				double d = row[i];
				if(d == 0) {
					row[i] = input.get(r-1)[i];
				}
			}
		}
		return input;
	}

	private List<Double[]> variazioni(List<Double[]> input){
		List<Double[]> ret = new ArrayList<Double[]>();

		for(int i=1;i<input.size(); i++) {
			Double[] pred = input.get(i-1);
			Double[] cur = input.get(i);
			Double[] var = new Double[VECTOR_SIZE];
			for(int j=0; j<VECTOR_SIZE; j++) {
				double v = calcVariazione(pred[j],cur[j]);
				var[j] = v;
			}
			ret.add(var);
		}
		return ret;
	}

	private double calcVariazione(double pv, double cp) {
		if(pv == 0 || cp == 0) return 0;
		double ret = cp / pv * 100 - 100;
		return new BigDecimal(ret).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	private  List<Double[]> normalizeMinMax(List<Double[]> input){
		log.info("Normalize {} training data...", symbol);
		for(Double[] row: input) {
			for(int i=0; i<VECTOR_SIZE; i++) row[i] = (row[i] - minArray[i]) / (maxArray[i] - minArray[i]);
		}
		return input;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSymbol() { return symbol; }

	public void setSymbol(String symbol) { this.symbol = symbol; }

	public List<String> getHeaderRecord() {
		return headerRecord;
	}

	public void setHeaderRecord(List<String> headerRecord) {
		this.headerRecord = headerRecord;
	}
}
