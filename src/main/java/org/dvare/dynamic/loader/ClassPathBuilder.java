/*The MIT License (MIT)

Copyright (c) 2019 Muhammad Hammad

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


package org.dvare.dynamic.loader;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClassPathBuilder {

    public String getClassPath(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            return getClassPath((URLClassLoader) classLoader);
        }
        return "";
    }

    public String getClassPath(URLClassLoader cl) {
        List<String> paths = new ArrayList<>();
        try {

            ClassLoader c = cl;
            while (c instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) c).getURLs()) {
                    if (url.openConnection() instanceof JarURLConnection) {
                        paths.add(url.toString());
                    } else {
                        String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
                        paths.add(new File(decodedPath).getAbsolutePath());
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
