package com.opi.archive.cmdline.cmd;

import com.beust.jcommander.Parameter;
import com.opi.archive.cmdline.service.*;
import com.opi.archive.cmdline.util.CalendarConverter;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class ArchiveCmdExport implements ICommand {

    public ArchiveCmdExport() {
    }

    @Parameter( description = "archive fields to use", required = true )
    private List<String> fields = new ArrayList<String>();

    @Parameter( names = "-start", description = "start date yyyy-MM-dd", required = true, converter = CalendarConverter.class )
    private Calendar startDate;

    @Parameter( names = "-end", description = "end date yyyy-MM-dd", required = true, converter = CalendarConverter.class )
    private Calendar endDate;

    @Parameter( names = "-rows", hidden = true)
    private Integer rows = 1000000;

    @Parameter( names = "-gap", description = "+1MONTH (or DAY,YEAR) " )
    private String gap = "+1MONTH";

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public String getDescription() {
        return "Export messages into csv files.";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute( ICommandApi api ) throws Exception {
        PrintWriter pw = api.getPrintWriter();

        // verify settings
        ArchiveCmdService service = ArchiveCmdService.getInstance();
        List<IValidation> validations = new ArrayList<IValidation>();
        validations.add( new ValidateUrl() );
        validations.add( new ValidateTimezone() );
        if( !service.validate( validations, pw ) ) {
            return;
        }

        if( !fields.isEmpty() ) {
            ArchiveExportService.ExportOptions exportOptions = new ArchiveExportService.ExportOptions();
            ArchiveExportService exportService = ArchiveExportService.getInstance();

            fields = service.getSorlFields( fields );
            if( setupGap( exportOptions, gap ) ) {
                exportOptions.outputPath = service.getUniqueOutputPath( "csv" );
                exportOptions.startDate = startDate;
                exportOptions.endDate = endDate;
                exportOptions.fields = fields;
                exportOptions.rows = rows;
                exportOptions.printWriter = pw;
                exportService.export( exportOptions );
            } else {
                pw.println( "unknown format for options gap:" + gap + " use +1MONTH (or DAY,YEAR) " );
            }
        }
    }

    boolean setupGap(ArchiveExportService.ExportOptions exportOptions, String gap ) {
        boolean success = false;

        Pattern pattern = Pattern.compile( "\\+(\\d+)(.*)" );
        Matcher matcher = pattern.matcher( gap );
        if( matcher.matches() ) {
            int gapN = Integer.valueOf( matcher.group( 1 ) );
            if( gapN > 0 ) {
                String strGapUnit = matcher.group( 2 );
                Integer gapUnit = ArchiveExportService.ExportOptions.getGapId( strGapUnit.toUpperCase() );
                if( gapUnit != null ) {
                    exportOptions.gapUnit = gapUnit;
                    exportOptions.gapN = gapN;
                    success = true;
                }
            }
        }

        return success;
    }

    public static void main( String argv[] ) {
        Pattern pattern = Pattern.compile( "\\+(\\d+)(.*)" );
        Matcher matcher = pattern.matcher( "+0YEAR" );
        if( matcher.matches() ) {
            int gapN = Integer.valueOf( matcher.group( 1 ) );
            String strGapUnit = matcher.group( 2 );

            System.out.println( "gapN:" + gapN + " gapUnit:" + strGapUnit  );
        }
    }
}
