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
public enum Face{
    Top(0), Left(1), Front(2), Right(3), Back(4), Bottom(5);
 
    private int index;
 
    Face(int i){
        index = i;
    }
 
    public int GetFaceOffset(){
        return index;
    }
}
