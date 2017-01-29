/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoder;

import java.util.Arrays;

/**
 *
 * @author Colin
 */
public class OnesEncoder implements IEncoder{
    
    @Override
    public String Encode(IEncodeable encodeable, int maxBytes) {
        int[] properties = encodeable.GetProperties();
        
        String encoding = "";
        int j = 0;
        for(int property : properties){
            byte[] bytes = new byte[maxBytes];
            Arrays.fill(bytes, 0, maxBytes, (byte)-1);
            bytes[Clamp((property), 0, maxBytes-1)] = (byte)1;
            String me = "";
            for(int i = 0; i < bytes.length; i++){
                me += (i != 0 ? "," : "")+bytes[i];
            }
            encoding += (j != 0 ? "," : "") + me;
            j++;
        }
    
        return encoding;
    }
    
    private int Clamp(int v, int min, int max){
        if(v > max)
            return max;
        if(v < min)
            return min;
        return v;
    }
    
}
