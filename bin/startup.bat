@ECHO OFF

set "CURRENT_DIR=%cd%"
if not "%APPS_HOME%" == "" goto gotHome
set "APPS_HOME=%CURRENT_DIR%"
if exist "%APPS_HOME%\bin\startup.bat" goto okHome
cd ..
set "APPS_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome
if exist "%APPS_HOME%\bin\startup.bat" goto okHome
    echo The APPS_HOME environment variable is not defined correctly
    echo This environment variable is needed to run this program
goto end
:okHome

rem ### Set JavaHome if it exists
if exist { "%JAVA_HOME%\bin\javaw" } (
    set "JAVA=%JAVA_HOME%\bin\javaw"
) else (
    set "JAVA=javaw"
)

echo Using JAVA_HOME: "%JAVA_HOME%"
echo Using APPS_HOME: "%APPS_HOME%"

set JAVA_OPTS=-server
rem set JAVA_OPTS_SCRIPT=-XX:+HeapDumpOnOutOfMemoryError -Djava.awt.headless=true
    set JAVA_OPTS_SCRIPT=-XX:+HeapDumpOnOutOfMemoryError

rem ### Use the Hotspot garbage-first collector.
set JAVA_OPTS=%JAVA_OPTS%  -XX:+UseG1GC

rem ### Have the JVM do less remembered set work during STW, instead
rem ### preferring concurrent GC. Reduces p99.9 latency.
set JAVA_OPTS=%JAVA_OPTS%  -XX:G1RSetUpdatingPauseTimePercent=5

rem ### Main G1GC tunable: lowering the pause target will lower throughput and vise versa.
rem ### 200ms is the JVM default and lowest viable setting
rem ### 1000ms increases throughput. Keep it smaller than the timeouts.
set JAVA_OPTS=%JAVA_OPTS%  -XX:MaxGCPauseMillis=500

rem ### Optional G1 Settings

rem ### Save CPU time on large (>= 16GB) heaps by delaying region scanning
rem ### until the heap is 70% full. The default in Hotspot 8u40 is 40%.
rem set JAVA_OPTS=%JAVA_OPTS%  -XX:InitiatingHeapOccupancyPercent=70

rem ### For systems with > 8 cores, the default ParallelGCThreads is 5/8 the number of logical cores.
rem ### Otherwise equal to the number of cores when 8 or less.
rem ### Machines with > 10 cores should try setting these to <= full cores.
rem set JAVA_OPTS=%JAVA_OPTS%  -XX:ParallelGCThreads=16

rem ### By default, ConcGCThreads is 1/4 of ParallelGCThreads.
rem ### Setting both to the same value can reduce STW durations.
rem set JAVA_OPTS=%JAVA_OPTS%  -XX:ConcGCThreads=16

rem ### GC logging options -- uncomment to enable

rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCDetails
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCDateStamps
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintHeapAtGC
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintTenuringDistribution
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCApplicationStoppedTime
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintPromotionFailure
rem set JAVA_OPTS=%JAVA_OPTS% -XX:PrintFLSStatistics=1
rem set JAVA_OPTS=%JAVA_OPTS% -XX:+UseGCLogFileRotation
rem set JAVA_OPTS=%JAVA_OPTS% -XX:NumberOfGCLogFiles=10
rem set JAVA_OPTS=%JAVA_OPTS% -XX:GCLogFileSize=10M

rem ### Console Log Chinese Support
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8

rem ### Startup Optional ARGUMENTS: -console -start -gui
set OPTS_ARGS=-gui

rem set ISB=start
set ISB=start /B
set CMD=%JAVA% %JAVA_OPTS% %JAVA_OPTS_SCRIPT% -jar %APPS_HOME%\file-service.jar %OPTS_ARGS%

rem ### Set CTL if OPTS_ARGS == -console
set CTL=%ISB% %CMD%
if %OPTS_ARGS% == -console (
   set CTL=%CMD%
)

rem ### Console Log Parameters
set "year=%date:~0,4%"
set "month=%date:~5,2%"
set "day=%date:~8,2%"
set "hour_ten=%time:~0,1%"
set "hour_one=%time:~1,1%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
set LOGS_NAME=%year%%month%%day%.%hour_ten%%hour_one%%minute%%second%.log
set LOGS_FILE=%APPS_HOME%\logs\%LOGS_NAME%
echo Using LOGS_FILE: "%LOGS_FILE%"

echo %CTL%
%CTL% > %LOGS_FILE%