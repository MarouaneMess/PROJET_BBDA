# MiniSGBDR - Projet BDDA (Bases de Données Avancées)

Un système de gestion de base de données relationnelles simplifié développé dans le cadre du cours de Bases de Données Avancées. Ce dépôt couvre les TPs jusqu'à la mise en place du Disk Manager (TP2) et du Buffer Manager (TP3) en plus des bases (TP1).

## Description

Ce projet implémente un **MiniSGBDR** (Mini Système de Gestion de Bases de Données Relationnelles) avec les caractéristiques suivantes :
- Mono-utilisateur
- Pas de gestion de la concurrence
- Pas de transactions
- Pas de droits d'accès
- Pas de crash recovery
- Interface console simple
- Commandes similaires à SQL mais simplifiées

## Structure du projet (à jour)

```
PROJET_BBDA/
├── README.md               # Documentation du projet
├── src/                    # Code source (package par défaut)
│   ├── DBConfig.java       # Classe de configuration (TP1/TP3)
│   ├── DiskManager.java    # Gestionnaire disque + persistance .meta (TP2)
│   ├── BufferManager.java  # Gestionnaire de buffers LRU/MRU (TP3)
│   ├── MiniSGBDR.java      # Application principale (console)    
│   ├── PageId.java         # Identifiant de page (fileIdx,pageIdx)
│   ├── Relation.java       # Gestion d'une relation / schéma (TP4)
│   ├── Record.java         # Représentation d'un tuple (TP4)
│   ├── ColumnInfo.java     # Métadonnées d'une colonne (TP4)
│   └── ColumnType.java     # Enum des types (INT/FLOAT/CHAR/VARCHAR) (TP4)
├── tests/                  # Tests (exécutables via AllTests)
│   ├── DBConfigTest.java
│   ├── DiskManagerTests.java
│   ├── BufferManagerTests.java
│   └── AllTests.java       # Agrégateur (lance tous les tests)
├── config/                 # Fichiers de configuration d'exemple
│   ├── config.txt          # Format simple (clé = valeur)
│   └── config.properties   # Format Properties
├── scripts/                # Scripts Windows (.bat) et Unix (.sh)
│   ├── compile.bat / compile.sh
│   ├── run.bat     / run.sh
│   ├── run_all.bat / run_all.sh
│   ├── test_all_tests_folder.bat / test_all_tests_folder.sh
│   └── test_dbconfig.bat   # Exécution ciblée des tests DBConfig (Windows)
├── docs/                   # Documentation additionnelle
│   ├── ARCHITECTURE.md     # Vue d'ensemble TP1→TP3
│   └── QUICKSTART.md       # Démarrage rapide (scripts & tests)
├── BinData/                # Fichiers de données (créé à l'exécution)
└── build/                  # Classes compilées (généré)
```

## Installation et Compilation

### Prérequis
- Java JDK 8 ou supérieur
- Un terminal/invite de commande

### Compilation

**Windows :**
```cmd
scripts\compile.bat
```

**Unix/Linux/Mac :**
```bash
chmod +x scripts/compile.sh
scripts/compile.sh
```

## Utilisation

### Lancement de l'application

**Windows :**
```cmd
scripts\run.bat [fichier_config]
```

**Unix/Linux/Mac :**
```bash
chmod +x scripts/run.sh
scripts/run.sh [fichier_config]
```

**Exemples :**
```cmd
# Avec configuration par défaut
scripts\run.bat

# Avec fichier de configuration personnalisé
scripts\run.bat config\config.properties
```

### Interface Console

Une fois lancé, le MiniSGBDR présente une interface en ligne de commande :

```
MiniSGBDR> HELP
```

### Commandes disponibles

| Commande | Description |
|----------|-------------|
| `EXIT` | Quitter l'application |
| `HELP` | Afficher l'aide |
| `STATUS` | Afficher le statut du système |
| `CONFIG` | Afficher la configuration actuelle |
| `DEBUG INFO` | Afficher les informations de débogage |

**Commandes à venir dans les prochains TPs :**
- `CREATE TABLE ...` - Créer une table
- `INSERT INTO ...` - Insérer des données
- `SELECT ...` - Sélectionner des données
- `DROP TABLE ...` - Supprimer une table

## Configuration

### Format des fichiers de configuration

**Format simple (config.txt) :**
```
# Commentaire
dbpath = ./DB
```

**Format Properties (config.properties) :**
```properties
dbpath=./DB
pagesize=4096
dm_maxfilecount=4
bm_buffercount=2
bm_policy=LRU   # LRU ou MRU
```

### Classe DBConfig (champs)

La classe `DBConfig` gère la configuration du système :

- `dbpath` (String) : chemin racine des données (ex. `./DB`)
- `pagesize` (int) : taille des pages en octets (ex. 4096)
- `dm_maxfilecount` (int) : nombre max de fichiers Data{i}.bin
- `bm_buffercount` (int, TP3) : nombre de frames en mémoire (défaut 2)
- `bm_policy` (String, TP3) : politique de remplacement (`LRU` par défaut, ou `MRU`)

**Constructeurs / chargement :**
```java
DBConfig config = new DBConfig("./DB");
DBConfig config = new DBConfig("./DB", 4096, 4, 2, "LRU");
```

**Chargement depuis fichier :**
```java
// Format Properties
DBConfig config = DBConfig.LoadDBConfig("config.properties");

// Format simple
DBConfig config = DBConfig.LoadDBConfigSimple("config.txt");
```

## Tests

### Exécution des tests

**Windows :**
```cmd
scripts\compile.bat
scripts\run_all.bat
```

**Unix/Linux/Mac :**
```bash
chmod +x scripts/*.sh
scripts/run_all.sh
```

### Tests inclus

- `DBConfigTest` : construction, parsing fichiers (simple/Properties), erreurs, utilitaires, paramètres TP3 (`bm_*`)
- `DiskManagerTests` : allocation/écriture/lecture, persistance via `.meta`, croissance/limites
- `BufferManagerTests` : get/free/flush, remplacement `LRU`/`MRU`, changement de politique
- `AllTests` : lance les trois suites et renvoie un code de retour agrégé

Astuce: les scripts d’exécution font un `pushd` vers la racine du projet avant `java ...` afin que tous les chemins relatifs (ex. `./BinData`) soient créés au niveau 0 du dépôt, pas dans `scripts/`.

## Documentation

- Architecture détaillée: `docs/ARCHITECTURE.md`
- Démarrage rapide (compilation/tests): `docs/QUICKSTART.md`

## Développement

### Ajout de nouvelles fonctionnalités

1. **Nouvelle classe :** Ajouter dans `src/`
2. **Tests :** Ajouter dans `tests/`
3. **Configuration :** Étendre `DBConfig` si nécessaire
4. **Compilation :** Les scripts se chargent automatiquement des nouveaux fichiers

### Architecture (résumé)

Le projet suit une architecture modulaire :
- **DBConfig** : Gestion de la configuration
- **DiskManager (TP2)** : Fichiers `BinData/Data{i}.bin` + `Data{i}.meta` (bitmap persistant), `PageId(fileIdx,pageIdx)`, `Alloc/Read/Write/Dealloc`, `Init/Finish`
- **BufferManager (TP3)** : Pool de frames (pinCount/dirty/lastTouch), remplacement `LRU`/`MRU`, `FlushBuffers`, `GetPage/FreePage`
- **MiniSGBDR** : Application principale (console)
- **Tests** : Validation du code (agrégées via `AllTests`)

### Conventions de codage

- Classes en PascalCase
- Méthodes en camelCase
- Documentation Javadoc complète
- Gestion d'erreurs appropriée

## Évolution prévue

Ce projet évoluera au fil des TPs pour inclure :

### TP2 - Couche de stockage
- Gestion des fichiers de données (`DiskManager`) et persistance de l’état via `.meta`
- Structure des pages identifiées par `PageId`

### TP3 - Buffer Manager
- Gestion d’un pool de buffers en mémoire avec politiques `LRU` et `MRU`
- Écriture différée via `FlushBuffers` et gestion des pages « dirty »

### TP4 - Gestion des relations et des records (nouveau)
- Introduit les classes `Relation`, `Record`, `ColumnInfo` et `ColumnType`.
- La `Relation` contient le schéma (nom, colonnes, types, tailles) et fournit
	des méthodes pour sérialiser/désérialiser un `Record` dans un buffer.
- Les types supportés : `INT` (4 octets), `FLOAT` (4 octets), `CHAR(T)` (T octets
	fixes), `VARCHAR(T)` (taille variable, encodée selon la stratégie discutée en CM
	; stockée sur une taille fixe au niveau du record en respectant la taille maximale T).

Stockage des records :
- Format à taille fixe par relation. Chaque colonne occupe un nombre fixe d'octets
	déterminé par son type et (pour CHAR/VARCHAR) par la taille T.
- `writeRecordToBuffer(record, buff, pos)` : sérialise un `Record` dans le
	`ByteBuffer` `buff` à la position `pos` (utilisez les méthodes `putInt`,
	`putFloat`, `put`, et manipulez la position du buffer si nécessaire).
- `readRecordFromBuffer(record, buff, pos)` : lit depuis `buff` à `pos` et
	remplit la liste de valeurs du `Record` (opération inverse de l'écriture).

Tests TP4 :
- Vérifier qu'un `Record` écrit puis relu conserve exactement les valeurs
	(notamment chaînes CHAR/VARCHAR, et conversion int/float depuis chaînes si
	nécessaire).

## Problèmes connus

- Aucun problème connu à ce stade

## Auteurs

Projet développé dans le cadre du cours BDDA (Bases de Données Avancées)

## Licence

Ce projet est développé à des fins éducatives.