package org.dvare.dynamic.resources;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class MemorySource extends SimpleJavaFileObject {
    private String contents = null;
    private String className;

    public MemorySource(String className, CharSequence contents) {
        this(className, contents.toString());
    }

    public MemorySource(String className, String contents) {
        super(URI.create("file:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.contents = contents;
        this.className = className;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return contents;
    }

    public String getClassName() {
        return className;
    }
}
