# 🔐 Sécurité — DataShare

> **Objectif :** documenter les mécanismes de sécurité de DataShare, les scans de dépendances réalisés et les décisions prises à partir des résultats obtenus.

---

## 1. Périmètre

La sécurité de DataShare est suivie sur deux axes :

| Périmètre | Outil / mécanisme |
|---|---|
| Frontend Angular | `npm audit` |
| Backend Spring Boot | OWASP Dependency-Check |
| Authentification | JWT |
| Mots de passe utilisateurs | BCrypt |
| Protection des fichiers | Mot de passe hashé |
| Autorisations HTTP | Spring Security |
| Validation | Contrôles côté client et serveur |

---

## 2. Mesures de sécurité applicatives

### Authentification

DataShare utilise une authentification basée sur :

```text
Email + mot de passe
        ↓
Vérification du hash
        ↓
Génération d'un JWT
        ↓
JWT transmis par le frontend
        ↓
JwtAuthenticationFilter
        ↓
Accès aux routes protégées
```

Les routes privées nécessitent une authentification valide.

### Stockage des mots de passe

Les mots de passe utilisateurs sont hashés avec **BCrypt**.

> Un mot de passe utilisateur n'est jamais stocké en clair.

### Protection des fichiers — US09

Un fichier peut également être protégé par un mot de passe.

Le mot de passe du fichier :

- doit comporter au minimum 6 caractères lorsqu'il est renseigné ;
- est hashé avant stockage ;
- n'est pas retourné par les DTO ;
- doit être validé avant le téléchargement.

```text
Mauvais mot de passe
→ HTTP 403 Forbidden
→ téléchargement refusé
```

### JWT

La sécurité JWT est assurée notamment par :

```text
JwtService
JwtAuthenticationFilter
CustomUserDetailsService
SecurityConfig
```

Les tests automatisés couvrent la génération, l'extraction, la validation du token, les tokens invalides ou expirés et le comportement du filtre.

---

## 3. Configuration HTTP et CORS

La configuration Spring Security applique notamment :

- une politique de session **stateless** ;
- la désactivation du formulaire de login Spring par défaut ;
- l'authentification JWT ;
- des routes publiques explicitement définies ;
- une réponse HTTP `401 Unauthorized` lorsqu'une authentification est requise ;
- une politique CORS limitée au frontend local utilisé pour le projet.

Origine autorisée :

```text
http://localhost:4200
```

Headers autorisés :

```text
Authorization
Content-Type
```

---

# 4. Scan de sécurité frontend

## Scan initial

Commande :

```bash
cd frontend
npm audit
```

Résultat initial :

| Sévérité | Nombre |
|---|---:|
| Low | 1 |
| Moderate | 6 |
| High | 5 |
| **Total** | **12** |

> Le scan initial contenait **5 vulnérabilités de niveau High**.

---

## Correction appliquée

Commande :

```bash
npm audit fix
```

Un nouveau scan a ensuite été exécuté :

```bash
npm audit
```

### Résultat après correction

| Sévérité | Nombre |
|---|---:|
| Low | 1 |
| Moderate | 3 |
| High | **0** |
| **Total** | **4** |

> Les vulnérabilités **High ont été supprimées**.

Les vulnérabilités restantes sont liées à `esbuild` via la chaîne de dépendances de `vite`.

---

## Décision frontend

La commande suivante n'a volontairement pas été utilisée :

```bash
npm audit fix --force
```

### Justification

`--force` peut provoquer :

- des montées de versions majeures ;
- des incompatibilités avec Angular ou Vite ;
- des changements cassants ;
- des régressions fonctionnelles en fin de projet.

La décision retenue est donc :

> corriger les vulnérabilités pouvant l'être sans rupture ;  
> supprimer les vulnérabilités High ;  
> conserver temporairement les 4 vulnérabilités restantes de niveau Low/Moderate ;  
> les réévaluer lors d'une prochaine mise à jour des dépendances.

---

# 5. Scan de sécurité backend

Le backend a été analysé avec **OWASP Dependency-Check 12.2.x**.

Commande utilisée :

```bash
mvn -U org.owasp:dependency-check-maven:12.2.2:check
```

Rapport :

```text
backend/target/dependency-check-report.html
```

---

## Résumé OWASP

| Indicateur | Résultat |
|---|---:|
| Dépendances scannées | **94** |
| Dépendances uniques | **52** |
| Dépendances signalées vulnérables | **5** |
| Vulnérabilités détectées | **13** |
| Vulnérabilités supprimées du rapport | **0** |

> Dependency-Check réalise une analyse par correspondance de dépendances/CPE. Le rapport rappelle lui-même que des **faux positifs et faux négatifs peuvent exister**.

---

## 6. Dépendances backend signalées

OWASP signale cinq dépendances :

| Dépendance | Version | Sévérité max. indiquée | CVE |
|---|---:|---:|---:|
| `jackson-databind` | 2.21.4 | Medium | 1 |
| `log4j-api` | 2.25.4 | Medium | 1 |
| `postgresql` | 42.7.11 | High | 1 |
| `spring-boot-devtools` | 4.1.0 | Critical | 1 |
| `tomcat-embed-core` | 11.0.22 | Critical | 9 |

Ces résultats doivent être **analysés**, et non traités automatiquement uniquement selon le niveau affiché.

---

# 7. Analyse des résultats backend

## Apache Tomcat Embedded

Dépendance :

```text
tomcat-embed-core-11.0.22
```

Le rapport signale plusieurs CVE, dont :

```text
CVE-2026-55956
```

Cette vulnérabilité concerne un problème d'autorisation dans Apache Tomcat.

Le rapport indique que la branche Tomcat `11.0.22` est concernée et recommande une mise à jour vers :

```text
11.0.23 ou version corrigée ultérieure
```

### Décision

> **À traiter en priorité lors de la prochaine mise à jour du backend.**

La mise à jour doit idéalement être effectuée via la version de Spring Boot qui gère la dépendance Tomcat, afin de conserver un ensemble de dépendances cohérent.

Après mise à jour :

```bash
mvn clean test
mvn org.owasp:dependency-check-maven:12.2.2:check
```

---

## Spring Boot DevTools

Dépendance signalée :

```text
spring-boot-devtools-4.1.0
```

OWASP l'associe notamment à :

```text
CVE-2022-31691
CVSS 9.8 — Critical
```

Cependant, la description de la CVE dans le rapport concerne **Spring Tools 4 / extensions d'IDE** utilisant SnakeYAML, et non directement le composant d'exécution `spring-boot-devtools` de l'application.

### Décision

> **Détection à considérer comme potentiellement fausse positive / mauvaise correspondance CPE.**

La dépendance `spring-boot-devtools` reste un outil destiné au développement et ne doit pas faire partie d'un déploiement de production.

Il est recommandé de :

- conserver `devtools` uniquement pour le développement ;
- ne pas l'intégrer à une image ou un artefact de production ;
- réévaluer cette alerte à chaque nouveau scan.

---

## PostgreSQL JDBC

Dépendance signalée :

```text
postgresql-42.7.11
```

Le rapport la classe **High**, mais avec une confiance d'identification faible dans son tableau de synthèse.

### Décision

> **À surveiller et à réévaluer lors de la prochaine mise à jour du driver PostgreSQL.**

Une mise à jour du driver doit être testée avec :

```bash
mvn clean test
```

et suivie d'un nouveau scan OWASP.

---

## Jackson Databind

Dépendance :

```text
jackson-databind-2.21.4
```

Niveau maximal signalé :

```text
Medium
```

### Décision

> **Risque modéré à surveiller.**

Les versions gérées transitivement par Spring Boot doivent être privilégiées afin d'éviter des incompatibilités entre modules Jackson.

---

## Log4j API

Dépendance :

```text
log4j-api-2.25.4
```

Niveau maximal signalé :

```text
Medium
```

### Décision

> **Risque modéré à suivre lors des mises à jour de dépendances.**

Aucune suppression ou forçage de version isolée n'est appliqué sans vérification de compatibilité avec le reste de la stack.

---

# 8. Politique de traitement des vulnérabilités

La stratégie retenue est :

| Sévérité | Action |
|---|---|
| Critical | Analyse immédiate et mise à jour prioritaire si applicable |
| High | Correction prioritaire après validation de l'impact |
| Moderate | Surveillance et correction lors d'une mise à jour compatible |
| Low | Surveillance |
| Faux positif probable | Documenter la décision et réévaluer au scan suivant |

> Une CVE ne doit pas être supprimée du rapport uniquement pour obtenir un résultat « vert ».

---

# 9. Validation après mise à jour

Après toute évolution de dépendance backend :

```bash
cd backend
mvn clean test
```

Le projet dispose actuellement d'une couverture JaCoCo de référence :

```text
72 %
```

Les scénarios Cypress critiques doivent également être rejoués lorsque la mise à jour peut affecter les parcours frontend/backend :

```bash
cd frontend
npx cypress run
```

---

# 10. Procédure de scan

## Frontend

```bash
cd frontend
npm audit
```

Correction sans montée majeure automatique :

```bash
npm audit fix
```

Puis :

```bash
npm audit
```

## Backend

```bash
cd backend
mvn org.owasp:dependency-check-maven:12.2.2:check
```

Rapport :

```text
target/dependency-check-report.html
```

---

# 11. Conservation des preuves

Organisation recommandée :

```text
docs/
├── TESTING.md
├── SECURITY.md
├── PERF.md
├── MAINTENANCE.md
├── jacoco-coverage.png
└── dependency-check-report.html
```

Le rapport OWASP constitue la preuve du scan backend.

Le résultat `npm audit` peut également être conservé sous forme de capture ou de fichier texte si nécessaire.

---

# 12. Secrets et configuration

Aucun secret ne doit être versionné dans Git.

Sont concernés notamment :

- secrets JWT ;
- mots de passe PostgreSQL ;
- mots de passe utilisateurs ;
- tokens d'accès ;
- clés API ;
- futures clés NVD éventuelles.

Les secrets doivent être fournis par la configuration de l'environnement.

> **Ne jamais intégrer une clé NVD ou un secret JWT directement dans le repository.**

---

# 13. Bonnes pratiques de maintenance sécurité

Avant un commit important :

- [ ] Vérifier qu'aucun secret n'est présent dans le diff
- [ ] Exécuter les tests backend
- [ ] Vérifier les routes publiques et protégées
- [ ] Vérifier le hash des mots de passe
- [ ] Vérifier les contrats DTO
- [ ] Lancer `npm audit`
- [ ] Lancer OWASP Dependency-Check lors des mises à jour backend
- [ ] Réévaluer les CVE restantes
- [ ] Documenter les décisions de non-correction

---

# 14. Bilan

## Frontend

```text
Avant correction : 12 vulnérabilités
Après correction : 4 vulnérabilités
High restantes   : 0
```

> Les vulnérabilités High détectées par `npm audit` ont été supprimées sans utiliser de mise à jour forcée susceptible de casser le projet.

## Backend

```text
94 dépendances analysées
52 dépendances uniques
5 dépendances signalées vulnérables
13 vulnérabilités détectées
```

Les alertes backend ont été analysées selon leur criticité et leur pertinence.

La priorité de maintenance concerne notamment la mise à jour de **Tomcat Embedded**, tandis que certaines alertes, comme celle associée à `spring-boot-devtools`, nécessitent une interprétation en raison d'une correspondance CPE potentiellement erronée.

> **Conclusion :** DataShare dispose d'une démarche de sécurité reproductible combinant contrôles applicatifs, tests automatisés, audit npm, analyse OWASP et documentation des décisions.
