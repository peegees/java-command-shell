package com.opi.archive.cmdline.cmd;

import com.beust.jcommander.Parameter;
import com.opi.archive.cmdline.service.ArchiveCmdService;
import com.opi.archive.cmdline.service.IValidation;
import com.opi.archive.cmdline.service.ValidateTimezone;
import com.opi.archive.cmdline.service.ValidateUrl;
import com.opi.archive.cmdline.util.CalendarConverter;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.util.DateUtil;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class ArchiveCmdRange implements ICommand {

    @Parameter( description = "archive fields to use" )
    private List<String> fields = new ArrayList<String>();

    @Parameter( names = "-start", description = "start date yyyy-MM-dd", required = true, converter = CalendarConverter.class )
    private Calendar start;

    @Parameter( names = "-end", description = "end date yyyy-MM-dd", required = true, converter = CalendarConverter.class )
    private Calendar end;

    @Parameter( names = "-gap", description = "+1MONTH (or DAY,YEAR) " )
    private String gap = "+1MONTH";

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public String getDescription() {
        return "Count messages in a given time range.";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute( ICommandApi api ) throws Exception {
        PrintWriter pw = api.getPrintWriter();

        if( fields.isEmpty() ) {
            fields.add( "firstTimestamp" );
        }

        // verify settings
        ArchiveCmdService service = ArchiveCmdService.getInstance();
        Properties properties = service.getProperties();

        List<IValidation> validations = new ArrayList<IValidation>();
        validations.add( new ValidateUrl() );
        validations.add( new ValidateTimezone() );
        if( !service.validate( validations, pw ) ) {
            return;
        }

        // translate fields
        fields = service.getSorlFields( fields );
        String solrField = fields.get( 0 );

        // get solr query
        SolrQuery solrQuery = service.getSolrQuery();

        // setup facet
        solrQuery.addDateRangeFacet( solrField, start.getTime(), end.getTime(), gap );
        // solrQuery.setFacetLimit(-1);
        //solrQuery.setFacetMinCount( 1 );
        solrQuery.setRows( 0 );

        // c.writeOutput( solrQuery.toString() + "\n" );

        // execute the query
        QueryResponse response = service.query( solrQuery );
        pw.println( String.format( "qtime:      %dms", response.getQTime() ) );
        pw.println( String.format( "timezone:   %s" , start.getTimeZone().getID() ) );

        // process output
        List<RangeFacet> facets = response.getFacetRanges();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.US );
        sdf.setTimeZone( start.getTimeZone() );

        // loop fields
        for( RangeFacet facet : facets ) {
            String fmt = String.format( "facet name: %s", facet.getName() );
            fmt += String.format( ", gap: %s\n", facet.getGap() );

            // print field values and counts
            List<RangeFacet.Count> counts = facet.getCounts();
            int total = 0;
            for( int i=0; i<counts.size(); i++ ) {
                RangeFacet.Count count = counts.get( i );
                String ts = count.getValue();
                try {
                    Date date = DateUtil.parseDate( ts );
                    ts = sdf.format( date );
                } catch( Exception ignore ) {}
                total += count.getCount();
                fmt += String.format( "   %s  %d\n", ts, count.getCount() );
            }
            fmt += "total: " + total + "\n";
            pw.println( fmt );
        }
    }
}
