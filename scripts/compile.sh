#!/bin/bash
echo "=== Compilation du MiniSGBDR ==="

# Créer le dossier build s'il n'existe pas
mkdir -p build

# Compiler les classes principales
echo "Compilation des classes principales..."
javac -d build src/*.java
if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation des classes principales"
    exit 1
fi

# Les tests sont maintenant dans src/ avec les autres classes
# Pas besoin de compilation séparée

echo "Compilation terminée avec succès !"
echo "Les fichiers compilés sont dans le dossier 'build'"