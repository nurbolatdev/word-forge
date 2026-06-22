@echo off
setlocal
set GRADLE_VERSION=8.14.3
set GRADLE_USER_HOME=%CD%\.gradle
set GRADLE_DIR=%GRADLE_USER_HOME%\bootstrap\gradle-%GRADLE_VERSION%
set GRADLE_ZIP=%GRADLE_USER_HOME%\bootstrap\gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  if not exist "%GRADLE_USER_HOME%\bootstrap" mkdir "%GRADLE_USER_HOME%\bootstrap"
  if not exist "%GRADLE_ZIP%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%GRADLE_USER_HOME%\bootstrap' -Force"
)

call "%GRADLE_DIR%\bin\gradle.bat" %*
