/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

/**
 *
 * @author Colin
 */
public interface ISearchable {
    
    public ISearchable[] GetNext();
    
    public boolean Equivalent(ISearchable other);
    
    public int GetHeuristic();
    
    public int GetDistance(ISearchable other);
    
}
