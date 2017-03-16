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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicCompiler {
    private static Logger logger = LoggerFactory.getLogger(DynamicCompiler.class);
    private final javax.tools.JavaCompiler javac;
    private final StandardJavaFileManager standardFileManager;
    private List<String> options;
    private DynamicClassLoader dynamicClassLoader;
    private Map<String, JavaFileObject> sourceCodes = new HashMap<>();
    private List<URL> jars = new ArrayList<>();
    private String classpath = "";
    private boolean separateContext = false;
    private boolean updateContextClassLoader = false;
    private boolean updateClassFile = false;


    public DynamicCompiler() {
        options = new ArrayList<>(Arrays.asList("-Xlint:unchecked"));
        javac = ToolProvider.getSystemJavaCompiler();
        standardFileManager =
                javac.getStandardFileManager(null, null, null);
    }

    public void addSource(String className, String testSourceCode) {
        sourceCodes.put(className, new SourceCode(className, testSourceCode));
    }


    public void addSource(String className, File sourceFile) {
        Iterable<? extends JavaFileObject> compilationUnits = standardFileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
        for (JavaFileObject javaFileObject : compilationUnits) {
            sourceCodes.put(className, javaFileObject);
        }
    }


    public void addJar(URL url) {
        jars.add(url);
    }


    public Map<String, Class<?>> build() throws DynamicCompilerException {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        if (separateContext) {
            classLoader = new CustomClassLoader().getCustomURLClassLoader();
        }


        if (!(classLoader instanceof URLClassLoader)) {
            classLoader = this.getClass().getClassLoader();
        }


        if (classLoader instanceof URLClassLoader) {

            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

            try {
                if (!jars.isEmpty()) {

                    for (URL url : jars) {
                        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                        method.setAccessible(true);
                        method.invoke(urlClassLoader, new Object[]{url});
                    }
                }

                Method method2 = URLClassLoader.class.getDeclaredMethod("getURLs");
                method2.setAccessible(true);
                URL[] urls = (URL[]) method2.invoke(classLoader);

                if (urls.length == 0) {

                    urls = (URL[]) method2.invoke(getClass().getClassLoader());
                }
                for (URL url : urls) {
                    File file = new File(url.getFile());

                    classpath += file.getAbsolutePath() + File.pathSeparator;
                    logger.debug(file.getAbsolutePath() + File.pathSeparator);
                }

                if (!classpath.trim().isEmpty()) {
                    options.addAll(Arrays.asList("-classpath", classpath));
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        dynamicClassLoader = new DynamicClassLoader(classLoader, updateClassFile);
        Collection<JavaFileObject> compilationUnits = sourceCodes.values();
        List<CompiledCode> compiledCodes = new ArrayList<>();

        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, compiledCodes, dynamicClassLoader);


        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);


        if (updateContextClassLoader) {
            Thread.currentThread().setContextClassLoader(dynamicClassLoader);
        }

        try {

            if (!sourceCodes.keySet().isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {
                    throw new DynamicCompilerException(collector.getDiagnostics());
                }
            }

            Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
            for (String className : sourceCodes.keySet()) {
                classes.put(className, dynamicClassLoader.getClass(className));
            }
            return classes;
        } catch (ClassFormatError | ClassNotFoundException e) {
            throw new DynamicCompilerException(e);
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

    public void setUpdateClassFile(boolean updateClassFile) {
        this.updateClassFile = updateClassFile;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
}