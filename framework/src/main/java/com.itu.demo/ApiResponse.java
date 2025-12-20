package com.itu.demo;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private String status;
    private Object data;
    private String error;

    public static ApiResponse success(Object data) {
        ApiResponse response = new ApiResponse();
        response.status = "success";
        response.data = data;
        return response;
    }

    public static ApiResponse error(String errorMessage) {
        ApiResponse response = new ApiResponse();
        response.status = "error";
        response.error = errorMessage;
        return response;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\":\"").append(status).append("\"");
        
        if (data != null) {
            sb.append(",\"data\":").append(objectToJson(data));
        }
        
        if (error != null) {
            sb.append(",\"error\":\"").append(escapeJson(error)).append("\"");
        }
        
        sb.append("}");
        return sb.toString();
    }

    private String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) obj) {
                if (!first) sb.append(",");
                sb.append(objectToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(objectToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        // Pour les objets Java, utiliser la réflexion
        return beanToJson(obj);
    }

    private String beanToJson(Object obj) {
        try {
            StringBuilder sb = new StringBuilder("{");
            java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(obj.getClass());
            boolean first = true;
            for (java.beans.PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if ("class".equals(pd.getName()) || pd.getReadMethod() == null) continue;
                if (!first) sb.append(",");
                sb.append("\"").append(pd.getName()).append("\":");
                Object value = pd.getReadMethod().invoke(obj);
                sb.append(objectToJson(value));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return "\"" + escapeJson(obj.toString()) + "\"";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Getters/Setters
    public String getStatus() { return status; }
    public Object getData() { return data; }
    public String getError() { return error; }
}