package com.opi.cli.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.opi.cli.Cli;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class Help implements ICommand {

    @Parameter
    List<String> commandNames = new ArrayList<String>();

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "type help <command> for detailed help";
    }


    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute( ICommandApi api ) throws Exception {
        String argv[] = api.getArgv();
        Cli cli = Cli.getInstance();
        List<ICommand> commands = cli.getCommands();
        PrintWriter pw = api.getPrintWriter();

        if( commandNames.isEmpty() ) {
            showHelpForAllCommands(commands, pw);
        } else {
            showHelpForOneCommand(commands, pw);
        }
    }

    private void showHelpForOneCommand(List<ICommand> commands, PrintWriter pw) {
        String commandName = commandNames.get( 0 );
        for( ICommand command : commands ) {
            if( commandName.equals( command.getName() ) ) {
                JCommander commander = new JCommander();
                commander.setProgramName( command.getName() );
                commander.addObject( command );
                StringBuilder sb = new StringBuilder();
                sb.append( "\n" );
                commander.usage(sb);
                pw.println( sb.toString() );
            }
        }
    }

    private void showHelpForAllCommands(List<ICommand> commands, PrintWriter pw) {
        for( ICommand command : commands ) {
            String description = command.getDescription();
            if( description == null ) {
                description = "";
            }
            pw.println(String.format("%-20s%s", command.getName(), description));
        }
    }
}
