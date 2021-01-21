package org.predictor.model;

import java.util.HashMap;
import java.util.Map;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
//import org.deeplearning4j.nn.conf.LearningRatePolicy;
//import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.impl.LossMCXENT;
import org.nd4j.linalg.lossfunctions.impl.LossMSE;
import org.predictor.utils.Costants;

/**
	SOFTMAX = per regressione logistica di classificazioni multiple, schiaccia il risultato in un range da 0 e 1, la somma di tutte le probabilità è 1
	SIGMOID = per classificazioni binarie, la somma delle probabilità non deve essere 1
	RELU 	= schiaccia a 0 i valori negativi e arriva a infinito per quelli positivi, usata per problemi di regressione
	TANH 	= funzione iperbolica, per valori positivi e negativi
 
 *
 */

public class RecurrentNets {
	
//	private static final double learningRate = 0.005;
	private static final int iterations = 1;
	private static final int seed = 12345;

    private static final int lstmLayerSize = Costants.lstmLayerSize;//256;
    private static final int denseLayerSize = Costants.denseLayerSize;//32;
    private static final double dropoutRatio = 0.2;
//    private static final int truncatedBPTTLength = 22;

    public MultiLayerNetwork buildLstmNetworks(int nIn, int nOut, double learningRate, double rmsDecay, double l2, double l1, int truncatedBPTTLength) {
        Map<Integer, Double> lrSchedule = new HashMap<Integer, Double>();
        lrSchedule.put(0, 0.005);
        lrSchedule.put(5000, learningRate);
         
        RmsProp rmsProp = new RmsProp.Builder()
        					.rmsDecay(0.95)
        					.learningRate(learningRate)
        					.build();
//        rmsProp.setLearningRate(0.05);
//        rmsProp.setRmsDecay(0.95);
        
        INDArray weightsArray = Nd4j.create(new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00});//, 0.88,0.63,0.62,0.66,0.5});
        LossMSE lossMSE = new LossMSE(weightsArray);
    	
    	MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater( rmsProp )
                .l2(1e-4)
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(nIn)
                        .nOut(lstmLayerSize)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.SIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(lstmLayerSize)
                        .nOut(lstmLayerSize)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.SIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(2, new DenseLayer.Builder()
                		.nIn(lstmLayerSize)
                		.nOut(denseLayerSize)
                		.activation(Activation.RELU)
                		.build())
                .layer(3, new RnnOutputLayer.Builder()
                        .nIn(denseLayerSize)
                        .nOut(nOut)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength( 17 )//10
                .tBPTTBackwardLength( 17 )
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(5000));
        return net;
    }
}
