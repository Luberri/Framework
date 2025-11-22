package com.itu.demo;
//4-bis
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;

public class Scanner {
    
    private ServletContext servletContext;
    
    public Scanner(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Scanner toutes les classes dans WEB-INF/classes et le classpath
     * @return Liste des noms de classes trouvées
     */
    public List<String> scanAllClasses() {
        List<String> classNames = new ArrayList<>();
        
        // Scanner WEB-INF/classes (classes de l'application web)
        String webInfPath = servletContext.getRealPath("/WEB-INF/classes");
        System.out.println("Scanning WEB-INF/classes: " + webInfPath);
        if (webInfPath != null) {
            File webInfDir = new File(webInfPath);
            if (webInfDir.exists()) {
                scanDirectory(webInfDir, webInfDir, classNames);
            }
        }
        
        // Scanner le classpath système
        String cp = System.getProperty("java.class.path");
        String[] entries = cp.split(File.pathSeparator);

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

        System.out.println("Total classes found: " + classNames.size());
        return classNames;
    }
    
    /**
     * Scanner un répertoire de manière récursive pour trouver les fichiers .class
     */
    private void scanDirectory(File root, File dir, List<String> out) {
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

    /**
     * Scanner un fichier JAR pour trouver les fichiers .class
     */
    private void scanJar(File jarFile, List<String> out) {
        try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jarFile)) {
            java.util.Enumeration<java.util.jar.JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                java.util.jar.JarEntry je = en.nextElement();
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