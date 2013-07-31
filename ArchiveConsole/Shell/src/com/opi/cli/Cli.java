package com.opi.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ISubCommand;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import java.io.*;
import java.util.*;

/**
 */
public class Cli {

    private static Cli instance = new Cli();

    private boolean verbose = false;
    private String promt = "";
    private boolean exit = false;
    private Map<String, ICommand> commandMap;


    private Cli() {}

    public static Cli getInstance() {
        return instance;
    }

    public void run( String mainArgv[] ) throws Exception {

        if( System.getProperty( "verbose" ) != null ) {
            verbose = true;
        }

        PrintWriter pw = new PrintWriter( System.out, true );
        /*PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(
                        System.out,
                        System.getProperty(
                                "jline.WindowsTerminal.output.encoding",
                                System.getProperty("file.encoding") ) ) );*/
        //PrintWriter pw = new PrintWriter( new FileOutputStream( FileDescriptor.out ) );

        promt = System.getProperty( "user.name" ) + " > ";
        // ConsoleReader consoleReader = new ConsoleReader( is, pw, null, new UnsupportedTerminal());
        ConsoleReader consoleReader = getConsoleReader( mainArgv, System.out  );

        setupCommands(pw);

        while( !exit ) {
            // read line from console
            String line = consoleReader.readLine( promt );
            // System.out.print( line.length() );
            if( line != null ) {
                // create argv array
                String[] argv = line.trim().split( "\\s+" );
                if( argv != null && argv.length > 0 ) {
                    // create commands argv array
                    CommandApi api = new CommandApi();
                    String[] cmdArgv = new String[ argv.length - 1 ];
                    System.arraycopy( argv, 1, cmdArgv, 0, argv.length - 1 );
                    api.setArgv( cmdArgv );
                    api.setPrintWriter( pw );

                    handleCommand( argv[0], api );
                }
            } else {
                exit = true;
            }
        }
    }

    public List<ICommand> getCommands() {
        List<ICommand> commands = new ArrayList<ICommand>( commandMap.values() );
        Collections.sort( commands, new Comparator<ICommand>() {
            @Override
            public int compare(ICommand o1, ICommand o2) {
                return o1.getName().compareTo( o2.getName() );
            }
        });
        return commands;
    }

    public void setExit( boolean exit ) {
        this.exit = exit;
    }

    private void handleCommand(String commandName, CommandApi api) {
        try {
            // get command
            ICommand command = getCommand( commandName );
            if( command != null ) {
                // setup commander object
                JCommander commander = setupCommander( command );

                // parse the parameters
                commander.parse( api.getArgv() );

                // execute command or sub command
                executeCommand( commander, command, api );
            } else {
                if( commandName.length() > 0 ) {
                    api.getPrintWriter().println( String.format( "unknown command: '%s'", commandName) );
                }
            }
        } catch( ParameterException pe ) {
            api.getPrintWriter().println( commandName + " " + pe.getMessage() );
        }
        catch( Exception  e ) {
            printException(e, api.getPrintWriter());
        }
    }

    private JCommander setupCommander(ICommand command) {
        JCommander commander = new JCommander();
        commander.addObject( command );

        List<ISubCommand> subCommands = command.getSubCommands();
        if( subCommands != null ) {
            for( ISubCommand subCommand : subCommands ) {
                commander.addCommand( subCommand );
            }
        }

        return commander;
    }

    private ICommand getCommand(String commandName) throws IllegalAccessException, InstantiationException {
        ICommand command = null;

        command = commandMap.get( commandName );
        if( command != null ) {
            command = command.getClass().newInstance();
        }

        return command;
    }

    private void executeCommand(JCommander commander, ICommand command, CommandApi api) throws Exception {
        String subCommandName = commander.getParsedCommand();
        if( subCommandName == null ) {
            command.execute( api );
        } else {
            ISubCommand subCommand = (ISubCommand) commander.getObjects().get( 0 );
            subCommand.execute( command, api );
        }
    }

    private void setupCommands(PrintWriter pw) {
        commandMap = new Hashtable<String, ICommand>();
        ServiceLoader<ICommand> serviceLoader = ServiceLoader.load( ICommand.class );
        Iterator<ICommand> iter = serviceLoader.iterator();
        while( iter.hasNext() ) {
            ICommand command = iter.next();
            String commandName = command.getName();
            if( commandName != null ) {
                if( !commandMap.containsKey( commandName ) ) {
                    commandMap.put( commandName, command );
                }
            }
        }

        for( ICommand command : getCommands() ) {
            CommandApi commandApi = new CommandApi();
            commandApi.setPrintWriter( pw );
            command.init( commandApi );
        }
    }

    private ConsoleReader getConsoleReader( String[] mainArgv, OutputStream os ) throws IOException {
        ConsoleReader consoleReader = null;

        // setup input: system input or a file provided by the  main command line
        InputStream is = new FileInputStream( FileDescriptor.in );
        if( mainArgv != null && mainArgv.length > 0 ) {
            is = new FileInputStream( mainArgv[ 0 ] );
            final BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            consoleReader = new ConsoleReader() {
                @Override
                public String readLine( String promt ) throws IOException {
                    return br.readLine();
                }
            };

        } else {
            try {
                //consoleReader = new ConsoleReader( is, os );
                consoleReader = new ConsoleReader();
            } catch( Throwable t ) {
                consoleReader = new ConsoleReader( is, os, new UnsupportedTerminal() );
            }
        }

        return consoleReader;
    }

    private void printException( Exception e, PrintWriter pw ) {
        if( verbose ) {
            e.printStackTrace( pw );
        } else {
            Throwable t = e;
            while( t != null && t != t.getCause() ) {
                String msg = t.getMessage();
                pw.println( msg );
                t = t.getCause();
            }
        }
    }

}
