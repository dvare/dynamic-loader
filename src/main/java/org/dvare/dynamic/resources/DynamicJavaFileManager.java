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
        this.classLoader.addCodes(compiledCodes);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return getCompiledCode(className);
    }


    private CompiledCode getCompiledCode(String className) {
        for (CompiledCode compiledCode : compiledCodes) {
            if (compiledCode.getName().equals(className)) {
                return compiledCode;
            }
        }
        //nested class
        try {
            CompiledCode innerClass = new CompiledCode(className);
            compiledCodes.add(innerClass);
            classLoader.addCode(innerClass);
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
