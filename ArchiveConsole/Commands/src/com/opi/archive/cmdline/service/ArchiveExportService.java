package com.opi.archive.cmdline.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p/>
 */
public class ArchiveExportService {

    private static ArchiveExportService instance = new ArchiveExportService();

    private ArchiveExportService() {}

    public static ArchiveExportService getInstance() {
        return instance;
    }


    /**
     *
     * @param options -
     */
    public void export( ExportOptions options  ) {
        // solr time range query -> create solr dates

        ArchiveCmdService service = ArchiveCmdService.getInstance();

        options.printWriter.println( "output path: " + options.outputPath );
        options.printWriter.println( "gap: " + options.gapN + ExportOptions.getGapUnit( options.gapUnit ) );
        //options.printWriter.flush();

        Calendar startDate = options.startDate;

        try {
            while( startDate.before( options.endDate ) ) {
                // solr between query startDate - queryEndDate
                Calendar queryEndDate = (Calendar) startDate.clone();
                queryEndDate.add( options.gapUnit, options.gapN );

                // create output stream to filename outputPath/yyyyMMd_HHmmss.csv
                String csvFile = getCsvFile( startDate, queryEndDate );

                // construct query solr url
                SolrQuery solrQuery = service.getSolrQuery();
                solrQuery.remove( "shards" );       // disable distributed query
                String query = getDateRangeQuery( startDate,  queryEndDate );
                solrQuery.setQuery( query );
                solrQuery.setRows( options.rows );

                // restrict to field list
                solrQuery.setFields( options.fields.toArray( new String[ options.fields.size() ] ) );

                // write to file
                options.printWriter.println( "writing file:" + csvFile + " ..." );

                // service.queryCsv( solrQuery, options.outputPath + csvFile );
                service.queryCsv( solrQuery, startDate.getTime(), queryEndDate.getTime(), options.outputPath + csvFile );


                startDate = queryEndDate;
            }
        } catch( Exception e ) {
            throw ServiceException.create(e);
        }
    }

    public static class ExportOptions {

        public String outputPath = "./";
        public Calendar startDate;
        public Calendar endDate;
        public List<String> fields = new ArrayList<String>();
        public PrintWriter printWriter = new PrintWriter( System.out );
        public int gapN = 1;
        public int gapUnit = Calendar.MONTH;
        public int rows = 1000000;


        public static Integer getGapId( String gapUnit ) {
            return gapUnitMap.get( gapUnit );
        }

        public static String getGapUnit( Integer gapId ) {
            String ret = null;

            for( String key : gapUnitMap.keySet() ) {
                if( gapUnitMap.get( key ).equals( gapId ) ) {
                    ret = key;
                    break;
                }
            }

            return ret;
        }

        private final static Map<String,Integer> gapUnitMap = createMap();
        private static Map<String,Integer> createMap() {
            Map<String,Integer> gapUnitMap = new Hashtable<String, Integer>();
            gapUnitMap.put( "DAY", Calendar.DATE );
            gapUnitMap.put( "DAYS", Calendar.DATE );
            gapUnitMap.put( "MONTH", Calendar.MONTH );
            gapUnitMap.put( "MONTHS", Calendar.MONTH );
            gapUnitMap.put( "YEAR", Calendar.YEAR );
            gapUnitMap.put( "YEARS", Calendar.YEAR );
            return gapUnitMap;
        }
    }

    private String getCsvFile( Calendar startDate, Calendar queryEndDate ) {
        String csvFile = File.separator;

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.US );
        sdf.setTimeZone( startDate.getTimeZone() );

        csvFile += sdf.format( startDate.getTime() );
        csvFile += "-";

        csvFile += sdf.format( queryEndDate.getTime() );
        csvFile += ".csv";

        return csvFile;
    }

    private String getDateRangeQuery( Calendar startDate, Calendar queryEndDate ) throws IOException {
        Calendar cal = null;
        StringBuilder sb = new StringBuilder();
        sb.append( "firstTimestamp_dis:[" );
        cal = DateUtil.formatDate( startDate.getTime(), cal,  sb );
        sb.append( " TO " );
        DateUtil.formatDate( queryEndDate.getTime(), cal,  sb );
        sb.append( "]" );
        return sb.toString();
    }
}