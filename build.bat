@echo off
REM ============================================================
REM build.bat — Maven Bootstrap & Build Script
REM This fully replaces the manual /lib jar downloading script.
REM It automatically downloads Maven locally and builds a fat JAR.
REM ============================================================

set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%JAVA_HOME%\bin;%PATH%

set MAVEN_VERSION=3.9.6
set MAVEN_DIR=apache-maven-%MAVEN_VERSION%

echo [1/3] Checking for local Maven environment...
if not exist %MAVEN_DIR% (
    echo       Downloading Maven %MAVEN_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip -OutFile maven.zip"
    echo       Extracting Maven...
    powershell -Command "Expand-Archive -Path maven.zip -DestinationPath . -Force"
    del maven.zip
)

set PATH=%CD%\%MAVEN_DIR%\bin;%PATH%

echo [2/3] Building with Maven (resolving ALL transitive dependencies)...
call mvn clean package

if errorlevel 1 (
    echo.
    echo [ERROR] Maven build failed.
    exit /b 1
)

echo.
echo [3/3] Build complete!
echo ============================================================
echo   Executable JAR is compiled and packed with all dependencies.
echo   Run the app with:  run.bat
echo   (or manually: java -jar target\prolab2-1.0-SNAPSHOT-jar-with-dependencies.jar)
echo ============================================================
