# Dynamic Loader [![Build Status](https://travis-ci.org/dvare/DynamicLoader.svg?branch=master)](https://travis-ci.org/dvare/DynamicLoader)
Dynamic Loader is In Memory Java Compiler and classpath loader which enables to compile both java file and code string in memory and also provide support to load supportive jars in classpath which need to compile the dynamic code.

## Example

##### In Memory Compilation..

```java
public class SourceTest {

    @Test
    public void sourceCodeTest() throws Exception {
        
        StringBuilder sourceCode = new StringBuilder();
        sourceCode.append("package org.dvare.dynamic;\n");
        sourceCode.append("public class SourceTestClass {\n");
        sourceCode.append("   public String testMethod() { return \"inside test method\"; }");
        sourceCode.append("}");

        DynamicCompiler dynamicCompiler = new DynamicCompiler();
        dynamicCompiler.addSource("org.dvare.dynamic.SourceTestClass", sourceCode.toString());
        Map<String, Class<?>> compiled = dynamicCompiler.build();
        Class<?> aClass = compiled.get("org.dvare.dynamic.SourceTestClass");
        Assert.assertNotNull(aClass);
        Assert.assertEquals(1, aClass.getDeclaredMethods().length);
        }
    }
```


## Current version

* The current stable version is `1.1` : [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dvare/dynamic-loader/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|org.dvare|dynamic-loader|1.1|)

 Maven dependency:

```xml

<dependencies>
        <dependency>
            <groupId>org.dvare</groupId>
            <artifactId>dynamic-loader</artifactId>
            <version>1.1</version>
        </dependency>            
<dependencies>
```

## License
Dynamic Loader is released under the [![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT).

```
The MIT License (MIT)

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
THE SOFTWARE.
```

