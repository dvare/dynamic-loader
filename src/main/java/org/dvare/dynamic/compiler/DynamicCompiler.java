package org.dvare.dynamic.compiler;

import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.dvare.dynamic.resources.CompiledCode;
import org.dvare.dynamic.resources.DynamicClassLoader;
import org.dvare.dynamic.resources.DynamicJavaFileManager;
import org.dvare.dynamic.resources.SourceCode;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicCompiler {
    private static final List<String> options = new ArrayList<>(Arrays.asList("-Xlint:unchecked"));
    private static javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private Map<String, SourceCode> sourceCode = new HashMap<>();
    private List<URL> jars = new ArrayList<>();

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

    public Map<String, Class<?>> compileSource() throws Exception {
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

        options.addAll(Arrays.asList("-classpath",classpath));


        Collection<SourceCode> compilationUnits = sourceCode.values();
        List<CompiledCode> compiledCodes = new ArrayList<>();

        for (SourceCode sourceCode : compilationUnits) {
            compiledCodes.add(new CompiledCode(sourceCode.getClassName()));
        }

        DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(urlClassLoader);
        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCodes, dynamicClassLoader);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);


        try {
            boolean result = task.call();

            if (!result || collector.getDiagnostics().size() > 0) {
                throw new DynamicCompilerException(collector.getDiagnostics());
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


    public Class<?> compile(String className, String sourceCodeInText) throws Exception {
        addSource(className, sourceCodeInText);
        Map<String, Class<?>> compiled = compileSource();
        Class<?> compiledClass = compiled.get(className);
        return compiledClass;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}