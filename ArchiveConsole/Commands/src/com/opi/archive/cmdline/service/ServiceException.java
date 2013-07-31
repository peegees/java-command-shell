package com.opi.archive.cmdline.service;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * <p/>
  */
public class ServiceException extends RuntimeException {

    private ServiceException( Exception e ) {
        super( e );
    }

    private ServiceException(String message, Exception e) {
        super( message, e );
    }

    public static RuntimeException create( String message, Exception e ) {
        RuntimeException ret;

        if( e instanceof ServiceException ) {
            ret = (RuntimeException) e;
        } else {
            ret = new ServiceException( message, e );
        }

        return ret;
    }

    public static RuntimeException create( Exception e ) {
        RuntimeException ret;

        if( e instanceof ServiceException ) {
            ret = (RuntimeException) e;
        } else {
            ret = new ServiceException( e );
        }

        return ret;
    }

    @Override
    public String getMessage() {
        return super.getCause().getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getCause().getStackTrace();
    }

    @Override
    public Throwable getCause() {
        return super.getCause().getCause();
    }

    @Override
    public void printStackTrace() {
        super.getCause().printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.getCause().printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.getCause().printStackTrace(s);
    }

    @Override
    public String toString() {
        return super.getCause().toString();
    }

}