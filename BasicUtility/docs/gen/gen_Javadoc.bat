cls

@echo off

echo =====================================================
echo Generating javadoc
echo =====================================================

:::::::::::::::::::::::::::::::::::::::::
:: Variables
:::::::::::::::::::::::::::::::::::::::::

set JDK_PATH=E:\Programmieren\01_Java\01_JDKs\java6_0_25\bin

set javadoc_path=%JDK_PATH%\javadoc.exe
set jar_path=%JDK_PATH%\jar.exe
set destination="../api"
set joptions=javadoc_options.txt
set jpackages=javadoc_packages.txt
set jstdlog=javadoc_std_out.log
set jerrlog=javadoc_std_err.log

echo . Current directory: %cd%
echo . Javadoc path: %javadoc_path%
echo . Destination path: %destination%
echo . Javadoc option file: %joptions%
echo . Javadoc package file: %jpackages%
echo . Javadoc std log: %jstdlog%
echo . Javadoc error log: %jerrlog%

:::::::::::::::::::::::::::::::::::::::::
:: Clean output path
:::::::::::::::::::::::::::::::::::::::::

echo . Clearing destination path  %destination%

rmdir /s /q %destination%

mkdir %destination%

echo . Trying to generate javadoc ....
%javadoc_path% -d %destination% @%joptions% @%jpackages% 1> %jstdlog% 2> %jerrlog% 
echo 
echo . Finished javadoc
echo . You find the results in the destination directory %destination% and the log files %jstdlog% and %jerrlog%

echo [DONE]
echo ------------------
set /P ende=Press [Enter] to proceed