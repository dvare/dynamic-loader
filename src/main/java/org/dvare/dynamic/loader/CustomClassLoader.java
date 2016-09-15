package org.dvare.dynamic.loader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomClassLoader extends ClassLoader {
    private CustomURLClassLoader customURLClassLoader;

    public CustomClassLoader() {
        super(Thread.currentThread().getContextClassLoader());

        try {
            Method method2 = URLClassLoader.class.getDeclaredMethod("getURLs");
            method2.setAccessible(true);
            URL[] urls = (URL[]) method2.invoke((URLClassLoader) Thread.currentThread().getContextClassLoader());
            customURLClassLoader = new CustomURLClassLoader(urls, new DetectClass(this.getParent()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CustomURLClassLoader getCustomURLClassLoader() {
        return customURLClassLoader;
    }

    public void setCustomURLClassLoader(CustomURLClassLoader customURLClassLoader) {
        this.customURLClassLoader = customURLClassLoader;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return customURLClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }

    public class CustomURLClassLoader extends URLClassLoader {
        private DetectClass realParent;

        public CustomURLClassLoader(URL[] urls, DetectClass realParent) {
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                Class<?> loaded = super.findLoadedClass(name);
                if (loaded != null)
                    return loaded;
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                return realParent.loadClass(name);
            }
        }
    }

    private class DetectClass extends ClassLoader {
        public DetectClass(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
}