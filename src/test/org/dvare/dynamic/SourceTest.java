package org.dvare.dynamic;

import org.dvare.dynamic.compiler.DynamicCompiler;
import org.junit.Assert;
import org.junit.Test;

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
        Class<?> helloClass = dynamicCompiler.compile("org.dvare.dynamic.SourceClass", sourceCode.toString());
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }


    @Test
    public void compile_severalFiles() throws Exception {
        String cls1 = "public class SourceClass1 { public SourceClass2 getSourceClass2() { return new SourceClass2(); }}";
        String cls2 = "public class SourceClass2 { public String toString() { return \"SourceClass2\"; }}";

        DynamicCompiler compiler = new DynamicCompiler();
        compiler.addSource("SourceClass1", cls1);
        compiler.addSource("SourceClass2", cls2);
        Map<String, Class<?>> compiled = compiler.compileSource();
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
        Class<?> aClass = dynamicCompiler.compile("org.dvare.dynamic.SourceClass", sourceCode.toString());
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
    }
}
