/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.system.functional.Func1;

/**
 *
 * @author Colin Halseth
 */
public class Sigmoid {
    
    public static final Sigmoid tanh = new Sigmoid(
            (in)->{return 1.7 * Math.tanh(0.6 * in);},
            (in)->{return 1.7 * 0.6  / Math.pow(Math.cosh(0.6 * in), 2);}
    );
    
    public static final Sigmoid atan = new Sigmoid(
            (in)->{return (2.0 * Math.PI) * Math.atan((2.0 * Math.PI * in));},
            (in)->{return 4.0 / (4.0*in*in + Math.PI*Math.PI);}
    );
    
    public static final Sigmoid exp = new Sigmoid(
            (in) -> {return 1.0 / (1 + Math.exp(-in));},
            (in) -> {return Math.exp(in) / Math.pow(Math.exp(in) + 1, 2);}
    );
    
    public static final Sigmoid abs = new Sigmoid(
            (in) -> {return in / (1 + Math.abs(in)); },
            (in) -> {return 1.0 / Math.pow(Math.abs(in) + 1, 2);}
    );
    
    public static final Sigmoid linear = new Sigmoid(
            (in) -> {return in; },
            (in) -> {return 1.0; }
    );
    
    
    private Func1<Double, Double> fn;
    private Func1<Double, Double> ddx;
    
    public Sigmoid(Func1<Double, Double> fn, Func1<Double, Double> derivative){
        this.fn = fn;
        this.ddx = derivative;
    }
    
    public Func1<Double, Double> GetFunction(){
        return fn;
    }
    
    public Func1<Double, Double> GetDerivative(){
        return ddx;
    }
    
}
