package com.itu.demo;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.itu.demo.annotations.Controller;
import com.itu.demo.annotations.Get;
import com.itu.demo.annotations.Post;
import com.itu.demo.annotations.Param;
import com.itu.demo.annotations.RestApi;

@WebServlet(name = "FrontServlet", urlPatterns = { "/" }, loadOnStartup = 1)
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 50     // 50 MB
)
public class FrontServlet extends HttpServlet {

    private final Map<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
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
                    registerController(c);
                }
            } catch (Throwable t) {
                System.err.println("Impossible de charger : " + cn);
            }
        }
    }

    private void registerController(Class<?> controllerClass) {
        for (Method method : controllerClass.getDeclaredMethods()) {

            String url = null;
            String httpMethod = null;

            if (method.isAnnotationPresent(Get.class)) {
                url = method.getAnnotation(Get.class).value();
                httpMethod = "GET";
            } else if (method.isAnnotationPresent(Post.class)) {
                url = method.getAnnotation(Post.class).value();
                httpMethod = "POST";
            }

            if (url != null && !url.isEmpty()) {
                String key = httpMethod + ":" + url;
                urlMappings.put(key, new Mapping(controllerClass, method, httpMethod));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri.substring(context.length());
        String method = request.getMethod();

        Mapping mapping = urlMappings.get(method + ":" + path);

        if (mapping == null) {
            mapping = findMatchingPattern(path, method, request);
        }

        if (mapping != null) {
            try {
                handleControllerMethod(mapping, request, response);
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private Mapping findMatchingPattern(String path,
                                        String httpMethod,
                                        HttpServletRequest request) {

        for (Map.Entry<String, Mapping> entry : urlMappings.entrySet()) {

            if (!entry.getKey().startsWith(httpMethod + ":")) continue;

            String pattern = entry.getKey().substring(httpMethod.length() + 1);

            if (!pattern.contains("{")) continue;

            String[] p1 = pattern.split("/");
            String[] p2 = path.split("/");

            if (p1.length != p2.length) continue;

            Map<String, String> params = new HashMap<>();
            boolean match = true;

            for (int i = 0; i < p1.length; i++) {
                if (p1[i].startsWith("{")) {
                    String name = p1[i].substring(1, p1[i].length() - 1);
                    params.put(name, p2[i]);
                } else if (!p1[i].equals(p2[i])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                for (Map.Entry<String, String> e : params.entrySet()) {
                    request.setAttribute("_path_param_" + e.getKey(), e.getValue());
                }
                return entry.getValue();
            }
        }
        return null;
    }

    private void handleControllerMethod(Mapping mapping,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {

        Object controller = mapping.getControllerClass()
                .getDeclaredConstructor()
                .newInstance();

        Method method = mapping.getMethod();
        Class<?>[] types = method.getParameterTypes();
        java.lang.reflect.Parameter[] params = method.getParameters();
        Object[] values = new Object[types.length];

        // Extraire les fichiers et les données du formulaire
        Map<String, Object> formData = new HashMap<>();
        Map<String, UploadedFile> uploadedFiles = new HashMap<>();
        
        extractFormDataAndFiles(request, formData, uploadedFiles);

        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            java.lang.reflect.Parameter p = params[i];

            Param paramAnn = p.getAnnotation(Param.class);
            String name = (paramAnn != null) ? paramAnn.value() : p.getName();

            // Gestion Map<String, UploadedFile> pour les fichiers
            if (Map.class.isAssignableFrom(type)) {
                // Vérifier si c'est une Map de fichiers via le nom du paramètre
                if (name.toLowerCase().contains("file") || name.toLowerCase().contains("upload")) {
                    values[i] = uploadedFiles;
                } else {
                    values[i] = formData;
                }
                continue;
            }

            // Gestion UploadedFile directement
            if (type == UploadedFile.class) {
                values[i] = uploadedFiles.get(name);
                continue;
            }

            // Gestion des objets complexes
            if (!type.isPrimitive()
                    && !type.getName().startsWith("java.")
                    && !type.isEnum()) {
                values[i] = bindAndPopulate(type, name, request);
                continue;
            }

            // Gestion des paramètres simples
            Object value = formData.get(name);
            if (value == null) {
                Object pathValue = request.getAttribute("_path_param_" + name);
                if (pathValue != null) value = pathValue.toString();
            }

            if (value != null) {
                values[i] = convertParameter(value.toString(), type);
            }
        }

        Object result = method.invoke(controller, values);

        // Vérifier si c'est une API REST
        if (method.isAnnotationPresent(RestApi.class)) {
            handleRestResponse(result, response);
            return;
        }

        if (result instanceof ModelView) {
            ModelView mv = (ModelView) result;

            mv.getData().forEach(request::setAttribute);

            String view = mv.getViewName();
            if (view.startsWith("redirect:")) {
                response.sendRedirect(request.getContextPath()
                        + view.substring(9));
            } else {
                if (!view.startsWith("/")) view = "/" + view;
                RequestDispatcher rd = request.getRequestDispatcher(view);
                rd.forward(request, response);
            }
        }
    }

    private void extractFormDataAndFiles(HttpServletRequest request,
                                         Map<String, Object> formData,
                                         Map<String, UploadedFile> uploadedFiles) throws Exception {

        String contentType = request.getContentType();

        // Vérifier si c'est un formulaire multipart
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            for (Part part : request.getParts()) {
                String fieldName = part.getName();
                String fileName = getFileName(part);

                if (fileName != null && !fileName.isEmpty()) {
                    // C'est un fichier
                    byte[] content = readPartContent(part);
                    UploadedFile uploadedFile = new UploadedFile(
                        fileName, 
                        content, 
                        part.getContentType()
                    );
                    uploadedFiles.put(fieldName, uploadedFile);
                } else {
                    // C'est un champ de formulaire normal
                    String value = new String(readPartContent(part), "UTF-8");
                    formData.put(fieldName, value);
                }
            }
        } else {
            // Formulaire classique sans fichier
            request.getParameterMap().forEach((k, v) ->
                formData.put(k, v.length == 1 ? v[0] : v)
            );
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String fileName = token.substring(token.indexOf('=') + 1).trim()
                            .replace("\"", "");
                    // Gérer les chemins Windows
                    int lastIndex = fileName.lastIndexOf("\\");
                    if (lastIndex >= 0) {
                        fileName = fileName.substring(lastIndex + 1);
                    }
                    return fileName;
                }
            }
        }
        return null;
    }

    private byte[] readPartContent(Part part) throws IOException {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    private void handleRestResponse(Object result, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse apiResponse;
        if (result instanceof ApiResponse) {
            apiResponse = (ApiResponse) result;
        } else {
            apiResponse = ApiResponse.success(result);
        }

        response.getWriter().write(apiResponse.toJson());
    }

    private Object convertParameter(String v, Class<?> t) {
        if (t == int.class || t == Integer.class) return Integer.parseInt(v);
        if (t == long.class || t == Long.class) return Long.parseLong(v);
        if (t == double.class || t == Double.class) return Double.parseDouble(v);
        if (t == float.class || t == Float.class) return Float.parseFloat(v);
        if (t == boolean.class || t == Boolean.class) return Boolean.parseBoolean(v);
        return v;
    }

    private Object bindAndPopulate(Class<?> type,
                                   String prefix,
                                   HttpServletRequest request) throws Exception {

        Object obj = type.getDeclaredConstructor().newInstance();
        BeanInfo info = Introspector.getBeanInfo(type);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (pd.getWriteMethod() == null) continue;
            if ("class".equals(pd.getName())) continue;

            String key = prefix + "." + pd.getName();
            String val = request.getParameter(key);

            if (val == null) {
                Object p = request.getAttribute("_path_param_" + pd.getName());
                if (p != null) val = p.toString();
            }

            if (val != null) {
                Object converted = convertParameter(val, pd.getPropertyType());
                pd.getWriteMethod().invoke(obj, converted);
            }
        }
        return obj;
    }

    private static class Mapping {
        private final Class<?> controllerClass;
        private final Method method;
        private final String httpMethod;

        public Mapping(Class<?> c, Method m, String h) {
            controllerClass = c;
            method = m;
            httpMethod = h;
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
