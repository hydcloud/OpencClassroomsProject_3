# 📦 DataShare

> Application web de partage de fichiers développée avec **Spring Boot**, **Angular** et **PostgreSQL**.

DataShare permet à un utilisateur de créer un compte, de se connecter, d'envoyer des fichiers, de consulter son historique et de télécharger ou supprimer ses fichiers.  
Le projet intègre également deux fonctionnalités avancées :

- **US07 — Upload anonyme**
- **US09 — Protection d'un fichier par mot de passe**

---

## Fonctionnalités principales

### Utilisateur authentifié

- création de compte ;
- connexion sécurisée ;
- authentification par JWT ;
- upload de fichiers ;
- consultation de l'historique ;
- téléchargement ;
- suppression des fichiers.

### Upload anonyme — US07

Un utilisateur non connecté peut :

```text
Sélectionner un fichier
        ↓
Envoyer le fichier
        ↓
Recevoir un lien unique
        ↓
Télécharger le fichier
```

### Protection par mot de passe — US09

Un fichier peut être protégé par un mot de passe.

```text
Mot de passe correct
→ téléchargement autorisé

Mot de passe incorrect
→ HTTP 403 Forbidden
→ téléchargement refusé
```

Les mots de passe de fichiers sont stockés sous forme hashée.

---

# Architecture technique

L'application suit une architecture séparant clairement frontend, backend, persistance et stockage.

```text
┌─────────────────────┐
│   Angular Frontend  │
│ localhost:4200      │
└──────────┬──────────┘
           │ HTTP / JSON
           ▼
┌─────────────────────┐
│ Spring Boot Backend │
│ localhost:8080      │
└──────────┬──────────┘
           │
      ┌────┴────┐
      ▼         ▼
 PostgreSQL   Stockage local
 port 5432    backend/uploads/
```

Le backend respecte une architecture Java en couches :

```text
Controller
    ↓
Service
    ↓
Repository / Storage
    ↓
PostgreSQL / système de fichiers
```

---

# Technologies

## Backend

| Technologie | Utilisation |
|---|---|
| Java 21 | Langage backend |
| Spring Boot | API REST |
| Spring Security | Authentification et autorisations |
| JWT | Authentification stateless |
| BCrypt | Hash des mots de passe |
| Spring Data JPA | Persistance |
| PostgreSQL 17 | Base de données |
| Maven | Build et dépendances |
| JUnit 5 | Tests |
| Mockito | Tests unitaires |
| JaCoCo | Couverture de code |

Le backend a été développé avec **IntelliJ IDEA**.

---

## Frontend

| Technologie | Utilisation |
|---|---|
| Angular | Framework frontend |
| TypeScript | Langage frontend |
| HTML / SCSS | Interface |
| Cypress | Tests End-to-End |
| npm | Gestion des dépendances |

Le frontend a été développé avec **Visual Studio Code**.

---

## Environnement de développement

Le projet a été développé sous **WSL2**.

Versions utilisées pendant le développement :

```text
Java       : OpenJDK 21.0.11
Maven      : 3.9.12
Node.js    : 22.22.1
npm        : 9.2.0
PostgreSQL : 17
Docker     : utilisé pour PostgreSQL
```

> Sur l'environnement actuel, `npx ng version` indique que l'Angular CLI installé demande au minimum **Node.js 22.22.3**. Pour une nouvelle installation, utiliser **Node.js 22.22.3 ou supérieur** est donc recommandé.

---

# Organisation du projet

```text
Projet_2/
├── backend/
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   └── uploads/
│
├── frontend/
│   ├── package.json
│   ├── angular.json
│   ├── cypress/
│   └── src/
│
├── docs/
│   ├── TESTING.md
│   ├── SECURITY.md
│   ├── PERF.md
│   └── MAINTENANCE.md
│
├── perf/
│   └── files-history.js
│
└── Schemas et Contrat d'interface/
```

---

# Structure du backend

Le code Java se trouve dans :

```text
backend/src/main/java/fr/datashare/backend/
```

```text
backend/
├── config/
├── controller/
├── dto/
│   ├── auth/
│   └── file/
├── entity/
├── exception/
├── repository/
├── security/
└── service/
```

| Couche | Responsabilité |
|---|---|
| `controller` | Endpoints HTTP |
| `service` | Logique métier |
| `repository` | Accès à PostgreSQL |
| `entity` | Entités persistées |
| `dto` | Contrats API |
| `security` | JWT et authentification |
| `config` | Configuration Spring Security |
| `exception` | Gestion centralisée des erreurs |

---

# Base de données

PostgreSQL est exécuté dans Docker avec l'image :

```text
postgres:17
```

Configuration du conteneur :

```text
Conteneur : datashare-postgres
Base      : datashare
Utilisateur: datashare
Port      : 5432
```

Le fichier de configuration est :

```text
backend/docker-compose.yml
```

Il définit PostgreSQL 17, le conteneur `datashare-postgres`, le port `5432` et un volume Docker persistant `postgres-data`. fileciteturn91file0L9-L27

### Tables utilisées

```text
users
stored_files
download_links
```

Relations principales :

```text
users
  │
  └── stored_files
          │
          └── download_links
```

Un fichier anonyme peut être enregistré sans propriétaire utilisateur.

---

# Installation et lancement

Les commandes ci-dessous sont à exécuter depuis la racine du projet.

## 1. Prérequis

Installer :

- WSL2 ou Linux ;
- Java 21 ;
- Maven ;
- Docker ;
- Node.js 22.22.3+ recommandé ;
- npm.

---

## 2. Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
cd Projet_2
```

---

## 3. Démarrer PostgreSQL

```bash
cd backend
docker compose up -d
```

Vérifier le conteneur :

```bash
docker ps
```

Le conteneur attendu est :

```text
datashare-postgres
```

Pour accéder à PostgreSQL :

```bash
docker exec -it datashare-postgres psql -U datashare
```

Afficher les tables :

```sql
\dt
```

---

## 4. Démarrer le backend

Depuis :

```bash
cd backend
```

Lancer :

```bash
mvn spring-boot:run
```

L'API est disponible sur :

```text
http://localhost:8080
```

---

## 5. Installer le frontend

Dans un autre terminal :

```bash
cd frontend
npm install
```

Pour une installation reproductible à partir du `package-lock.json`, il est également possible d'utiliser :

```bash
npm ci
```

---

## 6. Démarrer Angular

```bash
npm start
```

L'interface est disponible sur :

```text
http://localhost:4200
```

---

# URLs locales

| Service | URL |
|---|---|
| Frontend Angular | `http://localhost:4200` |
| Backend Spring Boot | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |

---

# Sécurité

DataShare utilise :

- Spring Security ;
- authentification JWT ;
- architecture stateless ;
- BCrypt pour les mots de passe ;
- contrôle des routes publiques et privées ;
- CORS ;
- validation des entrées ;
- protection facultative des fichiers par mot de passe.

Flux simplifié :

```text
Connexion
   ↓
Email + mot de passe
   ↓
AuthService
   ↓
JWT
   ↓
JwtAuthenticationFilter
   ↓
Accès aux routes protégées
```

La stratégie de sécurité et les audits sont détaillés dans :

```text
docs/SECURITY.md
```

---

# Accessibilité et interface utilisateur

Une revue de l’interface frontend est menée en s’appuyant sur les recommandations des WCAG 2.2 (Web Content Accessibility Guidelines), avec une attention particulière portée aux bonnes pratiques de niveau AA.

Les points pris en compte sont notamment :

- Utilisation d’éléments HTML sémantiques ;
- Association explicite des champs de formulaire à leurs labels ;
- Navigation au clavier et visibilité du focus ;
- Intitulés compréhensibles pour les boutons, liens et actions ;
- Restitution textuelle et explicite des messages d’erreur et de confirmation ;
- Annonce des messages dynamiques aux technologies d’assistance à l’aide notamment de aria-live, role="alert" et role="status" ;
- Gestion accessible des composants interactifs, notamment des fenêtres modales (identification du rôle, fermeture au clavier et gestion du focus) ;
- Vérification des contrastes entre les textes, contrôles et arrière-plans ;
- Utilisation d’attributs ARIA lorsque la sémantique HTML native ne suffit pas ;
- Adaptation de l’affichage aux différentes tailles d’écran et au zoom afin de préserver la lisibilité et l’utilisation de l’interface.

Les vérifications d’accessibilité comprennent une navigation manuelle au clavier, le contrôle du comportement du focus, la vérification des formulaires et des messages d’erreur, des tests de redimensionnement et de zoom, ainsi qu’un audit d’accessibilité avec les outils du navigateur.

Cette démarche vise à prendre en compte les principaux critères d’accessibilité applicables à l’interface, sans pour autant revendiquer une conformité complète aux WCAG 2.2 AA en l’absence d’un audit exhaustif.

---

# Tests

## Backend

```bash
cd backend
mvn clean test
```

Résultat de référence :

```text
Tests backend : 50
Failures      : 0
Errors        : 0
```

---

## Couverture JaCoCo

Rapport :

```text
backend/target/site/jacoco/index.html
```

Couverture de référence :

```text
72 %
```

> Objectif indicatif de couverture ≥ 70 % atteint.

---

## Tests End-to-End Cypress

Trois scénarios critiques sont présents :

```text
frontend/cypress/e2e/
├── anonymous-upload.cy.ts
├── authenticated-upload.cy.ts
└── password-protected-file.cy.ts
```

Exécution :

```bash
cd frontend
npx cypress run
```

Scénarios couverts :

1. connexion et upload authentifié ;
2. upload anonyme et téléchargement ;
3. fichier protégé : mauvais mot de passe puis bon mot de passe.

Documentation complète :

```text
docs/TESTING.md
```

---

# Performance

Un test de charge léger a été réalisé avec **k6**.

Script :

```text
perf/files-history.js
```

Commande :

```bash
k6 run perf/files-history.js
```

Résultats de référence :

| Indicateur | Résultat |
|---|---:|
| Utilisateurs virtuels | 10 |
| Durée | 30 s |
| Requêtes HTTP | 560 |
| Échecs HTTP | 0 % |
| Temps moyen | 48,74 ms |
| p95 | 117,52 ms |
| Maximum | 146,67 ms |

Documentation :

```text
docs/PERF.md
```

---

# Audits de dépendances

## Frontend

```bash
cd frontend
npm audit
```

Après correction sans mise à jour forcée :

```text
4 vulnérabilités restantes
1 low
3 moderate
0 high
```

---

## Backend

Le backend est analysé avec **OWASP Dependency-Check** :

```bash
cd backend
mvn org.owasp:dependency-check-maven:12.2.2:check
```

Le rapport est généré dans :

```text
backend/target/dependency-check-report.html
```

Les décisions issues de l'analyse sont documentées dans :

```text
docs/SECURITY.md
```

---

# Maintenance

Les règles permettant d'ajouter ou modifier une fonctionnalité sont documentées dans :

```text
docs/MAINTENANCE.md
```

Principe général :

```text
DTO
 ↓
Service
 ↓
Repository / Storage
 ↓
Controller
 ↓
Frontend
 ↓
Tests
```

---

# Documentation du projet

| Document | Contenu |
|---|---|
| `TESTING.md` | Tests unitaires, E2E et couverture |
| `SECURITY.md` | Sécurité et scans de dépendances |
| `PERF.md` | Tests de performance |
| `MAINTENANCE.md` | Maintenance et évolution |
| `Schemas et Contrat d'interface/` | Architecture, flux et modèle de données |

---

# Utilisation de l'intelligence artificielle

L'intelligence artificielle a été utilisée comme **outil d'accompagnement au développement**.

Elle a notamment servi à :

- expliquer certains concepts Java, Spring Boot et Angular ;
- accompagner le débogage ;
- analyser des erreurs de compilation et de tests ;
- proposer des pistes d'amélioration de l'architecture ;
- accompagner l'écriture et l'amélioration des tests ;
- aider à structurer la documentation technique ;
- préparer des scénarios de tests et de validation.

Les propositions produites avec l'aide de l'IA ont été **relues, intégrées, exécutées et validées dans l'environnement réel du projet**.

L'IA n'a donc pas remplacé la validation technique : le comportement de l'application a été vérifié par compilation, tests automatisés, tests E2E, mesure de couverture, tests de performance et audits de dépendances.

---

# Commandes utiles

### Backend

```bash
cd backend

docker compose up -d
mvn spring-boot:run
mvn clean test
```

### Frontend

```bash
cd frontend

npm install
npm start
npx cypress run
npm audit
```

### Performance

```bash
k6 run perf/files-history.js
```

### Arrêter PostgreSQL

```bash
cd backend
docker compose down
```

Pour supprimer également les données persistantes du volume Docker :

```bash
docker compose down -v
```

> ⚠️ Cette dernière commande supprime les données PostgreSQL du projet.

---

## Configuration du secret JWT

Le backend utilise un secret JWT pour générer et vérifier les jetons d'authentification.

Pour des raisons de sécurité, ce secret ne doit pas être stocké directement dans le code source ni ajouté au dépôt Git.

L'application attend la variable d'environnement suivante :

`JWT_SECRET`

### Générer un secret JWT

Un secret sécurisé peut être généré depuis un terminal Linux ou WSL avec OpenSSL :

```bash
openssl rand -base64 64
```

La commande génère une valeur aléatoire qui pourra être utilisée comme secret JWT.

> **Important :** ne jamais publier ou commiter le secret généré.

### Option 1 — Variable d'environnement Linux / WSL

Il est possible de générer le secret et de l'enregistrer directement dans la variable d'environnement :

```bash
export JWT_SECRET="$(openssl rand -base64 64)"
```

Vérifier que la variable est correctement définie :

```bash
echo $JWT_SECRET
```

Puis démarrer le backend depuis le même terminal :

```bash
./mvnw spring-boot:run
```

> La variable définie avec `export` n'est disponible que dans la session courante du terminal.

Pour la rendre persistante, elle peut être ajoutée au fichier `~/.bashrc` ou `~/.zshrc`, selon le shell utilisé.

### Option 2 — IntelliJ IDEA

Si le backend est démarré directement depuis IntelliJ IDEA, la variable définie dans un terminal Linux/WSL n'est pas nécessairement disponible dans l'environnement utilisé par l'IDE.

Générer d'abord un secret :

```bash
openssl rand -base64 64
```

Copier la valeur générée, puis dans IntelliJ IDEA :

1. Ouvrir **Run > Edit Configurations**.
2. Sélectionner la configuration d'exécution du backend.
3. Ajouter une variable d'environnement (dans Environment variables) :
   - JWT_SECRET=***
4. Enregistrer la configuration.
5. Redémarrer l'application.

Spring Boot récupère ensuite cette variable grâce à la configuration suivante :

```properties
jwt.secret=${JWT_SECRET}
```

### Sécurité

- Ne jamais écrire le secret directement dans le code source.
- Ne jamais commiter le secret dans Git.
- Ne jamais publier le secret dans le README.
- Générer un secret différent pour chaque environnement.
- En production, utiliser un mécanisme sécurisé de gestion des secrets.

---

# État du projet

| Élément | Statut |
|---|---|
| Backend Spring Boot | ✅ |
| Frontend Angular | ✅ |
| PostgreSQL Docker | ✅ |
| JWT / sécurité | ✅ |
| Upload authentifié | ✅ |
| Upload anonyme US07 | ✅ |
| Protection par mot de passe US09 | ✅ |
| Tests backend | ✅ |
| Cypress E2E | ✅ |
| JaCoCo ≥ 70 % | ✅ 72 % |
| Test k6 | ✅ |
| Audit npm | ✅ |
| Audit OWASP | ✅ |
| Documentation maintenance | ✅ |

---

# Conclusion

DataShare fournit un MVP fonctionnel de partage de fichiers reposant sur une architecture séparée **Angular / Spring Boot / PostgreSQL**.

Le projet comporte également une démarche de qualité reproductible basée sur :

- des tests automatisés ;
- des tests End-to-End ;
- une mesure de couverture ;
- des audits de dépendances ;
- un test de performance ;
- une documentation de maintenance.

L'objectif est de disposer d'une base simple, testée et maintenable pouvant évoluer vers de nouveaux modes de stockage, de nouvelles politiques de sécurité ou de nouvelles fonctionnalités de partage.
