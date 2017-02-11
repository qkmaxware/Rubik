/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.ArrayList;
import plus.system.functional.Func1;

/**
 *
 * @author Colin Halseth
 */
public class Neuron {
    
    private Func1<Double, Double> activationFn;
    private Func1<Double, Double> derivativeFn;
    
    private ArrayList<Synapse> inbound;
    private ArrayList<Synapse> outbound = new ArrayList<Synapse>();
    
    private double inValue;
    private double outValue;
    private double error;
    
    private double biasweight;
    private double bias;
    
    public Neuron(double bias){
        this.bias = bias;
        this.activationFn = (in) -> {return in;};
        this.derivativeFn = (in) -> {return 1.0;};
    }
    
    public Neuron(double bias, Func1<Double, Double> activation, Func1<Double, Double> derivative){
        this.bias = bias;
        this.activationFn = activation;
        this.derivativeFn = derivative;
    }
    
    public void Connect(Neuron... ns){
        if(this.inbound != null){
            for(Synapse connection : this.inbound)
                connection.GetSource().outbound.remove(this);
        }
        
        this.inbound = new ArrayList<Synapse>();
        for(int i = 0; i < ns.length; i++){
            Synapse connection = new Synapse(ns[i], this);
            this.inbound.add(connection);
            ns[i].outbound.add(connection);
        }
    }
    
    public int Connections(){
        return this.inbound.size();
    }
    
    public double GetWeight(Neuron n){
        for(Synapse conn : this.inbound){
            if(conn.GetSource() == n){
                return conn.GetWeight();
            }
        }
        return -1;
    }
    
    public double GetWeight(int i){
        return this.inbound.get(i).GetWeight();
    }
    
    public void SetWeight(Neuron n, double d){
        for(Synapse conn : this.inbound){
            if(conn.GetSource() == n){
                conn.SetWeight(d);
                break;
            }
        }
    }
    
    
    public void SetWeight(int i, double d){
        this.inbound.get(i).SetWeight(d);
    }
    
    public ArrayList<Synapse> GetUpstream(){
        return this.inbound;
    }
    
    public ArrayList<Synapse> GetDownstream(){
        return this.outbound;
    }
    
    public double GetError(){
        return this.error;
    }
    
    public void SetError(double error){
        this.error = error;
    }
    
    public boolean HasInputs(){
        return this.inbound.size() > 0;
    }
    
    public boolean HasOutputs(){
        return this.outbound.size() > 0;
    }
    
    public Neuron GetSource(int i){
        return this.inbound.get(i).GetSource();
    }
    
    public Neuron GetTarget(int i){
        return this.outbound.get(i).GetTarget();
    }
    
    public double GetInput(){
        return this.inValue;
    }
    
    public double GetValue(){
        return this.outValue;
    }
    
    public void SetValue(double value){
        this.outValue = value;
    }
    
    public double GetDerivative(){
        return this.derivativeFn.Invoke(this.inValue);
    }
    
    public void Fire(){
        if(!this.HasInputs())
            return;
        
        double net = 0;
        for(Synapse connection : this.inbound){
            net += connection.GetWeight() * connection.GetSource().GetValue();
        }
        
        this.inValue = net + this.bias;
        this.outValue = this.activationFn.Invoke(this.inValue);
    }
    
}
