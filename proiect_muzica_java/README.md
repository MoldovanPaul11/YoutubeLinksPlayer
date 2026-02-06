YouTube Playlist Music Player (VLCJ)

This project is a simple desktop player that loads a YouTube playlist URL, extracts direct audio stream URLs using yt-dlp, and plays audio using VLC (via VLCJ). The UI is Swing-based.

Requirements
- Java 17+ (JDK installed)
- Maven (to build and download dependencies)
- VLC installed (so VLCJ can use the native libraries)
- yt-dlp installed and available on PATH

Install suggestions (Windows)
1) Install Java (Adoptium / OpenJDK 17+)
2) Install Maven: https://maven.apache.org/
   - verify with: mvn -v
3) Install VLC: https://www.videolan.org/vlc/
   - ensure VLC is installed and the native libraries are available (default installer is fine)
4) Install yt-dlp:
   - with pip: python -m pip install -U yt-dlp
   - or download yt-dlp.exe and put it on PATH
   - verify: yt-dlp -v

Quick install (Windows, automated)

I included two helper scripts in the project root you can run as Administrator to install required tools using winget / pip.

- `install-tools.bat` — batch wrapper that calls PowerShell script.
- `install-tools.ps1` — PowerShell script that tries to install Maven, VLC and Python (via winget) and then installs `yt-dlp` via pip.

Usage (as Administrator in cmd.exe):

```cmd
cd "C:\Users\Paul\Desktop\proiecte personale\proiect_muzica_java"
install-tools.bat
```

Or run the PowerShell script directly (open PowerShell as Administrator):

```powershell
cd 'C:\Users\Paul\Desktop\proiecte personale\proiect_muzica_java'
.\install-tools.ps1
```

Note: `winget` is available on modern Windows 10/11; if you don't have it, install tools manually (links above) or use Chocolatey.

Build and run (commands)

After installation, in cmd.exe run:

```cmd
cd "C:\Users\Paul\Desktop\proiecte personale\proiect_muzica_java"
mvn -DskipTests compile package
```

This will build a fat JAR in `target/` (because the project uses the Shade plugin). Run it with:

```cmd
java -jar target\proiect_muzica_java-1.0-SNAPSHOT-shaded.jar
```

Or run directly via Maven (downloads deps and runs):

```cmd
mvn -DskipTests compile exec:java
```

Notes and troubleshooting
- If VLC native libs aren't found, the UI will show a warning and playback controls will be disabled. Ensure VLC is installed and the architecture (32/64-bit) matches your JDK.
- Some YouTube streams may still not play due to DRM or format differences. In that case consider changing the extraction format in `YouTubePlayer` or using a different playback strategy.
- `yt-dlp` must be on PATH for `YouTubePlayer` to work; otherwise loading the playlist will fail.

Common errors and fixes

- "Cannot resolve symbol 'javafx' / 'org.json' / 'uk.co.caprica'":
  - This means Maven dependencies haven't been downloaded. Run `mvn -DskipTests compile` and re-open your IDE project (or use "Reload Maven Project" in IntelliJ).
- "VLC native library not found" (UI shows red warning):
  - Make sure VLC is installed and its installation folder (e.g. `C:\Program Files\VideoLAN\VLC`) is on your PATH. Restart your IDE/terminal after installing.
- `yt-dlp` not on PATH: run `yt-dlp -v` to verify. If missing, use `python -m pip install -U yt-dlp` or download `yt-dlp.exe` and add to PATH.

If you run the automated installer and something fails, copy-paste the terminal output here and I'll debug the exact error and fix code/scripts if needed.

Next steps I can do for you

- Implement full crossfade (two players) to avoid any gaps between tracks — I can change the code accordingly if you want.
- Improve error dialogs and add logging to a file for easier debugging.

If you want I can switch playback to JavaFX MediaPlayer instead (simpler classpath but less robust streaming support) or improve the VLCJ usage (e.g. better error handling, video surface).
