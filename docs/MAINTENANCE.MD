# 🔧 Maintenance --- DataShare

> **Objectif :** fournir un guide simple permettant de comprendre,
> maintenir et faire évoluer l'application DataShare sans dégrader son
> fonctionnement, sa sécurité ou sa qualité.

------------------------------------------------------------------------

## 1. Vue d'ensemble

DataShare est organisé en deux applications principales :

  -----------------------------------------------------------------------------------
  Partie                  Technologie             Emplacement
  ----------------------- ----------------------- -----------------------------------
  Backend                 Java / Spring Boot /    `backend/`
                          Maven                   

  Frontend                Angular / TypeScript    `frontend/`

  Tests E2E               Cypress                 `frontend/cypress/`

  Tests de performance    k6                      `perf/`

  Documentation           Markdown                `docs/`

  Schémas techniques      draw.io / documents     `Schemas et Contrat d'interface/`
  -----------------------------------------------------------------------------------

Cette séparation permet de faire évoluer indépendamment l'interface
utilisateur, l'API et les outils de validation.

------------------------------------------------------------------------

## 2. Organisation générale du projet

``` text
Projet_2/
├── backend/
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── src/
│       ├── main/
│       └── test/
├── frontend/
│   ├── package.json
│   ├── angular.json
│   ├── cypress/
│   └── src/
├── docs/
├── perf/
├── Schemas et Contrat d'interface/
└── README.md
```

> 💡 Les fichiers générés (`target/`, `node_modules/`, résultats
> temporaires, fichiers téléchargés par Cypress, etc.) ne doivent pas
> être considérés comme du code source à maintenir.

------------------------------------------------------------------------

## 3. Architecture du backend

Le code Java principal se trouve dans :

``` text
backend/src/main/java/fr/datashare/backend/
```

### Structure

``` text
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

### Responsabilité des couches

  -----------------------------------------------------------------------
  Couche                              Responsabilité
  ----------------------------------- -----------------------------------
  `controller`                        Exposer les endpoints HTTP et
                                      recevoir les requêtes

  `service`                           Porter la logique métier

  `repository`                        Accéder aux données persistées

  `entity`                            Représenter les entités métier
                                      persistées

  `dto`                               Définir les contrats
                                      d'entrée/sortie de l'API

  `security`                          Gérer JWT et authentification
                                      Spring Security

  `config`                            Centraliser la configuration de
                                      sécurité

  `exception`                         Centraliser les erreurs métier et
                                      les réponses HTTP associées
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 4. Controllers

Les contrôleurs actuels sont :

``` text
controller/
├── FileController.java
└── UserController.java
```

### `FileController`

À modifier lorsqu'une évolution concerne notamment :

-   l'upload ;
-   le téléchargement ;
-   l'historique ;
-   la suppression ;
-   les liens de téléchargement ;
-   les opérations liées aux fichiers.

### `UserController`

À modifier lorsqu'une évolution concerne notamment :

-   l'inscription ;
-   la connexion ;
-   les opérations liées au compte utilisateur.

> ⚠️ Un controller doit rester léger. La logique métier doit être placée
> dans un service plutôt que directement dans le controller.

------------------------------------------------------------------------

## 5. Services métier

Les services sont situés dans :

``` text
backend/src/main/java/fr/datashare/backend/service/
```

Services actuels :

``` text
AuthService.java
DownloadLinkService.java
DownloadService.java
ExpiredFileCleanupService.java
FileDeletionService.java
FileHistoryService.java
LocalStorageService.java
StorageService.java
UploadService.java
UserRegistrationService.java
```

### Rôle des principaux services

  Service                       Responsabilité principale
  ----------------------------- --------------------------------------------
  `AuthService`                 Authentification
  `UserRegistrationService`     Inscription utilisateur
  `UploadService`               Upload et validation des fichiers
  `DownloadService`             Téléchargement et contrôle du mot de passe
  `DownloadLinkService`         Création et validation des liens
  `FileHistoryService`          Historique des fichiers
  `FileDeletionService`         Suppression des fichiers
  `ExpiredFileCleanupService`   Nettoyage des fichiers expirés
  `StorageService`              Contrat d'accès au stockage
  `LocalStorageService`         Implémentation locale du stockage

### Principe d'évolution

Lorsqu'une nouvelle règle métier est ajoutée :

``` text
Controller
    ↓
Service
    ↓
Repository / Storage
```

Le controller reçoit la requête, le service applique les règles métier
et les repositories ou le stockage assurent la persistance.

------------------------------------------------------------------------

## 6. Stockage

L'accès au stockage est séparé grâce à :

``` text
StorageService.java
LocalStorageService.java
```

`StorageService` représente le contrat de stockage tandis que
`LocalStorageService` fournit l'implémentation locale utilisée
actuellement.

Cette séparation facilite une évolution future vers une autre solution
de stockage sans déplacer la logique métier dans les controllers.

> ⚠️ Le dossier `backend/uploads/` contient les fichiers physiques
> utilisés par l'application. Son contenu ne doit pas être confondu avec
> le code source.

------------------------------------------------------------------------

## 7. Données et repositories

### Entités

``` text
entity/
├── DownloadLink.java
├── StoredFile.java
└── User.java
```

### Repositories

``` text
repository/
├── DownloadLinkRepository.java
├── StoredFileRepository.java
└── UserRepository.java
```

Une modification du modèle de données doit être vérifiée dans :

1.  l'entité concernée ;
2.  son repository ;
3.  les services qui l'utilisent ;
4.  les DTO exposés ;
5.  les tests associés ;
6.  le contrat frontend/backend.

------------------------------------------------------------------------

## 8. DTO et contrats d'API

Les DTO sont séparés par domaine.

### Authentification

``` text
dto/auth/
├── LoginRequest.java
├── LoginResponse.java
├── RegisterRequest.java
└── UserResponse.java
```

### Fichiers

``` text
dto/file/
├── DownloadFileRequest.java
├── FileHistoryResponse.java
└── FileUploadResponse.java
```

> 🔒 Ne pas exposer directement les entités JPA lorsque la réponse API
> doit masquer des données internes ou sensibles.

Toute modification d'un DTO doit être répercutée côté Angular si le
contrat de l'API change.

------------------------------------------------------------------------

## 9. Sécurité

Les composants de sécurité sont :

``` text
security/
├── CustomUserDetailsService.java
├── JwtAuthenticationFilter.java
└── JwtService.java
```

La configuration principale est :

``` text
config/SecurityConfig.java
```

Lors d'une modification de l'authentification ou des autorisations,
vérifier au minimum :

-   génération et validation des JWT ;
-   extraction de l'utilisateur ;
-   routes publiques/protégées ;
-   filtre JWT ;
-   configuration CORS ;
-   hash des mots de passe ;
-   tests de sécurité.

> 🔒 Une modification de `SecurityConfig`, `JwtService` ou
> `JwtAuthenticationFilter` doit être accompagnée d'une exécution
> complète des tests de sécurité.

------------------------------------------------------------------------

## 10. Gestion des erreurs

Les exceptions sont centralisées dans :

``` text
backend/src/main/java/fr/datashare/backend/exception/
```

On y trouve notamment les erreurs liées :

-   aux liens expirés ou inexistants ;
-   aux emails déjà utilisés ;
-   aux fichiers introuvables ;
-   aux uploads invalides ;
-   aux identifiants invalides ;
-   aux mots de passe de fichiers incorrects ;
-   à la validation.

`GlobalExceptionHandler` centralise la transformation des exceptions en
réponses HTTP.

Lorsqu'une nouvelle erreur métier est nécessaire :

``` text
Nouvelle exception
      ↓
Service
      ↓
GlobalExceptionHandler
      ↓
Réponse HTTP cohérente
```

------------------------------------------------------------------------

## 11. Architecture du frontend

Le frontend se trouve dans :

``` text
frontend/src/
```

Structure principale observée :

``` text
src/
├── app/
│   ├── core/
│   ├── features/
│   ├── models/
│   ├── app.config.ts
│   ├── app.routes.ts
│   └── app.ts
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
├── index.html
├── main.ts
└── styles.scss
```

### Organisation

  Dossier / fichier   Rôle
  ------------------- ------------------------------------------
  `core/`             Éléments transverses de l'application
  `features/`         Fonctionnalités et écrans métier
  `models/`           Modèles TypeScript
  `app.routes.ts`     Routes Angular
  `app.config.ts`     Configuration de l'application
  `environments/`     URLs et paramètres selon l'environnement
  `styles.scss`       Styles globaux

Lorsqu'un contrat backend change, vérifier les modèles et services
Angular correspondants.

------------------------------------------------------------------------

## 12. Tests à maintenir

### Backend

Tests actuels :

``` text
backend/src/test/java/fr/datashare/backend/
├── config/
├── controller/
├── security/
└── service/
```

Les tests couvrent les controllers, services métier, JWT, filtre
d'authentification et configuration de sécurité.

Commande :

``` bash
cd backend
mvn clean test
```

### Couverture

Le projet dispose d'une couverture JaCoCo de référence de :

> ✅ **72 % --- objectif indicatif ≥ 70 % atteint**

Rapport :

``` text
backend/target/site/jacoco/index.html
```

### End-to-End

``` text
frontend/cypress/e2e/
├── anonymous-upload.cy.ts
├── authenticated-upload.cy.ts
└── password-protected-file.cy.ts
```

Exécution :

``` bash
cd frontend
npx cypress run
```

> 🧪 Une évolution d'un parcours critique doit être accompagnée d'une
> mise à jour du scénario Cypress correspondant.

------------------------------------------------------------------------

## 13. Performance

Le scénario k6 de référence est :

``` text
perf/files-history.js
```

Exécution :

``` bash
k6 run perf/files-history.js
```

Résultat de référence :

  Indicateur         Résultat
  --------------- -----------
  Requêtes HTTP           560
  Échecs HTTP             0 %
  Temps moyen        48,74 ms
  p95               117,52 ms
  Maximum           146,67 ms

> Budget backend de référence : **p95 \< 500 ms**

Après une évolution importante du backend, le test peut être rejoué pour
détecter une régression.

------------------------------------------------------------------------

## 14. Gestion des dépendances

### Backend

Les dépendances Java sont déclarées dans :

``` text
backend/pom.xml
```

Après modification :

``` bash
mvn clean test
```

### Frontend

Les dépendances sont définies dans :

``` text
frontend/package.json
frontend/package-lock.json
```

Installation reproductible :

``` bash
npm ci
```

Audit :

``` bash
npm audit
```

> Éviter `npm audit fix --force` sans analyser les changements
> majeurs qu'il peut provoquer.

------------------------------------------------------------------------

## ➕ 15. Ajouter une fonctionnalité

Pour une nouvelle fonctionnalité backend, suivre autant que possible ce
parcours :

``` text
1. Définir / modifier le DTO
        ↓
2. Ajouter la règle métier dans un service
        ↓
3. Ajouter ou adapter le repository si nécessaire
        ↓
4. Exposer l'opération dans le controller
        ↓
5. Gérer les nouvelles erreurs
        ↓
6. Vérifier la sécurité
        ↓
7. Ajouter les tests unitaires / controller
        ↓
8. Adapter Angular
        ↓
9. Ajouter / adapter Cypress si parcours critique
        ↓
10. Relancer la suite complète
```

------------------------------------------------------------------------

## 16. Modifier une fonctionnalité existante

Avant toute modification :

-   identifier le controller concerné ;
-   identifier le service métier ;
-   identifier les DTO ;
-   identifier les entités/repositories concernés ;
-   rechercher les tests existants.

Après modification :

``` bash
cd backend
mvn clean test
```

Puis, si le parcours utilisateur est concerné :

``` bash
cd frontend
npx cypress run
```

Et pour une modification susceptible d'affecter les performances :

``` bash
k6 run perf/files-history.js
```

------------------------------------------------------------------------

## 17. Checklist de maintenance

### Avant modification

-   [ ] Identifier les composants concernés
-   [ ] Vérifier les tests existants
-   [ ] Vérifier le contrat frontend/backend
-   [ ] Identifier les impacts sécurité

### Après modification

-   [ ] Compiler le projet
-   [ ] Exécuter les tests backend
-   [ ] Vérifier JaCoCo
-   [ ] Exécuter Cypress si nécessaire
-   [ ] Rejouer k6 si nécessaire
-   [ ] Vérifier `npm audit` lors d'une mise à jour frontend
-   [ ] Mettre à jour la documentation
-   [ ] Vérifier le diff Git avant commit

------------------------------------------------------------------------

## 18. Hygiène du dépôt

Ne pas versionner inutilement :

-   `node_modules/` ;
-   `backend/target/` ;
-   builds frontend ;
-   fichiers temporaires ;
-   téléchargements générés par les tests ;
-   métadonnées Windows telles que `:Zone.Identifier` ;
-   secrets, mots de passe ou tokens ;
-   fichiers utilisateur de test non nécessaires.

Les règles correspondantes doivent être présentes dans `.gitignore`.

------------------------------------------------------------------------

## 19. Documentation associée

La maintenance doit être réalisée en cohérence avec :

``` text
docs/
├── TESTING.md
├── PERF.md
└── SECURITY.md
```

Les schémas et contrats techniques sont conservés dans :

``` text
Schemas et Contrat d'interface/
```

Lorsqu'une évolution modifie l'architecture, les flux ou le modèle de
données, les schémas concernés doivent également être mis à jour.

------------------------------------------------------------------------

## 20. Bilan

L'architecture de DataShare sépare clairement :

``` text
Frontend Angular
       ↓
Controllers Spring Boot
       ↓
Services métier
       ↓
Repositories / Storage
       ↓
Persistance
```

Cette organisation permet de faire évoluer les fonctionnalités tout en
conservant une séparation entre présentation, API, logique métier,
sécurité et persistance.

> **Règle de maintenance principale :** toute évolution fonctionnelle
> doit rester accompagnée de tests adaptés et ne doit pas dégrader les
> références actuelles de sécurité, de couverture et de performance.
