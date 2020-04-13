package org.dvare.dynamic.resources;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.*;


public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final DynamicClassLoader classLoader;
    private final JavaClassesFinder finder;

    public MemoryFileManager(JavaFileManager standardJavaFileManager, DynamicClassLoader classLoader) {
        super(standardJavaFileManager);
        this.classLoader = classLoader;
        classLoader.getRamFileSystem().put(new LocationAndKind(StandardLocation.CLASS_OUTPUT, Kind.CLASS), new HashMap<>());
        finder = new JavaClassesFinder(classLoader);
    }


    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
                                         boolean recurse) throws IOException {

        List<JavaFileObject> result = new ArrayList<>();
        if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return super.list(location, packageName, kinds, recurse);
        } else if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
            for (JavaFileObject f : super.list(location, packageName, kinds, recurse)) {
                result.add(f);
            }
            result.addAll(finder.listAll(packageName));
        }
        result.addAll(addInMemoryClasses(location, packageName, kinds, recurse));
        return result;
    }

    private List<JavaFileObject> addInMemoryClasses(Location location, String pkg, Set<Kind> kinds, boolean recurse) {
        List<JavaFileObject> result = new ArrayList<>();

        if (location == StandardLocation.CLASS_PATH) {
            location = StandardLocation.CLASS_OUTPUT;
        }

        for (Kind kind : kinds) {
            LocationAndKind key = new LocationAndKind(location, kind);
            if (classLoader.getRamFileSystem().containsKey(key)) {
                Map<String, JavaFileObject> locatedFiles = classLoader.getRamFileSystem().get(key);
                for (Map.Entry<String, JavaFileObject> entry : locatedFiles.entrySet()) {
                    JavaFileObject processedFile = processLocatedFile(pkg, kinds, recurse, entry);
                    if (processedFile != null) {
                        result.add(processedFile);
                    }
                }
            }
        }

        return result;
    }


    private JavaFileObject processLocatedFile(String pkg, Set<Kind> kinds, boolean recurse, Map.Entry<String, JavaFileObject> entry) {
        String name = entry.getKey();
        String packageName = "";
        if (name.indexOf('.') > -1) {
            packageName = name.substring(0, name.lastIndexOf('.'));
        }
        if (recurse ? packageName.startsWith(pkg) : packageName.equals(pkg)) {
            JavaFileObject candidate = entry.getValue();
            if (kinds.contains(candidate.getKind())) {
                return candidate;
            }
        }
        return null;
    }


    @Override
    public boolean hasLocation(Location location) {
        return (location == StandardLocation.SOURCE_PATH
                || location == StandardLocation.CLASS_PATH
                || location == StandardLocation.PLATFORM_CLASS_PATH);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (location == StandardLocation.SOURCE_PATH) {
            return null;
        }
        String fileName = file.getName();
        String classname;
        if (!fileName.contains(".jar!")) {
            classname = fileName.replace('/', '.').replace('\\', '.');
        } else {
            classname = fileName.substring(0, fileName.indexOf(".jar!") + 5) + (fileName.substring(fileName.indexOf(".jar!") + 5).replace('/', '.').replace('\\', '.'));
        }

        classname = classname.substring(0, classname.lastIndexOf(".class"));
        return classname;
    }


}
