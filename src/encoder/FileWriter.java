/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoder;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Colin
 */
public class FileWriter {
    
    PrintWriter writer;
    
    public FileWriter(String filename){
        try{
            writer = new PrintWriter(filename, "UTF-8");
        }catch(IOException e){
            
        }
    }
    
    public void Write(String text){
        writer.print(text);
    }
    
    public void WriteLn(String line){
        writer.println(line);
    }
    
    public void Save(){
        writer.flush();
        writer.close();
    }
    
}
