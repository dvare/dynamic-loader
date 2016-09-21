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
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.junit.Assert;
import org.junit.Test;

public class JarTest {
    @Test
    public void DynamicJarTest() throws Exception {
        try {


            DynamicCompiler dynamicCompiler = new DynamicCompiler();
            dynamicCompiler.setSeparateContext(true);
            dynamicCompiler.setUpdateContextClassLoader(false);

            dynamicCompiler.addJar(getClass().getClassLoader().getResource("commons-math3.jar"));
            dynamicCompiler.build();

            Class aClass = Class.forName("org.apache.commons.math3.Field", false, dynamicCompiler.getClassLoader());

            Assert.assertNotNull(aClass);


        } catch (DynamicCompilerException e) {
            System.out.println(e.getErrorList());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void DynamicJarCodeTest() throws Exception {
        try {
            StringBuilder sourceCode = new StringBuilder();
            sourceCode.append("package org.dvare.dynamic;\n");
            sourceCode.append("import org.apache.commons.math3.Field;\n");
            sourceCode.append("public class SourceClass {\n");
            sourceCode.append("   public String test() { \n");
            sourceCode.append("   return \"inside test method\";\n");
            sourceCode.append("   }\n");
            sourceCode.append("}");


            DynamicCompiler dynamicCompiler = new DynamicCompiler();
            dynamicCompiler.setSeparateContext(true);
            dynamicCompiler.setUpdateContextClassLoader(false);

            dynamicCompiler.addJar(getClass().getClassLoader().getResource("commons-math3.jar"));
            dynamicCompiler.addSource("org.dvare.dynamic.SourceClass", sourceCode.toString());
            dynamicCompiler.build();

            Class aClass = Class.forName("org.dvare.dynamic.SourceClass", false, dynamicCompiler.getClassLoader());
            Assert.assertNotNull(aClass);


        } catch (DynamicCompilerException e) {
            System.out.println(e.getErrorList());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
