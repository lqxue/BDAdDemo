package com.baidu.mobads.demo.main.mediaExamples.novel.utils;

import java.io.Closeable;
import java.io.IOException;



public class IOUtils {

    public static void close(Closeable closeable){
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
            //close error
        }
    }
}
