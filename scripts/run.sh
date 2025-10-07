#!/bin/bash
echo "=== Lancement du MiniSGBDR ==="

#!/bin/bash
echo "=== Lancement du MiniSGBDR ==="

# Change working directory to project root so relative paths (e.g. ./DB) resolve from project root
cd "$(dirname "$0")/.." || { echo "Impossible de se placer dans la racine du projet"; exit 1; }

# Vérifier que les classes sont compilées (depuis la racine du projet)
if [ ! -f "build/MiniSGBDR.class" ]; then
    echo "Les classes ne sont pas compilées. Exécutez d'abord scripts/compile.sh"
    exit 1
fi

echo "Lancement de l'application..."
java -cp build MiniSGBDR "$@"