package com.opi.cli.api;

import java.io.PrintWriter;
import java.io.Writer;

/**
 */
public interface ICommandApi {
    String[] getArgv();
    PrintWriter getPrintWriter();
}
