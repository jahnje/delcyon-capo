package com.delcyon.capo.util;

import java.util.HashMap;

public class InternHashMap extends HashMap<String, String>
{
    private static final String[] CommonStrings = new String[]{"0","false","true"};
    
    public String put(String key, String value) {
        for (String commonString : CommonStrings)
        {
            if(commonString.equals(value))
            {
                value = commonString;
                return super.put(key, commonString);
            }
        }
        return super.put(key.intern(), value);
    };
    
}
