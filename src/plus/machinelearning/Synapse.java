/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.system.Random;

/**
 *
 * @author Colin Halseth
 */
public class Synapse {
    
    private final Neuron source;
    private final Neuron target;
    private double weight;
    private double lastDelta = 0;
    private double delta;
    
    public Synapse(Neuron source, Neuron target){
        this.source = source;
        this.target = target;
        weight = Random.Range(-1.0f, 1.0f);
    }
    
    /**
     * Get the weight update value
     * @return 
     */
    public double GetDelta(){
        return this.delta;
    }
    
    /**
     * Set the weight update value
     * @param delta 
     */
    public void SetDelta(double delta){
        this.lastDelta = delta;
        this.delta = delta;
    }
    
    /**
     * Update the weight value based on the set-delta weight and the momentum
     * @param momentum 
     */
    public void UpdateWeight(double momentum){
        //TODO momentum
        //W = Wm + (1-a)*delta + a*oldDeltaSetDelta
        double W = this.GetWeight() + (1-momentum)*this.GetDelta() + momentum*this.lastDelta;
        this.SetWeight(W);
    }
    
    /**
     * Get the source (start) neuron of this connection
     * @return 
     */
    public Neuron GetSource(){
        return this.source;
    }
    
    /**
     * Get the target (end) neuron of this connection
     * @return 
     */
    public Neuron GetTarget(){
        return this.target;
    }
    
    /**
     * Get the assigned weight of this connection
     * @return 
     */
    public double GetWeight(){
        return this.weight;
    }
    
    /**
     * Set the desired weight of this connection
     * @param weight 
     */
    public void SetWeight(double weight){
        this.weight = weight;
    }
    
}
