/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

import plus.system.functional.Func1;

/**
 *
 * @author Colin
 */
public class ActivationFunction {
 
    public static final ActivationFunction tanh = new ActivationFunction(
        (x) -> { return 1.7 * Math.tanh(0.6*x); },
        (x) -> { return 4.08 * (Math.cosh(0.6*x) * Math.cosh(0.6*x)) / ((Math.cosh(1.2 * x) + 1) * (Math.cosh(1.2 * x) + 1)); }
    );
    
    public static final ActivationFunction exp = new ActivationFunction(
            (in) -> {return 1.0 / (1 + Math.exp(-in));},
            (in) -> {return Math.exp(in) / Math.pow(Math.exp(in) + 1, 2);}
    );
    
    public static final ActivationFunction linear = new ActivationFunction(
            (in) -> {return in; },
            (in) -> {return 1.0; }
    );
    
    private Func1<Double,Double> function;
    private Func1<Double,Double> derivative;
    
    public ActivationFunction(Func1<Double,Double> fn, Func1<Double, Double> derivative){
        this.function = fn;
        this.derivative = derivative;
    }
    
    public double ApplyFunction(double in){
        return function.Invoke(in);
    }
    
    public double ApplyDerivative(double in){
        return derivative.Invoke(in);
    }
    
}
