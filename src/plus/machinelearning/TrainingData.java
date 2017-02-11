/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.ArrayList;

/**
 *
 * @author Colin Halseth
 */
public class TrainingData {
    
    public static class Pair{
        double[] in;
        double[] out;
    }
    
    private ArrayList<Pair> data = new ArrayList<Pair>();
    
    public int Count(){
        return data.size();
    }
    
    public void Add(double[] in, double[] out){
        Pair p = new Pair();
        p.in = in; p.out = out;
        data.add(p);
    }
    
    public Pair Get(int i){
        return this.data.get(i);
    }
    
    public double[] GetInput(int i){
        return this.data.get(i).in;
    }
 
    public double[] GetOutput(int i){
        return this.data.get(i).out;
    }
    
    public ArrayList<Pair> Random(){
        ArrayList<Pair> rando = new ArrayList<Pair>(this.data.size());
        
        
        return rando;
    }
    
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(Pair p : data){
            for(int i = 0; i < p.in.length; i++){
                builder.append(p.in[i]);
                builder.append(",");
            }
            builder.append("|");
            for(int i = 0; i < p.out.length; i++){
                builder.append(p.out[i]);
                builder.append(",");
            }
        }
        return builder.toString();
    }
    
}
