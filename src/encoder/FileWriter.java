/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import plus.system.Debug;

/**
 *
 * @author Colin
 */
public class FileWriter {
    
    PrintWriter writer;
    
    public FileWriter(String filename, String type){
        filename = GetUniqueFileName(filename, type);
        try{
            writer = new PrintWriter(filename, "UTF-8");
        }catch(IOException e){
            Debug.Log(e);
        }
    }
    
    /**
     * Gets a unique file name from a base name 
     * @param name
     * @return 
     */
    public static String GetUniqueFileName(String name, String type){
        File f = new File(name);
        String uniquename = name+type;
        int unique = 1;
        while(f.exists()){
            uniquename = name+"("+(unique++)+")"+type;
            f = new File(uniquename);
        }
        return uniquename;
    }
    
    private static String[] Strip(String name){
        int pos = name.lastIndexOf(".");
        if(pos == -1)
            return new String[]{name,""};
        
        return new String[]{name.substring(0, pos), name.substring(pos)};
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
