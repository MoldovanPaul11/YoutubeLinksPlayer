# 🎵 YouTube Playlist Player

O aplicație desktop Java pentru redarea playlist-urilor YouTube cu suport pentru video integrat, crossfade între piese și pre-descărcare automată.

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36?logo=apache-maven)
![VLC](https://img.shields.io/badge/VLC-Required-orange?logo=vlcmediaplayer)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-blue)

---

## 📋 Cuprins

- [Descriere](#-descriere)
- [Funcționalități](#-funcționalități)
- [Capturi de Ecran](#-capturi-de-ecran)
- [Cerințe de Sistem](#-cerințe-de-sistem)
- [Instalare](#-instalare)
- [Rulare](#-rulare)
- [Utilizare](#-utilizare)
- [Scurtături Tastatură](#-scurtături-tastatură)
- [Structura Proiectului](#-structura-proiectului)
- [Tehnologii Utilizate](#-tehnologii-utilizate)
- [Troubleshooting](#-troubleshooting)
- [Dezvoltare](#-dezvoltare)

---

## 📝 Descriere

**YouTube Playlist Player** este o aplicație desktop dezvoltată în Java care permite redarea playlist-urilor YouTube direct pe computer. Aplicația extrage stream-urile audio/video folosind `yt-dlp`, le descarcă local și le redă folosind VLC (prin biblioteca VLCJ).

### Caracteristici principale:
- 🎬 **Video integrat** - Vizualizare video direct în aplicație
- 🔄 **Crossfade** - Tranziție lină între piese (1.5 secunde)
- ⚡ **Pre-descărcare** - Descarcă automat următoarele 3 piese pentru redare fără întreruperi
- 📋 **Playlist complet** - Suport pentru playlist-uri YouTube (până la 50 de piese)
- 🎨 **Interfață modernă** - UI dark theme cu stilizare profesională

---

## ✨ Funcționalități

### Redare Media
| Funcție | Descriere |
|---------|-----------|
| ▶️ Play | Pornește redarea piesei selectate |
| ⏸️ Pause | Pune pe pauză / Continuă redarea |
| ⏹️ Stop | Oprește complet redarea |
| ⏭️ Next | Trece la piesa următoare |
| ⏮️ Previous | Trece la piesa anterioară |

### Controale Avansate
- 📊 **Progress Slider** - Navigare în piesă prin drag & drop
- 🔊 **Control Volum** - Slider 0-100% cu persistență
- 🕒 **Afișare Timp** - Format `MM:SS / MM:SS` (curent / total)
- 📋 **Playlist Manager** - Double-click pentru redare directă

### Funcții Speciale
- 🔄 **Crossfade Audio** - Tranziție lină de 1.5 secunde între piese
- ⚡ **Pre-loading** - Descarcă anticipat următoarele 3 piese
- 🗑️ **Cache Management** - Curăță automat fișierele vechi
- 🔁 **Auto-skip** - Sare peste piesele care nu pot fi redate

---

## 🖼️ Capturi de Ecran

<img width="1918" height="1030" alt="image" src="https://github.com/user-attachments/assets/6ea3c75b-bfa3-4500-a838-dfd5e364e6a9" />


## 💻 Cerințe de Sistem

### Obligatorii
| Component | Versiune | Notă |
|-----------|----------|------|
| **Java JDK** | 8+ | OpenJDK sau Oracle JDK |
| **Maven** | 3.6+ | Pentru build |
| **VLC Media Player** | 3.0+ | **64-bit** (potrivit cu JDK) |
| **yt-dlp** | Latest | Pentru extragere YouTube |
| **FFmpeg** | Latest | Pentru conversie audio |

### Opționale
| Component | Utilizare |
|-----------|-----------|
| `cookies.txt` | Pentru acces la videoclipuri restricționate |

### Resurse Hardware
- **RAM:** Minim 512 MB (recomandat 1 GB+)
- **Disc:** 100 MB pentru aplicație + spațiu pentru cache
- **Rețea:** Conexiune la internet pentru streaming

---

## 📦 Instalare

### 1. Instalare Java JDK

**Windows (cu winget):**winget install Microsoft.OpenJDK.17
**Linux (Ubuntu/Debian):**sudo apt update
sudo apt install openjdk-17-jdk
**macOS:**
brew install openjdk@17
Verificare:
java -version
### 2. Instalare Maven

**Windows:**winget install Apache.Maven
**Linux:**
sudo apt install maven
**macOS:**
brew install maven
Verificare:
mvn -v
### 3. Instalare VLC Media Player

**Windows:**winget install VideoLAN.VLC
**Linux:**
sudo apt install vlc
**macOS:**
brew install --cask vlc
> ⚠️ **Important:** Arhitectura VLC (32/64-bit) trebuie să corespundă cu JDK-ul instalat!

### 4. Instalare yt-dlp și FFmpeg

**Cu Python/pip:**pip install -U yt-dlp
**Windows (descărcare directă):**
1. Descarcă `yt-dlp.exe` de la: https://github.com/yt-dlp/yt-dlp/releases
2. Descarcă `ffmpeg.exe` de la: https://ffmpeg.org/download.html
3. Plasează ambele în folderul proiectului sau adaugă-le în PATH

Verificare:yt-dlp --version
ffmpeg -version
### 5. Configurare Cookies (Opțional)

Pentru videoclipuri restricționate la vârstă sau regiune:

1. Instalează extensia browser "Get cookies.txt LOCALLY"
2. Navighează la youtube.com și autentifică-te
3. Exportă cookie-urile ca `cookies.txt`
4. Plasează fișierul în folderul proiectului

---

## 🚀 Rulare

### Opțiunea 1: Cu Maven (recomandat pentru dezvoltare)
# Navighează în folder
cd proiect_muzica_java

# Compilează și rulează
mvn clean compile exec:java
### Opțiunea 2: Build JAR și rulare
# Build
mvn clean package -DskipTests

# Rulare JAR
java -jar target/proiect_muzica_java-1.0-SNAPSHOT.jar
### Opțiunea 3: Scripturi rapide

**Windows:**RUN.bat
**Linux/macOS:**
chmod +x run.sh
./run.sh
---

## 📖 Utilizare

### Încărcare Playlist

1. **Copiază URL-ul** unui playlist YouTube sau al unei piese dintr-un mix
2. **Lipește** în câmpul de text
3. **Apasă** butonul "📥 Încarcă Playlist"
4. **Așteaptă** încărcarea listei (până la 50 de piese)

### Exemple URL-uri acceptate:https://www.youtube.com/playlist?list=PLxxxxxxx
https://www.youtube.com/watch?v=xxxxx&list=PLxxxxxxx
https://www.youtube.com/watch?v=xxxxx&list=RDxxxxx (Mix)
### Redare

- **Double-click** pe o piesă din playlist pentru a o reda
- Folosește **butoanele de control** pentru Play/Pause/Stop
- **Trage slider-ul** pentru a naviga în piesă
- **Ajustează volumul** cu slider-ul dedicat

---

## ⌨️ Scurtături Tastatură

| Tastă | Funcție |
|-------|---------|
| `Space` | Play / Pause |
| `←` (Left Arrow) | Înapoi 5 secunde |
| `→` (Right Arrow) | Înainte 5 secunde |

---

## 📁 Structura Proiectului
proiect_muzica_java/
├── 📁 src/
│   └── 📁 main/
│       └── 📁 java/
│           └── 📁 org/example/
│               ├── 📄 Main.java           # Aplicația principală cu UI
│               ├── 📄 MusicPlayer.java    # Player simplu pentru fișiere locale
│               ├── 📄 YouTubePlayer.java  # Logică extragere și descărcare YouTube
│               └── 📄 YoutubeUtils.java   # Utilități helper
├── 📁 target/                              # Folder output (după build)
│   └── 📄 proiect_muzica_java-1.0-SNAPSHOT.jar
├── 📄 pom.xml                              # Configurare Maven
├── 📄 README.md                            # Acest fișier
├── 📄 cookies.txt                          # Cookie-uri YouTube (opțional)
├── 📄 yt-dlp.exe                           # Executabil yt-dlp (Windows)
└── 📄 ffmpeg.exe                           # Executabil FFmpeg (Windows)
---

## 🛠️ Tehnologii Utilizate

### Limbaj și Framework
- **Java 8+** - Limbaj de programare principal
- **Swing** - Framework GUI pentru interfața grafică
- **Maven** - Build tool și management dependențe

### Biblioteci
| Bibliotecă | Versiune | Utilizare |
|------------|----------|-----------|
| **VLCJ** | 4.8.2 | Integrare VLC pentru redare media |
| **JNA** | 5.13.0 | Java Native Access pentru librării native |
| **org.json** | 20230227 | Parsare JSON pentru output yt-dlp |
| **SLF4J** | 1.7.36 | Logging |

### Unelte Externe
| Unealtă | Utilizare |
|---------|-----------|
| **yt-dlp** | Extragere URL-uri și descărcare de pe YouTube |
| **FFmpeg** | Conversie și procesare audio/video |
| **VLC** | Backend pentru redare media |

---

## 🔧 Troubleshooting

### ❌ "VLC native library not found"

**Cauză:** VLC nu este instalat sau arhitectura nu se potrivește.

**Soluție:**
1. Instalează VLC Media Player (64-bit pentru JDK 64-bit)
2. Adaugă folderul VLC în PATH: `C:\Program Files\VideoLAN\VLC`
3. Repornește terminalul/IDE-ul

### ❌ "yt-dlp nu a fost găsit"

**Cauză:** yt-dlp nu este în PATH sau în folderul proiectului.

**Soluție:**# Verifică instalarea
yt-dlp --version

# Sau plasează yt-dlp.exe în folderul proiectului
### ❌ "Eroare la încărcarea playlist-ului"

**Cauze posibile:**
1. URL invalid
2. Playlist privat
3. Restricții regionale

**Soluții:**
- Verifică dacă URL-ul funcționează în browser
- Adaugă `cookies.txt` pentru autentificare
- Încearcă un alt playlist

### ❌ "Nu s-a putut descărca video-ul"

**Cauză:** YouTube blochează accesul anonim pentru unele videoclipuri.

**Soluție:**
- Adaugă fișierul `cookies.txt` cu cookie-urile tale YouTube
- Aplicația va încerca automat mai multe metode (Android, iOS client)

### ❌ "Cannot resolve symbol 'vlcj' / 'org.json'"

**Cauză:** Dependențele Maven nu au fost descărcate.

**Soluție:**mvn clean compile
# Sau în IntelliJ: Click dreapta pe pom.xml → Maven → Reload Project
### ❌ Aplicația pornește dar nu se aude nimic

**Verificări:**
1. Volumul în aplicație nu este 0
2. Volumul sistemului nu este mut
3. VLC funcționează independent

---

## 👨‍💻 Dezvoltare

### Build pentru dezvoltaremvn clean compile
### Rulare în mod debugmvn exec:java -Dexec.mainClass="org.example.Main"
### Generare JAR executabilmvn clean package -DskipTests
### Structura codului

- **Main.java** - Clasa principală cu interfața Swing, logica de redare și management playlist
- **YouTubePlayer.java** - Extragere playlist-uri și descărcare stream-uri YouTube
- **MusicPlayer.java** - Player alternativ pentru fișiere locale
- **YoutubeUtils.java** - Utilități helper pentru extragere URL-uri

---

## 📄 Licență

Acest proiect este dezvoltat pentru uz educațional.

---

## 🤝 Contribuții

Contribuțiile sunt binevenite! Pentru modificări majore, deschide mai întâi un issue pentru a discuta schimbările propuse.

---

## 📞 Contact

Pentru întrebări sau probleme, creează un issue în repository.

---

**Dezvoltat cu ❤️ în Java**
