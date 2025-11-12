package com.itu.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScannerMain {
    public static void main(String[] args) {
        String cp = System.getProperty("java.class.path");
        String[] entries = cp.split(File.pathSeparator);
        List<String> classNames = new ArrayList<>();

        for (String entry : entries) {
            File f = new File(entry);
            if (f.exists()) {
                if (f.isDirectory()) {
                    scanDirectory(f, f, classNames);
                } else if (f.isFile() && entry.toLowerCase().endsWith(".jar")) {
                    scanJar(f, classNames);
                }
            }
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.out.println("Classes annotated with @Controller:");
        for (String cn : classNames) {
            if (cn.contains("$")) continue;
            try {
                Class<?> c = Class.forName(cn, false, cl);
                // Correction: utiliser le bon package
                if (c.isAnnotationPresent(com.itu.demo.annotations.Controller.class)) {
                    System.out.println(" - " + cn);
                }
            } catch (Throwable t) {
                // ignore classes that cannot be loaded
            }
        }
    }

    private static void scanDirectory(File root, File dir, List<String> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File child : files) {
            if (child.isDirectory()) {
                scanDirectory(root, child, out);
            } else if (child.isFile() && child.getName().endsWith(".class")) {
                String rel = child.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                String className = rel.replace(File.separatorChar, '.').replaceAll("\\.class$", "");
                out.add(className);
            }
        }
    }

    private static void scanJar(File jarFile, List<String> out) {
        try (JarFile jf = new JarFile(jarFile)) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                if (!je.isDirectory() && je.getName().endsWith(".class")) {
                    String className = je.getName().replace('/', '.').replaceAll("\\.class$", "");
                    out.add(className);
                }
            }
        } catch (IOException e) {
            // ignore unreadable jars
        }
    }
}