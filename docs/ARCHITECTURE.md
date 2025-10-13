# Architecture du MiniSGBDR - TP1 à TP3

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
### TP2 - Couche de stockage (implémentée)
```
Storage Layer
├── DiskManager      # Fichiers BinData/Data{i}.bin + Data{i}.meta (bitmap persistant)
├── PageId           # Identifiant logique (fileIdx,pageIdx)
└── (File access)    # RandomAccessFile pour IO bas niveau
```

Fonctionnalités clés:
- Init/Finish: ouverture/fermeture, chargement/écriture des bitmaps `.meta`
- AllocPage/DeallocPage: allocation/libération de pages
- ReadPage/WritePage: IO de pages de taille `pagesize`
- Gestion de la croissance jusqu’à `dm_maxfilecount`

### TP3 - Buffer Manager (implémenté)
```
Buffer Layer
├── BufferManager    # Pool de frames, pinCount/dirty/lastTouch
└── Policy: LRU/MRU  # Politique de remplacement configurable
```

Fonctionnalités clés:
- GetPage/FreePage: épingles/désépingles, marquage dirty
- FlushBuffers: persistance des pages dirty via DiskManager
- Remplacement `LRU`/`MRU` en évitant les frames épinglées

### TP4 - Couche de requêtes
```
Query Layer
├── Parser          # Analyse syntaxique SQL
├── Optimizer       # Optimisation des requêtes
└── Executor        # Exécution des requêtes
```

## Diagramme de flux (simplifié)

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
[Buffer Layer]              [Storage Files]
```

## Patterns utilisés

1. **Command Pattern** : Pour les commandes utilisateur
2. **Factory Pattern** : Pour le chargement de configuration
3. **Singleton Pattern** : (Potentiel futur) Pour des managers globaux

## Points d'extension

1. **Nouveaux formats de config** : Étendre `DBConfig.LoadDBConfig()`
2. **Nouvelles commandes** : Ajouter dans `processCommand()`
3. **Nouveaux composants** : Intégrer via `DBConfig`
4. **Nouvelles politiques** : Étendre `BufferManager` (ex. CLOCK)