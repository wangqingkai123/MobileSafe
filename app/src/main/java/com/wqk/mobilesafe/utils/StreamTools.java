package com.wqk.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamTools {
    public static String readFromStream(InputStream is){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1){
                baos.write(buffer,0,len);
            }
            is.close();
            String ret = baos.toString();
            baos.close();
            return ret;
        }catch (Exception e){
            return "";
        }

    }
}
