SET BASEDIR=%~dp0
SET PYTHONDIR=C:\Python27\
SET PYTHONPATH=%BASEDIR%/../scripts/pdfCropper/pyPdf-1.13/pyPdf

echo Normal Figures

:: Save PPT file as PDF
cscript %BASEDIR%/../scripts/savePowerPointAsPDF/OpenAndSavePowerPointAsPDF.vbs %BASEDIR%/figures/FiguresTotal.ppt %BASEDIR%/figures/FiguresTotal.pdf

:: Split PDF file into individual figures
%PYTHONDIR%\python.exe %BASEDIR%/../scripts/pdfCropper/PDFCropper.py %BASEDIR%/figures %BASEDIR%/figures/FiguresTotal.pdf

echo Big Figures

:: Save PPT file as PDF
cscript %BASEDIR%/../scripts/savePowerPointAsPDF/OpenAndSavePowerPointAsPDF.vbs %BASEDIR%/figures/FiguresBig.ppt %BASEDIR%/figures/FiguresBig.pdf

:: Split PDF file into individual figures
%PYTHONDIR%\python.exe %BASEDIR%/../scripts/pdfCropper/PDFCropper.py %BASEDIR%/figures %BASEDIR%/figures/FiguresBig.pdf
