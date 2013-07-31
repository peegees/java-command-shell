package com.opi.archive.cmdline.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p/>
  */
public class ValidateUrl implements IValidation {
    @Override
    public List<String> validate(Properties properties) {
        List<String> msgs = new ArrayList<String>();
        String strUrl = properties.getProperty("archive.url");

        if( strUrl == null ) {
            msgs.add( "please configure archive url with the configure command." );
        } else {
            try {
                // open url and read content
                URL url = new URL( strUrl + "/admin/initializer" );
                String content = readContent(url);

                // check content
                if( content.contains( "<initialized>true" ) ) {
                    // ok
                } else {
                    msgs.add( "The archive is not initialized. Please initialize the archive using the ipc archive module." );
                }
            } catch( Exception e ) {
                msgs.add( "cannot access the url '" + strUrl + "' " + e.getClass().getSimpleName() + ":" + e.getMessage() );
            }
        }

        return msgs;
    }

    private String readContent( URL url ) throws IOException {
        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
        String line = null;
        StringBuilder sb = new StringBuilder();
        do {
            line = br.readLine();
            if( line != null ) {
                sb.append( line );
            }
        } while( line != null );

        return sb.toString();
    }
}