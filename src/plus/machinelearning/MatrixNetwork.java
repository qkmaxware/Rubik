/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.math.*;
import plus.system.Debug;
import plus.system.Random;
import plus.system.functional.Func2;

/**
 *
 * @author Colin
 */
public class MatrixNetwork implements NeuralNetwork{

    private Matrix[] weights;
    private Matrix[] net;
    private Matrix[] activations;
    private Matrix[] derivatives;
    private Matrix[] errors;
    
    private int inputs;
    private int outputs;
    private double bias;
    
    private Config initial_config;
    private Sigmoid sigmoid;
    
    private static Func2<Double,Double,Double> dot = (val,val2) -> {return val * val2;};
    
    public static class Config{
        
        public double bias;
        public int inputs;
        public int outputs;
        public int[] hidden;
        public Sigmoid sigmoidFn;
        
        public Config(){}
        public static Config GetDefaults(){
            Config Default = new Config();
            Default.bias = 1;
            Default.sigmoidFn = Sigmoid.tanh;
            return Default;
        }
    }
    
    public static void main(String[] args){
        Config con = new Config();
        con.bias = 1;
        con.inputs = 2;
        con.outputs = 1;
        con.hidden = new int[]{2};
        con.sigmoidFn = Sigmoid.tanh;
        
        MatrixNetwork net = new MatrixNetwork(con);
        TrainingData data = new TrainingData();
        data.Add(new double[]{-1,-1}, new double[]{-1});
        data.Add(new double[]{-1,1}, new double[]{1});
        data.Add(new double[]{1,-1}, new double[]{1});
        data.Add(new double[]{1,1}, new double[]{-1});
       
        //for(int i = 0; i < 100; i++)
            //for(int j = 0; j<data.Count(); j++)
                //net.Learn(0.1, 0, data.Get(j));
        
        Debug.Log(net.Feed(new double[]{-1,-1})[0]);
        Debug.Log(net.Feed(new double[]{1,-1})[0]);
        Debug.Log(net.Feed(new double[]{-1,1})[0]);
        Debug.Log(net.Feed(new double[]{1,1})[0]);
        
    }
    
    public MatrixNetwork(Config config){
        this.initial_config = config;
        this.sigmoid = config.sigmoidFn;
        
        this.inputs = Mathx.Clamp(config.inputs, 2, Integer.MAX_VALUE);
        this.outputs = Mathx.Clamp(config.outputs, 1, inputs - 1);
        
        this.weights = new Matrix[config.hidden.length+1];
        this.net = new Matrix[this.weights.length];
        this.activations = new Matrix[this.weights.length];
        this.errors = new Matrix[this.weights.length];
        this.derivatives = new Matrix[this.weights.length];
        //this.deltas = new Matrix[this.weights.length];
        this.bias = config.bias;
        
        int size = inputs;
        for(int i = 0; i < weights.length - 1; i++){
            Matrix weight = Matrix.Random(size, config.hidden[i]);
            this.weights[i] = weight;
            size = config.hidden[i];
        }
        this.weights[this.weights.length - 1] = Matrix.Random(size, outputs);
        
    }
    
    @Override
    public void Randomize() {
        for(Matrix w : this.weights){
            int ins = w.GetWidth();
            w.operate((in) -> {
                return (double)Random.Range(-(float)(1.0/Math.sqrt(ins)), (float)(1.0/Math.sqrt(ins)));
            });
        }
    }
    
    @Override
    public double[] Feed(double[] inputs) {
        //Row matrix input
        Matrix X = Matrix.Row(inputs);
        
        //Repeated matrix multiplication
        for(int i = 0; i < this.weights.length; i++){
            Matrix net = X.mul(this.weights[i]); //X * W
            Matrix bias = new Matrix(net).operate((in)->{return this.bias;});
            this.net[i] = net.add(bias);
            
            this.activations[i] = net.operate(this.sigmoid.GetFunction());
            //this.derivatives[i] = net.operate(this.sigmoid.GetDerivative()).Transpose();
            X = this.activations[i];
        }
        
        //Row matrix output
        double[] d = new double[X.GetWidth()];
        for(int i = 0; i < X.GetWidth(); i++){
            d[i] = X.Get(0, i);
        }
        return d;
    }

    //http://neuralnetworksanddeeplearning.com/chap2.html
    @Override
    public void Learn(double learningRate, double momentum, TrainingData.Pair set) {
        double[] in = set.in;
        
        //Output layer back-propagtion
        Matrix out = Matrix.Row(this.Feed(in));
        Matrix expected = Matrix.Row(set.out);
        Matrix[] deltas = new Matrix[this.weights.length];
        
        //Output layer recalc
        //Gradiant(Cost) DOT activation'(output)
        Matrix delta = Matrix.operate(
                out.sub(expected), 
                this.net[this.net.length - 1], 
                dot
        );
        deltas[deltas.length - 1] = delta;
        
        //Hidden layer recalc
        //Wl-1^T * deltal+1 DOT activation'(output)
        for(int i = this.weights.length - 2; i >= 0; i--){
            Debug.Log(this.weights[i+1]);
            Debug.Log(deltas[i+1]);
            Matrix prev = this.weights[i+1].mul(deltas[i+1]);
            Debug.Log(prev);
            Debug.Log(this.activations[i]);
            delta = Matrix.operate(
                    prev.Transpose(), 
                    this.activations[i].operate(this.sigmoid.GetDerivative()), 
                    dot
            );
            deltas[i] = delta;
        }
        
        //Update the biases
        for(int i = this.weights.length - 1; i >= 0; i--){
            Matrix dW =  deltas[i];
            this.weights[i] = this.weights[i].sub(dW);
        }
        
    }
    
}
