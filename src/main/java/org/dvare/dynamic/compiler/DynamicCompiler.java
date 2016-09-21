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


package org.dvare.dynamic.compiler;

import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.dvare.dynamic.loader.CustomClassLoader;
import org.dvare.dynamic.resources.CompiledCode;
import org.dvare.dynamic.resources.DynamicClassLoader;
import org.dvare.dynamic.resources.DynamicJavaFileManager;
import org.dvare.dynamic.resources.SourceCode;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicCompiler {
    private static final List<String> options = new ArrayList<>(Arrays.asList("-Xlint:unchecked"));
    private static javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    DynamicClassLoader dynamicClassLoader;
    private Map<String, SourceCode> sourceCode = new HashMap<>();
    private List<URL> jars = new ArrayList<>();
    private boolean separateContext = false;
    private boolean updateContextClassLoader = false;

    public void addJar(URL url) {
        jars.add(url);
    }

    public void addSource(String className, String testSourceCode)
            throws Exception {
        sourceCode.put(className, new SourceCode(className, testSourceCode));
    }

    public void addSource(String classPackage, String className, String testSourceCode)
            throws Exception {
        sourceCode.put(className, new SourceCode(classPackage + "." + className, testSourceCode));
    }

    public Map<String, Class<?>> build() throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (separateContext) {
            classLoader = new CustomClassLoader().getCustomURLClassLoader();
        }

        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        if (!jars.isEmpty()) {
            try {
                for (URL url : jars) {
                    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(urlClassLoader, new Object[]{url});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Method method2 = URLClassLoader.class.getDeclaredMethod("getURLs");
        method2.setAccessible(true);
        URL[] urls = (URL[]) method2.invoke((URLClassLoader) classLoader);
        String classpath = "";
        for (URL url : urls) {
            classpath += url + File.pathSeparator;
        }

        options.addAll(Arrays.asList("-classpath", classpath));


        Collection<SourceCode> compilationUnits = sourceCode.values();
        List<CompiledCode> compiledCodes = new ArrayList<>();

        for (SourceCode sourceCode : compilationUnits) {
            compiledCodes.add(new CompiledCode(sourceCode.getClassName()));
        }

      /*  StandardJavaFileManager stdFileManager =
                compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager
                .getJavaFileObjectsFromFiles(filesToCompile);

        Iterable<? extends JavaFileObject> compilationUnits = stdFileManager
                .getJavaFileObjectsFromStrings(Arrays.asList("MyClass.java"));
        */

        dynamicClassLoader = new DynamicClassLoader(urlClassLoader);
        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCodes, dynamicClassLoader);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);


        if (updateContextClassLoader) {
            Thread.currentThread().setContextClassLoader(dynamicClassLoader);
        }

        try {

            if (!sourceCode.keySet().isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {
                    throw new DynamicCompilerException(collector.getDiagnostics());
                }
            }

            Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
            for (String className : sourceCode.keySet()) {
                classes.put(className, dynamicClassLoader.loadClass(className));
            }
            return classes;
        } catch (ClassFormatError e) {
            throw new DynamicCompilerException(collector.getDiagnostics());
        }


    }




    /*Getter and Setters*/

    public void setSeparateContext(boolean separateContext) {
        this.separateContext = separateContext;
    }

    public ClassLoader getClassLoader() {
        return dynamicClassLoader;
    }


    public void setUpdateContextClassLoader(boolean updateContextClassLoader) {
        this.updateContextClassLoader = updateContextClassLoader;
    }


}