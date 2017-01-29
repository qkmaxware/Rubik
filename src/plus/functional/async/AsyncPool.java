/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plus.functional.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import plus.system.functional.Action;

/**
 *
 * @author Colin
 */
public class AsyncPool {
    
    public static final int DefaultThreadCount = 4;
    
    public static class Worker extends Thread{
        
        private AsyncPool myPool;
        private boolean stopped = false;
        
        protected Worker(AsyncPool pool){
            this.myPool = pool;
        }
        
        public void run(){
            while(!stopped){
                try{
                    Action a = myPool.queue.poll();
                    if(a != null)
                        a.Invoke();
                }catch(Exception e){
                    //Something has occured, report it
                    
                }
            }
        }
        
        public void Start(){
            this.stopped = false;
            this.start();
        }
        
        public void Stop(){
            this.stopped = true;
            this.interrupt();
        }
        
        public synchronized boolean Active(){
            return !stopped;
        }
        
    }
    
    private int threadCount = 0;
    private Worker[] workers;
    private BlockingQueue<Action> queue;
    
    public AsyncPool(){
        this(DefaultThreadCount);
    }
    
    public AsyncPool(int threadCount){
        this.threadCount = threadCount;
        workers = new Worker[this.threadCount];
        queue = new LinkedBlockingQueue<Action>();
        for(int i = 0; i < workers.length; i++){
            workers[i] = new Worker(this);
            workers[i].Start();
        }
    }
    
    public synchronized void Stop(){
        for(int i = 0; i < this.workers.length; i++){
            workers[i].Stop();
        }
    }
    
    public synchronized void Enqueue(Action action){
        queue.offer(action);
    }
    
}
