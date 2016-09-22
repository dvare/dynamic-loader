/*The MIT License (MIT)

Copyright (c) 2016 Muhammad Hammad

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

import java.io.File;
import java.util.Map;

public class SourceTest {

    @Test
    public void compile_whenTypical() throws Exception {
        StringBuilder sourceCode = new StringBuilder();

        sourceCode.append("package org.dvare.dynamic;\n");
        sourceCode.append("public class SourceClass {\n");
        sourceCode.append("   public String test() { return \"inside test method\"; }");
        sourceCode.append("}");

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode.toString());
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.SourceClass");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
    }


    @Test
    public void compile_severalFiles() throws Exception {
        String cls1 = "public class SourceClass1 { public SourceClass2 getSourceClass2() { return new SourceClass2(); }}";
        String cls2 = "public class SourceClass2 { public String toString() { return \"SourceClass2\"; }}";

        DynamicCompiler compiler = new DynamicCompiler();
        compiler.addSource("SourceClass1", cls1);
        compiler.addSource("SourceClass2", cls2);
        Map<String, Class<?>> compiled = compiler.build();
        ;
        Assert.assertNotNull(compiled.get("SourceClass1"));
        Assert.assertNotNull(compiled.get("SourceClass2"));

        Class<?> aClass = compiled.get("SourceClass1");
        Object a = aClass.newInstance();
        Assert.assertEquals("SourceClass2", aClass.getMethod("getSourceClass2").invoke(a).toString());
    }


    @Test
    public void compile_filesWithInnerClasses() throws Exception {
        StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.dvare.dynamic;\n");
        sourceCode.append("public class SourceClass {\n");
        sourceCode.append("   private static class InnerSourceClass { int inner; }\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode.toString());
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.SourceClass");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
    }


    @Test
    public void javaFileTest() throws Exception {


        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        dynamicCompiler.addSource("org.dvare.dynamic.JavaFile", new File("/Users/hammad/git/DynamicLoader/src/main/resources/JavaFile.java"));
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.JavaFile");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
        Object a = aClass.newInstance();
        Object result = aClass.getMethod("javaFileTest").invoke(a);
        System.out.println(result);
        Assert.assertEquals("javaFileTest", result);

    }
}
