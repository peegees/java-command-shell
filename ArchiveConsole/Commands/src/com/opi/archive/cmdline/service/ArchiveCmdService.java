package com.opi.archive.cmdline.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.luke.FieldFlag;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class ArchiveCmdService {

    public final static String PARAM_SHARDS = "shards";

    private static ArchiveCmdService instance = new ArchiveCmdService();
    private Map<String, LukeResponse.FieldInfo> fieldInfoMap;
    private List<CoreInfo> coreInfos;

    private ArchiveCmdService() {}

    public static ArchiveCmdService getInstance() {
        return instance;
    }

    /**
     * Creates a new SolrQuery object
     * sets the q parameter to *:*
     * sets the shards parameter
     * @return -
     */
    public SolrQuery getSolrQuery() {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery( "*:*" );

        String shardsParam = getShardsParam();
        if( shardsParam.length() > 0 ) {
            solrQuery.setParam( PARAM_SHARDS, shardsParam );
        }

        return solrQuery;
    }

    public String getDateRangeQuery( Date start, Date end ) throws IOException {
        String ret = null;

        if( start != null || end != null ) {
            Calendar cal = null;
            StringBuilder sb = new StringBuilder();
            sb.append( "firstTimestamp_dis:[" );
            if( start == null ) {
                sb.append( "*" );
            } else {
                cal = DateUtil.formatDate( start, cal, sb );
            }
            sb.append( " TO " );
            if( end == null ) {
                sb.append( "*" );
            } else {
                DateUtil.formatDate( end, cal, sb );
            }
            sb.append( "]" );
            ret = sb.toString();
        }

        return ret;
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        try {
            String fileName = getPropertiesFile();
            File file = new File( fileName );
            if( file.exists() ) {
                properties.load( new FileInputStream( fileName ) );
            }
        } catch( Exception e ) {
            throw ServiceException.create(e);
        }

        return properties;
    }

    public String saveProperties(Properties prop) {
        String propFile = null;
        try {
            propFile = getPropertiesFile();
            OutputStream out = new FileOutputStream( propFile );
            prop.store( out, null );
            out.close();
        } catch( Exception e ) {
            throw ServiceException.create(e);
        }

        return propFile;
    }

    public QueryResponse query(SolrQuery solrQuery) {
        QueryResponse response = null;
        String url = null;

        try {
            url = getArchiveUrl(true);
            SolrServer server = new HttpSolrServer( url );
            response = server.query(solrQuery);
        } catch ( Exception e ) {
            throw ServiceException.create("archive.url:" + url, e);
        }

        return response;
    }

    /*public void queryCsv(SolrQuery solrQuery, String csvFile ) {
        String url = null;

        try {
            url = getArchiveUrl( true );
            SolrServer server = new HttpSolrServer( url );
            CsvRequest csvRequest = new CsvRequest( solrQuery, csvFile );
            server.request( csvRequest );
        } catch ( Exception e ) {
            throw ServiceException.create( "url:" + url, e );
        }
    }*/

    public void queryCsv( SolrQuery solrQuery, Date start, Date end, String csvFile ) {
        String url = null;
        BufferedOutputStream bos = null;

        try {
            // get the list of shards to query
            List<CoreInfo> shards = getShardList(start, end);

            // create directories
            File file = new File( csvFile );
            file.getParentFile().mkdirs();

            // create stream to output file
            FileOutputStream fos = new FileOutputStream( csvFile );
            bos = new BufferedOutputStream( fos );

            // remove shards parameter -> this shall be direct request (vs. distributed request)
            solrQuery.remove( PARAM_SHARDS );

            // loop cores
            for( CoreInfo coreInfo : shards ) {
                // connect to core
                url = coreInfo.getUrl();
                SolrServer server = new HttpSolrServer( url );

                // query
                CsvRequest csvRequest = new CsvRequest( solrQuery, bos );
                server.request( csvRequest );

                // show csv header on first one only
                solrQuery.setParam( "csv.header", false );
            }
        } catch ( Exception e ) {
            throw ServiceException.create( "url:" + url, e );
        } finally {
            if( bos != null ) {
                try {
                    bos.close();
                } catch( Exception ignore ) {}
            }
        }
    }

    public List<CoreInfo> getCoreInfos() {
        if( coreInfos == null ) {
            coreInfos = new ArrayList<CoreInfo>();
            try {
                // create server
                String url = getArchiveUrl( false );
                SolrServer solrServer = new HttpSolrServer( url );

                CoreAdminResponse response = CoreAdminRequest.getStatus("", solrServer);
                // Not own cores with different id's are also included
                // Can be used for fail over
                for (Map.Entry<String, NamedList<Object>> core : response.getCoreStatus()) {

                    // skip in.core.1 and in.core.2
                    if( core.getKey().contains( "core.1" ) || core.getKey().contains( "core.2" ) ) {
                        continue;
                    }

                    CoreInfo coreInfo = new CoreInfo();

                    // general nv pairs
                    coreInfo.setName( core.getKey() );
                    coreInfo.setDataDir( core.getValue().get( "dataDir" ) + "index" );
                    coreInfo.setUrl( url + "/" + core.getKey());

                    // handle index nv pairs
                    NamedList indexMap = (NamedList) core.getValue().get( "index" );
                    coreInfo.setNumDocs( (Integer) indexMap.get( "numDocs" ) );
                    coreInfo.setMaxDoc( (Integer) indexMap.get( "maxDoc" ) );
                    coreInfo.setHasDeletions( (Boolean) indexMap.get( "hasDeletions" ) );
                    coreInfo.setSegmentCount( (Integer) indexMap.get( "segmentCount" ) );
                    // coreInfo.setOptimized( (Boolean) indexMap.get( "optimized" ) );
                    coreInfo.setLastModified( (Date) indexMap.get( "lastModified" ) );
                    coreInfos.add( coreInfo );
                }

                Collections.sort( coreInfos, new Comparator<CoreInfo>() {
                    @Override
                    public int compare(CoreInfo o1, CoreInfo o2) {
                        return o2.getName().compareTo( o1.getName() );
                    }
                });
            } catch( Exception e ) {
                throw ServiceException.create( e );
            }
        }

        return coreInfos;
    }

    public void clearCaches() {
        coreInfos = null;
        fieldInfoMap = null;
    }

    public String getFmtCoreInfos() {
        StringBuilder sb = new StringBuilder();

        List<CoreInfo> coreInfos = getCoreInfos();

        for( CoreInfo coreInfo : coreInfos ) {
            sb.append(String.format("%-18s doc:%,10d    seg:%3d\n", coreInfo.getName(), coreInfo.getNumDocs(), coreInfo.getSegmentCount()));
        }

        return sb.toString();
    }

    /**
     * returns the full path to a unique, non existing directory name
     * basePath/prefix_yyyy-MM-dd_HHmmss/
     *
     * @param prefix -
     * @return -
     */
    public String getUniqueOutputPath( String prefix ) {

        // base path is jar installDir/prefix/
        String basePath = getBasePath();
        basePath += prefix + File.separator;

        // create a sub dir like basePath/2013-07-25_115932/
        DateFormat sdf = new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.US );
        String uniquePath = basePath + sdf.format( new Date() ) + File.separator;

        // check if the directory exists
        File file = new File( uniquePath );
        while( file.exists() ) {
            try {
                Thread.sleep( 1000 );
            } catch( Exception ignore ) {}

            // try again next second
            uniquePath = basePath + sdf.format( new Date() ) + File.separator;
            file = new File( uniquePath );
        }

        return uniquePath;
    }

    /**
     * accepts field names like organisation or organisation_sis
     * will always return solr field name like organisation_sis
     *
     * @param fields -
     * @return -
     */
    public List<String> getSorlFields( List<String> fields ) {
        List<String> outList = new ArrayList<String>();

        // query
        if( fieldInfoMap == null ) {
            LukeResponse lukeResponse = getSchemaInfo();
            fieldInfoMap = lukeResponse.getFieldInfo();
        }

        List<String> keys = new ArrayList<String>();
        keys.addAll( fieldInfoMap.keySet() );

        // translate user field names like organisation to organisation_sis
        for( String userFieldName : fields ) {

            if( !keys.contains( userFieldName ) ) {
                for( String solrFieldName : keys ) {
                    if( solrFieldName.startsWith( userFieldName + "_" ) ) {
                        userFieldName = solrFieldName;
                        break;
                    }
                }
            }

            outList.add( userFieldName );
        }

        return outList;
    }

    public LukeResponse getSchemaInfo() {
        String url = getArchiveUrl( true );
        LukeResponse response = new LukeResponse();
        try {
            HttpSolrServer solrServer = new HttpSolrServer( url );
            LukeRequest lukeRequest = new LukeRequest();
            NamedList<Object> namedList = solrServer.request( lukeRequest );
            response.setResponse( namedList );

        } catch( Exception e ) {
            throw ServiceException.create( "url:" + url, e );
        }

        return response;
    }

    /**
     * Returns a visually formatted list of solr fields and their flags
     * fieldname    type    [flags]
     */
    public String getFmtSchemaFieldsInfo() {
        StringBuilder sb = new StringBuilder();
        LukeResponse response = getSchemaInfo();
        Map<String, LukeResponse.FieldInfo> fieldInfoMap  = response.getFieldInfo();
        List<String> keys = new ArrayList<String>();
        keys.addAll(fieldInfoMap.keySet());
        Collections.sort( keys );
        for( String key : keys ) {
            LukeResponse.FieldInfo fieldInfo = fieldInfoMap.get( key );
            EnumSet<FieldFlag> flags = fieldInfo.parseFlags(fieldInfo.getSchema());
            List<String> strFlags = new ArrayList<String>();
            for( FieldFlag flag : flags ) {
                strFlags.add( flag.getDisplay() );
            }
            sb.append( String.format("%-25s  %-15.15s  %s\n", fieldInfo.getName(), fieldInfo.getType(), strFlags ) );
        }

        return sb.toString();
    }

    public boolean validate( List<IValidation> validations, PrintWriter pw ) {
        Properties properties = getProperties();
        List<String> msgs = new ArrayList<String>();

        for( IValidation validation : validations ) {
            List<String> vmsgs = validation.validate( properties );
            msgs.addAll( vmsgs );
        }

        for( String msg : msgs ) {
            pw.println( msg );
        }

        return msgs.isEmpty();
    }

    /**
     * Returns a the list of cores affected between start and end date
     *
     * @param start -
     * @param end -
     * @return -
     */
    private List<CoreInfo> getShardList( Date start, Date end ) {
        List<CoreInfo> coreInfos = new ArrayList<CoreInfo>();

        // loop the core strings (201301, 201302 ...)
        for( String coreString : getCoreStrings( start, end ) ) {
            // find the matching core
            for( CoreInfo coreInfo : getCoreInfos() ) {
                if( coreInfo.getName().contains( coreString ) ) {
                    coreInfos.add( coreInfo );
                    break;
                }
            }
        }

        return coreInfos;
    }

    /**
     * Returns strings of the form 201301, 201302 for each month between start and end
     * @param start -
     * @param end -
     * @return -
     */
    List<String> getCoreStrings( Date start, Date end ) {
        List<String> coreStrings = new ArrayList<String>();

        Calendar startCalendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ), Locale.US );
        startCalendar.setTime( start );
        startCalendar.set( Calendar.DAY_OF_MONTH, 1 );

        Calendar endCalendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ), Locale.US );
        endCalendar.setTime( end );
        endCalendar.set( Calendar.DAY_OF_MONTH, 2 );

        do {
            String strCore = String.format( "%d%02d", startCalendar.get( Calendar.YEAR ),startCalendar.get( Calendar.MONTH ) + 1 );
            startCalendar.add( Calendar.MONTH, 1 );
            coreStrings.add( strCore );
        } while( startCalendar.before( endCalendar ) );

        return coreStrings;
    }

    /**
     * returns the shards param (url to each core) if there are at least 2 cores -
     * returns empty string if there is only one core
     * @return -
     */
    private String getShardsParam() {
        StringBuilder sb = new StringBuilder();

        List<CoreInfo> coreInfos = getCoreInfos();
        if( coreInfos.size() > 1 ) {
            for( CoreInfo coreInfo : coreInfos ) {
                sb.append( coreInfo.getShard() ).append( "," );
            }
        }

        sb.deleteCharAt( sb.length() - 1 );

        return sb.toString();
    }

    private String getArchiveUrl( boolean considerMulticore ) {
        Properties prop = getProperties();
        String url = prop.getProperty( "archive.url" );

        if( considerMulticore ) {
            // if multi core, change baseUrl to http://host:port/ipc-index/coreName/admin/luke
            List<CoreInfo> coreInfos = getCoreInfos( );
            if( !coreInfos.isEmpty() && coreInfos.size() > 1 ) {
                CoreInfo coreInfo = coreInfos.get( 0 );
                url = coreInfo.getUrl();
            }
        }

        return url;
    }

    private String getPropertiesFile() {
        String path = getBasePath();
        path += "archive.properties";
        return path;
    }

    private String getBasePath() {
        String path = null;

        try {
            StackTraceElement elements[] = Thread.currentThread().getStackTrace();
            StackTraceElement element = elements[ elements.length - 2 ];
            Class cls = Class.forName( element.getClassName() );
            URL url = cls.getProtectionDomain().getCodeSource().getLocation();
            path = url.getPath();
            path = path.replaceAll( ".*file:", "" ).replaceAll( "jar!.*", "jar" );
            path = path.substring( 0, path.lastIndexOf( '/' ) );
            File file = new File( path );
            if( !file.isDirectory() ) {
                path = null;
            }
        } catch( Exception ignore ) {}

        if( path == null ) {
            path = ".";
            try {
                path = new File( path ).getCanonicalPath();
            } catch( Exception ignore ) {}
        }

        return path + File.separatorChar;
    }

}