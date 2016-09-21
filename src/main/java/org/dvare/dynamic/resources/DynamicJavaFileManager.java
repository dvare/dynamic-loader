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

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private DynamicClassLoader classLoader;
    private List<CompiledCode> compiledCodes;


    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager   delegate to this file manager
     * @param classLoader
     * @param compiledCodes
     * @param classLoader
     */
    public DynamicJavaFileManager(JavaFileManager fileManager, List<CompiledCode> compiledCodes, DynamicClassLoader classLoader) {
        super(fileManager);
        this.compiledCodes = compiledCodes;
        this.classLoader = classLoader;
        this.classLoader.registerCodes(compiledCodes);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

        for (CompiledCode compiledCode : compiledCodes) {
            if (compiledCode.getName().equals(className)) {
                return compiledCode;
            }
        }
        //nested class found
        try {
            CompiledCode innerClass = new CompiledCode(className);
            compiledCodes.add(innerClass);
            classLoader.registerCode(innerClass); //register in classloader
            return innerClass;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }


    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return classLoader;
    }
}
