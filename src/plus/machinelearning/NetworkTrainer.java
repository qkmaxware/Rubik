/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.ArrayList;
import plus.system.Debug;
import plus.system.Random;

/**
 *
 * @author Colin Halseth
 */
public class NetworkTrainer {
    
    private static java.util.Random rng = new java.util.Random();
    
    /**
     * Perform a training iteration loop for a neural network based on training data
     * @param network
     * @param epochs
     * @param iterations
     * @param learningRate
     * @param momentum
     * @param accuracy
     * @param training
     * @param test
     * @return 
     */
    public boolean Train(NeuralNetwork network, int epochs, int iterations, double learningRate, double momentum, double accuracy, TrainingData training, TrainingData test){
        for(int e = 0; e < epochs; e++){
            Debug.Log("Epoch "+e);
            network.Randomize();
            
            //Training loop
            for(int i = 0; i < iterations; i++){
                int id = rng.nextInt(training.Count());
                TrainingData.Pair pair = training.Get(id);
                network.Learn(learningRate,momentum, pair);
            }
            
            //Compute network accuracy on testing data
            double testAccuracy = 0;
            if(test != null){
                double[] acc = null;
                for(int i = 0; i < test.Count(); i++){
                    TrainingData.Pair pair = test.Get(i);
                    double[] t = network.Feed(pair.in);
                    double[] z = pair.out;
                    double[] diffSquared = new double[Math.min(t.length, z.length)];
                    for(int j = 0; j < diffSquared.length; j++){
                        diffSquared[j] = (t[j]-z[j])*(t[j]-z[j]);
                    }
                    acc = ArrayAdd(acc, diffSquared);
                }
                
                if(acc != null)
                    for(int i = 0; i < acc.length; i++){
                        testAccuracy += acc[i];
                    }
                
                testAccuracy /= acc.length;
            }
            
            Debug.Log("Training accurate to: "+testAccuracy);
            
            //If within tolerance, don't do any more epochs. Return "true" for successful training
            if(testAccuracy < accuracy)
                return true;
        }
        return false;
    }
    
    
    private double[] ArrayAdd(double[] a, double[] b){
        if(a == null)
            return b;
        if(b == null)
            return a;
        
        double[] sum = new double[Math.min(a.length, b.length)];
        for(int i = 0; i < sum.length; i++){
            sum[i] = a[i] + b[i];
        }
        return sum;
    }
}
