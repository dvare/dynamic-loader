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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SourceTest {
    private static final Logger log = LoggerFactory.getLogger(SourceTest.class);

    @Test
    public void stringSourceCompileTest() throws Exception {

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        String sourceCode = "package org.dvare.dynamic;" +
                "public class SourceClass {" +
                "   public String test() { return \"inside test method\"; }" +
                "}";
        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode);
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.SourceClass");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
    }

    @Test
    public void stringSourceImportCompileTest() throws Exception {

        String sourceCode = "package org.dvare.dynamic;" +
                "import java.time.LocalDate;" +
                "public class DateUtil {" +
                "   public static String getTestDate() { return LocalDate.of(2020,5,15).toString(); }" +
                "}";

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        try {
            Class.forName("com.sun.tools.sjavac.Module"); //if java module present
            dynamicCompiler.addOption("--add-exports", "java.base/java.time=ALL-UNNAMED");
        } catch (Exception ignored) {
        }
        dynamicCompiler.addSource("org.dvare.dynamic.DateUtil", sourceCode);
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> dateUtilClass = compiled.get("org.dvare.dynamic.DateUtil");
        Assert.assertNotNull(dateUtilClass);
        Assert.assertEquals(1, dateUtilClass.getDeclaredMethods().length);

        Object dateUtil = dateUtilClass.newInstance();
        Object result = dateUtilClass.getMethod("getTestDate").invoke(dateUtil);
        Assert.assertNotNull(result);
        log.debug(result.toString());
        Assert.assertEquals("2020-05-15", result);
    }


    @Test
    public void multipleSourcesCompileTest() throws Exception {
        DynamicCompiler compiler = new DynamicCompiler();

        String source1 = "public class SourceClass1 { public SourceClass2 getSourceClass2() { return new SourceClass2(); }}";
        compiler.addSource("SourceClass1", source1);

        String source2 = "public class SourceClass2 { public String toString() { return \"SourceClass2\"; }}";
        compiler.addSource("SourceClass2", source2);


        Map<String, Class<?>> compiled = compiler.build();

        Assert.assertNotNull(compiled.get("SourceClass1"));
        Assert.assertNotNull(compiled.get("SourceClass2"));

        Class<?> aClass = compiled.get("SourceClass1");

        Object instance = aClass.newInstance();
        Object result = aClass.getMethod("getSourceClass2").invoke(instance);
        Assert.assertEquals("SourceClass2", result.toString());
    }


    @Test
    public void innerCLassCompileTest() throws Exception {
        DynamicCompiler dynamicCompiler = new DynamicCompiler();

        String sourceCode = "package org.dvare.dynamic;" +
                "public class SourceClass {" +
                "   private static class InnerSourceClass { int inner; }" +
                "   public String hello() { return \"hello\"; }" +
                "}";
        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode);

        dynamicCompiler.build();

        Class<?> aClass = Class.forName("org.dvare.dynamic.SourceClass",
                false, dynamicCompiler.getClassLoader());
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);


        Class<?> innerClass = Class.forName("org.dvare.dynamic.SourceClass$InnerSourceClass",
                false, dynamicCompiler.getClassLoader());
        Assert.assertNotNull(innerClass);


    }


}
