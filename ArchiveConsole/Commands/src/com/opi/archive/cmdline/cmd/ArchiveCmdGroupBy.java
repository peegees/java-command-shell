package com.opi.archive.cmdline.cmd;

import com.beust.jcommander.Parameter;
import com.opi.archive.cmdline.service.*;
import com.opi.archive.cmdline.util.CalendarConverter;
import com.opi.cli.CommandApi;
import com.opi.cli.api.ICommand;
import com.opi.cli.api.ICommandApi;
import com.opi.cli.api.ISubCommand;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 */
public class ArchiveCmdGroupBy implements ICommand {

    @Parameter( description = "archive fields to group" )
    private List<String> fields = new ArrayList<String>();

    @Parameter( names = "-start", description = "start date yyyy-MM-dd", converter = CalendarConverter.class )
    private Calendar start;

    @Parameter( names = "-end", description = "end date yyyy-MM-dd", converter = CalendarConverter.class )
    private Calendar end;

    @Override
    public void init(CommandApi api) {
    }

    @Override
    public String getName() {
        return "groupBy";
    }

    @Override
    public String getDescription() {
        return "Count messages by given archive field (ex: organisation).";
    }

    @Override
    public List<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public void execute(ICommandApi api) throws Exception {
        PrintWriter pw = api.getPrintWriter();

        try {
            if( !fields.isEmpty() ) {
                // validate
                ArchiveCmdService service = ArchiveCmdService.getInstance();
                List<IValidation> validations = new ArrayList<IValidation>();
                validations.add( new ValidateUrl() );
                validations.add( new ValidateTimezone() );
                if( !service.validate( validations, pw ) ) {
                    return;
                }

                // get solr query
                SolrQuery solrQuery = service.getSolrQuery();
                fields = service.getSorlFields( fields );

                // setup facet
                solrQuery.setFacet( true );
                solrQuery.setFacetLimit( -1 );
                solrQuery.setFacetMissing( true );
                //solrQuery.setFacetMinCount( 1 );
                solrQuery.setRows( 0 );
                setupDateRangeQuery( solrQuery, start, end, pw );

                // add the fields
                solrQuery.addFacetField( fields.toArray( new String[ fields.size() ] ) );

                // c.writeOutput( solrQuery.toString() + "\n" );

                // execute the query
                QueryResponse response = service.query( solrQuery );
                pw.println( String.format( "qtime: %dms", response.getQTime() ) );

                // process output
                List<FacetField> fields = response.getFacetFields();
                int total = 0;
                // loop fields
                for( FacetField field : fields ) {
                    String fmt = String.format( "%s  %d\n", field.getName(), field.getValueCount() );
                    // print field values and counts
                    List<FacetField.Count> counts = field.getValues();
                    if( counts != null ) {
                        for( int i = 0; i < counts.size(); i++ ) {
                            FacetField.Count count = counts.get( i );
                            total += count.getCount();
                            String name = count.getName();
                            if( name == null ) {
                                name = "missing";
                                if( count.getCount() > 0 ) {
                                    fmt += String.format( "   %20s  %10d\n", name, count.getCount() );
                                }
                            } else {
                                fmt += String.format( "   %20s  %10d\n", name, count.getCount() );
                            }
                        }
                    }
                    pw.println( fmt );
                    pw.println( "total:" + total );
                }
            }
        } catch ( Exception e ) {
            throw ServiceException.create( e );
        }
    }

    private void setupDateRangeQuery(SolrQuery solrQuery, Calendar start, Calendar end, PrintWriter pw) throws IOException {
        Date dStart = start == null ? null : start.getTime();
        Date dEnd = end == null ? null : end.getTime();
        ArchiveCmdService service = ArchiveCmdService.getInstance();
        String query = service.getDateRangeQuery( dStart, dEnd );
        if( query != null ) {
            pw.println( "date range: " + query );
            solrQuery.setQuery( query );
        }
    }
}
