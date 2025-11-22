<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Détails de l'Etudiant</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 600px;
            margin: 0 auto;
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            padding: 30px;
        }
        
        h1 {
            color: #333;
            margin-bottom: 30px;
            text-align: center;
            font-size: 2.5em;
        }
        
        .detail-group {
            margin-bottom: 20px;
            padding-bottom: 20px;
            border-bottom: 1px solid #ddd;
        }
        
        .detail-group:last-child {
            border-bottom: none;
        }
        
        .detail-label {
            font-weight: 600;
            color: #667eea;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .detail-value {
            color: #333;
            font-size: 1.1em;
            margin-top: 5px;
        }
        
        .btn-container {
            margin-top: 30px;
            display: flex;
            gap: 10px;
            justify-content: center;
        }
        
        .btn {
            display: inline-block;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 5px;
            transition: all 0.3s ease;
            border: none;
            cursor: pointer;
            font-size: 1em;
            flex: 1;
            text-align: center;
        }
        
        .btn-primary {
            background-color: #667eea;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #764ba2;
        }
        
        .btn-secondary {
            background-color: #95a5a6;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #7f8c8d;
        }
        
        .btn-danger {
            background-color: #e74c3c;
            color: white;
        }
        
        .btn-danger:hover {
            background-color: #c0392b;
        }
        
        .message {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
            text-align: center;
        }
        
        .message.error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Détails de l'Etudiant</h1>
        
        <c:if test="${empty etudiant}">
            <div class="message error">
                Etudiant non trouvé.
            </div>
            <div class="btn-container">
                <a href="<c:url value='/etudiant/list'/>" class="btn btn-secondary">Retour à la liste</a>
            </div>
        </c:if>
        
        <c:if test="${not empty etudiant}">
            <div class="detail-group">
                <div class="detail-label">ID</div>
                <div class="detail-value">${etudiant.id}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Numéro d'Étudiant</div>
                <div class="detail-value">${etudiant.numeroEtudiant}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Nom</div>
                <div class="detail-value">${etudiant.nom}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Prénom</div>
                <div class="detail-value">${etudiant.prenom}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Email</div>
                <div class="detail-value">${etudiant.email}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Date de Naissance</div>
                <div class="detail-value">${etudiant.dateNaissance}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Promotion</div>
                <div class="detail-value">${etudiant.promotion}</div>
            </div>
            
            <div class="detail-group">
                <div class="detail-label">Date d'Inscription</div>
                <div class="detail-value">${etudiant.dateInscription}</div>
            </div>
            
            <div class="btn-container">
                <a href="<c:url value='/etudiant/list'/>" class="btn btn-secondary">Retour</a>
            </div>
        </c:if>
    </div>
</body>
</html>