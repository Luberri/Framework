package com.itu.demo.controller;

import com.itu.demo.annotations.HandleURL;
import com.itu.demo.annotations.Controller;
import com.itu.demo.entity.Etudiant;
import com.itu.demo.ModelView;
import java.util.List;

@Controller
public class EtudiantController {

    @HandleURL("/etudiant/list")
    public ModelView listEtudiants() {
        List<Etudiant> etudiants = Etudiant.readAll();
        ModelView modelView = new ModelView("etudiant/list.jsp");
        modelView.addObject("etudiants", etudiants);
        return modelView;
    }

    @HandleURL("/etudiant/1")
    public ModelView getEtudiant15() {
        Etudiant etudiant = Etudiant.readById(1);
        ModelView modelView = new ModelView("etudiant/detail.jsp");
        modelView.addObject("etudiant", etudiant);
        return modelView;
    }

    @HandleURL("/etudiant")
    public ModelView getEtudiant(int id) {
        Etudiant etudiant = Etudiant.readById(id);
        ModelView modelView = new ModelView("etudiant/detail.jsp");
        modelView.addObject("etudiant", etudiant);
        return modelView;
    }
}
