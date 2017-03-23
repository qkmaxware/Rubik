/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.math.Matrix;

/**
 *
 * @author Colin
 */
public interface NeuralNetwork {
    
    public void Randomize();
    public Matrix Feed(double[] input);
    public void Backpropagate(double[] inputs, double[] outputs, double eta);
    
}
