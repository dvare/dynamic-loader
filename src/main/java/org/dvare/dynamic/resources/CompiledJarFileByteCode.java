package org.dvare.dynamic.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CompiledJarFileByteCode extends CompiledByteCode {

    private final JarFile jarFile;
    private final JarEntry jarEntry;

    public CompiledJarFileByteCode(String binaryName, JarFile jarFile, JarEntry jarEntry, URI uri) {
        super(binaryName, uri);
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return jarFile.getInputStream(jarEntry);
    }

}