/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.LinkedList;

/**
 *
 * @author Colin
 */
public interface ISearch {
    
    public LinkedList<ISearchable> FindPath(ISearchable start, ISearchable end);
    
}
