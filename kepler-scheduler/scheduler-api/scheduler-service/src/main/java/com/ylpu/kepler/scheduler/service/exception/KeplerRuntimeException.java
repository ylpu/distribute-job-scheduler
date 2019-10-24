package com.ylpu.kepler.scheduler.service.exception;

public class KeplerRuntimeException extends RuntimeException{
    
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
    
    public KeplerRuntimeException(String message, Throwable cause) {
        super(message,cause);
    }
    
    public KeplerRuntimeException(Throwable cause) {
        super(cause);
    }
    
    public KeplerRuntimeException(String message) {
        super(message);
    }
    
    public KeplerRuntimeException(int errorCode, String message, Throwable cause) {
        super(message,cause);
        this.errorCode = errorCode;
    }
}
