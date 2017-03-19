REM ***********************
REM
REM  Helper script to
REM  generate Java classes
REM  from one or several
REM  XML schema (*.xsd)
REM  files with JAXB and
REM  Java 6
REM
REM ***********************
set JAVA_HOME=..\..\..\java\jre
set XJC_PATH=..\..\CommonLibs\jaxb2.2\jaxb-ri-20091104\bin\xjc.bat

set SRC_PATH=..\src
set OUT_PACKAGE=de.je.jmeta.extmanager.impl.jaxb
set SCHEMA_PATH=%SRC_PATH%\de\je\jmeta\extmanager\impl\schema

call %XJC_PATH% -npa -p %OUT_PACKAGE%.extpoints -d %SRC_PATH% %SCHEMA_PATH%\ExtensionPointsConfiguration.xsd
call %XJC_PATH% -npa -p %OUT_PACKAGE%.extbundles -d %SRC_PATH% %SCHEMA_PATH%\ExtensionBundleConfiguration.xsd
