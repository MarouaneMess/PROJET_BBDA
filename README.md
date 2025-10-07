# MiniSGBDR - Projet BDDA (Bases de Données Avancées)

Un système de gestion de base de données relationnelles simplifié développé dans le cadre du cours de Bases de Données Avancées.

## Description

Ce projet implémente un **MiniSGBDR** (Mini Système de Gestion de Bases de Données Relationnelles) avec les caractéristiques suivantes :
- Mono-utilisateur
- Pas de gestion de la concurrence
- Pas de transactions
- Pas de droits d'accès
- Pas de crash recovery
- Interface console simple
- Commandes similaires à SQL mais simplifiées

## Structure du projet

```
PROJET_BBDA/
├── README.md              # Documentation du projet
├── src/                   # Code source et tests
│   ├── DBConfig.java      # Classe de configuration
│   ├── MiniSGBDR.java     # Application principale
│   └── DBConfigTest.java  # Tests pour DBConfig
├── config/                # Fichiers de configuration
│   ├── config.txt         # Configuration format simple
│   └── config.properties  # Configuration format Properties
├── scripts/               # Scripts de compilation et exécution
│   ├── compile.bat        # Compilation Windows
│   ├── compile.sh         # Compilation Unix/Linux
│   ├── run.bat            # Exécution Windows
│   ├── run.sh             # Exécution Unix/Linux
│   ├── test_dbconfig.bat  # Tests Windows
│   └── test_dbconfig.sh   # Tests Unix/Linux
├── docs/                  # Documentation additionnelle
├── tests/                 # (Réservé pour futurs tests séparés)
├── DB/                    # Dossier de données (par défaut)
└── build/                 # Classes compilées (généré)
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
```

### Classe DBConfig

La classe `DBConfig` gère la configuration du système :

**Constructeur :**
```java
DBConfig config = new DBConfig("./DB");
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
scripts\test_dbconfig.bat
```

**Unix/Linux/Mac :**
```bash
chmod +x scripts/test_dbconfig.sh
scripts/test_dbconfig.sh
```

### Tests inclus

La classe `DBConfigTest` teste :
- Construction d'instances en mémoire
- Chargement depuis fichiers valides
- Gestion des cas d'erreur
- Fichiers inexistants ou malformés
- Méthodes utilitaires (`toString`, `equals`)

## Développement

### Ajout de nouvelles fonctionnalités

1. **Nouvelle classe :** Ajouter dans `src/`
2. **Tests :** Ajouter dans `tests/`
3. **Configuration :** Étendre `DBConfig` si nécessaire
4. **Compilation :** Les scripts se chargent automatiquement des nouveaux fichiers

### Architecture

Le projet suit une architecture modulaire :
- **DBConfig** : Gestion de la configuration
- **MiniSGBDR** : Application principale et interface console
- **Tests** : Validation du code

### Conventions de codage

- Classes en PascalCase
- Méthodes en camelCase
- Documentation Javadoc complète
- Gestion d'erreurs appropriée

## Évolution prévue

Ce projet évoluera au fil des TPs pour inclure :

### TP2 - Couches bas-niveau
- Gestion des fichiers de données
- Structure des pages
- Buffer manager

### TP3 - Structures de données
- B+ arbres
- Index
- Catalogues

### TP4 - Requêtes
- Parser SQL simplifié
- Exécuteur de requêtes
- Jointures

## Problèmes connus

- Aucun problème connu pour cette version TP1

## Auteurs

Projet développé dans le cadre du cours BDDA (Bases de Données Avancées)

## Licence

Ce projet est développé à des fins éducatives.