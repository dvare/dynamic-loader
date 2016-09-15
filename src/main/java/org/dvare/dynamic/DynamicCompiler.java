package org.dvare.dynamic;

import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.dvare.dynamic.resources.CompiledCode;
import org.dvare.dynamic.resources.DynamicClassLoader;
import org.dvare.dynamic.resources.DynamicJavaFileManager;
import org.dvare.dynamic.resources.SourceCode;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.*;

public class DynamicCompiler {
    private static final Iterable<String> options = Collections.singletonList("-Xlint:unchecked");
    private static javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private Map<String, SourceCode> sourceCode = new HashMap<>();


    public void addSource(String className, String testSourceCode)
            throws Exception {
        sourceCode.put(className, new SourceCode(className, testSourceCode));
    }

    public void addSource(String classPackage, String className, String testSourceCode)
            throws Exception {
        sourceCode.put(className, new SourceCode(classPackage + "." + className, testSourceCode));
    }

    public Map<String, Class<?>> compileSource() throws Exception {
        Collection<SourceCode> compilationUnits = sourceCode.values();
        List<CompiledCode> compiledCodes = new ArrayList<>();

        for (SourceCode sourceCode : compilationUnits) {
            compiledCodes.add(new CompiledCode(sourceCode.getClassName()));
        }

        DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(classLoader);
        DynamicJavaFileManager fileManager = new DynamicJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCodes, dynamicClassLoader);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector,
                options, null, compilationUnits);


        try {
            boolean result = task.call();

            if (!result || collector.getDiagnostics().size() > 0) {
                throw new DynamicCompilerException(collector.getDiagnostics());
            }

            Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
            for (String className : sourceCode.keySet()) {
                classes.put(className, classLoader.loadClass(className));
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