@echo off
echo === Compilation du MiniSGBDR ===

REM Créer le dossier build s'il n'existe pas
if not exist "build" mkdir build

REM Compiler les classes principales
echo Compilation des classes principales...
javac -d build src\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation des classes principales
    exit /b 1
)

REM Les tests sont maintenant dans src/ avec les autres classes
REM Pas besoin de compilation séparée

echo Compilation terminée avec succès !
echo Les fichiers compilés sont dans le dossier 'build'