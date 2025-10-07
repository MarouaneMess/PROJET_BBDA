@echo off
echo === Compilation du MiniSGBDR ===

REM Créer le dossier build s'il n'existe pas
if not exist "..\build" mkdir ..\build

REM Compiler les classes principales (CMD-friendly)
echo Compilation des classes principales...

REM This simple command works when run from the scripts folder (it compiles all .java directly under ..\src)
javac -d ..\build ..\src\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation des classes principales
    exit /b 1
)

echo Compilation terminée avec succès !
echo Les fichiers compilés sont dans le dossier '..\build'