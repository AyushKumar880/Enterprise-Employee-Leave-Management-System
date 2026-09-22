@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Employee Leave Management System
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "1" (@ECHO ON) ELSE (@ECHO OFF)

SETLOCAL

SET "DIRNAME=%~dp0"
IF "%DIRNAME%" == "" SET "DIRNAME=."
SET "MAVEN_CMD=C:\Users\ayush\apache-maven-3.9.9\bin\mvn.cmd"

IF EXIST "%MAVEN_CMD%" (
    "%MAVEN_CMD%" %*
) ELSE (
    mvn %*
)

ENDLOCAL
