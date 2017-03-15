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

import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {
    private static final char PKG_SEPARATOR = '.';

    private static final char DIR_SEPARATOR = '/';

    private static final String CLASS_FILE_SUFFIX = ".class";
    private Map<String, CompiledCode> customCompiledCode = new HashMap<>();
    private boolean updateClassFile;

    public DynamicClassLoader(ClassLoader parent, boolean updateClassFile) {
        super(parent);
        this.updateClassFile = updateClassFile;
    }

    void registerCodes(List<CompiledCode> compiledCodes) {

        for (CompiledCode cc : compiledCodes) {
            customCompiledCode.put(cc.getName(), cc);
        }
    }

    void registerCode(CompiledCode cc) {
        customCompiledCode.put(cc.getName(), cc);
    }


    public Class<?> getClass(String name)
            throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            return findClass(name);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        CompiledCode cc = customCompiledCode.get(name);
        if (cc == null) {
            return super.findClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        if (updateClassFile) {
            try {
                String classFileName = name;
                classFileName = classFileName.replace(PKG_SEPARATOR, DIR_SEPARATOR);
                classFileName += CLASS_FILE_SUFFIX;
                URL ClassFilePath = this.getResource(classFileName);
                if (ClassFilePath != null) {
                    FileOutputStream outputStream = new FileOutputStream(ClassFilePath.getFile());
                    outputStream.write(byteCode);
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return super.defineClass(name, byteCode, 0, byteCode.length);
    }
}
