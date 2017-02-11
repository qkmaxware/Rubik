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
    
    public double[] Feed (double[] inputs);
    
    public void Randomize();
    
    public void Learn(double learningRate, TrainingData.Pair set);
    
}
