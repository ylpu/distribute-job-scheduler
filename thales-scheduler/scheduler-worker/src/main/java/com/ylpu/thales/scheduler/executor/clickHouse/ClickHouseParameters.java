package com.ylpu.thales.scheduler.executor.clickHouse;

public class ClickHouseParameters {
   private String dsName;
   private String query;
   private Long sendTimeout;
   private long receiveTimeout;
   
   public String getDsName() {
      return dsName;
   }
   public void setDsName(String dsName) {
       this.dsName = dsName;
   }
   public String getQuery() {
       return query;
   }
   public void setQuery(String query) {
       this.query = query;
   }
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
