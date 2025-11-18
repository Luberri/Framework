package com.itu.demo;

public class Etudiant {
    
    private String matricule;
    private String nom;
    private String prenom;
    private int age;
    private String niveau;
    private String email;
    
    public Etudiant() {
    }
    
    public Etudiant(String matricule, String nom, String prenom, int age, String niveau, String email) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.niveau = niveau;
        this.email = email;
    }
    
    // Getters
    public String getMatricule() {
        return matricule;
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public int getAge() {
        return age;
    }
    
    public String getNiveau() {
        return niveau;
    }
    
    public String getEmail() {
        return email;
    }
    
    // Setters
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "Etudiant{" +
                "matricule='" + matricule + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", age=" + age +
                ", niveau='" + niveau + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}