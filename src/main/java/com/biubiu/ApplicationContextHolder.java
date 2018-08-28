package com.biubiu;

import org.springframework.context.ApplicationContext;

/**
 * Created by Haibiao.Zhang on 2018/8/27.
 */
public class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

}
