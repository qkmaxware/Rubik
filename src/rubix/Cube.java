/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

import search.ISearchable;
import encoder.IEncodeable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import plus.system.*;
import plus.system.functional.Action3;

/**
 *
 * @author Colin
 */
public class Cube implements IEncodeable, ISearchable{
    
    private int length;
    
    //LAYOUT OF CUBIC FACES
    //          TOP0
    //  LEFT    FRONT    RIGHT     BACK
    //          BOTTOM
    
    private Color[] colors;
    private int faceOffset;
    
    private Spin lastSpin;
    
    public Cube(int length){
        this.length = length;
        
        this.colors = new Color[6 * length * length];
        faceOffset = length * length;
        
        this.Fill(Face.Left, Color.Green);
        this.Fill(Face.Front, Color.Red);
        this.Fill(Face.Top, Color.White);
        this.Fill(Face.Bottom, Color.Yellow);
        this.Fill(Face.Right, Color.Blue);
        this.Fill(Face.Back, Color.Purple);
        
    }

    public Cube(Cube other){
        this.length = other.length;
        
        
        this.colors = other.colors.clone();
        this.faceOffset = other.faceOffset;
    }
    
    public int Length(){
        return this.length;
    }
    
    public int Height(){
        return this.length;
    }
    
    public int Depth(){
        return this.length;
    }
    
    
    /**
     * Convert a face coordinate pair to a 1D index
     * @param face
     * @param x
     * @param y
     * @return 
     */
    private int GetIndex(Face face, int x, int y){
        return face.GetFaceOffset() * faceOffset + y*length + x;
    }  

     /**
     * Convert a face coordinate pair to a 1D index
     * @param face
     * @param x
     * @param y
     * @return 
     */
    private int GetIndex(Face face, int i){
        return face.GetFaceOffset() * faceOffset + i;
    }  
    
    /**
     * Set a color to a particular spot
     * @param face
     * @param x
     * @param y
     * @param c 
     */
    public void Set(Face face, int x, int y, Color c){
        this.colors[GetIndex(face,x,y)] = c;
    }
    
    /**
     * Get the color of a spot on a face
     * @param face
     * @param x
     * @param y
     * @return 
     */
    public Color Get(Face face, int x, int y){
        return this.colors[GetIndex(face, x, y)];
    }
    
    /**
     * Fill a face with a particular color
     * @param face
     * @param color 
     */
    public void Fill(Face face, Color color){
        for(int i = 0; i < this.length * this.length; i++){
            this.colors[face.GetFaceOffset() * this.faceOffset + i] = color;
        }
    }
    
    /**
     * Get a column of colors
     * @param x
     * @return 
     */
    public Color[] GetColumn(Face face, int x){
        Color[] col = new Color[length];
        for(int y = 0; y < length; y++){
            col[y] = this.colors[GetIndex(face, x, y)];
        }
        return col;
    }
    
    /**
     * Set a column of colors from an array
     * @param x
     * @param col 
     */
    public void SetColumn(Face face, int x, Color[] col){
        for(int y = 0; y < Math.min(col.length, length); y++){
            this.colors[GetIndex(face, x,y)] = col[y];
        }
    }
    
     /**
     * Get a row of colors
     * @param y
     * @return 
     */
    public Color[] GetRow(Face face, int y){
        Color[] row = new Color[length];
        for(int x = 0; x < length; x++){
            row[x] = this.colors[GetIndex(face, x, y)];
        }
        return row;
    }
    
    /**
     * Set a row of colors from an array
     * @param y
     * @param row 
     */
    public void SetRow(Face face, int y, Color[] row){
        for(int x = 0; x < Math.min(row.length, length); x++){
            this.colors[GetIndex(face, x,y)] = row[x];
        }
    }
    
    /**
     * Rotate a face clockwise or counter clockwise
     * @param face
     * @param direction 
     */
    public void Rotate(Face face, Spin.Direction direction){
        
        //TODO optimize this
        Color[] newColor = this.colors.clone();
        
        //OTHER OPTIONS TRANSPOSE -> REVERSE ROWS (+90) 
        //TRANSPOSE -> REVERSE COLUMNS (-90);
        
        //TRANSPOSE -> x == y
        
        switch(direction){
            case CounterClockwise:
                //Y = ROW, X = COLUMN
                for(int x = 0; x < length; x++){
                    for(int y = 0; y < length; y++){
                        //TRANSPOSE x -> y, then reverse row
                        newColor[GetIndex(face, y,length - 1 - x)]
                                =
                        this.colors[GetIndex(face, x,y)];
                    }
                }
                
                break;
            case Clockwise:
                //Y = ROW, X = COLUMN
                for(int x = 0; x < length; x++){
                    for(int y = 0; y < length; y++){
                        //TRANSPOSE x -> y, then reverse column
                        newColor[GetIndex(face, length - 1 - y,x)]
                                =
                        this.colors[GetIndex(face, x,y)];
                    }
                }
                
                break; 
        }
        
        
        this.colors = newColor;
    }
    
    /**
     * Iterate through all values on a particular face
     * @param face
     * @param fn 
     */
    public void ForEach(Face face, Action3<Integer,Integer, Color> fn){
        for(int x = 0; x < length; x++){
            for(int y = 0; y < length; y++){
                fn.Invoke(x, y, this.colors[GetIndex(face, x, y)]);
            }
        }
    }
    
    /**
     * Test if each side of the cube is composed of the same color
     * @return 
     */
    public boolean IsSolved(){
        final Property<Boolean> stop = new Property<Boolean>(false);
        final Property<Color> next = new Property<Color>(Color.Green);
        Action3<Integer, Integer, Color> fn = (x,y,color) -> {
            if(color != next.get())
                stop.set(false);
        };
        
        next.set(Color.Green);
        ForEach(Face.Left, fn);
        if(stop.get())
            return false;
        next.set(Color.Blue);
        ForEach(Face.Right, fn);
        if(stop.get())
            return false;
        next.set(Color.Red);
        ForEach(Face.Front, fn);
        if(stop.get())
            return false;
        next.set(Color.Purple);
        ForEach(Face.Back, fn);
        if(stop.get())
            return false;
        next.set(Color.White);
        ForEach(Face.Top, fn);
        if(stop.get())
            return false;
        next.set(Color.Yellow);
        ForEach(Face.Bottom, fn);
        if(stop.get())
            return false;
        
        return true;
    }
    
    
    
    /**
     * Get the properties of this rubix cube for encoding
     * @return 
     */
    public int[] GetProperties(){
        int[] args = new int[6*length*length];

        for(int i = 0; i < this.colors.length; i++)
            args[i] = this.colors[i].ordinal();
        
        return args;
    }
    
    /**
     * Apply a random number of perturbations to the cube
     * @param Perturbations 
     */
    public void Perturb(int Perturbations){
        for(int i = 0; i < Perturbations; i++){
            Spin.Mode mode = Spin.Mode.Row;
            switch(Random.Range(0,2)){
                case 0:
                    mode = Spin.Mode.Row;
                    break;
                case 1: 
                    mode = Spin.Mode.Column;
                    break;
                case 2:
                    mode = Spin.Mode.Slice;
                    break;
            }
            
            Spin.Direction direction = Spin.Direction.Clockwise;
            switch(Random.Range(0,1)){
                case 0:
                    direction = Spin.Direction.Clockwise;
                    break;
                case 1:
                    direction = Spin.Direction.CounterClockwise;
                    break;
            }
            
            Spin spin = new Spin(Random.Range(0,length-1), mode, direction);
            Perturb(spin);
        }
    }
    
    public Spin LastSpin(){
        return this.lastSpin;
    }
    
    /**
     * Get a list of new rubix cubes for each possible move starting with this cube
     * @return 
     */
    @Override
    public ISearchable[] GetNext(){
        ISearchable[] next = new ISearchable[this.length * 6];
        int i = 0;
        for(int x = 0; x < this.length; x++){
            rubix.Cube cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Column, Spin.Direction.Clockwise));
            next[i++] = cube;
            cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Column, Spin.Direction.CounterClockwise));
            next[i++] = cube;
            
            cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Row, Spin.Direction.Clockwise));
            next[i++] = cube;
            cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Row, Spin.Direction.CounterClockwise));
            next[i++] = cube;
            
            cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Slice, Spin.Direction.Clockwise));
            next[i++] = cube;
            cube = new rubix.Cube(this);
            cube.Perturb(new Spin(x, Spin.Mode.Slice, Spin.Direction.CounterClockwise));
            next[i++] = cube;
        }
        
        return next;
    }
    
    /**
     * Spin a part of the cube clockwise or counter clockwise
     * @param spin 
     */
    public void Perturb(Spin spin){
        this.lastSpin = spin;
        
        switch(spin.GetMode()){
            case Row:
                //Trivial
                //Left -> Front -> Right -> Back, TOP/BOTTOM rotate 
                Color[] l = GetRow(Face.Left, spin.GetLayer());
                Color[] r = GetRow(Face.Right, spin.GetLayer());
                Color[] f = GetRow(Face.Front, spin.GetLayer());
                Color[] b = GetRow(Face.Back, spin.GetLayer());
                
                if(spin.GetDirection() == Spin.Direction.Clockwise){
                    //Clockwise is to the LEFT
                    SetRow(Face.Back, spin.GetLayer(), l);
                    SetRow(Face.Left, spin.GetLayer(), f);
                    SetRow(Face.Front, spin.GetLayer(), r);
                    SetRow(Face.Right, spin.GetLayer(), b);
                    //Deal with caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Top, Spin.Direction.Clockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Bottom, Spin.Direction.CounterClockwise);
                }else{
                    //CC is to the right
                    SetRow(Face.Back, spin.GetLayer(), r);
                    SetRow(Face.Left, spin.GetLayer(), b);
                    SetRow(Face.Front, spin.GetLayer(), l);
                    SetRow(Face.Right, spin.GetLayer(), f);
                    //Deal with the caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Top, Spin.Direction.CounterClockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Bottom, Spin.Direction.Clockwise);
                }
                
                break;
            case Column:
                //Trivial
                //Front -> Top -> Back -> Bottom
                
                //TODO
                f = GetColumn(Face.Front, spin.GetLayer());
                Color[] t = GetColumn(Face.Top, spin.GetLayer());
                Color[] bk = GetColumn(Face.Back, spin.GetLayer());
                Color[] bt = GetColumn(Face.Bottom, spin.GetLayer());
                
                if(spin.GetDirection() == Spin.Direction.Clockwise){
                    //Going to the right
                    SetColumn(Face.Front, spin.GetLayer(), bt);
                    SetColumn(Face.Top, spin.GetLayer(), f);
                    SetColumn(Face.Back, spin.GetLayer(), Reverse(t));
                    SetColumn(Face.Bottom, spin.GetLayer(), Reverse(bk));
                    //Do caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Left, Spin.Direction.CounterClockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Right, Spin.Direction.Clockwise);
                    
                }else{
                    //Going to the left
                    SetColumn(Face.Front, spin.GetLayer(), t);
                    SetColumn(Face.Top, spin.GetLayer(), Reverse(bk));
                    SetColumn(Face.Back, spin.GetLayer(), Reverse(bt));
                    SetColumn(Face.Bottom, spin.GetLayer(), f);
                    //Do caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Left, Spin.Direction.Clockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Right, Spin.Direction.CounterClockwise);
                }
                
                break;
            case Slice:
                //Non-Trivial
                //ROTATE VALUES 90 DEGREES (clock or anti-clock)
                //Left -> Top -> Right -> Bottom
                l = GetColumn(Face.Left, spin.GetLayer());
                t = GetRow(Face.Top, spin.GetLayer());
                r = GetColumn(Face.Right, length - 1 - spin.GetLayer());
                b = GetRow(Face.Bottom, length - 1 - spin.GetLayer());
                
                if(spin.GetDirection() == Spin.Direction.Clockwise){
                    //Go right
                    SetRow(Face.Top, spin.GetLayer(), Reverse(l));
                    SetColumn(Face.Right, length - 1 - spin.GetLayer(), t);
                    SetRow(Face.Bottom, length - 1 - spin.GetLayer(), Reverse(r));
                    SetColumn(Face.Left, spin.GetLayer(), b);
                    //Do caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Back, Spin.Direction.CounterClockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Front, Spin.Direction.Clockwise);
                }else{
                    //Go left
                    SetRow(Face.Top, spin.GetLayer(), r);
                    SetColumn(Face.Right, length - 1 - spin.GetLayer(), Reverse(b));
                    SetRow(Face.Bottom, length - 1 - spin.GetLayer(), l);
                    SetColumn(Face.Left, spin.GetLayer(), Reverse(t));
                    //Do caps
                    if(spin.GetLayer() == 0)
                        Rotate(Face.Back, Spin.Direction.Clockwise);
                    if(spin.GetLayer() == length - 1)
                        Rotate(Face.Front, Spin.Direction.CounterClockwise);
                }
                break;
        }
    }
    
    protected Color[] Reverse(Color[] array){
        List<Color> colors = java.util.Arrays.asList(array);
        Collections.reverse(colors);
        return (Color[])colors.toArray();
    }
    
    @Override
    public boolean Equivalent(ISearchable other){
        return equals(other);
    }
    
    /**
     * Get the distance from this node to the end result
     * @return 
     */
    @Override
    public int GetHeuristic(){
        int d1 = 0;
        
        for(int i = 0; i < this.faceOffset; i++){
           d1 += GetDistance(this.colors[GetIndex(Face.Front, i)],  Face.Front);
           d1 += GetDistance(this.colors[GetIndex(Face.Back, i)],   Face.Back);
           d1 += GetDistance(this.colors[GetIndex(Face.Left, i)],   Face.Left);
           d1 += GetDistance(this.colors[GetIndex(Face.Right, i)],  Face.Right);
           d1 += GetDistance(this.colors[GetIndex(Face.Top, i)],    Face.Top);
           d1 += GetDistance(this.colors[GetIndex(Face.Bottom, i)], Face.Bottom);
        }
        
        return d1;
    }
    
    /**
     * Get the distance from one one searchable to another
     * @param other
     * @return 
     */
    @Override
    public int GetDistance(ISearchable other){
        return 12;
    }
    
    private int GetDistance(Color c, Face on){
        //Green, Red, Yellow, White, Blue, Purple
        switch(c){
            case Green:     //On Left
                if(on == Face.Left)
                    return 0;
                else if(on == Face.Front || on == Face.Back || on == Face.Bottom || on == Face.Top)
                    return 1;
                else
                    return 2;
            case Red:       //On Front
                if(on == Face.Front)
                    return 0;
                else if(on == Face.Left || on == Face.Right || on == Face.Bottom || on == Face.Top)
                    return 1;
                else
                    return 2;
            case Yellow:   //On Bottom
                if(on == Face.Bottom)
                    return 0;
                else if(on == Face.Front || on == Face.Back || on == Face.Left || on == Face.Right)
                    return 1;
                else
                    return 2;
            case White:     //On Top
                if(on == Face.Top)
                    return 0;
                else if(on == Face.Front || on == Face.Back || on == Face.Left || on == Face.Right)
                    return 1;
                else
                    return 2;
            case Blue:      //On Right
                if(on == Face.Right)
                    return 0;
                else if(on == Face.Top || on == Face.Front || on == Face.Bottom || on == Face.Back)
                    return 1;
                else
                    return 2;
            case Purple:    //On Back
                if(on == Face.Back)
                    return 0;
                else if(on == Face.Left || on == Face.Right || on == Face.Bottom || on == Face.Top)
                    return 1;
                else
                    return 2;
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object other){
        if(!(other instanceof rubix.Cube))
            return false;
        
        rubix.Cube oc = (rubix.Cube)other;
        
        if(length != oc.length)
            return false;
        
        for(int i = 0; i < this.colors.length; i++){
            if(this.colors[i] != oc.colors[i])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        
           int hash = 0;
           for(int i = 0; i < this.colors.length; i++){
            hash += colors[i].ordinal();
            hash += hash << 10;
            hash ^= hash >> 6;
           }

           hash += hash << 3;
           hash ^= hash >> 11;
           hash += hash << 15;

        return hash;

    }
    
    @Override
    public String toString(){
        String str = "";
        for(int i = 0; i < this.colors.length; i++){
            str += this.colors[i].toString();
        }
        return str;
    }
    
}
