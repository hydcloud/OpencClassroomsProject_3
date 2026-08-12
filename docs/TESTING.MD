Plan de tests --- DataShare



1. Objectif

Ce document décrit la stratégie de tests mise en place pour l'application DataShare.

Les tests ont pour objectifs de vérifier :

le bon fonctionnement des fonctionnalités du MVP ;

le comportement des fonctionnalités avancées implémentées :

US07 --- Upload anonyme ;

US09 --- Protection des fichiers par mot de passe ;

la validation des données ;

la gestion des erreurs ;

la sécurité des opérations sensibles ;

les principaux parcours utilisateur côté frontend et backend.



2. Technologies utilisées



Backend

Java

Spring Boot

JUnit 5

Mockito

MockMvc

Maven

JaCoCo

Frontend / End-to-End

Angular

Cypress



3. Tests backend

Les tests backend sont exécutés avec Maven.

Depuis le dossier backend :

mvn clean test



Résultat final



La suite de tests backend contient :

Tests : 50
Failures : 0
Errors : 0

Les tests couvrent notamment :

l'inscription ;

l'authentification ;

l'upload de fichiers ;

l'upload anonyme ;

le téléchargement ;

la protection par mot de passe ;

l'historique des fichiers ;

la suppression des fichiers ;

la génération et la validation des liens de téléchargement ;

la génération et la validation des JWT ;

le filtre d'authentification JWT ;

le chargement des utilisateurs Spring Security ;

la configuration du PasswordEncoder ;

la configuration CORS.



4. Tests des principales fonctionnalités

4.1 Création de compte



Les tests vérifient notamment :

la création d'un utilisateur valide ;

la validation des données saisies ;

le comportement de l'API lorsqu'une requête est invalide ;

les réponses HTTP retournées par le contrôleur.



4.2 Connexion



Les tests vérifient :

l'authentification avec un email et un mot de passe valides ;

le retour d'un token JWT ;

le retour de l'adresse email de l'utilisateur ;

le rejet d'identifiants incorrects ;

la validation des JWT.



4.3 Upload de fichier



Les tests de UploadService couvrent notamment :

l'upload d'un fichier valide ;

l'upload par un utilisateur authentifié ;

l'association du fichier à son propriétaire ;

l'upload anonyme ;

la génération du lien de téléchargement ;

la durée d'expiration ;

la protection facultative par mot de passe ;

le hash du mot de passe ;

le rejet d'un fichier vide ;

le rejet d'une extension interdite ;

le rejet d'une durée d'expiration invalide ;

le rejet d'un mot de passe trop court ;

le comportement lorsqu'un utilisateur authentifié est introuvable ;

la suppression du fichier physique lorsqu'une erreur survient pendant son enregistrement en base de données.

4.4 Téléchargement

Les tests vérifient :

le téléchargement d'un fichier non protégé ;

le téléchargement avec un mot de passe correct ;

le refus lorsqu'aucun mot de passe n'est fourni pour un fichier protégé ;

le refus lorsqu'un mot de passe incorrect est fourni.

4.5 Historique

Les tests vérifient :

la récupération des fichiers appartenant à l'utilisateur ;

la transformation des fichiers en DTO ;

le token de téléchargement ;

l'indication passwordProtected ;

le retour d'une liste vide lorsqu'aucun fichier n'est disponible ;

le comportement lorsqu'un utilisateur est introuvable.

Le hash du mot de passe n'est jamais exposé dans la réponse destinée au frontend.

4.6 Suppression

Les tests vérifient :

la suppression d'un fichier appartenant à l'utilisateur ;

la suppression du fichier physique ;

la suppression des données associées ;

le refus lorsque l'utilisateur est introuvable ;

le refus lorsque le fichier est introuvable ;

le refus lorsqu'un utilisateur tente de supprimer le fichier d'un autre utilisateur.

4.7 Liens de téléchargement

Les tests vérifient :

la création d'un lien ;

la génération d'un token ;

la récupération d'un lien valide ;

le rejet d'un token inexistant ;

le rejet d'un lien expiré.

5. Tests de sécurité

Les composants de sécurité font également l'objet de tests unitaires.

JWT

Les tests vérifient :

la génération d'un token ;

l'extraction de l'adresse email ;

la validation du token pour l'utilisateur attendu ;

le rejet d'un token utilisé pour un autre utilisateur ;

le comportement d'un token expiré.

Chargement utilisateur

Les tests de CustomUserDetailsService vérifient :

le chargement d'un utilisateur existant ;

la récupération de ses informations Spring Security ;

le comportement lorsqu'un utilisateur n'existe pas.

Filtre JWT

Les tests de JwtAuthenticationFilter couvrent notamment :

une requête sans header Authorization ;

un header qui n'utilise pas le schéma Bearer ;

un token valide ;

un token invalide ;

un token JWT mal formé ;

un utilisateur déjà authentifié.

Configuration

Les tests vérifient également :

l'utilisation de BCrypt pour le hash des mots de passe ;

la validation d'un mot de passe avec le hash généré ;

la configuration CORS ;

l'autorisation du frontend http://localhost:4200 ;

les méthodes HTTP autorisées ;

les headers Authorization et Content-Type.

6. Fonctionnalités avancées

US07 --- Upload anonyme

Les tests couvrent :

l'upload sans authentification ;

la création du fichier sans propriétaire ;

la génération d'un lien unique ;

le téléchargement du fichier à partir du lien généré.

Critère d'acceptation :

Utilisateur non connecté
→ upload autorisé
→ lien unique généré
→ fichier téléchargeable

US09 --- Protection par mot de passe

Les tests couvrent :

l'upload avec mot de passe ;

le stockage du mot de passe sous forme hashée ;

le téléchargement avec le mot de passe correct ;

le refus avec un mot de passe incorrect ;

le refus lorsqu'aucun mot de passe n'est fourni ;

le téléchargement direct d'un fichier non protégé.

Critères d'acceptation :

Mot de passe correct
→ HTTP 200
→ téléchargement autorisé

Mot de passe incorrect
→ HTTP 403 Forbidden
→ téléchargement refusé
→ message "Mot de passe incorrect."

7. Tests End-to-End avec Cypress

Trois scénarios E2E critiques ont été implémentés et validés avec Cypress.

Scénario 1 --- Connexion et upload authentifié

Fichier :

cypress/e2e/authenticated-upload.cy.ts

Parcours testé :

Accueil
→ connexion
→ saisie des identifiants
→ authentification
→ retour sur l'accueil
→ upload d'un fichier
→ message de succès
→ fichier visible dans "Mes fichiers"

Résultat : PASS

Scénario 2 --- Upload anonyme et téléchargement

Fichier :

cypress/e2e/anonymous-upload.cy.ts

Parcours testé :

Utilisateur non connecté
→ sélection d'un fichier
→ upload anonyme
→ HTTP 201
→ génération du lien
→ clic sur Télécharger
→ HTTP 200

Résultat : PASS

Scénario 3 --- Protection par mot de passe

Fichier :

cypress/e2e/password-protected-file.cy.ts

Parcours testé :

Upload anonyme avec mot de passe
→ génération du lien
→ mauvais mot de passe
→ HTTP 403
→ message "Mot de passe incorrect."
→ saisie du bon mot de passe
→ téléchargement
→ HTTP 200

Résultat : PASS

8. Exécution des tests E2E

Le backend et le frontend doivent être démarrés avant l'exécution des tests.

Backend

cd backend
mvn spring-boot:run

Frontend

cd frontend
npm start

Cypress en mode interactif

Depuis frontend :

npx cypress open

Cypress en ligne de commande

npx cypress run

9. Couverture de code

La couverture backend est mesurée avec JaCoCo.

Le rapport est généré lors de l'exécution des tests Maven et peut être consulté dans :

backend/target/site/jacoco/index.html

Résultat final

Couverture JaCoCo : 72 %
Objectif indicatif : >= 70 %
Statut : ATTEINT

La couverture a été améliorée en ajoutant des tests sur les composants métier et de sécurité réellement utilisés par l'application.

Les tests supplémentaires concernent notamment :

DownloadService ;

UploadService ;

FileHistoryService ;

FileDeletionService ;

DownloadLinkService ;

JwtService ;

CustomUserDetailsService ;

JwtAuthenticationFilter ;

SecurityConfig.

Rapport JaCoCo



10. Stratégie de non-régression

Avant une livraison importante, la suite backend doit être exécutée avec:

cd backend
mvn clean test

Le résultat attendu est :

Failures: 0
Errors: 0
BUILD SUCCESS

Les trois scénarios E2E Cypress doivent également être exécutés afin de vérifier les principaux parcours utilisateur.

Une attention particulière doit être portée aux tests après toute modification concernant :

l'authentification ;

les JWT ;

l'upload ;

le téléchargement ;

la protection par mot de passe ;

les routes Angular ;

la configuration Spring Security ;

les contrats entre le frontend et le backend.

11. Bilan

La stratégie de tests de DataShare combine :

des tests unitaires backend ;

des tests de contrôleurs ;

des tests des services métier ;

des tests des mécanismes de sécurité ;

des tests End-to-End Cypress ;

une mesure de couverture JaCoCo.

Résultats finaux :

Indicateur                           Résultat

Tests backend                              50Échecs backend                              0Erreurs backend                             0Scénarios E2E critiques                     3Couverture backend JaCoCo                72 %Objectif indicatif de couverture     >= 70 %Objectif de couverture atteint            Oui

Les parcours critiques du MVP ainsi que les fonctionnalités avancéesUS07 et US09 sont couverts par la stratégie de tests.

