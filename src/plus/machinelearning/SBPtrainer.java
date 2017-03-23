/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.Random;

/**
 *
 * @author Colin
 */
public class SBPtrainer {
 
    public static boolean Train(NeuralNetwork net, double[][] input, double[][] output, int epochs, int iterations, double accuracy, double learningRate){
        for(int e = 0; e < epochs; e++){
            //Randomize network weights
            net.Randomize();
            
            //Train network
            for(int i = 0; i < iterations; i++){
                int ind = rng.nextInt(input.length);
                double[] in = input[ind];
                double[] out = output[ind];

                net.Backpropagate(in, out, learningRate);
            }
            
            //Test network
            double teztAccuracy = 0;
            for(int i = 0; i < input.length; i++){
                double[] y = net.Feed(input[i]).GetData();
                double a = 0;
                for(int j = 0; j < y.length; j++){
                    a += Math.abs(y[j] - output[i][j]);
                }
                teztAccuracy += (a / y.length);
            }
            teztAccuracy /= input.length;
            
            if(teztAccuracy < accuracy){
                return true;
            }
        }
        
        return false;
    }
    
    public static Random rng = new Random(); 
    
    public static double Range(double min, double max){
        double t = rng.nextDouble();
        return (1 - t) * min + t * max;
    }
    
}
