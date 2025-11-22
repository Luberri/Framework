package com.itu.demo.entity;

import com.itu.demo.Conn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    
    // ========== CRUD OPERATIONS ========= =
    
    /**
     * Créer (Insérer) un nouvel étudiant dans la base de données
     * @return true si l'insertion est réussie, false sinon
     */
    public boolean create() {
        String sql = "INSERT INTO etudiant (nom, prenom, email, numero_etudiant, promotion) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, this.nom);
            pst.setString(2, this.prenom);
            pst.setString(3, this.email);
            pst.setString(4, this.matricule);
            pst.setString(5, this.niveau);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion: " + e.getMessage());
            return false;
        } finally {
            Conn.closeConnection(conn);
        }
    }
    
    /**
     * Lire (Récupérer) un étudiant par son matricule
     * @param matricule Matricule de l'étudiant
     * @return Objet Etudiant ou null si non trouvé
     */
    public static Etudiant read(String matricule) {
        String sql = "SELECT * FROM etudiant WHERE numero_etudiant = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, matricule);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Etudiant etudiant = new Etudiant();
                etudiant.setMatricule(rs.getString("numero_etudiant"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setNiveau(rs.getString("promotion"));
                rs.close();
                pst.close();
                return etudiant;
            }
            rs.close();
            pst.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture: " + e.getMessage());
            return null;
        } finally {
            Conn.closeConnection(conn);
        }
    }
    
    /**
     * Mettre à jour les informations d'un étudiant
     * @return true si la mise à jour est réussie, false sinon
     */
    public boolean update() {
        String sql = "UPDATE etudiant SET nom = ?, prenom = ?, email = ?, promotion = ? WHERE numero_etudiant = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, this.nom);
            pst.setString(2, this.prenom);
            pst.setString(3, this.email);
            pst.setString(4, this.niveau);
            pst.setString(5, this.matricule);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        } finally {
            Conn.closeConnection(conn);
        }
    }
    
    /**
     * Supprimer un étudiant de la base de données
     * @param matricule Matricule de l'étudiant à supprimer
     * @return true si la suppression est réussie, false sinon
     */
    public static boolean delete(String matricule) {
        String sql = "DELETE FROM etudiant WHERE numero_etudiant = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, matricule);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        } finally {
            Conn.closeConnection(conn);
        }
    }
    
    /**
     * Récupérer tous les étudiants de la base de données
     * @return Liste de tous les étudiants
     */
    public static List<Etudiant> readAll() {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                Etudiant etudiant = new Etudiant();
                etudiant.setMatricule(rs.getString("numero_etudiant"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setNiveau(rs.getString("promotion"));
                etudiants.add(etudiant);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture de tous les étudiants: " + e.getMessage());
        } finally {
            Conn.closeConnection(conn);
        }
        return etudiants;
    }
}