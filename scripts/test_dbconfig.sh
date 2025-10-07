#!/bin/bash
echo "=== Test de DBConfig ==="

# Vérifier que les classes sont compilées
if [ ! -f "build/DBConfigTest.class" ]; then
    echo "Les classes ne sont pas compilées. Exécutez d'abord scripts/compile.sh"
    exit 1
fi

# Lancer les tests
echo "Lancement des tests DBConfig..."
java -cp build DBConfigTest