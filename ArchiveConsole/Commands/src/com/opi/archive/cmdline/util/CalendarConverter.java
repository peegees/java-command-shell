package com.opi.archive.cmdline.util;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import com.opi.archive.cmdline.service.ArchiveCmdService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p/>
  */
public class CalendarConverter extends BaseConverter<Calendar> {

    public CalendarConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Calendar convert( String value ) {
        Calendar calendar = null;
        String strTimezone = null;

        try {
            Date date;
            // get timezone
            ArchiveCmdService service = ArchiveCmdService.getInstance();
            Properties prop = service.getProperties();
            strTimezone = prop.getProperty( "archive.timezone" );
            TimeZone timeZone = TimeZone.getTimeZone( strTimezone );

            char c = value.charAt( 0 );
            boolean isRelative =  c == '/' || c == '+' || c == '-';
            if( isRelative ) {
                date = convertRelativeDate( value, timeZone );
            } else {
                date = convertAbsoluteDate( value, timeZone );
            }

            // create calendar
            calendar = Calendar.getInstance( timeZone, Locale.US );
            calendar.setTime( date );
        } catch( Exception e ) {
            throw new ParameterException( getErrorString( value, " date (yyyy-MM-dd)" ) );
            //throw ServiceException.create( "cannot convert date:" + value + " timezone:" + strTimezone, e );
        }

        return calendar;
    }

    private Date convertRelativeDate(String value, TimeZone timeZone) throws ParseException {
        DateMathParser dmp = new DateMathParser( timeZone, Locale.US );
        return dmp.parseMath( value );
    }

    private Date convertAbsoluteDate(String value, TimeZone timeZone) throws ParseException {
        // parse date
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );
        sdf.setTimeZone( timeZone );
        Date date = sdf.parse( value );

        return date;
    }
}