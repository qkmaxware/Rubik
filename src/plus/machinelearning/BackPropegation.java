/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.ArrayList;

/**
 *
 * @author Colin Halseth
 */
public class BackPropegation {
    
    public void Learn(NeuralNetwork network, int epochs, int iterations, double learningRate, double accuracy, TrainingData training, TrainingData test){
        for(int e = 0; e < epochs; e++){
        
            for(int i = 0; i < iterations; i++){
                for(int t = 0; t < training.Count(); t++){
                    TrainingData.Pair pair = training.Get(t);
                    network.Learn(learningRate, pair);
                }
            }
        }
        
    }
    
}
