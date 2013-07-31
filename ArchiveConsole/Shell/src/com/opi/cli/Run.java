package com.opi.cli;

/**
 */
public class Run {
    public static void main( String argv[] ) {
        try {
            Cli.getInstance().run( argv );
        } catch( Exception e ) {
            System.out.println( e.getMessage() );
            e.printStackTrace();
        }
    }
}
