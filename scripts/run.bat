@echo off
echo === Lancement du MiniSGBDR ===

REM Vérifier que les classes sont compilées
if not exist "build\MiniSGBDR.class" (
    echo Les classes ne sont pas compilées. Exécutez d'abord scripts\compile.bat
    exit /b 1
)

REM Lancer l'application principale
echo Lancement de l'application...
java -cp build MiniSGBDR %*