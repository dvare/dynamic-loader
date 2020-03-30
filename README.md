## Dynamic Loader [![Build Status](https://travis-ci.org/dvare/dynamic-loader.svg?branch=master)](https://travis-ci.org/dvare/dynamic-loader) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/5ecf8a11d36e4211b21ae5cdbbba5e65)](https://www.codacy.com/app/hammadirshad/DynamicLoader?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dvare/DynamicLoader&amp;utm_campaign=Badge_Grade)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dvare/dynamic-loader/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|org.dvare|dynamic-loader|2.0|) [![Javadocs](http://www.javadoc.io/badge/org.dvare/dynamic-loader.svg)](http://www.javadoc.io/doc/org.dvare/dynamic-loader)
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

* The current stable version is `2.0`
* The current snapshot version is `2.1-SNAPSHOT` 

In order to use snapshot versions, you need to add the following maven repository in your `pom.xml`:

```xml
<repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
 Maven dependency:

```xml

<dependencies>
        <dependency>
            <groupId>org.dvare</groupId>
            <artifactId>dynamic-loader</artifactId>
            <version>2.0</version>
        </dependency>            
<dependencies>
```

## License
Dynamic Loader is released under the [![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT).

```
The MIT License (MIT)

Copyright (c) 2020 Muhammad Hammad

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```


