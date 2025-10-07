# Architecture du MiniSGBDR - TP1

## Vue d'ensemble

Le MiniSGBDR suit une architecture modulaire simple pour faciliter l'extension future.

## Composants actuels

### 1. DBConfig
**Responsabilité :** Gestion de la configuration du système

**Fonctionnalités :**
- Stockage du chemin vers les données (`dbpath`)
- Chargement depuis fichiers de configuration
- Support de multiples formats (simple, Properties)
- Validation des paramètres

**API :**
```java
// Construction directe
DBConfig config = new DBConfig("./DB");

// Chargement depuis fichier
DBConfig config = DBConfig.LoadDBConfig("config.properties");
DBConfig config = DBConfig.LoadDBConfigSimple("config.txt");
```

### 2. MiniSGBDR
**Responsabilité :** Application principale et interface utilisateur

**Fonctionnalités :**
- Boucle principale d'interaction
- Traitement des commandes utilisateur
- Gestion de l'état de l'application
- Interface console

**Architecture interne :**
- `main()` : Point d'entrée et initialisation
- `run()` : Boucle principale
- `processCommand()` : Dispatcher de commandes
- `handle*()` : Gestionnaires de commandes spécifiques

## Extensions futures

### TP2 - Couche de stockage
```
Storage Layer
├── PageManager      # Gestion des pages de données
├── BufferManager    # Cache des pages en mémoire
└── FileManager      # Accès aux fichiers sur disque
```

### TP3 - Couche d'indexation
```
Index Layer
├── BPlusTree       # Arbre B+ pour les index
├── IndexManager    # Gestion des index
└── Catalog         # Métadonnées des tables
```

### TP4 - Couche de requêtes
```
Query Layer
├── Parser          # Analyse syntaxique SQL
├── Optimizer       # Optimisation des requêtes
└── Executor        # Exécution des requêtes
```

## Diagramme de flux

```
[Utilisateur]
     |
     v
[Interface Console] --> [MiniSGBDR]
     |                       |
     v                       v
[Commandes]            [DBConfig]
     |                       |
     v                       v
[Handlers]             [Configuration]
     |                       |
     v                       v
[Future: Storage Layer]     [Future: Data Files]
```

## Patterns utilisés

1. **Command Pattern** : Pour les commandes utilisateur
2. **Factory Pattern** : Pour le chargement de configuration
3. **Singleton Pattern** : (Futur) Pour les managers globaux

## Points d'extension

1. **Nouveaux formats de config** : Étendre `DBConfig.LoadDBConfig()`
2. **Nouvelles commandes** : Ajouter dans `processCommand()`
3. **Nouveaux composants** : Intégrer via `DBConfig`