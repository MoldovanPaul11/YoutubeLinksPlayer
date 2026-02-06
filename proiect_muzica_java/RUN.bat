@echo off
REM Script pentru rulare Simple Music Player

echo.
echo ====================================
echo  🎵 Simple Music Player - Launcher
echo ====================================
echo.

REM Verifica dacă Java e instalat
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ EROARE: Java nu e instalat!
    echo Descarca Java de la: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Verifica dacă JAR exista
if not exist "target\proiect_muzica_java-1.0-SNAPSHOT.jar" (
    echo ⚠️  JAR-ul nu exista. Se compileaza...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ❌ Compilare esuata!
        pause
        exit /b 1
    )
)

REM Ruleaza aplicatia
echo ✓ Pornind aplicatia...
echo.
java -jar target\proiect_muzica_java-1.0-SNAPSHOT.jar

echo.
echo ✓ Aplicatia s-a inchis
pause

