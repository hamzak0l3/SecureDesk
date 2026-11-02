# 🛡️ SecureDesk — Application HelpDesk DevSecOps

## Description
SecureDesk est une application de gestion de tickets HelpDesk développée dans le cadre d'un projet DevSecOps. Elle permet de gérer des tickets de support avec trois rôles utilisateurs : Client, Agent et Administrateur.

## Technologies
| Domaine | Technologie |
|---------|-------------|
| Backend | Java 21, Spring Boot 3.2 |
| Base de données | PostgreSQL 16 |
| Sécurité | Spring Security |
| Frontend | Thymeleaf, Bootstrap 5 |
| Build | Maven |

## Prérequis
- **Java 21** (JDK)
- **Maven 3.9+**
- **PostgreSQL 16**

## Installation

### 1. Configurer PostgreSQL
```sql
CREATE DATABASE securedesk;
CREATE USER securedesk WITH PASSWORD 'securedesk';
GRANT ALL PRIVILEGES ON DATABASE securedesk TO securedesk;
```

### 2. Cloner et lancer
```bash
git clone <url-du-depot>
cd SecureDesk
mvn spring-boot:run
```

### 3. Accéder à l'application
Ouvrir http://localhost:8080

## Comptes par défaut
| Rôle | Utilisateur | Mot de passe |
|------|-------------|--------------|
| Admin | `admin` | `admin123` |
| Agent | `agent` | `agent123` |
| Client | `client` | `client123` |

## Fonctionnalités

### Client (ROLE_CLIENT)
- Créer un ticket de support
- Consulter ses tickets
- Ajouter des commentaires
- Suivre l'état de ses tickets

### Agent (ROLE_AGENT)
- Consulter les tickets assignés
- Modifier le statut des tickets
- Ajouter des commentaires
- Être assigné à des tickets

### Administrateur (ROLE_ADMIN)
- Gérer tous les tickets
- Gérer les utilisateurs (créer, modifier, supprimer)
- Gérer les catégories
- Assigner des tickets aux agents
- Consulter les statistiques

## Structure du projet
```
SecureDesk/
├── src/
│   ├── main/
│   │   ├── java/com/securedesk/
│   │   │   ├── config/          # Configuration Spring Security
│   │   │   ├── controller/      # Contrôleurs MVC
│   │   │   ├── dto/             # Objets de transfert
│   │   │   ├── entity/          # Entités JPA
│   │   │   ├── repository/      # Repositories Spring Data
│   │   │   └── service/         # Services métier
│   │   └── resources/
│   │       ├── templates/       # Templates Thymeleaf
│   │       ├── static/css/      # Feuilles de style
│   │       └── application.properties
│   └── test/                    # Tests unitaires
├── pom.xml
└── README.md
```

## Cycle de vie d'un ticket
```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
```

## Équipe
- **Rayen** — Kubernetes, Docker, DAST
- **Hamza** — Application, Vulnérabilités, Scanners
- **Oussama** — CI/CD, GitLab, Organisation
- **Aladin** — Monitoring

## Licence
Projet académique — Usage éducatif uniquement.

Here is a clear recap of how to run SecureDesk:

1. Working Directory
Open PowerShell or Command Prompt at the project root:

powershell
cd C:\Users\zizou\Desktop\SecureDesk
2. Run Commands
NOTE

Port 8080 is currently taken by the local Oracle listener (TNSLSNR), so we run the application on port 8081.

Option A: Quick / In-Memory H2 Mode (Zero DB Setup — Recommended for testing)
Runs immediately without needing a local PostgreSQL database:

powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=test" "-Dspring-boot.run.arguments=--server.port=8081"
Option B: Full PostgreSQL Mode
(Requires PostgreSQL installed and running on port 5432 with database securedesk):

powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
3. Access the Application
Once the console shows that the server has started:

URL: http://localhost:8081
Default Accounts:
Role	Username	Password	Access / Capabilities
Admin	admin	admin123	Dashboard, user management, category management, ticket assignment
Agent	agent	agent123	Assigned tickets, ticket status updates, comments
Client	client	client123	Create support tickets, view own tickets, add comments
New User	Register	Choose password	Self-registration creates a client account (ROLE_CLIENT)
4. Useful Commands
Run the automated test suite:
powershell
mvn test
Stop the server: Press Ctrl + C in the terminal where it is running.