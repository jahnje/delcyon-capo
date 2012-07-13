package com.delcyon.capo.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogPrefixFormatter extends Formatter
{

    private String prefix;
    private Formatter parentFormatter = new SimpleFormatter();

    public LogPrefixFormatter(String prefix)
    {
        this.prefix = prefix;
    }
    
    @Override
    public String format(LogRecord record)
    {
        String originalFormat = parentFormatter.format(record);
        return prefix+originalFormat;
    }

}
