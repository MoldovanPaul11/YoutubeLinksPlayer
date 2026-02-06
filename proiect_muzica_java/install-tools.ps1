# install-tools.ps1

Write-Host "Running installer script..." -ForegroundColor Cyan

function Install-WingetPackage($id) {
    Write-Host "Installing $id via winget..."
    winget install --silent --accept-package-agreements --accept-source-agreements $id
}

function Install-ChocoPackage($id) {
    Write-Host "Installing $id via choco..."
    choco install $id -y --no-progress
}

function Ensure-PipInstall($module) {
    Write-Host "Installing Python package: $module"
    python -m pip install -U $module
}

# Determine installer availability
$hasWinget = (Get-Command winget -ErrorAction SilentlyContinue) -ne $null
$hasChoco = (Get-Command choco -ErrorAction SilentlyContinue) -ne $null

if (-not $hasWinget -and -not $hasChoco) {
    Write-Host "Neither winget nor Chocolatey found on this system." -ForegroundColor Yellow
    Write-Host "Automated full install won't be available, but we'll still try to fetch yt-dlp directly." -ForegroundColor Cyan
    Write-Host "Please install Java 17, Maven and VLC manually if you need them (links in README)." -ForegroundColor Cyan
} else {
    Write-Host -NoNewLine "Package manager detected: "
    if ($hasWinget) { Write-Host "winget" -ForegroundColor Green }
    if ($hasChoco) { Write-Host "choco" -ForegroundColor Green }
}

# Helper to choose installer
function Install-PackageAuto($wingetId, $chocoId) {
    if ($hasWinget) {
        try {
            Install-WingetPackage $wingetId
        } catch {
            Write-Host ("winget failed for {0}: {1}" -f $wingetId, $_.Exception.Message) -ForegroundColor Yellow
        }
    } elseif ($hasChoco) {
        try {
            Install-ChocoPackage $chocoId
        } catch {
            Write-Host ("choco failed for {0}: {1}" -f $chocoId, $_.Exception.Message) -ForegroundColor Yellow
        }
    } else {
        Write-Host ("No package manager available to install {0} automatically." -f $wingetId) -ForegroundColor Yellow
    }
}

# Install Java 17 if not present or if Java < 17
$javaOk = $false
try {
    $verOut = & java -version 2>&1 | Out-String
    if ($verOut -match '"([0-9]+)') {
        $major = [int]$Matches[1]
        if ($major -ge 17) { $javaOk = $true }
    }
} catch { }

if (-not $javaOk) {
    Write-Host "Java 17+ not found. Attempting to install if a package manager is available..." -ForegroundColor Cyan
    Install-PackageAuto "EclipseAdoptium.Temurin.17" "temurin17"
} else { Write-Host "Java 17+ detected" -ForegroundColor Green }

# Install Maven if missing
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Maven not found. Attempting to install if a package manager is available..." -ForegroundColor Cyan
    Install-PackageAuto "Apache.Maven" "maven"
} else { Write-Host "Maven found" -ForegroundColor Green }

# Install VLC if missing
$vlcInstalled = Test-Path "C:\Program Files\VideoLAN\VLC"
if (-not $vlcInstalled -and -not (Get-Command vlc -ErrorAction SilentlyContinue)) {
    Write-Host "VLC not found. Attempting to install if a package manager is available..." -ForegroundColor Cyan
    Install-PackageAuto "VideoLAN.VLC" "vlc"
} else { Write-Host "VLC found" -ForegroundColor Green }

# Install Python/yt-dlp
$ytFound = (Get-Command yt-dlp -ErrorAction SilentlyContinue) -ne $null
if (-not $ytFound) {
    Write-Host "yt-dlp not found. Installing..." -ForegroundColor Cyan
    # Ensure python is available
    $pythonAvailable = (Get-Command python -ErrorAction SilentlyContinue) -ne $null
    if (-not $pythonAvailable) {
        Write-Host "Python not found. Attempting to install Python if a package manager is available..." -ForegroundColor Cyan
        Install-PackageAuto "Python.Python.3" "python"
        $pythonAvailable = (Get-Command python -ErrorAction SilentlyContinue) -ne $null
    }

    # Try to install via pip if Python available
    if ($pythonAvailable) {
        try {
            Ensure-PipInstall "yt-dlp"
            Write-Host "yt-dlp installed via pip" -ForegroundColor Green
        } catch {
            Write-Host "pip install failed, attempting to download yt-dlp.exe" -ForegroundColor Yellow
            try {
                $out = "$env:ProgramFiles\yt-dlp.exe"
                Invoke-WebRequest -Uri "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe" -OutFile $out -UseBasicParsing
                Write-Host ("Downloaded yt-dlp.exe to {0}" -f $out) -ForegroundColor Green
                Write-Host "You may want to add $env:ProgramFiles to your PATH" -ForegroundColor Yellow
            } catch {
                Write-Host ("Failed to download yt-dlp.exe: {0}" -f $_.Exception.Message) -ForegroundColor Red
            }
        }
    } else {
        # No Python: attempt to download yt-dlp.exe directly
        Write-Host "Python not available — downloading yt-dlp.exe directly to Program Files..." -ForegroundColor Cyan
        try {
            $out = "$env:ProgramFiles\yt-dlp.exe"
            Invoke-WebRequest -Uri "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe" -OutFile $out -UseBasicParsing
            Write-Host ("Downloaded yt-dlp.exe to {0}" -f $out) -ForegroundColor Green
            Write-Host "You may want to add $env:ProgramFiles to your PATH" -ForegroundColor Yellow
        } catch {
            Write-Host ("Failed to download yt-dlp.exe: {0}" -f $_.Exception.Message) -ForegroundColor Red
            Write-Host "As a fallback, download yt-dlp.exe manually from: https://github.com/yt-dlp/yt-dlp/releases/latest" -ForegroundColor Yellow
        }
    }
} else { Write-Host "yt-dlp found" -ForegroundColor Green }

Write-Host "Installer finished. You may need to restart your terminal for PATH changes to take effect." -ForegroundColor Green
