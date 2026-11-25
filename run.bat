@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
(for /r src %%f in (*.java) do @echo "%%f") > sources.txt
javac --release 17 -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1
java -cp out library.ui.Main %*
