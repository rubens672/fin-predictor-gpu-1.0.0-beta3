package org.predictor.main.TI;

import java.util.HashMap;
import java.util.Map;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
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
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
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

//    private static final int lstmLayerSize = Costants.lstmLayerSize;//256;
//    private static final int denseLayerSize = Costants.denseLayerSize;//32;
//    private static final double dropoutRatio = 0.2;
//    private static final int truncatedBPTTLength = 22;
    
	private static final double learningRate = 0.0005;
    private static final int lstmLayer1Size = 46;//256;
    private static final int lstmLayer2Size = 46;//256;
    private static final int denseLayerSize = 22;//32;
    private static final double dropoutRatio = 0.2;
    private static final int truncatedBPTTLength = 22;

    public static MultiLayerNetwork buildLstmNetworks(int nIn, int nOut) {
    	//https://github.com/deeplearning4j/nd4j/tree/master/nd4j-backends/nd4j-api-parent/nd4j-api/src/main/java/org/nd4j/linalg/schedule
        Map<Integer, Double> lrSchedule = new HashMap<Integer, Double>();
        lrSchedule.put(0, 0.005);
        lrSchedule.put(5000, learningRate);
        
        Map<Integer, Double> learningRateSchedule = new HashMap<>();
        learningRateSchedule.put(0, 0.05);
        learningRateSchedule.put(600, 0.005);
        learningRateSchedule.put(1000, learningRate);
        
        RmsProp rmsProp2 = new RmsProp(new MapSchedule(ScheduleType.ITERATION, learningRateSchedule));
        
        RmsProp rmsProp = new RmsProp.Builder()
        					.rmsDecay(0.95)
        					.learningRate(learningRate)
        					.build();
//        rmsProp.setLearningRate(0.05);
//        rmsProp.setRmsDecay(0.95);
        
        /* da verificare
        Idea with a weighted loss function: it allows us to add a weight to the outputs.
        For example, if we have 3 classes, and we consider predictions of the 3rd class to be more important, we might use
        a weight array of [0.5,0.5,1.0]. This means that the first 2 outputs will contribute only half as much as they
        normally would to the loss/score.
        Create the weights array. Note that we have 3 output classes, therefore we have 3 weights 
        CLOSE_GE,CLOSE_CH,CLOSE_JA,CLOSE_EN,CLOSE_IT*/
        INDArray weightsArray = Nd4j.create(new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00});//, 0.88,0.63,0.62,0.66,0.5});
        LossMSE lossMSE = new LossMSE(weightsArray);
    	
    	MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
//                .iterations(iterations)
//                .learningRate(learningRate)
//                .learningRateDecayPolicy(LearningRatePolicy.Schedule)
//                .learningRateSchedule(lrSchedule)
//                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .weightInit(WeightInit.XAVIER)
                .updater( rmsProp )//new Adam(learningRate))//RMSPROP orig
                .l2(1e-4)
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(nIn)
                        .nOut(lstmLayer1Size)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.SIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(lstmLayer1Size)
                        .nOut(lstmLayer2Size)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.SIGMOID)
                        .dropOut(dropoutRatio)
                        .build())
                .layer(2, new DenseLayer.Builder()
                		.nIn(lstmLayer2Size)
                		.nOut(denseLayerSize)
                		.activation(Activation.RELU)
                		.build())
                .layer(3, new RnnOutputLayer.Builder()
                        .nIn(denseLayerSize)
                        .nOut(nOut)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
//                        .lossFunction(lossMSE)
                        .build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(truncatedBPTTLength)
                .tBPTTBackwardLength(truncatedBPTTLength)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(100));
        return net;
    }
}
