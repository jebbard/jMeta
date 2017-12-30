SET BASEDIR=%~dp0
SET PYTHONDIR=C:\Python27\
SET PYTHONPATH=%BASEDIR%/scripts/pdfCropper/pyPdf-1.13/pyPdf

echo (1) design concept

:: Save PPT file as PDF
cscript %BASEDIR%/scripts/savePowerPointAsPDF/OpenAndSavePowerPointAsPDF.vbs %BASEDIR%/DesignConcept/figures/FiguresTotal.ppt %BASEDIR%/DesignConcept/figures/FiguresTotal.pdf

:: Split PDF file into individual figures
%PYTHONDIR%\python.exe %BASEDIR%/scripts/pdfCropper/PDFCropper.py %BASEDIR%/DesignConcept/figures %BASEDIR%/DesignConcept/figures/FiguresTotal.pdf