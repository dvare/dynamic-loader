/*The MIT License (MIT)

Copyright (c) 2020 Muhammad Hammad

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
import org.dvare.dynamic.exceptions.JavaCompilerNotFoundException;
import org.dvare.dynamic.loader.ClassPathBuilder;
import org.dvare.dynamic.resources.DynamicClassLoader;
import org.dvare.dynamic.resources.DynamicJavaFileManager;
import org.dvare.dynamic.resources.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicCompiler {
    private static final Logger log = LoggerFactory.getLogger(DynamicCompiler.class);
    private final JavaCompiler javaCompiler;
    private final StandardJavaFileManager standardFileManager;
    private final List<String> options = new ArrayList<>();
    private final DynamicClassLoader dynamicClassLoader;

    private final Collection<JavaFileObject> compilationUnits = new ArrayList<>();
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<>();
    private final ClassLoader classLoader;
    private String classpath = "";

    public DynamicCompiler() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public DynamicCompiler(ClassLoader classLoader) {
        this(classLoader, ToolProvider.getSystemJavaCompiler());
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler) {
        this(classLoader, javaCompiler, false);
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler, boolean writeClassFile) {

        if (javaCompiler == null) {
            throw new JavaCompilerNotFoundException("Java Compiler not found. JDK is required");
        }

        this.classLoader = classLoader;
        this.javaCompiler = javaCompiler;
        standardFileManager = javaCompiler.getStandardFileManager(null, null, null);


        options.add("-Xlint:unchecked");
        if (classLoader instanceof URLClassLoader) {
            classpath = new ClassPathBuilder().getClassPath((URLClassLoader) classLoader);
        } else {
            classpath = new ClassPathBuilder().getClassPath();
        }

        dynamicClassLoader = new DynamicClassLoader(classLoader, writeClassFile);

    }

    public void addSource(String className, String source) {
        addSource(new StringSource(className, source));
    }

    public void addSource(File sourceFile) {
        Iterable<? extends JavaFileObject> compilationUnitsIterable =
                standardFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(sourceFile));
        for (JavaFileObject javaFileObject : compilationUnitsIterable) {
            addSource(javaFileObject);

        }
    }

    public void addSource(JavaFileObject javaFileObject) {
        compilationUnits.add(javaFileObject);
    }

    public void addJar(URL url) throws Exception {
        if (url == null) {
            return;
        }
        File file = new File(url.getFile());
        if (file.exists()) {
            classpath = classpath + file.getAbsolutePath() + File.pathSeparator;
            log.debug(file.getAbsolutePath() + File.pathSeparator);

            if (classLoader instanceof URLClassLoader) {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, url);
            }
        }

    }


    public Map<String, Class<?>> build() throws DynamicCompilerException {

        errors.clear();
        warnings.clear();

        if (!classpath.trim().isEmpty()) {
            options.add("-classpath");
            options.add(classpath);
        }

        try {
            Class.forName("com.sun.tools.sjavac.Module");
            options.add("--add-exports");
            options.add("java.base/java.lang=ALL-UNNAMED");
        } catch (Exception ignored) {
        }

        JavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, dynamicClassLoader);

        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, collector, options, null, compilationUnits);


        try {

            if (!compilationUnits.isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {

                    for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                        switch (diagnostic.getKind()) {
                            case NOTE:
                            case MANDATORY_WARNING:
                            case WARNING:
                                warnings.add(diagnostic);
                                break;
                            case OTHER:
                            case ERROR:
                            default:
                                errors.add(diagnostic);
                                break;
                        }

                    }


                    if (!errors.isEmpty()) {
                        throw new DynamicCompilerException("Compilation Error", errors);
                    }
                }
            }

            return dynamicClassLoader.getClasses();
        } catch (ClassFormatError | ClassNotFoundException e) {
            throw new DynamicCompilerException(e);
        } finally {
            compilationUnits.clear();

        }


    }


    private List<String> diagnosticToString(List<Diagnostic<? extends JavaFileObject>> diagnostics) {

        List<String> diagnosticMessages = new ArrayList<>();

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            diagnosticMessages.add("line: " + diagnostic.getLineNumber() + ", message: " + diagnostic.getMessage(Locale.US));
        }

        return diagnosticMessages;

    }


    public List<String> getErrors() {
        return diagnosticToString(errors);
    }

    public List<String> getWarnings() {
        return diagnosticToString(warnings);
    }

    public ClassLoader getClassLoader() {
        return dynamicClassLoader;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }


}