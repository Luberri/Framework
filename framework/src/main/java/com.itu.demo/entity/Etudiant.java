package com.itu.demo.entity;

import com.itu.demo.Conn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Etudiant {
    
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String dateNaissance;
    private String numeroEtudiant;
    private String promotion;
    private String dateInscription;
    
    public Etudiant() {
    }
    
    public Etudiant(int id, String nom, String prenom, String email, String dateNaissance, String numeroEtudiant, String promotion, String dateInscription) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateNaissance = dateNaissance;
        this.numeroEtudiant = numeroEtudiant;
        this.promotion = promotion;
        this.dateInscription = dateInscription;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getDateNaissance() {
        return dateNaissance;
    }
    
    public String getNumeroEtudiant() {
        return numeroEtudiant;
    }
    
    public String getPromotion() {
        return promotion;
    }
    
    public String getDateInscription() {
        return dateInscription;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    
    public void setNumeroEtudiant(String numeroEtudiant) {
        this.numeroEtudiant = numeroEtudiant;
    }
    
    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }
    
    public void setDateInscription(String dateInscription) {
        this.dateInscription = dateInscription;
    }
    
    @Override
    public String toString() {
        return "Etudiant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", dateNaissance='" + dateNaissance + '\'' +
                ", numeroEtudiant='" + numeroEtudiant + '\'' +
                ", promotion='" + promotion + '\'' +
                ", dateInscription='" + dateInscription + '\'' +
                '}';
    }
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Créer (Insérer) un nouvel étudiant dans la base de données
     * @return true si l'insertion est réussie, false sinon
     */
    public boolean create() {
        String sql = "INSERT INTO etudiant (nom, prenom, email, date_naissance, numero_etudiant, promotion, date_inscription) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, this.nom);
            pst.setString(2, this.prenom);
            pst.setString(3, this.email);
            pst.setString(4, this.dateNaissance);
            pst.setString(5, this.numeroEtudiant);
            pst.setString(6, this.promotion);
            pst.setString(7, this.dateInscription);
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
     * Lire (Récupérer) un étudiant par son ID
     * @param id ID de l'étudiant
     * @return Objet Etudiant ou null si non trouvé
     */
    public static Etudiant readById(int id) {
        String sql = "SELECT * FROM etudiant WHERE id = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Etudiant etudiant = new Etudiant();
                etudiant.setId(rs.getInt("id"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setDateNaissance(rs.getString("date_naissance"));
                etudiant.setNumeroEtudiant(rs.getString("numero_etudiant"));
                etudiant.setPromotion(rs.getString("promotion"));
                etudiant.setDateInscription(rs.getString("date_inscription"));
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
     * Lire (Récupérer) un étudiant par son numéro d'étudiant
     * @param numeroEtudiant Numéro d'étudiant
     * @return Objet Etudiant ou null si non trouvé
     */
    public static Etudiant read(String numeroEtudiant) {
        String sql = "SELECT * FROM etudiant WHERE numero_etudiant = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, numeroEtudiant);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Etudiant etudiant = new Etudiant();
                etudiant.setId(rs.getInt("id"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setDateNaissance(rs.getString("date_naissance"));
                etudiant.setNumeroEtudiant(rs.getString("numero_etudiant"));
                etudiant.setPromotion(rs.getString("promotion"));
                etudiant.setDateInscription(rs.getString("date_inscription"));
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
        String sql = "UPDATE etudiant SET nom = ?, prenom = ?, email = ?, date_naissance = ?, numero_etudiant = ?, promotion = ?, date_inscription = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, this.nom);
            pst.setString(2, this.prenom);
            pst.setString(3, this.email);
            pst.setString(4, this.dateNaissance);
            pst.setString(5, this.numeroEtudiant);
            pst.setString(6, this.promotion);
            pst.setString(7, this.dateInscription);
            pst.setInt(8, this.id);
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
     * @param id ID de l'étudiant à supprimer
     * @return true si la suppression est réussie, false sinon
     */
    public static boolean delete(int id) {
        String sql = "DELETE FROM etudiant WHERE id = ?";
        Connection conn = null;
        try {
            conn = Conn.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
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
                etudiant.setId(rs.getInt("id"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setDateNaissance(rs.getString("date_naissance"));
                etudiant.setNumeroEtudiant(rs.getString("numero_etudiant"));
                etudiant.setPromotion(rs.getString("promotion"));
                etudiant.setDateInscription(rs.getString("date_inscription"));
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