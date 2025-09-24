@echo off
rem deploy.bat - copy the built project jar into XAMPP Tomcat webapps/FirstServlet/WEB-INF/lib

setlocal

rem Change TOMCAT_HOME below if your XAMPP Tomcat is installed elsewhere
set "TOMCAT_HOME=C:\xampp\tomcat"

set "APP_NAME=FirstServlet"
set "TARGET_JAR=target\FirstServlet-1.0-SNAPSHOT.jar"

if not exist "%TARGET_JAR%" (
    echo JAR not found: %TARGET_JAR%
    echo Please run: mvn package
    exit /b 1
)

set "DEST_DIR=%TOMCAT_HOME%\webapps\%APP_NAME%\WEB-INF\lib"

if not exist "%DEST_DIR%" (
    echo Creating destination directory: %DEST_DIR%
    mkdir "%DEST_DIR%"
)

echo Copying %TARGET_JAR% to %DEST_DIR%...
copy /Y "%TARGET_JAR%" "%DEST_DIR%\" > nul

rem Also copy web.xml from the project template to the webapp WEB-INF
set "WEBXML_SRC=webapp\WEB-INF\web.xml"
set "WEBINF_DIR=%TOMCAT_HOME%\webapps\%APP_NAME%\WEB-INF"
if not exist "%WEBINF_DIR%" (
    echo Creating WEB-INF directory: %WEBINF_DIR%
    mkdir "%WEBINF_DIR%"
)
if exist "%WEBXML_SRC%" (
    echo Copying %WEBXML_SRC% to %WEBINF_DIR%...
    copy /Y "%WEBXML_SRC%" "%WEBINF_DIR%\" > nul
) else (
    echo Warning: %WEBXML_SRC% not found; servlet annotations will be used instead.
)

if %ERRORLEVEL% EQU 0 (
    echo Deployed %TARGET_JAR% to %DEST_DIR%
    echo If Tomcat is running, restart it to pick up the new jar.
    exit /b 0
) else (
    echo Failed to copy jar. Error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
