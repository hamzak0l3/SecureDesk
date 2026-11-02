# SecureDesk — Architecture Globale DevSecOps & Déploiement Kubernetes

**Auteurs** : Oussama Hamada (CI/CD), Rayen Hassen (Kubernetes/DAST), Aladin Fadhli (Observabilité)  
**Contexte** : Pipeline automatisé GitLab CI, Déploiement Minikube local durci, Observabilité avec Loki/Alloy/Grafana  

---

## 1. Schéma d'Architecture Cible de Bout en Bout

```mermaid
flowchart TD
    subgraph Dev ["1. Développement Local"]
        DEV[Développeur] -->|git commit| PRE[Hook Pre-commit : Gitleaks]
        PRE -->|git push| GITLAB[Dépôt GitLab Privé]
    end

    subgraph Pipeline ["2. Pipeline GitLab CI (.gitlab-ci.yml) - Runner Dédié local-k8s"]
        direction TB
        S_BUILD[Stage build : Maven Package]
        S_TEST[Stage test : Tests Unitaires JUnit & H2]
        S_SECRETS[Stage secrets : Gitleaks Scan]
        S_SAST[Stage sast : Semgrep Static Analysis]
        S_DEPS[Stage deps : Trivy Filesystem Scan]
        S_IMAGE[Stage image : Docker Multi-stage Build & Non-root]
        S_SCAN[Stage scan : Trivy Container Image Scan]
        S_DEPLOY[Stage deploy : Kubectl apply sur Minikube]
        S_DAST[Stage dast : OWASP ZAP Dynamic Scan]

        S_BUILD --> S_TEST
        S_TEST --> S_SECRETS
        S_SECRETS --> S_SAST
        S_SAST --> S_DEPS
        S_DEPS --> S_IMAGE
        S_IMAGE --> S_SCAN
        S_SCAN --> S_DEPLOY
        S_DEPLOY --> S_DAST
    end

    GITLAB --> Pipeline

    subgraph Kube ["3. Cluster Minikube (VM Ubuntu Server 24.04)"]
        subgraph NS_HelpDesk ["Namespace : helpdesk"]
            ING[Ingress NGINX Controller]
            APP_POD[Pod : helpdesk-app<br/>Spring Boot 3 / JRE Alpine<br/>User non-root : UID 10001<br/>Read-only RootFS]
            DB_POD[Pod : postgresql<br/>PostgreSQL 16<br/>PersistentVolume / PVC]
            
            ING -->|HTTP 8080| APP_POD
            APP_POD -->|TCP 5432| DB_POD
        end

        subgraph NS_Monitoring ["Namespace : monitoring"]
            ALLOY[Grafana Alloy DaemonSet / Pod]
            LOKI[Loki Engine Single-Binary]
            GRAFANA[Grafana Web UI Dashboard]
            
            ALLOY -->|Logs d'accès & événements de sécurité| LOKI
            LOKI --> GRAFANA
        end
    end

    S_DEPLOY -->|Déploiement Manifestes| NS_HelpDesk
    S_DAST -.->|Attaques DAST & Traçabilité| ING
    APP_POD -.->|Collecte Logs applicatifs| ALLOY
    ING -.->|Collecte Logs Ingress NGINX| ALLOY
```

---

## 2. Politiques des Barrières de Sécurité (Security Gates)

Chaque livraison logicielle doit traverser un ensemble de filtres rigoureux. Un échec sur un seuil critique interrompt immédiatement la livraison :

| Outil | Étape du Pipeline | Condition de Rupture (Exit Code != 0) | Justification Technique |
|:---|:---|:---|:---|
| **Gitleaks** | `secrets` | Détection d'un secret (regex/entropie) | Tout secret présent dans le git log est potentiellement compromis. |
| **Semgrep** | `sast` | Règle retournant une sévérité `ERROR` | Les injections (SQLi, XSS) doivent bloquer la chaîne avant packaging. |
| **Trivy (fs)** | `deps` | CVE `CRITICAL` avec correctif disponible | Interdiction d'embarquer des dépendances tierces vulnérables connues. |
| **Trivy (image)**| `scan` | CVE `CRITICAL` sur les packages de l'OS | L'image de base doit être minimale et exempte de failles critiques. |
| **OWASP ZAP** | `dast` | Alerte confirmée de niveau `HIGH` | Confirmation dynamique d'exploitabilité sur l'application déployée. |

---

## 3. Durcissement de l'Infrastructure Kubernetes

Le déploiement sous Minikube intègre les règles de durcissement recommandées par le benchmark CIS Kubernetes :

1. **Isolation par Namespaces** :
   - `helpdesk` pour l'application et la persistance.
   - `monitoring` pour la suite Alloy / Loki / Grafana.
2. **Politique Réseau (NetworkPolicy Zero-Trust)** :
   - Règle de refus par défaut (`default-deny-all`) dans le namespace `helpdesk`.
   - Seul le contrôleur Ingress NGINX est autorisé à contacter le port 8080 du pod `helpdesk-app`.
   - Seul le pod `helpdesk-app` est autorisé à communiquer avec le port 5432 du pod `postgresql`.
   - Le namespace `monitoring` peut uniquement scraper les métriques et lire les logs.
3. **Contexte de Sécurité des Pods (SecurityContext)** :
   ```yaml
   securityContext:
     runAsNonRoot: true
     runAsUser: 10001
     runAsGroup: 10001
     allowPrivilegeEscalation: false
     readOnlyRootFilesystem: true
     capabilities:
       drop:
         - ALL
   ```
4. **Gestion des Secrets et Volumes** :
   - Découplage strict via objets Kubernetes `Secret` et `ConfigMap`.
   - Stockage persistant PostgreSQL garanti par `PersistentVolumeClaim` (PVC) pour éviter toute perte de données lors des redémarrages.

---

## 4. Chaîne d'Observabilité et Détection des Attaques

- **Grafana Alloy** collecte les logs applicatifs (format JSON Spring Boot) et les logs d'accès HTTP Ingress NGINX.
- **Loki** indexe les flux avec les labels de pods, namespaces et codes HTTP.
- **Grafana Dashboards** permet la corrélation immédiate lors des tests DAST et des scénarios d'attaque :
  - Pics d'erreurs HTTP 401 / 403 (Tentatives de bypass de rôle V4 ou IDOR V3).
  - Requêtes contenant des patterns suspects SQL (`' OR '1'='1`) ou scripts XSS (`<script>`).
  - Détection d'appels répétés sur `/actuator/**`.

