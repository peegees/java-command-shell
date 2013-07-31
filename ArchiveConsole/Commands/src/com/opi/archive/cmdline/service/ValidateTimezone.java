package com.opi.archive.cmdline.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p/>
  */
public class ValidateTimezone implements IValidation {

    @Override
    public List<String> validate(Properties properties) {
        List<String> msgs = new ArrayList<String>();

        String timezone = properties.getProperty( "archive.timezone" );
        if( timezone == null ) {
            msgs.add( "please configure timezone with the configure command." );
        }

        return msgs;
    }
}