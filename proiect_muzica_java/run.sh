#!/bin/bash

# Script pentru rulare Simple Music Player pe Linux/macOS

echo ""
echo "===================================="
echo "  🎵 Simple Music Player - Launcher"
echo "===================================="
echo ""

# Verifica daca Java e instalat
if ! command -v java &> /dev/null; then
    echo "❌ EROARE: Java nu e instalat!"
    echo "Instaleaza Java:"
    echo "  - macOS: brew install openjdk@8"
    echo "  - Linux: sudo apt-get install openjdk-8-jre"
    exit 1
fi

# Verifica Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F'"' '{print $2}' | head -c 3)
echo "✓ Java version: $JAVA_VERSION"

# Verifica daca JAR exista
if [ ! -f "target/proiect_muzica_java-1.0-SNAPSHOT.jar" ]; then
    echo "⚠️  JAR-ul nu exista. Se compileaza..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Compilare esuata!"
        exit 1
    fi
fi

# Ruleaza aplicatia
echo "✓ Pornind aplicatia..."
echo ""
java -jar target/proiect_muzica_java-1.0-SNAPSHOT.jar

echo ""
echo "✓ Aplicatia s-a inchis"

