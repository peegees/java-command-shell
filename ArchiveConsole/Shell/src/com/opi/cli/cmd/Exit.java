package com.opi.cli.cmd;

import com.opi.cli.Cli;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.util.List;

/**
 */
public class Exit implements ICommand {
    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "Exits the program.";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute(ICommandApi api) throws Exception {
        Cli.getInstance().setExit( true );
    }
}
