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

import java.net.URL;

public class JarTest {
    private static final Logger log = LoggerFactory.getLogger(JarTest.class);

    @Test
    public void DynamicJarTest() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        URL url = getClass().getClassLoader().getResource("jar_test_file.jar");
        Assert.assertNotNull(url);
        dynamicCompiler.addJar(url);
        dynamicCompiler.build();

        Class<?> aClass = Class.forName("org.dvare.dynamic.jar.Test", false, dynamicCompiler.getClassLoader());

        Assert.assertNotNull(aClass);

    }


    @Test
    public void DynamicJarCodeTest() throws Exception {
        String sourceCode = "package org.dvare.dynamic;" +
                "import org.dvare.dynamic.jar.Test;" +
                "public class SourceClass {" +
                "   public String test() { " +
                "   return new Test().print();" +
                "   }" +
                "}";


        DynamicCompiler dynamicCompiler = new DynamicCompiler();

        URL url = getClass().getClassLoader().getResource("jar_test_file.jar");
        Assert.assertNotNull(url);
        dynamicCompiler.addJar(url);
        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode);
        dynamicCompiler.build();

        Class aClass = Class.forName("org.dvare.dynamic.SourceClass", false, dynamicCompiler.getClassLoader());
        Assert.assertNotNull(aClass);

    }


    @Test
    public void dynamicJarCodeTestPrint() throws Exception {
        String sourceCode = "package org.dvare.dynamic;" +
                "import org.dvare.dynamic.jar.Test;" +
                "public class SourceClass {" +
                "   public String test() { " +
                "   return new Test().print();" +
                "   }" +
                "   public String test(String name) { " +
                "   return new Test().print(name);" +
                "   }" +
                "}";

        DynamicCompiler dynamicCompiler = new DynamicCompiler();

        URL url = getClass().getClassLoader().getResource("jar_test_file.jar");
        Assert.assertNotNull(url);
        dynamicCompiler.addJar(url);

        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode);
        dynamicCompiler.build();

        Class aClass = Class.forName("org.dvare.dynamic.SourceClass", false, dynamicCompiler.getClassLoader());
        Assert.assertNotNull(aClass);

        Object instance = aClass.newInstance();
        Object result = aClass.getMethod("test").invoke(instance);
        log.debug(result.toString());
        Assert.assertEquals("Inside Jar with parameter ", result);

        result = aClass.getMethod("test", String.class).invoke(instance, "test");
        log.debug(result.toString());
        Assert.assertEquals("Inside Jar with parameter test", result);

    }


}
