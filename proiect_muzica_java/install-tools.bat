@echo off
REM Wrapper to run PowerShell installer script as admin
powershell -ExecutionPolicy Bypass -File "%~dp0install-tools.ps1"
pause

