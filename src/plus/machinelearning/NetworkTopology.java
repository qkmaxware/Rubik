/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.machinelearning;

/**
 *
 * @author Colin
 */
public class NetworkTopology {
    
    public static final NetworkTopology nt221 = NetworkTopology.Construct(2,1,2);
    public static final NetworkTopology nt231 = NetworkTopology.Construct(2,1,3);
    
    private int inputSize;
    private int outputSize;
    private int layerCount;
    private int[] allLayerSizes;
    
    private NetworkTopology(int inS, int outS, int[] lyrs){
        inputSize = inS;
        outputSize = outS;
        int[] layers = new int[lyrs.length + 1];
        for(int i = 0; i < lyrs.length; i++){
            layers[i] = lyrs[i];
        }
        layers[layers.length - 1] = outS;
        this.allLayerSizes = layers;
        layerCount = lyrs.length + 2;
    };
    
    public int[] GetLayerSizes(){
        return allLayerSizes.clone();
    }
    
    public int CountAll(){
        return layerCount;
    }
    
    public int CountHidden(){
        return layerCount - 2;
    }
    
    public int GetInputSize(){
        return inputSize;
    }
    
    public int GetOutputSize(){
        return outputSize;
    }
    
    public int GetHiddenSize(int layer){
        return this.allLayerSizes[layer];
    }
    
    public static NetworkTopology Construct(int inputs, int outputs, int... layers){
        int i = Math.max(1, inputs);
        int o = Math.max (1, outputs);
        
        NetworkTopology topology = new NetworkTopology(i,o,layers);
        
        return topology;
    }   
    
}
