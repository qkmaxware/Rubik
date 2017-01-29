/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

/**
 *
 * @author Colin
 */
public class Property<T> {
    
    private T value;
    
    public Property(T value){
        this.value = value;
    }
    
    public T get(){
        return this.value;
    }
    
    public void set(T value){
        this.value = value;
    }
    
}
