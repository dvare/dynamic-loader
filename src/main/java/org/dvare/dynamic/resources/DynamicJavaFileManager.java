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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private static Logger logger = LoggerFactory.getLogger(DynamicJavaFileManager.class);
    private DynamicClassLoader classLoader;
    private List<MemoryByteCode> byteCodes = new ArrayList<>();


    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     * @param classLoader class Loader
     */
    public DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;

    }


    @Override
    public JavaFileObject getJavaFileForOutput(
            JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {

        for (MemoryByteCode byteCode : byteCodes) {
            if (byteCode.getClassName().equals(className)) {
                return byteCode;
            }
        }

        try {
            MemoryByteCode innerClass = new MemoryByteCode(className);
            byteCodes.add(innerClass);
            classLoader.registerCompiledSource(innerClass);
            return innerClass;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


        return null;
    }


    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }
}
