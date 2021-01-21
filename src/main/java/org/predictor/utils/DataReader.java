package org.predictor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.nd4j.linalg.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
/**
 * 
 * V. Algoritmo di Pan-Tompkins
 *
 */
public class DataReader {
	
	private static final Logger log = LoggerFactory.getLogger(DataReader.class);
	
	private final int TIMESTEP = 50;
	private final int VECTOR_SIZE = 40;
	
	private String fwCsvFile = "src/main/resources/dataset_convLstm_csv/features/";
	private String lwCsvFile = "src/main/resources/dataset_convLstm_csv/labels/";
	
	public static void main(String[] args) throws Exception {
		DataReader dr = new DataReader();
		dr.creaDataSetCsv();
//		dr.readCsv();
		log.info("done...");
	}
	
	public void creaDataSetCsv() throws IOException {
		List<Double[]> list = readCsv();
		
		int count;
		
		List<List<Double[]>> dataSet = new ArrayList<List<Double[]>>();
		List<List<Double[]>> dataSetTarget = new ArrayList<List<Double[]>>();
		List<Double[]> miniBatch = new ArrayList<Double[]>();
		List<Double[]> miniBatchTarget = new ArrayList<Double[]>();
		
		
		log.info("crea timesteps list...");
		count = 0;
		for(int i=0; i<list.size()-1; i++) {
			Double[] feauture = list.get(i);
			Double[] label = list.get(i+1);
			
			if(count == TIMESTEP) {
				count = 0;
				dataSet.add(miniBatch);
				dataSetTarget.add(miniBatchTarget);
				miniBatch = new ArrayList<Double[]>();
				miniBatchTarget = new ArrayList<Double[]>();
			}
			miniBatch.add(feauture);
			miniBatchTarget.add(label);
			
			count++;
		}
		dataSet.add(miniBatch);
		dataSetTarget.add(miniBatchTarget);
		int dataSetSize = dataSet.size();
		
		log.info("crea timesteps csv...");
		for(int i=0; i<dataSetSize; i++) { writeCsv(fwCsvFile + i + ".csv", dataSet.get(i)); }
		for(int i=0; i<dataSetSize; i++) { writeCsv(lwCsvFile + i + ".csv", dataSetTarget.get(i)); }
	}
	
	
	protected void writeCsv(String csvFile, List<Double[]> miniBatch) throws IOException {
		try (
				Writer writer = Files.newBufferedWriter(Paths.get(csvFile));
				CSVWriter csvWriter = new CSVWriter(writer,
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);
				) {
			for(Double[] row: miniBatch) { 
				String[] rowS = new String[row.length];
				for(int i=0; i<row.length; i++) rowS[i] = "" + row[i];
				csvWriter.writeNext(rowS); 
			}
		}
	}
	
	protected void writeLabelCsv(String csvFile, String row) throws IOException {
		try (
				Writer writer = Files.newBufferedWriter(Paths.get(csvFile));
				CSVWriter csvWriter = new CSVWriter(writer,
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);
				) {
			csvWriter.writeNext(row.split(","));
		}
	}

	protected List<Double[]> readCsv() throws IOException {
		List<Double[]> stockDataList = new ArrayList<Double[]>();
		String datasetConvLstm = "dataset_convLstm/prices-split-adjusted.csv";
		String filename = new ClassPathResource(datasetConvLstm).getFile().getAbsolutePath();
		
		log.info("CSVReader...");
		List<String[]> list = new CSVReader(new FileReader(filename)).readAll();
		
		log.info("create dataset...");
		for(int j=1; j<list.size(); j++) {
			String[] arr = list.get(j);
            Double[] nums = new Double[VECTOR_SIZE];
            for (int i = 0; i < arr.length - 2; i++) { nums[i] = Double.valueOf(arr[i + 2]); }
            stockDataList.add(nums);
        }
		return stockDataList;
	}

}
