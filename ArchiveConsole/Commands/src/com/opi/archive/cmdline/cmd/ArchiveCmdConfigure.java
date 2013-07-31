package com.opi.archive.cmdline.cmd;

import com.beust.jcommander.Parameter;
import com.opi.archive.cmdline.service.ArchiveCmdService;
import com.opi.archive.cmdline.service.IValidation;
import com.opi.archive.cmdline.service.ValidateTimezone;
import com.opi.archive.cmdline.service.ValidateUrl;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * <p/>
 */
public class ArchiveCmdConfigure implements ICommand {

    @Parameter( names = "-url", description = "url to the index example: http://devtest02:8080/index/" )
    public String url;

    @Parameter( names = "-timezone", description = "the timezone to use example: GMT+1 or UTC" )
    public String timeZone = TimeZone.getDefault().getID();

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "configure";
    }

    @Override
    public String getDescription() {
        return "Archive command configuration settings.";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute( ICommandApi api ) throws Exception {
        PrintWriter pw = api.getPrintWriter();

        // get properties
        ArchiveCmdService service = ArchiveCmdService.getInstance();
        Properties prop = service.getProperties();

        // configure
        boolean saveProperties = handleConfigure( prop );

        // save properties
        if( saveProperties ) {
            String propFile = service.saveProperties( prop );
            pw.println( "properties file:" + propFile );
            prop = service.getProperties();
        }

        // show
        prop.list( pw );

        // validate
        List<IValidation> validations = new ArrayList<IValidation>();
        if( url != null ) {
            validations.add( new ValidateUrl() );
        }

        if( timeZone != null ) {
            validations.add( new ValidateTimezone() );
        }

        service.validate( validations, pw );
    }

    private boolean handleConfigure( Properties prop ) {
        boolean saveProperties = false;

        if( url != null ) {
            prop.setProperty( "archive.url", url );
            saveProperties = true;
        }

        if( timeZone != null ) {
            TimeZone.getTimeZone(timeZone);
            prop.setProperty( "archive.timezone", timeZone );
            saveProperties = true;
        }
        return saveProperties;
    }

}