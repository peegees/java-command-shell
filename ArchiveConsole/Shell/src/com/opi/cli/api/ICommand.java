package com.opi.cli.api;

import com.opi.cli.CommandApi;

import java.util.List;

/**
 */
public interface ICommand {

    /**
     * called when all commands are loaded
     * @param api -
     */
    void init( CommandApi api );

    /**
     * The name of the command
     * @return -
     */
    String getName();

    /**
     * The description of the command
     * @return -
     */
    String getDescription();

    /**
     * List of sub commands
     * @return -
     */
    List<ISubCommand> getSubCommands();

    /**
     * The main function - called when the command is executed
     * @param api -
     * @throws Exception
     */
    void execute( ICommandApi api ) throws Exception;
}
