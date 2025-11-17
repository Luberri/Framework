package com.itu.demo;

import java.util.HashMap;

public class ModelView {
    
    private String viewName;
    private HashMap<String, Object> data;
    
    public ModelView() {
        this.data = new HashMap<>();
    }
    
    public ModelView(String viewName) {
        this.viewName = viewName;
        this.data = new HashMap<>();
    }
    
    /**
     * Ajouter un objet au modèle avec une clé
     * @param key Clé pour identifier l'objet
     * @param value Objet à ajouter
     */
    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }
    
    /**
     * Ajouter un objet au modèle (la clé sera le nom de la classe en minuscule)
     * @param value Objet à ajouter
     */
    public ModelView addObject(Object value) {
        if (value != null) {
            String key = value.getClass().getSimpleName();
            key = key.substring(0, 1).toLowerCase() + key.substring(1);
            this.data.put(key, value);
        }
        return this;
    }
    
    /**
     * Récupérer un objet du modèle
     * @param key Clé de l'objet
     * @return L'objet ou null si non trouvé
     */
    public Object getData(String key) {
        return this.data.get(key);
    }
    
    /**
     * Récupérer toutes les données du modèle
     * @return HashMap contenant toutes les données
     */
    public HashMap<String, Object> getData() {
        return this.data;
    }
    
    /**
     * Définir le nom de la vue
     * @param viewName Nom de la vue
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    
    /**
     * Récupérer le nom de la vue
     * @return Nom de la vue
     */
    public String getViewName() {
        return this.viewName;
    }
    
    /**
     * Vérifier si une clé existe dans le modèle
     * @param key Clé à vérifier
     * @return true si la clé existe, false sinon
     */
    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }
    
    /**
     * Supprimer un objet du modèle
     * @param key Clé de l'objet à supprimer
     * @return L'objet supprimé ou null si non trouvé
     */
    public Object removeData(String key) {
        return this.data.remove(key);
    }
    
    /**
     * Vider toutes les données du modèle
     */
    public void clear() {
        this.data.clear();
    }
    
    /**
     * Obtenir le nombre d'objets dans le modèle
     * @return Nombre d'objets
     */
    public int size() {
        return this.data.size();
    }
}