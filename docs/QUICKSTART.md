# Quickstart

Ce projet est un MiniSGBDR Java sans outil de build. Des scripts `.bat` (Windows) et `.sh` (Unix) sont fournis.

## Prérequis
- Java JDK 8+ dans le PATH
- Shell:
  - Windows: cmd.exe (les `.bat`), PowerShell peut lancer les `.bat`
  - Unix/macOS: bash/sh

## Compiler
- Windows:
  - scripts\\compile.bat
- Unix/macOS:
  - scripts/compile.sh

La compilation produit des `.class` dans `build/`.

## Lancer l'ensemble des tests
- Windows:
  - scripts\\run_all.bat
- Unix/macOS:
  - scripts/run_all.sh

Ces scripts:
- compilent si nécessaire
- se positionnent à la racine du dépôt (pushd ..)
- lancent la classe `AllTests`
- retournent un code de sortie 0 si tous les tests passent

## Autres exécutions
- Lancer uniquement DBConfigTest:
  - Windows: `scripts\\test_dbconfig.bat`
- Lancer l'appli (si présente):
  - Windows: `scripts\\run.bat`
  - Unix/macOS: `scripts/run.sh`

## Dossiers importants
- `BinData/` est créé à la racine du dépôt lors des tests (et non dans `scripts/`).
- `config/` contient des exemples de configuration.
- `tests/` contient les classes de test (main-based).

## Dépannage
- Si `java`/`javac` est introuvable, vérifiez le PATH/JAVA_HOME.
- Si `BinData` apparaît dans `scripts/`, exécutez les scripts fournis (ils font un `pushd ..`).
- Si des erreurs de redéfinition de classes apparaissent, vérifiez qu'il n'existe pas de doublons entre `src/` et `tests/`.# Guide de démarrage rapide - MiniSGBDR

## Installation rapide

1. **Compilation :**
   ```cmd
   scripts\compile.bat
   ```

2. **Test :**
   ```cmd
   scripts\test_dbconfig.bat
   ```

3. **Lancement :**
   ```cmd
   scripts\run.bat
   ```

## Première utilisation

Une fois l'application lancée :

1. Tapez `HELP` pour voir les commandes
2. Tapez `STATUS` pour voir l'état du système
3. Tapez `CONFIG` pour voir la configuration
4. Tapez `EXIT` pour quitter

## Exemple de session

```
=== MiniSGBDR - Système de Gestion de Base de Données Relationnelles ===
Version 1.0 - TP1 BDDA

Utilisation de la configuration par défaut
Configuration chargée : DBConfig{dbpath='./DB'}
Dossier de données : ./DB

=== MiniSGBDR démarré ===
Tapez 'HELP' pour voir les commandes disponibles
Tapez 'EXIT' pour quitter l'application

MiniSGBDR> STATUS
=== Statut du MiniSGBDR ===
État : En fonctionnement
Configuration : DBConfig{dbpath='./DB'}
Dossier de données : Accessible
Nombre de fichiers : 0
Mémoire JVM utilisée : 15 MB

MiniSGBDR> EXIT
Arrêt du MiniSGBDR...
Au revoir !
```

## Dépannage

**Problème de compilation :**
- Vérifiez que Java JDK est installé : `java -version` et `javac -version`
- Vérifiez que vous êtes dans le bon répertoire

**Problème d'exécution :**
- Compilez d'abord : `scripts\compile.bat`
- Vérifiez que le dossier `build` existe et contient les .class