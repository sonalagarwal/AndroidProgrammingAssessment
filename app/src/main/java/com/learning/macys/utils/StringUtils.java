package com.learning.macys.utils;

/**
 * Created by sonal on 3/2/18.
 */

public class StringUtils {

    public static String getExt(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }
}
