package org.dvare.dynamic.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClassPathBuilder {
    private static final Logger log = LoggerFactory.getLogger(ClassPathBuilder.class);

    public final static String pathSeparator = System.getProperty("path.separator");

    public String getClassPath() {
        return System
                .getProperty("java.class.path");
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
                            String decodedPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.toString());
                            paths.add(new File(decodedPath).getAbsolutePath());
                        }
                    }
                }
                c = c.getParent();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return String.join(pathSeparator, paths);
    }

}
