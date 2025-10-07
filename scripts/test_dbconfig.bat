@echo off
echo === Test de DBConfig ===

REM Vérifier que les classes sont compilées
if not exist "build\DBConfigTest.class" (
    echo Les classes ne sont pas compilées. Exécutez d'abord scripts\compile.bat
    exit /b 1
)

REM Lancer les tests
echo Lancement des tests DBConfig...
java -cp build DBConfigTest