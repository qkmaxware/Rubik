/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.math.*;
import plus.system.Debug;
import plus.system.Random;

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

    @Override
    public void Randomize() {
        for(Matrix w : this.weights){
            w.operate((in) -> {
                return (double)Random.Range(-1.0f, 1.0f);
            });
        }
    }
    
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
       
        for(int i = 0; i < 100; i++)
            for(int j = 0; j<data.Count(); j++)
                net.Learn(0.1, 0, data.Get(j));
        
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

    @Override
    public void Learn(double learningRate, double momentum, TrainingData.Pair set) {
        double[] in = set.in;
        Matrix[] deltas = new Matrix[this.errors.length];
        
        //Output layer back-propagtion
        Matrix out = Matrix.Row(this.Feed(in));
        Matrix expected = Matrix.Row(set.out);
        
        Matrix delta = Matrix.operate(
                out.sub(expected), 
                this.weights[this.weights.length - 1].mul(this.activations[this.activations.length - 2]).operate(this.sigmoid.GetDerivative()),
                (val, val2) -> {
                    return val * val2;
                }
        );
        
        //Hidden layer 
        for(int i = this.weights.length - 2; i >= 1; i--){
            Matrix.operate(
                    this.weights[i+1].Transpose().mul(deltas[i+1]),
                    (this.weights[i].mul(this.activations[i-1])).operate(this.sigmoid.GetDerivative()),
                    (val, val2) -> {
                        return val * val2;
                    }
            );
        }
        
        //Weight update
        for(int i = 1; i < this.weights.length; i--){
            Matrix change = deltas[i].mul(this.activations[i-1].Transpose());
            Matrix dot = Matrix.operate(
                    this.weights[i].scale(learningRate),
                    change,
                    (val, val2)-> { return val * val2; }
            );
            this.weights[i] = this.weights[i].sub(dot);
        }
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
