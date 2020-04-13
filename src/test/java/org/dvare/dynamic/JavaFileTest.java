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

package org.dvare.dynamic;

import org.dvare.dynamic.compiler.DynamicCompiler;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Map;

public class JavaFileTest {
    private static final Logger log = LoggerFactory.getLogger(JavaFileTest.class);

    @Test
    public void javaFileTest() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("JavaFile.java");
        Assert.assertNotNull(url);
        dynamicCompiler.addSource(new File(url.getPath()));

        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.JavaFile");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);


    }

    @Test
    public void javaFile_Compiled() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("JavaFile.java");
        Assert.assertNotNull(url);
        dynamicCompiler.addSource(new File(url.getPath()));
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.JavaFile");


        Object instance = aClass.newInstance();
        Object result = aClass.getMethod("getName").invoke(instance);
        log.debug(result.toString());
        Assert.assertEquals("InMemoryCompiled", result);


    }

    @Test
    public void javaFile_ClassForName() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("JavaFile.java");
        Assert.assertNotNull(url);
        dynamicCompiler.addSource(new File(url.getPath()));
        dynamicCompiler.build();
        Class<?> aClass = Class.forName("org.dvare.dynamic.JavaFile",
                false, dynamicCompiler.getClassLoader());

        Object instance = aClass.newInstance();
        Object result = aClass.getMethod("getName").invoke(instance);
        log.debug(result.toString());
        Assert.assertEquals("InMemoryCompiled", result);


    }
}
