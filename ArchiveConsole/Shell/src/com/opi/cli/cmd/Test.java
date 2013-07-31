package com.opi.cli.cmd;

import com.beust.jcommander.Parameter;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.io.PrintWriter;
import java.util.List;

/**
 */
public class Test implements ICommand {

    @Parameter( description = "list of field names" )
    List<String> fields;

    @Parameter( names = "-verbose", description = "verbose flag" )
    boolean verbose;

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute(ICommandApi api) throws Exception {
        PrintWriter pw = api.getPrintWriter();
        pw.println( String.format( "verbose:%b", verbose ) );
        pw.println( String.format( "fields:%s", fields ) );
    }
}
