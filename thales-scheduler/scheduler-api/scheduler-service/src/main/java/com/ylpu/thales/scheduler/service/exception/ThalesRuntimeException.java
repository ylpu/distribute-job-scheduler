package com.ylpu.thales.scheduler.service.exception;

public class ThalesRuntimeException extends RuntimeException{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private int errorCode;
    
    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public ThalesRuntimeException(String message, Throwable cause) {
        super(message,cause);
    }
    
    public ThalesRuntimeException(Throwable cause) {
        super(cause);
    }
    
    public ThalesRuntimeException(String message) {
        super(message);
    }
    
    public ThalesRuntimeException(int errorCode, String message, Throwable cause) {
        super(message,cause);
        this.errorCode = errorCode;
    }
}
