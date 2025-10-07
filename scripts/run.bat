@echo off
echo === Lancement du MiniSGBDR ===

REM Vérifier que les classes sont compilées
if not exist "..\build\MiniSGBDR.class" (
    echo Les classes ne sont pas compilées. Exécutez d'abord scripts\compile.bat
    exit /b 1
)

REM Lancer l'application principale
echo Lancement de l'application...
REM Change working directory to project root so relative paths (e.g. ./DB) resolve from project root
pushd .. >nul
java -cp build MiniSGBDR %*
set RC=%ERRORLEVEL%
popd >nul

exit /b %RC%