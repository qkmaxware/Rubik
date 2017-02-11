/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.LinkedList;
import plus.JSON.JSONarray;
import plus.JSON.JSONitem;
import plus.JSON.JSONobject;
import plus.system.Debug;
import plus.system.Random;

/**
 *
 * @author Colin Halseth
 */
public class ClassicNetwork implements NeuralNetwork {

    private int inputs;
    private int outputs;
    
    private double bias;
    
    private Config initial_config;
    
    private Neuron[] inputLayer;
    private Neuron[] outputLayer;
    private Neuron[][] hiddenLayers; 
    
    public static void main(String[] args){
        Config con = new Config();
        con.bias = 0;
        con.inputs = 2;
        con.outputs = 1;
        con.hidden = new int[]{3};
        con.sigmoidFn = Sigmoid.tanh;
        
        ClassicNetwork net = new ClassicNetwork(con);
        TrainingData data = new TrainingData();
        data.Add(new double[]{-1,-1}, new double[]{-1});
        data.Add(new double[]{-1,1}, new double[]{1});
        data.Add(new double[]{1,-1}, new double[]{1});
        data.Add(new double[]{1,1}, new double[]{-1});
       
        BackPropegation trainer = new BackPropegation();
        trainer.Train(net, 1, 800, 0.1, 0.1, data, null);
        
        double[] outs = new double[]{
            net.Feed(new double[]{-1,-1})[0],
            net.Feed(new double[]{-1,1})[0],
            net.Feed(new double[]{1,-1})[0],
            net.Feed(new double[]{1,1})[0]
        };
        
        double[] round = new double[]{
            Math.round(outs[0]),
            Math.round(outs[1]),
            Math.round(outs[2]),
            Math.round(outs[3])
        };
        
        Debug.Log("real: "+outs[0] + " round: "+round[0]+" expected: "+"-1");
        Debug.Log("real: "+outs[1] + " round: "+round[1]+" expected: "+1);
        Debug.Log("real: "+outs[2] + " round: "+round[2]+" expected: "+1);
        Debug.Log("real: "+outs[3] + " round: "+round[3]+" expected: "+"-1");
        
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
    
    public ClassicNetwork(Config config){
        this.inputs = Clamp(config.inputs, Integer.MAX_VALUE, 1);
        this.outputs = Clamp(config.outputs, this.inputs, 1);
        this.bias = bias;
        this.initial_config = config;
        
        //Create input layer
        inputLayer = new Neuron[this.inputs];
        for(int i = 0; i < inputLayer.length; i++){
            inputLayer[i] = new Neuron(0);  //0 bias input neuron with no fire method
        }
        
        //Create hidden layers
        Neuron[] lastLayer = inputLayer;
        hiddenLayers = new Neuron[config.hidden.length+1][];
        for(int l = 0; l < config.hidden.length; l++){
            int size = Clamp(config.hidden[l], Integer.MAX_VALUE, 1);
            Neuron[] layer = new Neuron[size];
            for(int i = 0; i < size; i++){
                //Neuron with bias and with a sigmoid activation function + derivative
                layer[i] = new Neuron(
                        bias, 
                        config.sigmoidFn.GetFunction(),
                        config.sigmoidFn.GetDerivative()
                );
                layer[i].Connect(lastLayer);
            }
            hiddenLayers[l] = layer;
            lastLayer = layer;
        }
        
        //Create output layer
        outputLayer = new Neuron[this.outputs];
        hiddenLayers[hiddenLayers.length - 1] = outputLayer;
        for(int i = 0; i < outputLayer.length; i++){
            outputLayer[i] = new Neuron(0); //0 bias output neuron
            outputLayer[i].Connect(lastLayer);
        }
    }
    
    private int Clamp(int v, int max, int min){
        if(v < min)
            return min;
        if(v > max)
            return max;
        else 
            return v;
    }
    
    @Override
    public void Randomize(){
        for(int i = 0; i < this.hiddenLayers.length; i++){
            for(int j = 0; j < this.hiddenLayers[i].length; j++){
                for(Synapse connection : this.hiddenLayers[i][j].GetUpstream()){
                    connection.SetWeight(Random.Range(-1.0f, 1.0f));
                }
            }
        }
    }
    
    @Override
    public double[] Feed(double[] inputs) {
        //Step one feed inputs to the input neurons; (zero padded)
        for(int i = 0; i < this.inputLayer.length; i++){
            this.inputLayer[i].SetValue((i < inputs.length)?inputs[i]:0.0);
        }
        
        //Step two go through each neuron hiddenlayer fireing as we go
        for(int i = 0; i < this.hiddenLayers.length; i++){
            for(int j = 0; j < this.hiddenLayers[i].length; j++){
                Neuron n = this.hiddenLayers[i][j];
                n.Fire();
            }
        }
        
        //Fire output and extract it (no sigmoid function)
        double[] outputs = new double[this.outputLayer.length];
        for(int i = 0; i < this.outputLayer.length; i++){
            outputs[i] = this.outputLayer[i].GetValue();
        }
        
        return outputs;
    }
    
    @Override
    public void Learn(double learningRate, TrainingData.Pair set){
            double[] in = set.in;
            
            double[] test = this.Feed(in);
            double[] real = set.out;
         
            //For each neuron in the output layer
            for(int k = 0; k < this.outputLayer.length; k++){
                Neuron n = this.outputLayer[k];
                double neuron_error = (n.GetDerivative() * (real[k] - test[k]));
                
                n.SetError(neuron_error);   //Set the error for this node
                
                //Set the deltas for each synapse
                for(Synapse connection : n.GetUpstream()){
                    connection.SetDelta(learningRate * n.GetError() * connection.GetSource().GetValue());
                }
            }
            
            //For each neuron in the hidden layer
            for(int l = this.hiddenLayers.length - 2; l >= 0; l--){
                Neuron[] layer = this.hiddenLayers[l];
                
                for(int k = 0; k < layer.length; k++){
                    Neuron n = layer[k];
                    
                    double error = n.GetDerivative();
                    double sum = 0;
                    
                    for(Synapse connection : n.GetDownstream()){
                        sum += connection.GetWeight() * connection.GetTarget().GetError();
                    }
                    
                    error *= sum;
                    n.SetError(error);
                    
                    for(Synapse connection : n.GetUpstream()){
                        connection.SetDelta(learningRate * connection.GetSource().GetValue() * n.GetError());
                    }
                }
            }
            
            //Set the layer weights
            for(int j = this.hiddenLayers.length - 1; j >= 0; j--){
                Neuron[] layer = this.hiddenLayers[j];
                
                for(int k = 0; k < layer.length; k++){
                    Neuron n = layer[k];
                    
                    for(Synapse connection : n.GetUpstream()){
                        connection.UpdateWeight();
                    }
                }
            }
    }
    
    public static ClassicNetwork FromJSON(JSONobject obj){
        //Initial Configs
        int inputs = (int)(((JSONitem)obj.Get("inputs")).Get());
        int outputs = (int)(((JSONitem)obj.Get("outputs")).Get());
        double bias = (double)(((JSONitem)obj.Get("bias")).Get());
        int[] hidden = new int[(int)(((JSONitem)obj.Get("hidden")).Get())];
        
        //Get Layer Data From JSON
        JSONarray layers = (JSONarray)obj.Get("layers");
        for(int i = 0; i < layers.Count(); i++){
            JSONobject layer = (JSONobject)layers.Get(i);
            int size = (int)(((JSONitem)layer.Get("neurons")).Get());
            hidden[i] = size;
        }
        
        //Create the network
        Config con = new Config();
        con.bias = bias;
        con.inputs = inputs;
        con.outputs = outputs;
        con.hidden = hidden;
        con.sigmoidFn = Sigmoid.tanh;
        
        ClassicNetwork network = new ClassicNetwork(con);
             
        //Assign layer weights
        for(int i = 0; i < layers.Count(); i++){
            JSONobject layer = (JSONobject)layers.Get(i);
            int size = (int)(((JSONitem)layer.Get("neurons")).Get());
            for(int k = 0; k < size; k++){
                JSONarray weights = (JSONarray)layer.Get("weights"+i);
                Neuron n = network.hiddenLayers[i][k];
                for(int w = 0; w < weights.Count(); w++){
                    n.SetWeight(w, (double)(((JSONitem)weights.Get(w)).Get()));
                }
            }
        }
        
        return network;
    }
    
    public JSONobject ToJSON(){
        JSONobject obj = new JSONobject();
        
        obj.Add("inputs", new JSONitem(this.inputs));
        obj.Add("outputs", new JSONitem(this.outputs));
        obj.Add("hidden", new JSONitem(this.hiddenLayers.length-1));
        obj.Add("bias", new JSONitem(this.bias));
        
        JSONarray weights = new JSONarray();
        obj.Add("layers", weights);
        
        for(int i = 0; i < this.hiddenLayers.length; i++){
            JSONobject layer = new JSONobject();
            layer.Add("neurons", new JSONitem(this.hiddenLayers[i].length));
            for(int n = 0; n < this.hiddenLayers[i].length; n++){
                JSONarray w = new JSONarray();
                for(Synapse connection : this.hiddenLayers[i][n].GetUpstream()){
                    w.Add(new JSONitem(connection.GetWeight()));
                }
                layer.Add("weights"+i, w);
            }
            weights.Add(layer);
        }
        
        
        return obj;
    }
    
    public String toString(){
        String str = "";
        str += "\nInputs: "+ this.inputLayer.length+"\n0\n";
        String depthPrefab = " --- ";
        String depth = depthPrefab;
        for(int l = 0; l < this.hiddenLayers.length; l++){
            for(int k = 0; k < this.hiddenLayers[l].length; k++){
                str += depth+"("+this.hiddenLayers[l][k].Connections()+" )";
                str+="\n";
            }
            depth += depthPrefab;
        }
        return str;
    }
    
}
