/**
 * Original work by Ola Aronsson 2020
 * Courtesy of nollettnoll AB &copy; 2012 - 2020
 * <p>
 * Licensed under the Creative Commons Attribution 4.0 International (the "License")
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * https://creativecommons.org/licenses/by/4.0/
 * <p>
 * The software is provided “as is”, without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other liability,
 * whether in an action of contract, tort or otherwise, arising from, out of or
 * in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl.source.processor.loading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CustomClassLoader extends ClassLoader {

    private final String basePackage;

    public CustomClassLoader(String basePackage, ClassLoader parent) {
        super(parent);
        this.basePackage = basePackage;
    }

    private Class<?> getClass(String name) {
        byte[] b = null;
        b = loadClassData(name);
        if (b != null) {
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } else {
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(basePackage)) {
            Class c = getClass(name);
            if (c != null) {
                return c;
            }
        }
        return super.loadClass(name);
    }

    private byte[] loadClassData(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName.replace('.', File.separatorChar) + ".class");
        if (inputStream == null) {
            return null; // NOSONAR
        }
        byte[] buffer;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;
        try {
            while ((nextValue = inputStream.read()) != -1) {
                byteStream.write(nextValue);
            }
        } catch (IOException e) {
            return null;  // NOSONAR
        }
        buffer = byteStream.toByteArray();
        return buffer;
    }


}