package com.itu.demo;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.io.PrintWriter;

import com.itu.demo.annotations.Controller;
import com.itu.demo.annotations.Get;
import com.itu.demo.annotations.Post;
import com.itu.demo.ModelView;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {
    
    // Map pour stocker URL + méthode HTTP -> Mapping (classe + méthode)
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
        Scanner scanner = new Scanner(getServletContext());
        List<String> classNames = scanner.scanAllClasses();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String cn : classNames) {
            if (cn.contains("$")) continue;
            try {
                Class<?> c = Class.forName(cn, false, cl);
                if (c.isAnnotationPresent(Controller.class)) {
                    System.out.println("Found @Controller: " + cn);
                    registerController(c);
                }
            } catch (Throwable t) {
                System.err.println("Cannot load class: " + cn + " - " + t.getMessage());
            }
        }
        
        System.out.println("Registered URL mappings: " + urlMappings.keySet());
    }
    
    private void registerController(Class<?> controllerClass) throws Exception {
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            String url = null;
            String httpMethod = null;
            
            // Vérifier @Get
            if (method.isAnnotationPresent(Get.class)) {
                Get getAnnotation = method.getAnnotation(Get.class);
                url = getAnnotation.value();
                httpMethod = "GET";
            }
            // Vérifier @Post
            else if (method.isAnnotationPresent(Post.class)) {
                Post postAnnotation = method.getAnnotation(Post.class);
                url = postAnnotation.value();
                httpMethod = "POST";
            }
            
            if (url != null && !url.isEmpty()) {
                // Créer une clé unique: méthode HTTP + URL
                String key = httpMethod + ":" + url;
                urlMappings.put(key, new Mapping(controllerClass, method, httpMethod));
                System.out.println("Mapped " + httpMethod + " " + url + " -> " + 
                    controllerClass.getName() + "." + method.getName());
            }
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());
        String httpMethod = request.getMethod();
        
        System.out.println("Request: " + httpMethod + " " + resourcePath);
        
        // Créer la clé avec la méthode HTTP
        String mappingKey = httpMethod + ":" + resourcePath;
        Mapping mapping = urlMappings.get(mappingKey);
        
        // Si pas de correspondance exacte, chercher un pattern
        if (mapping == null) {
            mapping = findMatchingPattern(resourcePath, httpMethod, request);
        }
        
        if (mapping != null) {
            try {
                handleControllerMethod(mapping, request, response);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        showFrameworkPage(request, response, resourcePath);
    }

    private Mapping findMatchingPattern(String resourcePath, String httpMethod, HttpServletRequest request) {
        for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {
            String key = entry.getKey();
            
            // Vérifier que la méthode HTTP correspond
            if (!key.startsWith(httpMethod + ":")) {
                continue;
            }
            
            // Extraire le pattern de l'URL
            String pattern = key.substring(httpMethod.length() + 1);
            
            // IMPORTANT: Ne traiter que les patterns avec des variables {xxx}
            if (!pattern.contains("{")) {
                continue; // Ignorer les URLs exactes (déjà vérifiées)
            }
            
            String[] patternParts = pattern.split("/");
            String[] resourceParts = resourcePath.split("/");
            
            if (patternParts.length == resourceParts.length) {
                boolean matches = true;
                Map<String, String> pathParams = new HashMap<>();
                
                for (int i = 0; i < patternParts.length; i++) {
                    String patternPart = patternParts[i];
                    String resourcePart = resourceParts[i];
                    
                    if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                        String paramName = patternPart.substring(1, patternPart.length() - 1);
                        pathParams.put(paramName, resourcePart);
                        System.out.println("Found path parameter: " + paramName + " = " + resourcePart);
                    } else if (!patternPart.equals(resourcePart)) {
                        matches = false;
                        break;
                    }
                }
                
                if (matches) {
                    for (Map.Entry<String, String> param : pathParams.entrySet()) {
                        request.setAttribute("_path_param_" + param.getKey(), param.getValue());
                    }
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private void handleControllerMethod(Mapping mapping, HttpServletRequest request, 
                                       HttpServletResponse response) throws Exception {
        Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
        Method method = mapping.getMethod();
        
        Class<?>[] paramTypes = method.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        Object[] paramValues = new Object[paramTypes.length];
        
        if (paramTypes.length > 0) {
            for (int i = 0; i < paramTypes.length; i++) {
                java.lang.reflect.Parameter param = parameters[i];
                
                com.itu.demo.annotations.Param paramAnnotation = param.getAnnotation(com.itu.demo.annotations.Param.class);
                String paramName;
                
                if (paramAnnotation != null) {
                    paramName = paramAnnotation.value();
                } else {
                    paramName = param.getName();
                }
                
                String paramValue = request.getParameter(paramName);
                
                if (paramValue == null) {
                    Object pathParam = request.getAttribute("_path_param_" + paramName);
                    if (pathParam != null) {
                        paramValue = pathParam.toString();
                        System.out.println("Using path parameter: " + paramName + " = " + paramValue);
                    }
                }
                
                if (paramValue != null) {
                    paramValues[i] = convertParameter(paramValue, paramTypes[i]);
                    System.out.println("Parameter: " + paramName + " = " + paramValue);
                } else {
                    System.err.println("Paramètre manquant: " + paramName);
                }
            }
        }
        
        Object result = method.invoke(controllerInstance, paramValues);
        
        if (result instanceof ModelView) {
            ModelView mv = (ModelView) result;
            
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            
            String viewName = mv.getViewName();
            if (viewName != null && !viewName.isEmpty()) {
                if (!viewName.startsWith("/")) {
                    viewName = "/" + viewName;
                }
                
                System.out.println("Forwarding to: " + viewName);
                RequestDispatcher dispatcher = request.getRequestDispatcher(viewName);
                dispatcher.forward(request, response);
            }
        }
    }
    
    private Object convertParameter(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }
        return value;
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
        out.println("            <div class='path'><strong>" + request.getMethod() + " " + requestedPath + "</strong></div>");
        out.println("        </div>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // Classe interne modifiée pour inclure la méthode HTTP
    private static class Mapping {
        private final Class<?> controllerClass;
        private final Method method;
        private final String httpMethod;
        
        public Mapping(Class<?> controllerClass, Method method, String httpMethod) {
            this.controllerClass = controllerClass;
            this.method = method;
            this.httpMethod = httpMethod;
        }
        
        public Class<?> getControllerClass() {
            return controllerClass;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public String getHttpMethod() {
            return httpMethod;
        }
    }
}