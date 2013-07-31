package com.opi.archive.cmdline.cmd;

import com.beust.jcommander.Parameter;
import com.opi.archive.cmdline.service.ArchiveCmdService;
import com.opi.archive.cmdline.service.IValidation;
import com.opi.archive.cmdline.service.ValidateUrl;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ArchiveCmdInfo implements ICommand {

    @Parameter( names = "-fields", description = "show field info" )
    private boolean fields;

    @Parameter( names = "-cores", description = "show core info" )
    private boolean cores;

    @Override
    public void init(CommandApi api) {
        PrintWriter pw = api.getPrintWriter();
        pw.println( "***" );
        pw.println( "*** IPC archive utility 0.9" );
        pw.println( "***" );
        pw.println( "Type help for a list of commands. Type help <command> for options." );
        pw.println( "" );
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Show archive index info.";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute( ICommandApi api ) throws Exception {
        PrintWriter pw = api.getPrintWriter();

        ArchiveCmdService service = ArchiveCmdService.getInstance();
        service.clearCaches();

        List<IValidation> validations = new ArrayList<IValidation>();
        validations.add( new ValidateUrl() );
        if( !service.validate( validations, pw ) ) {
            return;
        }

        // get solr query
        if( fields ) {
            pw.println( service.getFmtSchemaFieldsInfo() );
        }

        if( cores ) {
            pw.println( service.getFmtCoreInfos() );
        }
    }
}
