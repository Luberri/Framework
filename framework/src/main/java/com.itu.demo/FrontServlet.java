package com.itu.demo;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.io.PrintWriter;

import com.itu.demo.annotations.Controller;
import com.itu.demo.annotations.HandleURL;

@WebServlet(name = "FrontServlet", urlPatterns = {"/", "*.jsp"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {
    
    // Map pour stocker URL -> Mapping (classe + méthode)
    private Map<String, Mapping> urlMappings = new HashMap<>();
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            scanControllers();
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des contrôleurs", e);
        }
    }
    
    private void scanControllers() throws Exception {
        String cp = System.getProperty("java.class.path");
        String[] entries = cp.split(File.pathSeparator);
        java.util.List<String> classNames = new java.util.ArrayList<>();

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
        for (String cn : classNames) {
            if (cn.contains("$")) continue;
            try {
                Class<?> c = Class.forName(cn, false, cl);
                if (c.isAnnotationPresent(Controller.class)) {
                    registerController(c);
                }
            } catch (Throwable t) {
                // ignore classes that cannot be loaded
            }
        }
    }
    
    private void registerController(Class<?> controllerClass) throws Exception {
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(HandleURL.class)) {
                HandleURL handleURL = method.getAnnotation(HandleURL.class);
                String url = handleURL.value();
                if (!url.isEmpty()) {
                    urlMappings.put(url, new Mapping(controllerClass, method));
                    System.out.println("Mapped URL: " + url + " -> " + 
                        controllerClass.getName() + "." + method.getName());
                }
            }
        }
    }
    
    private void scanDirectory(File root, File dir, java.util.List<String> out) {
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

    private void scanJar(File jarFile, java.util.List<String> out) {
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
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        String resourcePath = requestURI.substring(contextPath.length());
        
        // Vérifier si l'URL correspond à un mapping de contrôleur
        Mapping mapping = urlMappings.get(resourcePath);
        if (mapping != null) {
            try {
                handleControllerMethod(mapping, request, response);
                return;
            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'exécution du contrôleur", e);
            }
        }
        
        try {
            java.net.URL resource = getServletContext().getResource(resourcePath);
            if (resource != null) {
                RequestDispatcher defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) {
                    defaultServlet.forward(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de la vérification de la ressource: " + resourcePath, e);
        }
        
        showFrameworkPage(request, response, resourcePath);
    }
    
    private void handleControllerMethod(Mapping mapping, HttpServletRequest request, 
                                       HttpServletResponse response) throws Exception {
        Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
        Method method = mapping.getMethod();
        
        // Vérifier si le type de retour est String
        if (method.getReturnType().equals(String.class)) {
            Object result = method.invoke(controllerInstance);
            
            if (result != null) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print(result.toString());
            }
        } else {
            // Type de retour non géré
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("Erreur: La méthode doit retourner un String");
        }
    }
    
    private void showFrameworkPage(HttpServletRequest request, HttpServletResponse response, 
                                 String requestedPath) 
            throws IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang='fr'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>Framework Java - Page non trouvée</title>");
        out.println("    <style>");
        out.println("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 40px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); color: #212529; min-height: 100vh; }");
        out.println("        .container { max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e9ecef; }");
        out.println("        h1 { color: #212529; text-align: center; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 2px solid #333; font-weight: 300; font-size: 2.5em; }");
        out.println("        .message { background: #f8f9fa; padding: 25px; border-radius: 12px; border-left: 4px solid #333; margin: 25px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }");
        out.println("        .path { font-family: 'Courier New', monospace; background: #e9ecef; padding: 12px 16px; border-radius: 8px; display: inline-block; margin: 15px 0; font-weight: bold; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }");
        out.println("        .footer { margin-top: 30px; text-align: center; color: #6c757d; font-size: 14px; padding-top: 20px; border-top: 1px solid #dee2e6; }");
        out.println("        p { line-height: 1.6; margin-bottom: 15px; color: #495057; }");
        out.println("        h3 { color: #333; margin-bottom: 15px; font-weight: 500; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h1>FRAMEWORK JAVA</h1>");
        
        out.println("        <div class='message'>");
        out.println("            <h3>Ressource non trouvée</h3>");
        out.println("            <p>Voici l'URL demandée :</p>");
        out.println("            <div class='path'><strong>" + requestedPath + "</strong></div>");
        out.println("        </div>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // Classe interne pour stocker le mapping
    private static class Mapping {
        private final Class<?> controllerClass;
        private final Method method;
        
        public Mapping(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
        }
        
        public Class<?> getControllerClass() {
            return controllerClass;
        }
        
        public Method getMethod() {
            return method;
        }
    }
}