package com.opi.archive.cmdline.service;

import java.util.Date;

/**
 * <p/>
  */
public class CoreInfo {
    // name of the core ex: in.part:201101
    private String name;
    // absolute path name to the core ex: c:/idx/in.part.201101/index
    private String dataDir;
    // ex: http://localhost:8984/index/in.part.201101
    private String url;
    private Integer numDocs;
    private Integer maxDoc;
    private Integer segmentCount;
    private Boolean hasDeletions;
    private Date lastModified;

    public void setName(String name) {
        this.name = name;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getName() {
        return name;
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShard() {
        return url.replace("http://", "");
    }

    public void setNumDocs(Integer numDocs) {
        this.numDocs = numDocs;
    }

    public Integer getNumDocs() {
        return numDocs;
    }

    public Integer getMaxDoc() {
        return maxDoc;
    }

    public void setMaxDoc( Integer maxDoc ) {
        this.maxDoc = maxDoc;
    }

    public Integer getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount( Integer segmentCount ) {
        this.segmentCount = segmentCount;
    }

    public Boolean getHasDeletions() {
        return hasDeletions;
    }

    public void setHasDeletions( Boolean hasDeletions ) {
        this.hasDeletions = hasDeletions;
    }

    /**
     * Returns true if the core has more than 10 segments
     * optimization
     *   - reduces the number of segments to 1
     *   - sets hasDeletions = false
     *   - sets numDocs == maxDoc
     *
     * @return -
     */
    public Boolean isOptimized() {
        return segmentCount < 10;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
