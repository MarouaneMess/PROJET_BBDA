#!/bin/bash
echo "=== Lancement du MiniSGBDR ==="

# Vérifier que les classes sont compilées
if [ ! -f "build/MiniSGBDR.class" ]; then
    echo "Les classes ne sont pas compilées. Exécutez d'abord scripts/compile.sh"
    exit 1
fi

# Lancer l'application principale
echo "Lancement de l'application..."
java -cp build MiniSGBDR "$@"