package org.dvare.dynamic.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ClassPathBuilder {
    private static final Logger log = LoggerFactory.getLogger(ClassPathBuilder.class);

    public String getClassPath() {
        String pathSeparator = System
                .getProperty("path.separator");
        String[] classPathEntries = System
                .getProperty("java.class.path")
                .split(pathSeparator);
        return String.join(File.pathSeparator, classPathEntries);
    }

    public String getClassPath(URLClassLoader cl) {
        List<String> paths = new ArrayList<>();
        try {

            ClassLoader c = cl;
            while (c instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) c).getURLs()) {
                    if (url.getProtocol() != null) {
                        if (url.openConnection() instanceof JarURLConnection) {
                            paths.add(url.toString());
                        } else {
                            String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
                            paths.add(new File(decodedPath).getAbsolutePath());
                        }
                    }
                }
                c = c.getParent();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return String.join(File.pathSeparator, paths);
    }

}
