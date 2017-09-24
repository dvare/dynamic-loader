/*The MIT License (MIT)

Copyright (c) 2016 Muhammad Hammad

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Sogiftware.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/


package org.dvare.dynamic.resources;

import org.dvare.dynamic.loader.CustomClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {
    private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
    private Map<String, MemoryByteCode> byteCodes = new HashMap<>();
    private boolean writeClassFile;

    public DynamicClassLoader(ClassLoader ClassLoader) {
        this(ClassLoader, false, false);
    }

    public DynamicClassLoader(ClassLoader ClassLoader, boolean separateClassLoader,
                              boolean writeClassFile) {
        super(separateClassLoader ? new CustomClassLoader(ClassLoader).getCustomURLClassLoader() : ClassLoader);
        this.writeClassFile = writeClassFile;
    }


    public void registerCompiledSource(MemoryByteCode byteCode) {
        byteCodes.put(byteCode.getClassName(), byteCode);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        MemoryByteCode byteCode = byteCodes.get(name);
        if (byteCode == null) {
            return super.findClass(name);
        }

        if (writeClassFile) {
            writeClass(name, byteCode.getByteCode());
        }

        return super.defineClass(name, byteCode.getByteCode(), 0, byteCode.getByteCode().length);
    }

    private void writeClass(String name, byte[] byteCode) {
        try {
            final char PKG_SEPARATOR = '.';
            final char DIR_SEPARATOR = '/';
            final String CLASS_FILE_SUFFIX = ".class";
            String classFileName = name.replace(PKG_SEPARATOR, DIR_SEPARATOR) + CLASS_FILE_SUFFIX;
            URL ClassFilePath = this.getResource(classFileName);
            if (ClassFilePath != null) {
                FileOutputStream outputStream = new FileOutputStream(ClassFilePath.getFile());
                outputStream.write(byteCode);
                outputStream.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public Map<String, Class<?>> getClasses() throws ClassNotFoundException {
        Map<String, Class<?>> classes = new HashMap<>();
        for (MemoryByteCode byteCode : byteCodes.values()) {
            classes.put(byteCode.getClassName(), findClass(byteCode.getClassName()));
        }
        return classes;
    }
}
