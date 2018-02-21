package com.parserbox.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MemoryProfilerUtil extends Thread {
    private Log log = LogFactory.getLog(this.getClass());
    boolean kill = false;

    public MemoryProfilerUtil() {}


    public void run() {
        super.run();
        try {
            do {
                if (kill) break;


            } while(true);

        } catch (RuntimeException e) {
            throw e;
        }
    }



}
