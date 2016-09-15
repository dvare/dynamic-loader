package org.dvare.dynamic;


import org.dvare.dynamic.compiler.DynamicCompiler;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.dvare.dynamic.loader.CustomClassLoader;

public class DynamicContext {

    public static void main(String args[]) throws Exception {
        try {
            StringBuilder sourceCode = new StringBuilder();
            sourceCode.append("package org.dvare.dynamic;\n");
            sourceCode.append("import org.apache.log4j.Logger;\n");
            sourceCode.append("import org.apache.commons.math3.Field;\n");
            sourceCode.append("public class SourceClass {\n");
            sourceCode.append("Logger logger=Logger.getLogger(SourceClass.class);\n");
            sourceCode.append("   public String test() { \n");
            sourceCode.append("   //logger.info(\"Hammad\"); \n");
            sourceCode.append("   return \"inside test method\";\n");
            sourceCode.append("   }\n");
            sourceCode.append("}");


            ClassLoader classLoader = new CustomClassLoader().getCustomURLClassLoader();


            DynamicCompiler dynamicCompiler = new DynamicCompiler();
            dynamicCompiler.setClassLoader(classLoader);
            dynamicCompiler.addJar(new DynamicContext().getClass().getClassLoader().getResource("commons-math3.jar"));
            Class<?> helloClass = dynamicCompiler.compile("org.dvare.dynamic.SourceClass", sourceCode.toString());
            Object object = helloClass.newInstance();


            //Class.forName("org.apache.commons.math3.Field");

            System.out.println(helloClass.getMethod("test").invoke(object).toString());
        } catch (DynamicCompilerException e) {
            System.out.println(e.getErrorList());
        }

    }
}
