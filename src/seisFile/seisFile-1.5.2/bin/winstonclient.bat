@echo off
set APPDIR=%~dp0
set CMD_LINE_ARGS=%1
shift
:getArgs
if " "%1" "==" "" " goto doneArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto getArgs
:doneArgs

if "%JAVA%"=="" set JAVA=java
if "%SEISFILE_HOME%"=="" GOTO FIND
echo SEISFILE_HOME is no longer used and will be ignored
:FIND
PUSHD %APPDIR%
cd ..
set SEISFILE_HOME=%CD%
POPD

set LIB=%SEISFILE_HOME%\lib

set SEISFILE=%LIB%\seisFile-1.5.2.jar
set SEEDCODEC=%LIB%\seedCodec-1.0.8.jar
set SLF4JAPI=%LIB%\slf4j-api-1.6.6.jar
set STAXAPI=%LIB%\stax-api-1.0-2.jar
set WOODSTOXCORELGPL=%LIB%\woodstox-core-lgpl-4.1.0.jar
set MYSQLCONNECTORJAVA=%LIB%\mysql-connector-java-5.1.21.jar
set STAX2API=%LIB%\stax2-api-3.1.0.jar
set SLF4JLOG4J12=%LIB%\slf4j-log4j12-1.6.6.jar
set LOG4J=%LIB%\log4j-1.2.17.jar


if EXIST "%SEISFILE%" GOTO LIBEND
echo %SEISFILE% doesn't exist
echo SEISFILE requires this file to function.  It should be in the lib dir
echo parallel to the bin directory to this script in the filesystem.
echo If it seems like the lib dir is there, email sod@seis.sc.edu for help
GOTO END
:LIBEND
    
set CLASSPATH=%SEISFILE%;%SEEDCODEC%;%SLF4JAPI%;%STAXAPI%;%WOODSTOXCORELGPL%;%MYSQLCONNECTORJAVA%;%STAX2API%;%SLF4JLOG4J12%;%LOG4J%

%JAVA% -classpath %CLASSPATH%   -Xmx512m -DseisFile=1.5.2    edu.sc.seis.seisFile.winston.WinstonClient  %CMD_LINE_ARGS%
:END
