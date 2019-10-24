package com.ylpu.kepler.scheduler.common.test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.ylpu.kepler.scheduler.response.WorkerResponse;

class TaskCall{
    
    String worker;

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }
    
    
    
}
public class ResourceTest {
    
   private static LinkedBlockingQueue<TaskCall> waitingQueue = new LinkedBlockingQueue<TaskCall>();
    
    private static final int POOL_SIZE = 8;
    
    static {
        init();
    }
    
    private static void init() {
        ExecutorService es = Executors.newFixedThreadPool(POOL_SIZE);
        for(int i = 0; i < POOL_SIZE; i++) {
            es.execute(new ResourceScheduler(waitingQueue));
        }
    }
    
    public static void addTask(TaskCall call) {
        waitingQueue.add(call);
    }
    
    public static LinkedBlockingQueue<TaskCall> getQueue() {
        return waitingQueue;
    }
    
    private static final class ResourceScheduler implements Runnable{
        
        private LinkedBlockingQueue<TaskCall> queue;
        
        public ResourceScheduler(LinkedBlockingQueue<TaskCall> queue) {
            this.queue = queue;
        }
        
        public String getWorker() {
            return null;
        }
        @Override
        public void run() {
            TaskCall taskCall = null;
            String worker = null;
            try {
                 while(true) {
                     synchronized(queue) {
                         taskCall = queue.peek();
                         if(taskCall != null) {
                             worker = getWorker();
                             if(worker != null) {
                                 taskCall.setWorker("localhost");
                                 queue.poll();
                             }else {
                                 //等待资源
                                 System.out.println("等待资源");
                                 Thread.sleep(3000);
                                 
                             }
                         }
                     }
                     if(taskCall != null && taskCall.getWorker() != null) {
                         System.out.println("执行任务" + taskCall);
                     }
                    
                 }
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        ResourceTest.addTask(new TaskCall());
        ResourceTest.addTask(new TaskCall());
        ResourceTest.addTask(new TaskCall());
        ResourceTest.addTask(new TaskCall());
        ResourceTest.addTask(new TaskCall());
        ResourceTest.addTask(new TaskCall());
    }
}
