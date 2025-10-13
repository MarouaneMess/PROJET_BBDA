@echo off
echo === Compilation du MiniSGBDR ===

REM Créer le dossier build s'il n'existe pas
if not exist "..\build" mkdir ..\build

REM Compiler les classes principales (CMD-friendly)
echo Compilation des classes principales...

REM Compiler les sources (dossier src)
javac -d ..\build ..\src\*.java
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation des classes principales
    exit /b 1
)

REM Compiler les tests (dossier tests), en utilisant ..\build comme classpath
if exist "..\tests" (
    echo Compilation des tests...
    javac -cp ..\build -d ..\build ..\tests\*.java
    if %ERRORLEVEL% neq 0 (
            echo Erreur lors de la compilation des tests
            exit /b 1
    )
)

echo Compilation terminée avec succès !
echo Les fichiers compilés sont dans le dossier '..\build'