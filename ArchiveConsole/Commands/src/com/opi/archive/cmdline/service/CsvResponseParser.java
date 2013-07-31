package com.opi.archive.cmdline.service;

import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.common.util.NamedList;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;

/**
 * <p/>
  */
public class CsvResponseParser extends ResponseParser {

    private OutputStream os = null;


    public CsvResponseParser( OutputStream os ) {
            this.os = os;
    }

    @Override
    public String getWriterType() {
        return "csv";
    }

    @Override
    public NamedList<Object> processResponse( InputStream inputStream, String encoding ) {

        try {
            BufferedInputStream bis = new BufferedInputStream( inputStream );

            int b = bis.read();
            while( b >= 0 ) {
                os.write( b );
                b = bis.read();
            }
        } catch( Exception e ) {
            throw ServiceException.create( e );
        }

        return null;
    }

    @Override
    public NamedList<Object> processResponse(Reader reader) {
        throw new NotImplementedException();
    }
}