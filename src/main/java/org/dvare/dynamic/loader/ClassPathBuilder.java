package org.dvare.dynamic.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ClassPathBuilder {
    private static Logger logger = LoggerFactory.getLogger(ClassPathBuilder.class);
    private StringBuilder classpathBuilder = new StringBuilder();
    private boolean extractJar;

    public ClassPathBuilder(boolean extractJar) {
        this.extractJar = extractJar;
    }


    public String getClassPath(URLClassLoader urlClassLoader) {
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


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return classpathBuilder.toString();
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


                if (destDirFile.mkdir()) {


                    logger.info("destDir: " + destDir);
                    logger.info("pathToJar: " + pathToJar);


                    Enumeration enumEntries = jar.entries();
                    while (enumEntries.hasMoreElements()) {
                        JarEntry jarEntry = (java.util.jar.JarEntry) enumEntries.nextElement();
                        File f = new File(destDir + File.separator + jarEntry.getName());

                        if (jarEntry.isDirectory()) { // if its a directory, create it
                            if (f.mkdir()) {
                                continue;
                            } else {
                                break;
                            }

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

}
