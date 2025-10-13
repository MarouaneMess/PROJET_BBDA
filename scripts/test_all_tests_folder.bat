@echo off
echo === Compile and run all tests in tests/ via AllTests ===
call compile.bat
if %ERRORLEVEL% neq 0 (
  echo Compile failed
  exit /b %ERRORLEVEL%
)
REM Run from project root so relative paths are under repo root
pushd .. >nul
java -cp build AllTests
set RC=%ERRORLEVEL%
popd >nul
exit /b %RC%
