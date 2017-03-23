/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.Random;
import java.util.Scanner;
import plus.JSON.JSONproperty;
import plus.math.Matrix;

/**
 *
 * @author Colin
 */
public class MatrixNetwork implements NeuralNetwork, JSONproperty{

    private double bias = 1;
    private int size = 0;
    
    private Matrix[] weights;
    private Matrix[] biases;
    private Matrix[] Xs;
    private Matrix[] Ss;
    private Matrix[] Zs;
    
    private NetworkTopology topology;
    private ActivationFunction activation;
    
    //TODO make activation function matter
    public MatrixNetwork(NetworkTopology topology, ActivationFunction fn){
        this.topology = topology;
        this.activation = fn;
        
        int inputSize = topology.GetInputSize(); //2;//topology.GetInputSize();
        int outputSize = topology.GetOutputSize();//1;//topology.GetOutputSize();
        int[] layers = topology.GetLayerSizes();//new int[]{2,outputSize};//topology.GetLayerSizes();    //Hiddenlayers
        
        this.weights = new Matrix[layers.length];
        this.biases = new Matrix[layers.length];
        this.Xs = new Matrix[layers.length];
        this.Ss = new Matrix[layers.length];
        this.Zs = new Matrix[layers.length];
        
        int height = inputSize;
        for(int i = 0; i < this.weights.length; i++){
            this.weights[i] = new Matrix(height,layers[i]);
            this.biases[i] = new Matrix(1,layers[i]);
            height = layers[i];
            
        }
        
        Randomize(); 
        
        size = this.weights.length;
        
    }
    
    public Matrix[] GetWeights(){
        return weights;
    }
    
    public Matrix[] GetBiases(){
        return biases;
    }
    
    public static Random rng = new Random(); 
    
    public static double Range(double min, double max){
        double t = rng.nextDouble();
        return (1 - t) * min + t * max;
    }
    
    public void Randomize(){
        for(int i = 0; i < size; i++){
            this.weights[i] = this.weights[i].operate((in) -> {return Range(-1,1);});
            this.biases[i] = this.biases[i].operate((in) -> {return Range(-1,1);});
        }
    }
    
    public Matrix Feed(double[] in){
        this.Xs[0] = Matrix.Row(in);
        
        for(int i = 0; i < weights.length; i++){
            Matrix X = this.Xs[i];
            Matrix B = this.biases[i].operate((v) -> {return v * bias;});
            Matrix W = this.weights[i];
            
            Matrix S = (X.mul(W)).add(B);
            this.Ss[i] = S;
            
            Matrix Z = S.operate((v) -> {
                //return sigmoid(v);
                return this.activation.ApplyFunction(v);
            });
            this.Zs[i] = Z;
            
            if(i != weights.length - 1)
                Xs[i+1] = Z;
        }
        
        return this.Zs[this.Zs.length - 1];
    }
    
    public void Backpropagate(double[] inputs, double[] outputs, double eta){
        Matrix predicted = Feed(inputs);
        Matrix actual = Matrix.Row(outputs);
        
        double[][] del = new double[size][];
        double[][] dwb = new double[size][];
        double[][][] dw = new double[size][][];
        
        //For each neuron in the output layer
        double[] delta3 = new double[predicted.GetWidth()];
        double[][] dw2 = new double[this.weights[size - 1].GetHeight()][this.weights[size - 1].GetWidth()];
        double[] dwb2 = new double[this.biases[size - 1].GetWidth()];
        for(int i = 0; i < predicted.GetWidth(); i++){
            //delta3[i] = (actual.Get(0, i) - predicted.Get(0, i)) * sigmoidPrime(this.Ss[size - 1].Get(0, i));
            delta3[i] = (actual.Get(0, i) - predicted.Get(0, i)) * this.activation.ApplyDerivative(this.Ss[size - 1].Get(0, i));
        }
        for(int r = 0; r < dw2.length; r++){
            for(int c = 0; c < dw2[r].length; c++){
                dw2[r][c] = eta * delta3[c] * this.Xs[size - 1].Get(0, r); //Discrepancy here between this and previous 'c' for 'r'
            }
        }
        for(int i = 0; i < this.biases[size - 1].GetWidth(); i++){
            dwb2[i] = eta * delta3[i] * bias;
        }
        del[size - 1] = delta3;
        dw[size - 1] = dw2;
        dwb[size - 1] = dwb2;
        
        for(int l = size - 2; l >= 0; l--){
            //For each neuron in the hidden layer
            double[] delta2 = new double[this.Zs[l].GetWidth()];
            double[][] dw1 = new double[this.weights[l].GetHeight()][this.weights[l].GetWidth()];
            double[] dwb1 = new double[this.biases[l].GetWidth()];
            for(int i = 0; i < delta2.length; i++){
                double sum = 0;
                for(int c = 0; c < this.weights[l+1].GetWidth(); c++){
                    sum += this.weights[l+1].Get(i, c) * del[l+1][c]; //delta3[c];
                }
                //delta2[i] = sigmoidPrime(this.Ss[l].Get(0,i)) * sum;
                delta2[i] = this.activation.ApplyDerivative(this.Ss[l].Get(0,i)) * sum;
            }
            
            for(int r = 0; r < dw1.length; r++){
                for(int c = 0; c < dw1[r].length; c++){
                    dw1[r][c] = eta * delta2[c] * Xs[l].Get(0, r); //ERROR on DIMENTIONALITY
                }
            }
            for(int i = 0; i < this.biases[l].GetWidth(); i++){
                dwb1[i] = eta * delta2[i] * bias;
            }
            
            del[l] = delta2;
            dw[l] = dw1;
            dwb[l] = dwb1;
        }
        
        //Apply weights
        for(int i = 0; i < dw.length; i++){
            this.weights[i] = this.weights[i].add(new Matrix(dw[i]));
            this.biases[i] = this.biases[i].add(Matrix.Row(dwb[i]));
        }
        
    }
    
    public double sigmoid(double x){
        double r = 1.7 * Math.tanh(0.6*x);
        return r;
    }
    
    public double sigmoidPrime(double x){
        //4.08 * (Math.cosh(0.6*x) * Math.cosh(0.6*x)) / ((Math.cosh(1.2 * x) + 1) * (Math.cosh(1.2 * x) + 1))
        double r = 4.08 * (Math.cosh(0.6*x) * Math.cosh(0.6*x)) / ((Math.cosh(1.2 * x) + 1) * (Math.cosh(1.2 * x) + 1));
        return r;
    }
 
    @Override
    public String ToJSON() {
        StringBuilder s = new StringBuilder();
        s.append("{\"bias\":"+this.bias+", \"layers\": [");
        
        for(int i = 0; i < this.weights.length; i++){
            s.append(((i != 0)?",":"")+"{");
            
            s.append("\"weight\":");
            s.append("\""+this.weights[i].toString()+"\"");
            s.append(",");
            
            s.append("\"bias\":");
            s.append("\""+this.biases[i].toString()+"\"");
            
            s.append("}");
        }
        
        s.append("]}");
        return s.toString();
    }
    
}
