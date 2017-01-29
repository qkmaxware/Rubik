/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import encoder.IEncodeable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import plus.system.Debug;

/**
 *
 * @author Colin
 */
public class BinaryEncoder {
    
    public static String Encode(IEncodeable encodeable){
        try{
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(encodeable);
            oo.flush();
            
            return bo.toString("ISO-8859-1");
        }catch(Exception e){
            Debug.Log(e);
            return null;
        }
    }
    
    
    public static <T> T Decode(String string){
        try{
            byte[] bytes = string.getBytes("ISO-8859-1");
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (T)oi.readObject();
        }
        catch(Exception e){
            Debug.Log(e);
            return null;
        }
    }
    
}
