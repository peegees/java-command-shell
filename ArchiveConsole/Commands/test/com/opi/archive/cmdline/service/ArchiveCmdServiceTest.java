package com.opi.archive.cmdline.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 */
public class ArchiveCmdServiceTest {

    @Test
    public void testCoreStrings() {
        ArchiveCmdService service = ArchiveCmdService.getInstance();

        // I) Test 2013.01.01 - 2013.02.01
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone( "GMT+2" ), Locale.US );
        start.set( 2013, 0, 1, 0, 0, 0 );      // january

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone( "GMT+2" ), Locale.US );
        end.set(2013, 1, 1, 0, 0, 0);        // february

        List<String> coreStrings = service.getCoreStrings( start.getTime(), end.getTime() );
        Assert.assertArrayEquals( new Object[]{ "201212", "201301" }, coreStrings.toArray() );

        // II) Test 2013.01.01 - 2014.01.01
        start.clear();
        start.set( 2013, 0, 1, 0, 0, 0 );      // january

        end.clear();
        end.set( 2014, 0, 1, 0, 0, 0 );      // january
        coreStrings = service.getCoreStrings( start.getTime(), end.getTime() );
        Assert.assertArrayEquals(
                new Object[]{ "201212", "201301", "201302", "201303", "201304", "201305", "201306",
                        "201307", "201308", "201309", "201310", "201311", "201312" }, coreStrings.toArray() );
    }
}