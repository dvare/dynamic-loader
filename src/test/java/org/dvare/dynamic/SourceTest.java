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

import java.util.Map;

public class SourceTest {


    @Test
    public void compile_test() throws Exception {

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
    public void compile_multiple_sources() throws Exception {
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
    public void compile_innerCLass() throws Exception {
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
