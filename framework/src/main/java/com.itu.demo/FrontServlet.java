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
                Class<?> paramType = paramTypes[i];
                
                // Si le paramètre est une Map<String, Object>, injecter tous les paramètres de la requête
                if (Map.class.isAssignableFrom(paramType)) {
                    Map<String, Object> params = new HashMap<>();
                    Map<String, String[]> parameterMap = request.getParameterMap();
                    
                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        
                        // Si une seule valeur, stocker la valeur directement
                        if (values.length == 1) {
                            params.put(key, values[0]);
                        } else {
                            // Sinon, stocker le tableau
                            params.put(key, values);
                        }
                    }
                    
                    paramValues[i] = params;
                    System.out.println("Injected Map with parameters: " + params.keySet());
                } else {
                    // Comportement existant pour les paramètres individuels
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
                        paramValues[i] = convertParameter(paramValue, paramType);
                        System.out.println("Parameter: " + paramName + " = " + paramValue);
                    } else {
                        System.err.println("Paramètre manquant: " + paramName);
                    }
                }
            }
        }
        
        Object result = method.invoke(controllerInstance, paramValues);
        
        if (result instanceof ModelView) {
            ModelView mv = (ModelView) result;
            
            // Copier toutes les données de ModelView dans les attributs de la requête
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
                System.out.println("Set attribute: " + entry.getKey() + " = " + entry.getValue());
            }
            
            String viewName = mv.getViewName();
            if (viewName != null && !viewName.isEmpty()) {
                // Gérer les redirections
                if (viewName.startsWith("redirect:")) {
                    String redirectUrl = viewName.substring("redirect:".length());
                    if (!redirectUrl.startsWith("/")) {
                        redirectUrl = request.getContextPath() + "/" + redirectUrl;
                    } else {
                        redirectUrl = request.getContextPath() + redirectUrl;
                    }
                    System.out.println("Redirecting to: " + redirectUrl);
                    response.sendRedirect(redirectUrl);
                } else {
                    // Forward classique
                    if (!viewName.startsWith("/")) {
                        viewName = "/" + viewName;
                    }
                    
                    System.out.println("Forwarding to: " + viewName);
                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewName);
                    dispatcher.forward(request, response);
                }
            }
        }
    }
    
    private Object convertParameter(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class)
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