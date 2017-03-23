/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import plus.system.functional.Action2;

/**
 *
 * @author Colin
 */
public class TrainingData {
 
    private class Pair{
        double[] in;
        double[] out;
        String hash;
    }
    
    private HashSet<String> contains = new HashSet<String>();
    private LinkedList<Pair> set = new LinkedList<Pair>();
    
    public boolean Add(double[] in, double[] out){
        Pair p = new Pair();
        p.in = in;
        p.out = out;
        p.hash = Arrays.toString(in);
        
        if(!contains.contains(p.hash)){
            set.add(p);
            contains.add(p.hash);
            return true;
        }
        return false;
    }
    
    public void Clear(){
        contains.clear();
        set.clear();
    }
    
    public double[][] GetInputs(){
        double[][] o = new double[set.size()][];
        int i = 0;
        for(Pair p : set){
            o[i] = p.in;
            i++;
        }
        return o;
    }
    
    public double[][] GetOutputs(){
        double[][] o = new double[set.size()][];
        int i = 0;
        for(Pair p : set){
            o[i] = p.out;
            i++;
        }
        return o;
    }
    
}
