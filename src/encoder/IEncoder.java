/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoder;

/**
 *
 * @author Colin
 */
public interface IEncoder {
 
    public String Encode(IEncodeable encodable, int maxBytes);
    
}
