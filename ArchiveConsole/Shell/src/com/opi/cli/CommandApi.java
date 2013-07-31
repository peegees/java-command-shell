package com.opi.cli;

import com.opi.cli.api.ICommandApi;

import java.io.PrintWriter;

/**
 */
public class CommandApi implements ICommandApi {
    private String argv[];
    private PrintWriter pw;

    @Override
    public String[] getArgv() {
        return argv;
    }

    void setArgv( String argv[] ) {
        this.argv = argv;
    }

    @Override
    public PrintWriter getPrintWriter() {
        return pw;
    }

    public void setPrintWriter(PrintWriter pw) {
        this.pw = pw;
    }
}
