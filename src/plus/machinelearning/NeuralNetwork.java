/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

/**
 *
 * @author Colin Halseth
 */
public interface NeuralNetwork {
    
    /**
     * Provide inputs and perform a feed-forward action to obtain network output 
     * @param inputs
     * @return 
     */
    public double[] Feed (double[] inputs);
    
    /**
     * Randomize layer connection weights
     */
    public void Randomize();
    
    /**
     * Perform a single iteration of a learning loop
     * @param learningRate
     * @param momentum
     * @param set 
     */
    public void Learn(double learningRate, double momentum, TrainingData.Pair set);
    
}
