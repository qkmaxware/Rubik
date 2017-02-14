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
    
    /**
     * Create a neuron with a specific bias, but a linear activation function
     * @param bias 
     */
    public Neuron(double bias){
        this.bias = bias;
        this.activationFn = (in) -> {return in;};
        this.derivativeFn = (in) -> {return 1.0;};
    }
    
    /**
     * Create a neuron with a specific bias and a specific activation function
     * @param bias
     * @param activation
     * @param derivative 
     */
    public Neuron(double bias, Func1<Double, Double> activation, Func1<Double, Double> derivative){
        this.bias = bias;
        this.activationFn = activation;
        this.derivativeFn = derivative;
    }
    
    /**
     * Create synapses to connect this neuron to another group
     * @param ns 
     */
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
    
    /**
     * How many neurons is this one connected to
     * @return 
     */
    public int Connections(){
        return this.inbound.size();
    }
    
    /**
     * Get the assigned weight of the synapse to this neuron from another
     * @param n
     * @return 
     */
    public double GetWeight(Neuron n){
        for(Synapse conn : this.inbound){
            if(conn.GetSource() == n){
                return conn.GetWeight();
            }
        }
        return -1;
    }
    
    /**
     * Get the assigned weight of the 'i'th synapse
     * @param i
     * @return 
     */
    public double GetWeight(int i){
        return this.inbound.get(i).GetWeight();
    }
    
    /**
     * Set the weight of the synapse to this neuron from another 
     * @param n
     * @param d 
     */
    public void SetWeight(Neuron n, double d){
        for(Synapse conn : this.inbound){
            if(conn.GetSource() == n){
                conn.SetWeight(d);
                break;
            }
        }
    }
    
    /**
     * Set the weight of the 'i'th synapse
     * @param i
     * @param d 
     */
    public void SetWeight(int i, double d){
        this.inbound.get(i).SetWeight(d);
    }
    
    /**
     * Get all synapse connecting this neuron to the previous layer
     * @return 
     */
    public ArrayList<Synapse> GetUpstream(){
        return this.inbound;
    }
    
    /**
     * Get all synapse connecting this neuron to the next layer
     * @return 
     */
    public ArrayList<Synapse> GetDownstream(){
        return this.outbound;
    }
    
    /**
     * Get the last computed error of this neuron
     * @return 
     */
    public double GetError(){
        return this.error;
    }
    
    /**
     * Set the last computed error of this neuron
     * @param error 
     */
    public void SetError(double error){
        this.error = error;
    }
    
    /**
     * Does this neuron have any input synapses
     * @return 
     */
    public boolean HasInputs(){
        return this.inbound.size() > 0;
    }
    
    /**
     * Does this neuron have any output synapses
     * @return 
     */
    public boolean HasOutputs(){
        return this.outbound.size() > 0;
    }
    
    /**
     * Get the source neuron from upstream connection 'i'
     * @param i
     * @return 
     */
    public Neuron GetSource(int i){
        return this.inbound.get(i).GetSource();
    }
    
    /**
     * Get the target neuron from downstream connection 'i'
     * @param i
     * @return 
     */
    public Neuron GetTarget(int i){
        return this.outbound.get(i).GetTarget();
    }
    
    /**
     * Get the net value from firing this neuron before the activation function is applied
     * @return 
     */
    public double GetInput(){
        return this.inValue;
    }
    
    /**
     * Get the value from firing this neuron after the activation function is applied
     * @return 
     */
    public double GetValue(){
        return this.outValue;
    }
    
    /**
     * Set the value of this neuron (usually only important for the input neurons)
     * @param value 
     */
    public void SetValue(double value){
        this.outValue = value;
    }
    
    /**
     * Compute the derivative of the activation function at X = net
     * @return 
     */
    public double GetDerivative(){
        return this.derivativeFn.Invoke(this.inValue);
    }
    
    /**
     * Cause the neuron to fire which sums all the values from inbound connections and then applies the activation function
     */
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
