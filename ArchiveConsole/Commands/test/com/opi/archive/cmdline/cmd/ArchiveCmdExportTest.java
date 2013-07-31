package com.opi.archive.cmdline.cmd;

import com.opi.archive.cmdline.service.ArchiveExportService;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Calendar;

/**
 */
public class ArchiveCmdExportTest {

    @Test
    public void testGap() {
        ArchiveCmdExport cmd = new ArchiveCmdExport();
        ArchiveExportService.ExportOptions exportOptions = new ArchiveExportService.ExportOptions();
        boolean result = cmd.setupGap( exportOptions, "+1MONTH" );
        Assert.assertTrue( result );
        Assert.assertEquals( 1, exportOptions.gapN );
        Assert.assertEquals(Calendar.MONTH, exportOptions.gapUnit );

        result = cmd.setupGap( exportOptions, "+2YEAR" );
        Assert.assertTrue( result );
        Assert.assertEquals( 2, exportOptions.gapN );
        Assert.assertEquals(Calendar.YEAR, exportOptions.gapUnit );

        result = cmd.setupGap( exportOptions, "+0DAY" );
        Assert.assertFalse( result );

        result = cmd.setupGap( exportOptions, "+3days" );
        Assert.assertTrue( result );
        Assert.assertEquals( 3, exportOptions.gapN );
        Assert.assertEquals(Calendar.DATE, exportOptions.gapUnit );

        result = cmd.setupGap( exportOptions, "+3dayx" );
        Assert.assertFalse( result );
    }
}