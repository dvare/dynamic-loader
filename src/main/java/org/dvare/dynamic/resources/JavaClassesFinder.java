package org.dvare.dynamic.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static javax.tools.JavaFileObject.Kind.CLASS;

class JavaClassesFinder {
    private static final Logger logger = LoggerFactory.getLogger(DynamicJavaFileManager.class);
    private final ClassLoader classLoader;

    JavaClassesFinder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    public List<JavaFileObject> listAll(String packageName) throws IOException {
        String javaPackageName = packageName.replaceAll("\\.", "/");

        List<JavaFileObject> result = new ArrayList<>();

        Enumeration<URL> urlEnumeration = classLoader.getResources(javaPackageName);
        while (urlEnumeration.hasMoreElements()) {
            URL packageFolderURL = urlEnumeration.nextElement();
            result.addAll(listUnder(packageName, packageFolderURL));
        }
        return result;
    }

    private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) throws IOException {
        File directory = new File(packageFolderURL.getFile());
        if (directory.isDirectory()) {
            return processDir(packageName, directory);
        } else {
            return processJar(packageFolderURL);
        }
    }

    private List<JavaFileObject> processJar(URL packageFolderURL) throws IOException {
        List<JavaFileObject> result = new ArrayList<>();
        JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
        String jarUri = jarConn.getJarFileURL().toString();
        String rootEntryName = jarConn.getEntryName() != null ? jarConn.getEntryName() : "";
        int rootEnd = rootEntryName.length() + 1;

        JarFile jarFile = jarConn.getJarFile();
        Enumeration<JarEntry> entryEnum = jarFile.entries();
        while (entryEnum.hasMoreElements()) {
            JarEntry jarEntry = entryEnum.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1 && name.endsWith(CLASS.extension)) {
                URI uri = URI.create(jarUri + "!/" + name);
                String binaryName = name.replaceAll("/", ".");
                binaryName = binaryName.replaceAll(CLASS.extension + "$", "");

                result.add(new CompiledJarFileByteCode(binaryName, jarFile, jarEntry, uri));
            }
        }

        return result;
    }

    private List<JavaFileObject> processDir(String packageName, File directory) {
        List<JavaFileObject> result = new ArrayList<>();

        File[] childFiles = directory.listFiles();
        if (childFiles != null) {
            for (File childFile : childFiles) {
                if (childFile.isFile() && childFile.getName().endsWith(CLASS.extension)) {
                    String binaryName = packageName + "." + childFile.getName();
                    binaryName = binaryName.replaceAll(CLASS.extension + "$", "");

                    result.add(new CompiledByteCode(binaryName, childFile.toURI()));
                }
            }
        }

        return result;
    }
}