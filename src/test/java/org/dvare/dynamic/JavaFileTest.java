/*The MIT License (MIT)

Copyright (c) 2021 Muhammad Hammad

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

package org.dvare.dynamic;

import org.dvare.dynamic.compiler.DynamicCompiler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaFileTest {
    private static final Logger log = LoggerFactory.getLogger(JavaFileTest.class);

    @Test
    public void javaFile_compile_Test() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("JavaFile.java");
        assertNotNull(url);
        dynamicCompiler.addSource(new File(url.getPath()));

        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> javaFileClass = compiled.get("org.dvare.dynamic.JavaFile");

        Object instance = javaFileClass.newInstance();
        Object result = javaFileClass.getMethod("getName").invoke(instance);
        log.debug(result.toString());
        assertEquals("InMemoryCompiled", result);
    }

    @Test
    public void javaFile_ClassForName_find_Test() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("JavaFile.java");
        assertNotNull(url);
        dynamicCompiler.addSource(new File(url.getPath()));
        dynamicCompiler.build();

        Class<?> javaFileClass = Class.forName("org.dvare.dynamic.JavaFile",
                false, dynamicCompiler.getClassLoader());

        Object instance = javaFileClass.newInstance();
        Object result = javaFileClass.getMethod("getName").invoke(instance);
        log.debug(result.toString());
        assertEquals("InMemoryCompiled", result);
    }
}
