package com.ylpu.thales.scheduler.executor.clickHouse;

public class ClickHouseParameters {
   private Long sendTimeout;
   private long receiveTimeout;
   
   public Long getSendTimeout() {
       return sendTimeout;
   }
   public void setSendTimeout(Long sendTimeout) {
       this.sendTimeout = sendTimeout;
   }
   public long getReceiveTimeout() {
       return receiveTimeout;
   }
   public void setReceiveTimeout(long receiveTimeout) {
       this.receiveTimeout = receiveTimeout;
   }
}
