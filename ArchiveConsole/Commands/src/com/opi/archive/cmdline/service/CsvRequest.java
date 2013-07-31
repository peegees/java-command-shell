package com.opi.archive.cmdline.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.QueryRequest;

import java.io.OutputStream;

/**
 * <p/>
  */
public class CsvRequest extends QueryRequest {

    public CsvRequest( SolrQuery solrQuery, OutputStream os ) {
        super( solrQuery, METHOD.POST );
        setResponseParser( new CsvResponseParser( os ) );
    }
}