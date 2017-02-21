/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import plus.JSON.*;
import plus.math.*;
import plus.system.Debug;
import plus.system.Random;
import plus.system.functional.Func2;

/**
 *
 * @author Colin
 */
public class MatrixNetwork implements NeuralNetwork{

    private Layer[] layers;
    private Matrix[] lastWeightUpdate;
    
    private int inputs;
    private int outputs;
    private double bias;
    
    private Config initial_config;
    private Sigmoid sigmoid;
    
    private static Func2<Double,Double,Double> componentMul = (val,val2) -> {return val * val2;};
    
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
    
    private static class Layer{
        public Matrix input;        //Unaltered input
        public Matrix net;          //Weighted + biased input
        public Matrix netNobias;    //Weighted input
        public Matrix output;       //Sigmoid output
        public Matrix outputPrime;  //Sigmoid Prime output
        public Matrix inWeight;     //Weight to get into this node
        public Matrix inBiasWeight; //Weight fot bias into this node
        
        public Matrix S(){
            return this.net;
        }
        public Matrix Z(){
            return this.output;
        }
    }
    
    public static void main(String[] args){
        Config con = new Config();
        con.bias = 0;
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
       
        NetworkTrainer trainer = new NetworkTrainer();
        //network, epochs, iterations, learningRate, momentum, accuracy, data
        trainer.Train(net, 4, 100000, 0.1, 0.1, 0.1, data, data);
        
        Debug.Log(net.Feed(new double[]{-1,-1})[0]); //0
        Debug.Log(net.Feed(new double[]{1,-1})[0]);  //1
        Debug.Log(net.Feed(new double[]{-1,1})[0]);  //1
        Debug.Log(net.Feed(new double[]{1,1})[0]);   //0
        
        //Debug.Log(net.Encode().ToJSON());
    }
    
    public MatrixNetwork(Config config){
        this.initial_config = config;
        this.sigmoid = config.sigmoidFn;
        
        this.inputs = Mathx.Clamp(config.inputs, 2, Integer.MAX_VALUE);
        this.outputs = Mathx.Clamp(config.outputs, 1, inputs - 1);
        
        this.layers = new Layer[config.hidden.length + 1]; //output layer is last layer
        for(int i = 0; i < this.layers.length; i++){
            this.layers[i] = new Layer();
        }
        
        this.bias = config.bias;
        
        int size = inputs;
        for(int i = 0; i < this.layers.length - 1; i++){
            Matrix weight = Matrix.Random(size, config.hidden[i]);
            Matrix biasW = Matrix.Row(new double[config.hidden[i]]).operate((in) -> { return 1.0; });
            this.layers[i].inWeight = weight;
            this.layers[i].inBiasWeight = biasW;
            size = config.hidden[i];
        }
        
        this.layers[this.layers.length - 1].inWeight = Matrix.Random(size, outputs);
        this.layers[this.layers.length - 1].inBiasWeight = Matrix.Row(new double[outputs]).operate((in) -> { return 1.0; });
    }
    
    @Override
    public void Randomize() {
        for(int i = 0; i < this.layers.length; i++){
            Matrix w = this.layers[i].inWeight;
            Matrix b = this.layers[i].inBiasWeight;
            
            int ins = w.GetWidth();
            float r = (float)(1.0/Math.sqrt(ins));
            w.operate((in) -> {
                return (double)Random.Range(-r, r);
            });
            b.operate((in) -> {
                return (double)Random.Range(-r, r);
            });
        }
    }
    
    @Override
    public double[] Feed(double[] inputs) {
        //Row matrix input
        Matrix X = Matrix.Row(inputs);
        
        //Repeated matrix multiplication
        for(int i = 0; i < this.layers.length; i++){
            Layer l = this.layers[i];
            l.input = X;
            Matrix net = X.mul(l.inWeight); //X * W
            l.netNobias = net;
            Matrix bias = l.inBiasWeight.operate((in) -> { return in * this.bias; });
            l.net = net.add(bias);
            
            l.output = net.operate(this.sigmoid.GetFunction());
            l.outputPrime = net.operate(this.sigmoid.GetDerivative());
            X = l.output;
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
        Matrix[] deltas = ComputeDeltas(set.in, set.out);
        
        UpdateWeights(learningRate, momentum, deltas);
    }
    
    public Matrix[] ComputeDeltas(double[] in, double[] out){
        
        Matrix[] d = new Matrix[this.layers.length];
        Matrix[] ds = new Matrix[this.layers.length];
        
        Matrix real = Matrix.Row(this.Feed(in));
        Matrix expected = Matrix.Row(out);
        
        //The output layer
        Matrix h_outT = this.layers[this.layers.length - 1].input.Transpose();
        Matrix dL = Matrix.operate(
                this.layers[this.layers.length - 1].net.operate(this.sigmoid.GetDerivative()), 
                real.sub(expected), 
                componentMul
        );
        Matrix dsWL = Matrix.mul(h_outT, dL);
        d[d.length - 1] = dL;
        ds[ds.length - 1] = dsWL;
        
        //Hidden layer
        for(int i = this.layers.length - 2; i >= 0; i--){
            Matrix dh = Matrix.operate(
                    Matrix.mul(d[i+1], this.layers[i+1].inWeight.Transpose()), 
                    this.layers[i].net.operate(this.sigmoid.GetDerivative()), 
                    componentMul
            );
            Matrix transpose = this.layers[i].input.Transpose();
            Matrix dsWh = Matrix.mul(transpose, dh);
            d[i] = dh;
            ds[i] = dsWh;
        }
        
        return ds;
    }
    
    public void UpdateWeights(double learningRate, double momentum, Matrix[] deltas){
        for(int i = 0; i < Math.min(deltas.length, this.layers.length); i++){
            //Matrix lerp = Matrix.add(this.lastWeightUpdate[i].scale(momentum), deltas[i].scale(1-momentum));
            this.layers[i].inWeight =  Matrix.add(
                    this.layers[i].inWeight,
                    deltas[i].scale(-learningRate) //lerp.scale(-learningRate)
            );
        }
        this.lastWeightUpdate = deltas;
    }
 
    public JSONobject Encode(){
        JSONobject network = new JSONobject();
        network.Add("input", new JSONitem(this.inputs));
        network.Add("output", new JSONitem(this.outputs));
        network.Add("bias", new JSONitem(this.bias));
        
        //add encoded sigmoid maybe
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(this.sigmoid);
            network.Add("sigmoid", new JSONitem(Arrays.toString(stream.toByteArray())));
        }catch(Exception e){Debug.Log(e);}
        
        JSONarray ls = new JSONarray();
        network.Add("layers", ls);
        
        for(int i = 0; i < this.layers.length; i++){
            JSONobject layer = new JSONobject();
            
            JSONobject tM = new JSONobject();
            tM.Add("width", new JSONitem(this.layers[i].inWeight.GetWidth()));
            tM.Add("height", new JSONitem(this.layers[i].inWeight.GetHeight()));
            tM.Add("data", new JSONitem(Arrays.toString(this.layers[i].inWeight.GetData())));
            
            JSONobject bM = new JSONobject();
            bM.Add("width", new JSONitem(this.layers[i].inBiasWeight.GetWidth()));
            bM.Add("height", new JSONitem(this.layers[i].inBiasWeight.GetHeight()));
            bM.Add("data", new JSONitem(Arrays.toString(this.layers[i].inBiasWeight.GetData())));
            
            layer.Add("transitionWeights", tM);
            layer.Add("biasWeights", bM);
            
            ls.Add(layer);
        }
        
        return network;
    }
    
    public static MatrixNetwork FromJSON(String json){
        JSONparser reader = new JSONparser();
        JSONobject net = (JSONobject)reader.Parse(json);
        int ins = ((Long)((JSONitem)net.Get("input")).Get()).intValue();
        int ous = ((Long)((JSONitem)net.Get("output")).Get()).intValue();
        double bias = ((Double)((JSONitem)net.Get("output")).Get());
        
        Config config = new Config();
        config.bias = bias;
        config.inputs = ins;
        config.outputs = ous;
        
        try{
            String encoding = (String)((JSONitem)net.Get("sigmoid")).Get();
            encoding = encoding.substring(1, encoding.length() - 1);
            String[] bytes = encoding.split(",");
            byte[] b = new byte[bytes.length];
            for(int i = 0; i < b.length; i++)
                b[i] = Byte.parseByte(bytes[i]);
            
            ByteArrayInputStream stream = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(stream);
            Sigmoid s = (Sigmoid)ois.readObject();
            
            config.sigmoidFn = s;
        }catch(Exception e){Debug.Log(e);}
        
        JSONarray ls = (JSONarray)net.Get("layers");
        int[] lyrs = new int[ls.Count() - 1];
        Matrix[] ws = new Matrix[ls.Count()];
        Matrix[] bs = new Matrix[ls.Count()];
        for(int i = 0; i < ls.Count(); i++){
            //Decode matrices (bias and weight)
            //Bias weight
            JSONobject o = (JSONobject)ls.Get(i);
            JSONobject biasMat = (((JSONobject)o.Get("biasWeights")));
            int cols = ((Long)((JSONitem)biasMat.Get("width")).Get()).intValue();
            int rows = ((Long)((JSONitem)biasMat.Get("height")).Get()).intValue();
            String data = ((String)((JSONitem)biasMat.Get("data")).Get());
            data = data.substring(1, data.length() - 1);
            String[] dataR = data.split(",");
            double[] dbls = new double[dataR.length];
            for(int k = 0; k < dbls.length; k++)
                dbls[k] = Double.parseDouble(dataR[k]);
            Matrix b = new Matrix(rows, cols, dbls);
            bs[i] = b;
            
            //Synapse weight
            JSONobject transMat = (((JSONobject)o.Get("transitionWeights")));
            cols = ((Long)((JSONitem)transMat.Get("width")).Get()).intValue();
            rows = ((Long)((JSONitem)transMat.Get("height")).Get()).intValue();
            data = ((String)((JSONitem)transMat.Get("data")).Get());
            data = data.substring(1, data.length() - 1);
            dataR = data.split(",");
            dbls = new double[dataR.length];
            for(int k = 0; k < dbls.length; k++)
                dbls[k] = Double.parseDouble(dataR[k]);
            Matrix w = new Matrix(rows, cols, dbls);
            ws[i] = w;
            
            if(i != ls.Count() - 1) //Ignore output layer for the hidden config 
            lyrs[i] = cols;
        }
        config.hidden = lyrs;
        
        //Sub in real values for the weight matrices
        MatrixNetwork network = new MatrixNetwork(config);
        for(int i = 0; i < network.layers.length; i++){
            network.layers[i].inWeight = ws[i];
            network.layers[i].inBiasWeight = bs[i];
        }
        
        return network;
    }
    
}
