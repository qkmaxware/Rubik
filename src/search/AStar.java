/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import plus.system.Debug;

/**
 *
 * @author Colin
 */
public class AStar implements ISearch{

    private static class SearchNode{
        public ISearchable value;
        public double g = Double.MAX_VALUE;
        public double h = Double.MAX_VALUE;
        public SearchNode parent;
        
        public double f(){
            return g + h;
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
    
    //https://en.wikipedia.org/wiki/A*_search_algorithm
    @Override
    public LinkedList<ISearchable> FindPath(ISearchable start, ISearchable end) {
        //Already evaluated nodes
        HashMap<SearchNode,SearchNode> closedSet = new HashMap<SearchNode,SearchNode>();
        
        //Nodes I am currently looking at
        PriorityQueue<SearchNode> openSet = new PriorityQueue<SearchNode>(10, (SearchNode x, SearchNode y)->{
            //-1 = first is less than second 
            // 0 = first equal second
            // 1 = first more second
            return (x.f() > y.f()) ? 1 : ((x.f() == y.f())? 0 : -1);
        });

        //LinkedList<SearchNode> openSet = new LinkedList<SearchNode>();
        
        //Initailly I only know of the start node
        SearchNode node = new SearchNode();
        node.value = start;
        node.g = 0;
        node.h = start.GetHeuristic();
        openSet.add(node);
        
        //Start search
        while(!openSet.isEmpty()){
            
            //Get the node with the lowest F score
            SearchNode current = openSet.poll();
            
            //Test if at the end of the search
            if(current.value.Equivalent(end)){
                return GetPath(current);
            }
            
            //Add this node to the closed set
            openSet.remove(current);
            closedSet.put(current,current);
            
            //For each neighboring node
            ISearchable[] neighbors = current.value.GetNext();
            for(ISearchable neighbor_value : neighbors){
                SearchNode neighbor = new SearchNode();
                neighbor.value = neighbor_value;
                
                boolean add = true;
                
                //Compute neighbor scores -- don't assign yet
                double g_score = current.g + current.value.GetDistance(neighbor.value);
                
                //Ignore already evaluated neighbors that are better
                if(closedSet.containsKey(neighbor)){
                    add = false;
                    if(g_score < closedSet.get(neighbor).g){
                        closedSet.remove(neighbor);
                        add = true;
                    }
                }
                
                if(add){
                    
                    //Find if we have an equivalent node
                    SearchNode equivalent = this.GetEqual(openSet, neighbor);
                    
                    //If we have an equivalent node
                    if(equivalent != null){
                        
                        //This is a better path 
                        if(g_score < equivalent.g){
                            openSet.remove(equivalent);
                            
                            //This path is looking better, record it
                            neighbor.parent = current;
                            neighbor.g = g_score;
                            neighbor.h = neighbor.value.GetHeuristic();

                            openSet.add(neighbor);
                        }
                    }
                    //This is a new node, add it
                    else{
                        neighbor.parent = current;
                        neighbor.g = g_score;
                        neighbor.h = neighbor.value.GetHeuristic();

                        openSet.add(neighbor);
                    }
                }
                
            }
        }
        
        Debug.Log("No Path Found");
        return null;
    }
    
    private SearchNode GetEqual(PriorityQueue open, SearchNode node){
        Iterator it = open.iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(obj.equals(node)){
                return (SearchNode)obj; 
            }
        }
        return null;
    }
    
    private SearchNode FindLowestF(LinkedList<SearchNode> nodes){
        SearchNode lowest = nodes.getFirst();
        for(SearchNode node : nodes){
            if(node.f() < lowest.f())
                lowest = node;
        }
        return lowest;
    }
    
    private LinkedList<ISearchable> GetPath(SearchNode node){
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
