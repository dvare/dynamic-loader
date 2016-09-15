package org.dvare.dynamic.loader;


import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicJarLoader {

    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public void addJar(File jar) {
        try {
            URL url = jar.toURI().toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            Method method = MethodUtils.getAccessibleMethod(URLClassLoader.class, "addURL", URL.class);
            method.invoke(urlClassLoader, url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
