# Guide de démarrage rapide - MiniSGBDR

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