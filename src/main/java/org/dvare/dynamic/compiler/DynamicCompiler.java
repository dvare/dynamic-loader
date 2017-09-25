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
import org.dvare.dynamic.resources.DynamicClassLoader;
import org.dvare.dynamic.resources.DynamicJavaFileManager;
import org.dvare.dynamic.resources.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DynamicCompiler {
    private static Logger logger = LoggerFactory.getLogger(DynamicCompiler.class);
    private final JavaCompiler javaCompiler;
    private final StandardJavaFileManager standardFileManager;
    private List<String> options = new ArrayList<>();
    private DynamicClassLoader dynamicClassLoader;

    private Collection<JavaFileObject> compilationUnits = new ArrayList<>();

    private ClassLoader classLoader;
    private String classpath = "";

    private boolean updateClassLoader = false;
    private boolean extractJar = false;
    private StringBuilder classpathBuilder = new StringBuilder();


    public DynamicCompiler() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public DynamicCompiler(ClassLoader classLoader) {
        this(classLoader, ToolProvider.getSystemJavaCompiler());
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler) {
        this(classLoader, javaCompiler, false, false, false);
    }

    public DynamicCompiler(ClassLoader classLoader, JavaCompiler javaCompiler,
                           boolean writeClassFile, boolean separateClassLoader, boolean extractJar) {

        this.classLoader = classLoader;
        this.javaCompiler = javaCompiler;
        standardFileManager = javaCompiler.getStandardFileManager(null, null, null);

        this.extractJar = extractJar;

        options.add("-Xlint:unchecked");
        if (classLoader instanceof URLClassLoader) {
            getClassPath(URLClassLoader.class.cast(classLoader));
        }

        dynamicClassLoader = new DynamicClassLoader(classLoader, writeClassFile, separateClassLoader);

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

        File file = new File(url.getFile());
        if (file.exists()) {
            classpath = classpath + file.getAbsolutePath() + File.pathSeparator;
            logger.debug(file.getAbsolutePath() + File.pathSeparator);

            if (classLoader instanceof URLClassLoader) {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, url);
            }
        }

    }


    public Map<String, Class<?>> build() throws DynamicCompilerException {


        if (!classpath.trim().isEmpty()) {
            options.add("-classpath");
            options.add(classpath);
        }


        JavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, dynamicClassLoader);


        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, collector, options, null, compilationUnits);

        if (updateClassLoader) {
            Thread.currentThread().setContextClassLoader(dynamicClassLoader);
        }

        try {

            if (!compilationUnits.isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {
                    throw new DynamicCompilerException("Compilation Error", collector.getDiagnostics());
                }
            }

            return dynamicClassLoader.getClasses();
        } catch (ClassFormatError | ClassNotFoundException e) {
            throw new DynamicCompilerException(e);
        } finally {
            compilationUnits.clear();

        }


    }


    private void getClassPath(URLClassLoader urlClassLoader) {
        try {

            {
                URL url = urlClassLoader.findResource("META-INF/MANIFEST.MF");
                if (url != null) {
                    Manifest manifest = new Manifest(url.openStream());

                    String classpath = manifest.getMainAttributes().getValue("Class-Path");
                    if (classpath != null) {
                        for (String entry : classpath.split(" ")) {
                            URL entryUrl = new URL(entry);
                            String decodedPath = URLDecoder.decode(entryUrl.getPath(), "UTF-8");
                            File file = new File(decodedPath);
                            if (file.exists()) {
                                classpathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
                                logger.debug(file.getAbsolutePath() + File.pathSeparator);
                            }
                        }
                    }
                }

            }
            if (classpathBuilder.toString().isEmpty()) {
                ClassLoader c = urlClassLoader;
                String destDir = null;
                while (c instanceof URLClassLoader) {
                    for (URL url : ((URLClassLoader) c).getURLs()) {
                        String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
                        File file = new File(decodedPath);
                        if (file.exists()) {
                            classpathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
                            logger.debug(file.getAbsolutePath() + File.pathSeparator);
                        } else if (extractJar) {
                            destDir = extractJar(url, destDir);
                        }
                    }
                    c = c.getParent();
                }
            }

            classpath = classpathBuilder.toString();


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }


    private String extractJar(URL url, String destDir) throws IOException {
        if (destDir == null) {

            logger.info("Jar Extraction complete ");

            String pathToJar;
            if (url.getPath().contains("file:")) {
                pathToJar = url.getPath().substring(5, url.getPath().indexOf(".jar") + 4);
            } else {
                pathToJar = url.getPath().substring(0, url.getPath().indexOf(".jar") + 4);
            }

            File file = new File(pathToJar);
            JarFile jar = new JarFile(file);

            destDir = file.getParent() + File.separator + file.getName().substring(0, file.getName().indexOf(".jar"));


            File destDirFile = new File(destDir);
            if (!destDirFile.exists()) {

                destDirFile.mkdir();


                logger.info("destDir: " + destDir);
                logger.info("pathToJar: " + pathToJar);


                Enumeration enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    JarEntry jarEntry = (java.util.jar.JarEntry) enumEntries.nextElement();
                    File f = new File(destDir + File.separator + jarEntry.getName());

                    if (jarEntry.isDirectory()) { // if its a directory, create it
                        f.mkdir();
                        continue;
                    }

                    if (!jarEntry.getName().endsWith(".class") && !jarEntry.getName().endsWith(".jar")) {
                        continue;
                    }

                    InputStream is = jar.getInputStream(jarEntry); // get the input stream
                    FileOutputStream fos = new FileOutputStream(f);
                    while (is.available() > 0) {  // write contents of 'is' to 'fos'
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                }
                jar.close();

            }

            logger.info("Jar Extraction complete ");

        }


        String pathToFile = destDir + url.getPath().substring(url.getPath().indexOf(".jar") + 5,
                url.getPath().indexOf("!/", url.getPath().indexOf(".jar") + 5));


        File file = new File(pathToFile);

        if (file.exists()) {
            classpathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
            logger.debug(file.getAbsolutePath() + File.pathSeparator);
        }

        return destDir;
    }

    /*Getter and Setters*/

    public ClassLoader getClassLoader() {
        return dynamicClassLoader;
    }

    public void setUpdateClassLoader(boolean updateClassLoader) {
        this.updateClassLoader = updateClassLoader;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

}