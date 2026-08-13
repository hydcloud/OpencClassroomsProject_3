# Documentation technique — DataShare

> **Objectif :** présenter de manière concise l’architecture, les choix technologiques, le modèle de données, les principaux endpoints, la sécurité, la qualité, l’installation et l’utilisation de l’IA dans le développement de DataShare.

---

# 1. Architecture de l’application

DataShare repose sur une architecture **frontend / backend / base de données**.

```text
┌─────────────────────────┐
│     Angular Frontend    │
│   http://localhost:4200 │
└────────────┬────────────┘
             │ HTTP / JSON
             ▼
┌─────────────────────────┐
│  Spring Boot REST API   │
│   http://localhost:8080 │
└────────────┬────────────┘
             │
       ┌─────┴─────┐
       ▼           ▼
┌────────────┐  ┌────────────────┐
│ PostgreSQL │  │ Stockage local │
│ port 5432  │  │ backend/uploads│
└────────────┘  └────────────────┘
```

Le backend est organisé en couches :

```text
Controller
    ↓
Service
    ↓
Repository / Storage
    ↓
PostgreSQL / système de fichiers
```

Cette organisation sépare l’exposition HTTP, la logique métier, la persistance et le stockage physique.

Des diagrammes plus détaillés sont disponibles dans :

```text
Schemas et Contrat d'interface/
├── Architecture Finale.drawio
├── Schéma architecture.drawio
├── Schéma des flux.png
└── Flux - US*.drawio
```

---

# 2. Justification des choix technologiques

## Backend — Java 21 et Spring Boot

**Java 21** fournit une base stable, typée et adaptée aux applications backend maintenables.

**Spring Boot** a été retenu pour :

- construire rapidement une API REST ;
- structurer l’application par responsabilités ;
- gérer l’injection de dépendances ;
- intégrer facilement Spring Security ;
- utiliser Spring Data JPA pour la persistance ;
- bénéficier d’un écosystème mature de tests.

L’organisation `controller → service → repository` permet de limiter le couplage entre la couche HTTP et la logique métier.

## Spring Security, JWT et BCrypt

L’application utilise **Spring Security** pour la gestion des accès.

Le **JWT** permet une authentification stateless adaptée à une API REST consommée par Angular.

**BCrypt** est utilisé pour le hash des mots de passe afin de ne jamais stocker les mots de passe utilisateurs en clair.

La même logique de hash est utilisée pour la protection facultative des fichiers.

## Frontend — Angular et TypeScript

**Angular** a été retenu pour disposer :

- d’une architecture frontend structurée ;
- d’un système de routage ;
- de services pour les appels API ;
- de modèles TypeScript ;
- d’une séparation claire des fonctionnalités ;
- d’un environnement adapté aux tests E2E.

**TypeScript** apporte un typage statique utile pour maintenir les contrats entre frontend et backend.

## PostgreSQL

**PostgreSQL 17** assure la persistance des utilisateurs, fichiers et liens de téléchargement.

Il est exécuté dans un conteneur Docker afin de rendre l’environnement de base de données simple à reproduire.

## Docker

Docker est utilisé pour PostgreSQL afin :

- d’éviter une installation locale spécifique ;
- d’obtenir une version identique de PostgreSQL ;
- de simplifier le démarrage de l’environnement ;
- de conserver les données dans un volume dédié.

## Stockage local

Les fichiers sont actuellement stockés localement dans :

```text
backend/uploads/
```

Le backend utilise une abstraction :

```text
StorageService
        ↑
LocalStorageService
```

Cette séparation permet d’envisager ultérieurement une autre implémentation de stockage sans déplacer la logique métier dans les controllers.

---

# 3. Modèle de données

DataShare utilise trois tables principales :

```text
users
stored_files
download_links
```

Vue simplifiée :

```text
┌─────────────┐
│    users    │
└──────┬──────┘
       │ propriétaire
       │
       ▼
┌────────────────┐
│  stored_files  │
└───────┬────────┘
        │ lien(s)
        ▼
┌────────────────┐
│ download_links │
└────────────────┘
```

### `users`

Contient les informations nécessaires à l’identification et à l’authentification des utilisateurs.

### `stored_files`

Représente les fichiers connus de l’application :

- informations du fichier ;
- propriétaire éventuel ;
- expiration ;
- protection facultative par mot de passe.

Un upload anonyme peut exister sans utilisateur propriétaire.

### `download_links`

Représente les liens/token permettant d’accéder à un fichier.

Le modèle détaillé est fourni dans :

```text
Schemas et Contrat d'interface/Structure de données.xlsx
```

---

# 4. Documentation des endpoints principaux

Les endpoints ci-dessous correspondent aux controllers actuels de l’application.

## Utilisateurs et authentification

Base :

```text
/api
```

### Créer un compte

```http
POST /api/user
```

Corps JSON :

```json
{
  "email": "utilisateur@example.com",
  "password": "mot-de-passe"
}
```

Le backend valide le `RegisterRequest`, appelle `UserRegistrationService` et retourne une réponse **HTTP 201 Created**.

### Se connecter

```http
POST /api/login
```

Corps JSON :

```json
{
  "email": "utilisateur@example.com",
  "password": "mot-de-passe"
}
```

Le backend appelle `AuthService` et retourne une `LoginResponse` contenant les informations nécessaires à l’authentification du frontend.

Réponse nominale :

```text
HTTP 200 OK
```

---

## Fichiers

Base :

```text
/api/files
```

### Upload authentifié

```http
POST /api/files
```

Type :

```text
multipart/form-data
```

Paramètres :

| Paramètre | Obligatoire | Valeur |
|---|---|---|
| `file` | Oui | Fichier |
| `expirationDays` | Non | Défaut : 7 |
| `password` | Non | Protection facultative |

Authentification :

```text
JWT requis
```

Réponse nominale :

```text
HTTP 201 Created
```

---

### Upload anonyme — US07

```http
POST /api/files/anonymous
```

Type :

```text
multipart/form-data
```

Paramètres :

| Paramètre | Obligatoire | Valeur |
|---|---|---|
| `file` | Oui | Fichier |
| `expirationDays` | Non | Défaut : 7 |
| `password` | Non | Protection facultative |

Réponse nominale :

```text
HTTP 201 Created
```

Cet endpoint permet l’envoi d’un fichier sans authentification.

---

### Historique des fichiers

```http
GET /api/files
```

Authentification :

```text
JWT requis
```

Le backend récupère l’utilisateur depuis l’objet `Authentication` et retourne :

```text
List<FileHistoryResponse>
```

Réponse nominale :

```text
HTTP 200 OK
```

---

### Télécharger un fichier

```http
POST /api/files/{token}/file
```

Paramètre de chemin :

```text
token
```

Corps facultatif :

```json
{
  "password": "mot-de-passe-du-fichier"
}
```

Si le fichier n’est pas protégé, le body peut être absent.

Le backend retourne le fichier comme `Resource` avec un `Content-Disposition: attachment`.

Cas nominal :

```text
HTTP 200 OK
```

Mot de passe incorrect :

```text
HTTP 403 Forbidden
```

---

### Supprimer un fichier

```http
DELETE /api/files/{id}
```

Authentification :

```text
JWT requis
```

Le fichier est supprimé pour l’utilisateur authentifié.

Réponse nominale :

```text
HTTP 204 No Content
```

---

# 5. Sécurité et gestion des accès

Flux d’authentification :

```text
POST /api/login
       ↓
AuthService
       ↓
Vérification utilisateur / mot de passe
       ↓
JwtService
       ↓
JWT
       ↓
Angular
       ↓
Authorization: Bearer <token>
       ↓
JwtAuthenticationFilter
       ↓
Route protégée
```

Principales mesures :

- Spring Security ;
- JWT ;
- session stateless ;
- BCrypt ;
- contrôle des routes publiques et protégées ;
- CORS ;
- validation des requêtes ;
- gestion centralisée des exceptions ;
- mot de passe facultatif pour les fichiers ;
- absence d’exposition du hash des mots de passe dans les DTO.

La stratégie détaillée et les scans de dépendances sont disponibles dans :

```text
docs/SECURITY.md
```

---

# 6. Qualité et tests

Le projet utilise plusieurs niveaux de validation.

## Tests backend

Technologies :

```text
JUnit 5
Mockito
MockMvc
Maven
```

Commande :

```bash
cd backend
mvn clean test
```

Résultat de référence :

```text
50 tests
0 failure
0 error
```

## Couverture

JaCoCo :

```text
72 %
```

Objectif indicatif :

```text
≥ 70 %
```

## Tests End-to-End

Cypress couvre trois parcours critiques :

1. connexion + upload authentifié ;
2. upload anonyme + téléchargement ;
3. protection par mot de passe.

## Performance

k6 :

```text
10 utilisateurs virtuels
30 secondes
560 requêtes HTTP
0 % d’échec
p95 : 117,52 ms
```

Budget :

```text
p95 < 500 ms
```

Documentation détaillée :

```text
docs/TESTING.md
docs/PERF.md
docs/MAINTENANCE.md
```

---

# 7. Suivi des dépendances

## Frontend

```bash
npm audit
```

Après correction sans `--force` :

```text
4 vulnérabilités restantes
1 low
3 moderate
0 high
```

## Backend

OWASP Dependency-Check :

```bash
mvn org.owasp:dependency-check-maven:12.2.2:check
```

Le rapport permet d’identifier et d’analyser les CVE associées aux dépendances backend.

Les décisions sont détaillées dans :

```text
docs/SECURITY.md
```

---

# 8. Installation et exécution

## Prérequis

Environnement de développement utilisé :

```text
WSL2
OpenJDK 21.0.11
Maven 3.9.12
Node.js 22.22.1
npm 9.2.0
Docker
PostgreSQL 17
```

Pour une nouvelle installation du frontend, **Node.js 22.22.3 ou supérieur** est recommandé car l’Angular CLI actuellement installé demande au minimum cette version.

## Base PostgreSQL

```bash
cd backend
docker compose up -d
```

Conteneur :

```text
datashare-postgres
```

Base :

```text
datashare
```

Port :

```text
5432
```

## Backend

```bash
cd backend
mvn spring-boot:run
```

URL :

```text
http://localhost:8080
```

## Frontend

```bash
cd frontend
npm install
npm start
```

URL :

```text
http://localhost:4200
```

Les instructions détaillées sont disponibles dans :

```text
README.md
```

---

# 9. Maintenance

Une évolution backend suit autant que possible ce parcours :

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

Avant livraison :

```bash
mvn clean test
npx cypress run
```

Le test k6 peut être rejoué lorsqu’une modification est susceptible d’affecter les performances.

Les procédures détaillées sont disponibles dans :

```text
docs/MAINTENANCE.md
```

---

# 10. Utilisation de l’IA

L’intelligence artificielle a été utilisée comme **outil d’assistance au développement**, notamment pour :

- expliquer des concepts Java, Spring Boot et Angular ;
- accompagner le débogage ;
- analyser des erreurs de compilation ;
- analyser des échecs de tests ;
- proposer des pistes de tests complémentaires ;
- accompagner la structuration de certains composants ;
- préparer les scénarios de validation ;
- aider à structurer la documentation technique.

Les propositions issues de l’IA ont été :

```text
Proposition
    ↓
Analyse
    ↓
Intégration éventuelle
    ↓
Compilation
    ↓
Tests
    ↓
Validation
```

L’IA n’a donc pas remplacé la validation technique.

Le projet a été vérifié par :

- tests backend ;
- tests Cypress ;
- couverture JaCoCo ;
- test de performance k6 ;
- `npm audit` ;
- OWASP Dependency-Check.

---

# 11. Documents associés

```text
README.md

docs/
├── TECHNICAL.md
├── TESTING.md
├── SECURITY.md
├── PERF.md
└── MAINTENANCE.md

Schemas et Contrat d'interface/
├── Architecture Finale.drawio
├── Schéma des flux.png
├── Structure de données.xlsx
└── Flux - US*.drawio
```

---

# 12. Synthèse

DataShare repose sur :

| Domaine | Choix |
|---|---|
| Frontend | Angular / TypeScript |
| Backend | Java 21 / Spring Boot |
| Sécurité | Spring Security / JWT / BCrypt |
| Base | PostgreSQL 17 |
| BDD locale | Docker |
| Persistance | Spring Data JPA |
| Stockage | Local via abstraction `StorageService` |
| Tests backend | JUnit / Mockito / MockMvc |
| E2E | Cypress |
| Couverture | JaCoCo — 72 % |
| Performance | k6 |
| Audit frontend | npm audit |
| Audit backend | OWASP Dependency-Check |

> **Conclusion :** l’architecture vise à rester simple pour le MVP tout en séparant suffisamment les responsabilités pour permettre les tests, la maintenance et de futures évolutions.
