#!/bin/bash
echo "=== Compilation du MiniSGBDR ==="

# Créer le dossier build s'il n'existe pas (compile depuis scripts/ donc build est ../build)
BUILD_DIR="../build"
mkdir -p "$BUILD_DIR"

# Compiler les classes principales (recherche récursive des .java)
echo "Compilation des classes principales..."
SOURCES=$(find ../src -name "*.java")
if [ -z "$SOURCES" ]; then
    echo "Aucune source Java trouvée dans ../src"
    exit 1
fi

javac -d "$BUILD_DIR" $SOURCES
if [ $? -ne 0 ]; then
        echo "Erreur lors de la compilation des classes principales"
        exit 1
fi

echo "Compilation des tests (dossier tests)..."
if [ -d ../tests ]; then
    TESTS=$(find ../tests -maxdepth 1 -name "*.java")
    if [ -n "$TESTS" ]; then
        javac -cp "$BUILD_DIR" -d "$BUILD_DIR" $TESTS
        if [ $? -ne 0 ]; then
            echo "Erreur lors de la compilation des tests"
            exit 1
        fi
    fi
fi

echo "Compilation terminée avec succès !"
echo "Les fichiers compilés sont dans le dossier '../build'"