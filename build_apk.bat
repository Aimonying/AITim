@echo off
echo Setting up environment...

set JAVA_HOME=f:\APP\zhitingji\tools\jdk-17.0.11+9
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME set to: %JAVA_HOME%
echo Checking Java version...
java -version

echo.
echo Starting Gradle build...
gradlew.bat assembleRelease

echo.
if %ERRORLEVEL% equ 0 (
    echo Build succeeded!
    echo Looking for APK file...
    dir /s /b app\build\outputs\apk\release\*.apk
) else (
    echo Build failed with error code: %ERRORLEVEL%
)

pause