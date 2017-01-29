/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

import encoder.IEncodeable;

/**
 *
 * @author Colin
 */
public class Spin implements IEncodeable{
 
    public static enum Mode{
        Row, Column, Slice
    } 
    
    public static enum Direction{
        CounterClockwise, Clockwise
    }
    
    
    private int layerDepth;
    private Mode mode;
    private Direction direction;
    
    public Spin(int layer, Mode mode, Direction direction){
        this.layerDepth = layer;
        this.mode = mode;
        this.direction = direction;
    }
    
    /**
     * Create a new spin that will undo this spin
     * @return 
     */
    public Spin Invert(){
        Spin s = new Spin(this.layerDepth, this.mode, (this.direction == Spin.Direction.Clockwise)?Spin.Direction.CounterClockwise: Spin.Direction.Clockwise);
        return s;
    }
    
    /**
     * Get the encodeable properties of this spin
     * @return 
     */
    @Override
    public int[] GetProperties(){
        int[] args = new int[]{
            this.layerDepth,
            this.mode.ordinal(),
            this.direction.ordinal()
        };
        return args;        
    }
    
    /**
     * The spin mode 
     * @return 
     */
    public Mode GetMode(){
        return this.mode;
    }
    
    /**
     * The spin direction
     * @return 
     */
    public Direction GetDirection(){
        return this.direction;
    }
    
    /**
     * The layer id
     * @return 
     */
    public int GetLayer(){
        return this.layerDepth;
    }
    
    @Override
    public String toString(){
        return this.mode.toString() + ": "+this.layerDepth+" - "+this.direction.toString();
    }
}
