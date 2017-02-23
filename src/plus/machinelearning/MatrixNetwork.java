/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.Arrays;
import plus.math.Matrix;
import plus.system.Debug;
import plus.system.Random;

/**
 * TODO bias and momentum
 * @author Colin
 */
public class MatrixNetwork {
    
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
            Default.hidden = new int[]{2};
            Default.sigmoidFn = Sigmoid.tanh;
            return Default;
        }
    }
    
    private int inputs;
    private int outputs;
    private Sigmoid activationFn;
    
    private Config initialConfig;
    
    private int size;
    private Matrix[] weights;
    private Matrix[] Xs;
    private Matrix[] Zs;
    private Matrix[] As;
    
    public static void main(String[] args){
        Config conf = new Config();
        conf.inputs = 2;
        conf.hidden = new int[]{3};
        conf.outputs = 1;
        conf.sigmoidFn = Sigmoid.tanh;
        
        MatrixNetwork net = new MatrixNetwork(conf);
        Debug.Log(net.weights.length);
        
        
        //Not working
        Matrix data = new Matrix(new double[][]{
            {-1,-1},
            {-1, 1},
            { 1,-1},
            { 1, 1}
        });
        Matrix results = Matrix.Column(new double[]{-1,1,1,-1});
        
        /*
        //Working data
        Matrix data = new Matrix(new double[][]{
            {0,0},
            {0,1},
            {1,0},
            {1,1}
        });
        Matrix results = Matrix.Column(new double[]{0,1,1,0});
        */
        for(int i = 0; i < 1000; i++){
            net.Learn(0.1, 0, data, results);
            Matrix a = net.Feed(data);
            Debug.Log(i+": "+a.toString());
        }
        Matrix test = (net.Feed(data));
        Debug.Log("real");
        Debug.Log(results);
        Debug.Log("calculated");
        Debug.Log(test);
        Debug.Log("error");
        Debug.Log(test.sub(results).operate((in) -> {return (double)Math.round(in);}));
    }
    
    public MatrixNetwork(Config conf){
        this.inputs = Math.max(1, conf.inputs); //At least one input
        this.outputs = Math.max(1, conf.outputs);//At least one output 
        this.activationFn = conf.sigmoidFn;
        
        //Develop hidden layer weight matrices
        this.weights = new Matrix[Math.max(1,conf.hidden.length) + 1];
        int layerSize = this.inputs;
        for(int i = 0; i < weights.length - 1; i++){
            //Matrix from i-1 to i
            Matrix w = Matrix.Random(layerSize, conf.hidden[i]);
            weights[i] = w;
            layerSize = conf.hidden[i];
        }
        Matrix wo = Matrix.Random(layerSize, this.outputs);
        weights[weights.length - 1] = wo;
        
        //Final storage cleanup
        Xs = new Matrix[this.weights.length];
        Zs = new Matrix[this.weights.length];
        As = new Matrix[this.weights.length]; 
        
        size = this.weights.length;
        
        //Properly randomize weights
        Randomize();
    }
    
    public void Randomize() {
        for(int i = 0; i < this.weights.length; i++){
            Matrix w = this.weights[i];
            
            int ins = w.GetWidth();
            float r = (float)(1.0/Math.sqrt(ins));
            w.operate((in) -> {
                return (double)Random.Range(-r, r);
            });
        }
    }
    
    public Matrix Feed(double[] feature){
        return Feed(Matrix.Row(feature));
    }
    
    public Matrix Feed(Matrix inputs){
        //      Features
        // Ex1 [a, b, c]
        // Ex2 [d, e, f]
        // Ex3 [g, h , i]
        Matrix X = inputs;
        Matrix W = null;
        Matrix Z = null;
        Matrix A = null;
        
        for(int i = 0; i < this.weights.length; i++){
            Xs[i] = X; // Input layer is input for hidden layer 1(id = 0)
            
            W = this.weights[i]; //Weight from layer i-1 to i
            Z = Matrix.mul(X, W);
            A = Z.operate(this.activationFn.GetFunction());
            X = A;
            
            Zs[i] = Z;
            As[i] = A;
        }
        
        return Z;
    }
    
    public void Learn(double learningRate, double momentum, Matrix input, Matrix output){
        Matrix[] costs = CalculateCosts(input, output);
        Matrix[] updates = Scale(-learningRate, costs);
        UpdateWeights(momentum, updates);
    }
    
    public Matrix[] CalculateCosts(Matrix inputs, Matrix outputs){
        //Gradient dJdW (i) should be same size as W(i)
        Matrix test = Feed(inputs);
        Matrix expected = outputs;
        
        Matrix[] ds = new Matrix[size];
        Matrix[] dJs = new Matrix[size];
        
        //For output layer
        //(y(exp) - Y(out)) * dY(out)/dW(out)
        // (*) 
        //f'(z(out))
        // = 
        //delta out
        Matrix delta_out = Matrix.operate(
                test.sub(expected), //add? sub?
                Zs[size - 1].operate(this.activationFn.GetDerivative()),
                (a,b) -> {return a * b; }
        );
        Matrix dZdW_out = As[size - 2].Transpose();
        Matrix dJdW_out = Matrix.mul(dZdW_out, delta_out);
        ds[size - 1] = delta_out;
        dJs[size - 1] = dJdW_out;
        
        //Hidden layers
        for(int i = size - 2; i >= 0; i--){
            Matrix dZdA_h = weights[i+1].Transpose();
            Matrix dAdZ_h = Zs[i].operate(this.activationFn.GetDerivative());
            Matrix dZdW = Xs[i].Transpose();
            Matrix del = ds[i+1];
                
            Matrix delta_h = Matrix.operate(
                    del.mul(dZdA_h),
                    dAdZ_h,
                    (a,b) -> {return a * b;}
            );
            Matrix dJdW_h = dZdW.mul(delta_h);
            
            ds[i] = delta_h;
            dJs[i] = dJdW_h;
        }
        
        return dJs;
    }
    
    public Matrix[] Scale(double rate, Matrix[] ms){
        Matrix[] s = new Matrix[ms.length];
        for(int i = 0; i < ms.length; i++){
            s[i] = ms[i].scale(rate);
        }
        return s;
    }
    
    public void UpdateWeights(double momentum, Matrix[] deltas){
        for(int i = 0; i < Math.min(this.weights.length, deltas.length); i++){
            this.weights[i] = this.weights[i].add(deltas[i]);
        }
    }
}
