package com.opi.cli.api;

/**
 */
public interface ISubCommand {
    void execute( ICommand mainCommandOptions, ICommandApi api ) throws Exception;
}
