# SecureDesk — Manuel de l'Application & Documentation Technique (Phase 2)

**Projet** : SecureDesk (Jalon J2 — Application Fonctionnelle)  
**Auteurs** : Hamza Kalai (Lead Dev Application), Rayen Hassen (Lead Sécurité)  
**Technologies** : Java 21, Spring Boot 3.2.2, Spring Security 6, Spring Data JPA, PostgreSQL 16, Thymeleaf, H2 (tests)  

---

## 1. Vue d'Ensemble de l'Application

SecureDesk est un système de HelpDesk d'entreprise développé selon les principes de l'architecture en couches (*Layered Architecture*) :
- **Entités (`com.securedesk.entity`)** : `User`, `Role`, `Ticket`, `Category`, `Comment`, `TicketStatus`, `TicketPriority`.
- **Accès aux données (`com.securedesk.repository`)** : Repositories Spring Data JPA avec méthodes dérivées et optimisations de requêtes.
- **Logique métier (`com.securedesk.service`)** : Gestion du cycle de vie des tickets, hachage sécurisé BCrypt, vérification des permissions RBAC, statistiques de dashboard.
- **Contrôleurs Web MVC (`com.securedesk.controller`)** : `AuthController`, `TicketController`, `AgentController`, `AdminController`, `DashboardController`, `ProfileController`.
- **Vues (`templates/`)** : Templates Thymeleaf avec intégration responsive Bootstrap/CSS et Spring Security Dialect (`sec:authorize`).

---

## 2. Comptes et Données de Test par Défaut

Les données de démarrage sont automatiquement chargées par `DataInitializer.java` (et `data.sql`) avec hachage BCrypt (coût 10) :

| Rôle | Nom d'utilisateur | Mot de passe | Adresse e-mail | Permissions principales |
|:---|:---|:---|:---|:---|
| **Administrateur** | `admin` | `admin123` | `admin@securedesk.fr` | Gestion des comptes utilisateurs, modification des rôles, gestion des catégories, tableau de bord statistique complet. |
| **Agent Support** | `agent` | `agent123` | `agent@securedesk.fr` | Traitement des tickets, auto-assignation, modification des statuts (`IN_PROGRESS`, `RESOLVED`, `CLOSED`), commentaires, recherche. |
| **Client** | `client` | `client123` | `client@securedesk.fr` | Création de tickets, consultation de ses propres tickets exclusivement, réouverture de tickets, commentaires. |

---

## 3. Guide d'Exécution & Déploiement Local

### 3.1 Prérequis
- Java 21 LTS
- Maven 3.9+
- PostgreSQL 16 (ou conteneur Docker PostgreSQL)

### 3.2 Démarrage de PostgreSQL avec Docker (Optionnel)
```bash
docker run --name securedesk-postgres \
  -e POSTGRES_DB=securedesk \
  -e POSTGRES_USER=securedesk \
  -e POSTGRES_PASSWORD=securedesk \
  -p 5432:5432 -d postgres:16-alpine
```

### 3.3 Lancement de l'Application Spring Boot
```bash
# Compilation et packaging
mvn clean package -DskipTests

# Exécution de l'application
mvn spring-boot:run
```
L'application est accessible à l'adresse : `http://localhost:8080/`

---

## 4. Couverture des Tests Fonctionnels (Phase 2)

La suite de tests automatisée est configurée avec **JUnit 5**, **Mockito** et **Spring Security Test** sous le profil `test` (base H2 in-memory isolée) :

| Classe de Test | Périmètre Validé | Objectif DevSecOps |
|:---|:---|:---|
| **`UserServiceTest`** | Création, encodage BCrypt, unicité, vérification mot de passe, soft-delete. | Garantit l'absence de mots de passe en clair et l'intégrité des comptes. |
| **`TicketServiceTest`** | Création, transitions de statut (`OPEN` ➔ `IN_PROGRESS` ➔ `CLOSED`), closedAt, stats dashboard. | Valide la machine à états et les règles métier de traitement des tickets. |
| **`CategoryServiceTest`** | CRUD catégories, filtrage `findByActiveTrue`, soft-delete. | Assure la cohérence des catégories sans rupture de clés étrangères. |
| **`CommentServiceTest`** | Ajout de commentaire, horodatage, mise à jour automatique de `ticket.updatedAt`. | Valide la traçabilité des échanges sur chaque ticket. |
| **`SecurityAccessTest`** | Endpoints publics (`/login`, `/register`), redirection des anonymes, blocage RBAC strict (Client/Agent bloqués sur `/admin/**`). | Vérifie la barrière Spring Security avant introduction des failles. |
| **`SecureDeskApplicationTests`** | Chargement du contexte Spring Boot global avec profil H2. | Valide l'injection de dépendances et la cohérence des entités JPA. |

### Exécution des Tests :
```bash
mvn test
```

