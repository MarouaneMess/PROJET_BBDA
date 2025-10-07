# Note sur l'organisation des fichiers

## Structure actuelle (après résolution des problèmes d'import)

```
PROJET_BBDA/
├── src/                        # Tout le code source (classes + tests)
│   ├── DBConfig.java          # Classe de configuration
│   ├── MiniSGBDR.java         # Application principale  
│   └── DBConfigTest.java      # Tests pour DBConfig
├── config/                     # Fichiers de configuration
├── scripts/                    # Scripts de compilation/exécution
├── docs/                       # Documentation
├── DB/                         # Dossier de données
└── tests/                      # (Vide - tests déplacés dans src/)
```

## Raison du changement

**Problème initial :**
- `DBConfigTest.java` était dans `tests/`  
- VS Code s'attendait à `package tests;`
- Mais on utilisait le package par défaut
- Cela causait des erreurs d'import

**Solution adoptée :**
- Tous les fichiers .java dans `src/` (package par défaut)
- Compilation simple et unifiée
- Plus d'erreurs d'import dans l'IDE
- Structure compatible avec les outils Java standards

## Alternative (pour projets plus complexes)

Pour des projets plus grands, on pourrait utiliser :
```
src/
├── main/java/com/bdda/
│   ├── DBConfig.java
│   └── MiniSGBDR.java
└── test/java/com/bdda/
    └── DBConfigTest.java
```

Mais pour ce TP, la structure simple est parfaite.