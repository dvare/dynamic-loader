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

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddClasspathTest {
    private static final Logger log = LoggerFactory.getLogger(AddClasspathTest.class);

    @Test
    public void javaClassFileLoadingTest() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("org/dvare/dynamic/JavaFile.class");
        assertNotNull(url);
        dynamicCompiler.addClasspath(url);

        Class<?> javaFileClass = Class.forName("org.dvare.dynamic.JavaFile",
                false, dynamicCompiler.getClassLoader());

        assertNotNull(javaFileClass);
        assertEquals(1, javaFileClass.getDeclaredMethods().length);
    }

    @Test
    public void jarLoadingTest() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("jar_with_Test::print.jar");
        assertNotNull(url);

        dynamicCompiler.addClasspath(url);
        dynamicCompiler.build();

        Class<?> testClass = Class.forName("org.dvare.dynamic.jar.Test", false, dynamicCompiler.getClassLoader());
        assertNotNull(testClass);

        Object instance = testClass.newInstance();
        Object result = testClass.getMethod("print").invoke(instance);
        log.debug(result.toString());
        assertEquals("Inside Jar with parameter ", result);

        result = testClass.getMethod("print", String.class).invoke(instance, "value");
        log.debug(result.toString());
        assertEquals("Inside Jar with parameter value", result);
    }


    @Test
    public void jarCodeCompileTest() throws Exception {
        String sourceCode = "package org.dvare.dynamic;" +
                "import org.dvare.dynamic.jar.Test;" +
                "public class TestInvoker {" +
                "   public String test() { " +
                "   return new Test().print();" +
                "   }" +
                "}";


        DynamicCompiler dynamicCompiler = new DynamicCompiler();

        URL url = getClass().getClassLoader().getResource("jar_with_Test::print.jar");
        assertNotNull(url);
        dynamicCompiler.addClasspath(url);

        dynamicCompiler.addSource("org.dvare.dynamic.TestInvoker", sourceCode);
        dynamicCompiler.build();

        Class<?> testInvokerClass = Class.forName("org.dvare.dynamic.TestInvoker", false, dynamicCompiler.getClassLoader());
        assertNotNull(testInvokerClass);

    }


    @Test
    public void jarCodeCompileAndInvokeTest() throws Exception {
        String sourceCode = "package org.dvare.dynamic;" +
                "import org.dvare.dynamic.jar.Test;" +
                "public class TestInvoker {" +
                "   public String printWithoutParam() { " +
                "   return new Test().print();" +
                "   }" +
                "   public String printWithParam(String name) { " +
                "   return new Test().print(name);" +
                "   }" +
                "}";

        DynamicCompiler dynamicCompiler = new DynamicCompiler();

        URL url = getClass().getClassLoader().getResource("jar_with_Test::print.jar");
        assertNotNull(url);
        dynamicCompiler.addClasspath(url);

        dynamicCompiler.addSource("org.dvare.dynamic.TestInvoker", sourceCode);

        Class.forName("org.dvare.dynamic.jar.Test", false, dynamicCompiler.getClassLoader());
        dynamicCompiler.build();
        log.debug(dynamicCompiler.getErrors().toString());
        log.debug(dynamicCompiler.getWarnings().toString());

        Class<?> testInvokerClass = Class.forName("org.dvare.dynamic.TestInvoker", true, dynamicCompiler.getClassLoader());
        assertNotNull(testInvokerClass);

        Object instance = testInvokerClass.newInstance();
        Object result = testInvokerClass.getMethod("printWithoutParam").invoke(instance);
        log.debug(result.toString());
        assertEquals("Inside Jar with parameter ", result);

        result = testInvokerClass.getMethod("printWithParam", String.class).invoke(instance, "\"invoke test\"");
        log.debug(result.toString());
        assertEquals("Inside Jar with parameter \"invoke test\"", result);

    }


}
