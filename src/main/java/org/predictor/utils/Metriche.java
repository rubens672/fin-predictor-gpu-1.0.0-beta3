package org.predictor.utils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.deeplearning4j.eval.RegressionEvaluation;

public class Metriche implements Serializable {

	private static final long serialVersionUID = 1L;

	private String titolo;
	private String nomeImmagine;
	private String dataSetSize;
	private String splitRatio;
	private String epochs;
	private String learningRate;
	private String batchSize;
	private String exampleLength;
	private String averagingFrequency;
	private String examplesPerDataSetObject;
	private String l2 = "-";
	private String l1 = "-";
	private String bestEpoch;
	private String bestScore;
	private String durata;
	private String note = "-";
	private String t;
	
	private String MSE = "-"; 
	private String MAE = "-"; 
	private String RMSE = "-";
	private String RSE = "-"; 
	private String PC = "-";
	private String R2 = "-";
	
	public Metriche() {
		super();
	}
	
	
	public static Metriche leggiMetriche(RegressionEvaluation eval, long inizio, Integer bestEpoch, double bestScore) {
		long fine = System.currentTimeMillis();
		String durata = new SimpleDateFormat("HH:mm:ss").format(new Date((fine - inizio) - (1000 * 60 * 60)));
		String t = new SimpleDateFormat("ddHHmmss").format(new Date(System.currentTimeMillis()));
		DecimalFormat f = new DecimalFormat("####0.0000");
		String MSE = 	f.format(eval.averageMeanSquaredError());
		String MAE = 	f.format(eval.averageMeanAbsoluteError());
		String RMSE = 	f.format(eval.averagerootMeanSquaredError());
		String RSE = 	f.format(eval.averagerelativeSquaredError());
		String PC = 	f.format(eval.averageMeanSquaredError());
		String R2 = 	f.format(eval.averagecorrelationR2());
//		String R2 = 	f.format(eval.averageRSquared());

		Metriche metriche = new Metriche();
		metriche.setTitolo("StockPricePrediction Result");
		metriche.setNomeImmagine("Prediction_Result ");
		metriche.setDataSetSize("" + Costants.dataSetSize);
		metriche.setSplitRatio("" + Costants.splitRatio);
		metriche.setEpochs("" + Costants.epochs);
		metriche.setLearningRate("" + Costants.learningRate);
		metriche.setBatchSize("" + Costants.miniBatchSize);
		metriche.setExampleLength("" + Costants.timesteps);
		metriche.setAveragingFrequency("" + Costants.averagingFrequency);
		metriche.setExamplesPerDataSetObject("" + Costants.examplesPerDataSetObject);
		metriche.setL2("" + Costants.l2);
		metriche.setL1("" + Costants.l1);
		metriche.setBestEpoch(bestEpoch.toString());// + bestEpoch);
		metriche.setBestScore(f.format(bestScore));// + bestScore);
		metriche.setMSE(MSE);
		metriche.setMAE(MAE);
		metriche.setRMSE(RMSE);
		metriche.setRSE(RSE);
		metriche.setPC(PC);
		metriche.setR2(R2);
		metriche.setDurata(durata);
		metriche.setT(t);
		return metriche;
	}

	public String getDataSetSize() {
		return dataSetSize;
	}

	public void setDataSetSize(String dataSetSize) {
		this.dataSetSize = dataSetSize;
	}

	public String getSplitRatio() {
		return splitRatio;
	}

	public void setSplitRatio(String splitRatio) {
		this.splitRatio = splitRatio;
	}

	public String getEpochs() {
		return epochs;
	}

	public void setEpochs(String epochs) {
		this.epochs = epochs;
	}

	public String getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(String learningRate) {
		this.learningRate = learningRate;
	}

	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getExampleLength() {
		return exampleLength;
	}

	public void setExampleLength(String exampleLength) {
		this.exampleLength = exampleLength;
	}

	public String getAveragingFrequency() {
		return averagingFrequency;
	}

	public void setAveragingFrequency(String averagingFrequency) {
		this.averagingFrequency = averagingFrequency;
	}

	public String getExamplesPerDataSetObject() {
		return examplesPerDataSetObject;
	}

	public void setExamplesPerDataSetObject(String examplesPerDataSetObject) {
		this.examplesPerDataSetObject = examplesPerDataSetObject;
	}

	public String getL2() {
		return l2;
	}

	public void setL2(String l2) {
		this.l2 = l2;
	}

	public String getL1() {
		return l1;
	}

	public void setL1(String l1) {
		this.l1 = l1;
	}

	public String getBestEpoch() {
		return bestEpoch;
	}

	public void setBestEpoch(String bestEpoch) {
		this.bestEpoch = bestEpoch;
	}

	public String getBestScore() {
		return bestScore;
	}

	public void setBestScore(String bestScore) {
		this.bestScore = bestScore;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "Metriche: "
				+ "\nData Set Size = " + dataSetSize 
				+ "\nSplit Ratio = " + splitRatio 
				+ "\nEpochs = " + epochs
				+ "\nLearning Rate = " + learningRate 
				+ "\nBatch Size = " + batchSize 
				+ "\nExample Length = " + exampleLength
				+ "\naveragingFrequency = " + averagingFrequency 
				+ "\nexamplesPerDataSetObject = " + examplesPerDataSetObject 
				+ "\nL2 = " + l2 
				+ "\nL1 = " + l1 
				+ "\nBest Epoch = " + bestEpoch 
				+ "\nBest Score = " + bestScore 
				+ "\nMean Squared Error (MSE) = " + MSE 
				+ "\nMean Absolute Error (MAE) = " + MAE 
				+ "\nMean Squared Error (MSE) = " + RMSE
				+ "\nSquared Error (SE) = " + RSE 
				+ "\nPearson Correlation (PC) = " + PC  
				+ "\nR Squared (R^2) = " + R2  
				+ "\nTempo di esecuzione = " + durata 
				+ "\norario = " + t 
				+ "\nNote: " + note;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public String getDurata() {
		return durata;
	}

	public void setDurata(String durata) {
		this.durata = durata;
	}

	public String getNomeImmagine() {
		return nomeImmagine;
	}

	public void setNomeImmagine(String nomeImmagine) {
		this.nomeImmagine = nomeImmagine;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getMSE() {
		return MSE;
	}

	public void setMSE(String mSE) {
		MSE = mSE;
	}

	public String getMAE() {
		return MAE;
	}

	public void setMAE(String mAE) {
		MAE = mAE;
	}

	public String getRMSE() {
		return RMSE;
	}

	public void setRMSE(String rMSE) {
		RMSE = rMSE;
	}

	public String getRSE() {
		return RSE;
	}

	public void setRSE(String rSE) {
		RSE = rSE;
	}

	public String getPC() {
		return PC;
	}

	public void setPC(String pC) {
		PC = pC;
	}

	public String getR2() {
		return R2;
	}

	public void setR2(String r2) {
		R2 = r2;
	}
	
	
}
