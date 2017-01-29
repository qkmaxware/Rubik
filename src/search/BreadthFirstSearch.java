/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.HashSet;
import java.util.LinkedList;
import plus.system.Debug;

/**
 *
 * @author Colin
 */
public class BreadthFirstSearch implements ISearch {
    
    private class SearchNode {
        public ISearchable value;
        public int depth;

        public SearchNode parent = null;

        public SearchNode(){}
        public SearchNode(ISearchable v, int d){
            this.value = v;
            this.depth = d;
        }
        
        @Override
        public boolean equals(Object other){
            if(other instanceof SearchNode){
                return ((SearchNode)other).value.Equivalent(value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
    
    @Override
    public LinkedList<ISearchable> FindPath(ISearchable start, ISearchable end){
        SearchNode tree = new SearchNode(start,0);
        
        //List of nodes we have already, or are about to test
        HashSet<SearchNode> closedSet = new HashSet<SearchNode>();
        closedSet.add(tree);
        
        //List of nodes waiting to be tested
        LinkedList<SearchNode> q = new LinkedList<SearchNode>();
        q.add(tree); 
        
        while(!q.isEmpty()){
            //Get current treenode
            SearchNode current = q.pollFirst();
            
            //Broken node, can't search on this
            if(current == null || current.value == null){
                break;
            }
            
            //Is this the end node
            if(current.value.Equivalent(end)){
                return GetList(current);
            }
            
            //Get Children nodes
            ISearchable[] children = current.value.GetNext();
            for(ISearchable child : children){
                SearchNode childNode = new SearchNode(child, current.depth + 1);
                
                //Muahaha this is a new possibility, add it to the list to evaluate, and the list already evaluated
                if(!closedSet.contains(childNode)){
                    
                    childNode.parent = current;
                    q.addLast(childNode);
                    closedSet.add(childNode); // Add first as an optimization trick. Later things are closer to start state and as a result further from the goal, and less likely to show up in evaluations
                }
            }
        }
        
        Debug.Log("No Path Found"); 
        return null;
    }
    
    private LinkedList<ISearchable> GetList(SearchNode node){
        LinkedList<ISearchable> path = new LinkedList<ISearchable>();
        
        //Go up heiarchy and get list of moves
        SearchNode p = node;
        while(p.parent != null){
            path.add(p.value);
            p = p.parent;
        }
        
        return Reverse(path);
    }
    
    private LinkedList<ISearchable> Reverse(LinkedList<ISearchable> list){
        LinkedList<ISearchable> rlist = new LinkedList<ISearchable>();
        for(ISearchable node : list){
            rlist.addFirst(node);
        }
        return rlist;
    }
    
}
