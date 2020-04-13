package org.dvare.dynamic.resources;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class MemorySource extends SimpleJavaFileObject {
    private final String contents;
    private final String className;

    public MemorySource(String className, CharSequence contents) {
        this(className, contents.toString());
    }

    public MemorySource(String className, String contents) {
        super(URI.create("file:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.contents = contents;
        this.className = className;
    }


    public MemorySource(String className, URI uri, Kind kind) {
        super(uri, kind);
        this.contents = null;
        this.className = className;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return contents;
    }

    public String getClassName() {
        return className;
    }
}
