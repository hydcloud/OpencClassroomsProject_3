# Performance — DataShare

> **Objectif :** vérifier rapidement le comportement d'un endpoint critique sous charge légère et disposer d'une référence de non-régression.

---

## 1. Objectif

Le test de performance valide le comportement du backend DataShare sous une charge légère et définit un budget de performance reproductible.

---

## 2. Outil utilisé

Le test de charge est réalisé avec **k6**.

Métriques suivies :

- temps de réponse HTTP ;
- taux d'échec ;
- percentiles `p90` et `p95` ;
- débit de requêtes ;
- volumes de données échangés.

---

## 3. Endpoint testé

```text
GET /api/files
```

Pour chaque itération :

```text
POST /api/login
        ↓
Récupération du JWT
        ↓
GET /api/files
        ↓
HTTP 200
        ↓
Temps de réponse < 500 ms
```

---

## 4. Configuration du test

| Paramètre | Valeur |
|---|---:|
| Utilisateurs virtuels | **10 VUs** |
| Durée | **30 s** |
| Itérations | **280** |
| Requêtes HTTP | **560** |
| Seuil | **< 500 ms** |

**Script :**

```text
perf/files-history.js
```

**Commande :**

```bash
k6 run perf/files-history.js
```

Pour conserver les résultats :

```bash
k6 run perf/files-history.js | tee perf/k6-results.txt
```

---

## 5. Résultats k6

### Checks

```text
checks_total.......: 840
checks_succeeded...: 100.00% (840 / 840)
checks_failed......: 0.00% (0 / 840)
```

```text
✓ login status is 200
✓ history status is 200
✓ response time < 500 ms
```

> **840 / 840 checks réussis**

### Requêtes HTTP

| Indicateur | Résultat |
|---|---:|
| Requêtes | **560** |
| Échecs HTTP | **0 %** |
| Débit | **18,20 req/s** |

### Temps de réponse

| Métrique | Résultat |
|---|---:|
| Moyenne | **48,74 ms** |
| Minimum | **2 ms** |
| Médiane | **39,62 ms** |
| p90 | **111,14 ms** |
| p95 | **117,52 ms** |
| Maximum | **146,67 ms** |

### Réseau

| Indicateur | Résultat |
|---|---:|
| Données reçues | **343 kB** |
| Réception | **11 kB/s** |
| Données envoyées | **135 kB** |
| Envoi | **4,4 kB/s** |

---

## 6. Interprétation

Les **560 requêtes HTTP ont été traitées sans échec** et les **840 checks ont réussi**.

Le temps moyen est de **48,74 ms**, le `p95` de **117,52 ms** et le maximum de **146,67 ms**.

Ces valeurs restent largement sous le seuil retenu :

> **Budget : < 500 ms**  
> **Résultat : PASS**

Le test correspond à une charge légère de **10 utilisateurs virtuels pendant 30 secondes**. Il constitue une référence de performance du MVP, et non un test de capacité maximale de production.

---

## 7. Budget de performance

### Backend

| Indicateur | Budget | Résultat |
|---|---:|---:|
| Temps de réponse | < 500 ms | ✅ |
| p95 | < 500 ms | ✅ 117,52 ms |
| Échecs HTTP | 0 % | ✅ 0 % |

### Frontend

| Indicateur | Objectif |
|---|---:|
| Réponse API nominale | < 500 ms |
| Erreurs réseau nominales | 0 % |
| Bundles / ressources | Surveiller leur évolution |
| Fichiers transférés | Respecter les limites de l'application |

---

## 8. Taille des fichiers et transferts

Les métriques importantes à surveiller sont :

- taille des fichiers ;
- durée d'upload ;
- durée de téléchargement ;
- volume transféré ;
- utilisation mémoire ;
- comportement lors de transferts simultanés.

> Le scénario actuel teste principalement l'authentification et l'historique. Il ne constitue pas un benchmark d'upload de fichiers volumineux.

---

## 9. Reproductibilité

1. Démarrer le backend DataShare.
2. Vérifier que l'utilisateur du scénario existe.
3. Lancer :

```bash
k6 run perf/files-history.js
```

Le résultat de référence est conservé dans :

```text
perf/k6-results.txt
```

---

## 10. Preuves de performance

```text
perf/
├── files-history.js
└── k6-results.txt
```

Une capture peut être placée dans :

```text
docs/performance/k6-results.png
```

Puis affichée avec :

```markdown
![Résultats du test k6](docs/performance/k6-results.png)
```

---

## 11. Non-régression

Le test doit être rejoué après une modification importante concernant :

- l'authentification ;
- les JWT ;
- les accès à la base ;
- l'historique ;
- les repositories ;
- les services backend ;
- la configuration de sécurité.

### Référence actuelle

```text
HTTP requests        : 560
HTTP failures        : 0 %
Average response time: 48,74 ms
p95                  : 117,52 ms
Maximum              : 146,67 ms
Checks               : 100 % réussis
```

---

## 12. Bilan

| Indicateur | Résultat |
|---|---:|
| Utilisateurs virtuels | **10** |
| Durée | **30 s** |
| Requêtes HTTP | **560** |
| Débit | **18,20 req/s** |
| Checks réussis | ✅ **100 %** |
| Échecs HTTP | ✅ **0 %** |
| Temps moyen | **48,74 ms** |
| Médiane | **39,62 ms** |
| p95 | **117,52 ms** |
| Maximum | **146,67 ms** |
| Seuil < 500 ms | ✅ **Respecté** |

> **Conclusion :** le scénario respecte le budget de performance défini pour le MVP. `perf/k6-results.txt` constitue la référence pour les comparaisons futures.
