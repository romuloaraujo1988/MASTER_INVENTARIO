@echo off
@REM Maven Wrapper Script

@REM Set JAVA_HOME if not set
if "%JAVA_HOME%" == "" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
)

@REM Check if Java exists
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Error: JAVA_HOME is set to an invalid directory.
    echo JAVA_HOME = "%JAVA_HOME%"
    echo Please set the JAVA_HOME variable correctly.
    exit /B 1
)

@REM Set Maven home and executable
set "MAVEN_HOME=%~dp0tools\maven"
set "MAVEN_EXECUTABLE=%MAVEN_HOME%\bin\mvn.cmd"

@REM Check if Maven exists, if not download it
if not exist "%MAVEN_EXECUTABLE%" (
    echo Maven not found. Downloading Maven 3.9.6...
    mkdir "%~dp0tools" 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%~dp0tools\maven.zip'"
    powershell -Command "Expand-Archive -Path '%~dp0tools\maven.zip' -DestinationPath '%~dp0tools' -Force"
    move "%~dp0tools\apache-maven-3.9.6" "%MAVEN_HOME%" >nul 2>&1
    del "%~dp0tools\maven.zip" >nul 2>&1
)

@REM Execute Maven
"%MAVEN_EXECUTABLE%" %*