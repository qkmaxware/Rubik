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
    private double delta;
    
    public Synapse(Neuron source, Neuron target){
        this.source = source;
        this.target = target;
        weight = Random.Range(-1.0f, 1.0f);
    }
    
    public double GetDelta(){
        return this.delta;
    }
    
    public void SetDelta(double delta){
        this.delta = delta;
    }
    
    public void UpdateWeight(){
        this.SetWeight(this.GetWeight() + this.GetDelta());
        this.delta = 0;
    }
    
    public Neuron GetSource(){
        return this.source;
    }
    
    public Neuron GetTarget(){
        return this.target;
    }
    
    public double GetWeight(){
        return this.weight;
    }
    
    public void SetWeight(double weight){
        this.weight = weight;
    }
    
}
