'''
Created on 22.08.2010

Splits a PDF printed in PowerPoint with FreePDF into one PDF per page. The
name and scaling of the new PDF file figure must be given on the PowerPoint
page in an encoded form. Using this information, the file is named and the
PDF is cropped (i.e. clipped to an appropriate size which shows only
payload regions of the figure without potential outer whitespace).


@author: Jens Ebert
'''
from sys import argv
from os.path import join, sys
from pdf import PdfFileWriter, PdfFileReader
from re import findall


#######################
# Write page as PDF file. For details see docstring.
#######################
def writePDFPage(path, page):
    """
        Writes a new PDF consisting of the given single page to the given
        destination.
    """
    print "[INFO] --> ... writing new PDF page to file", path, "... "

    pdfWriter = PdfFileWriter()

    pdfWriter.addPage(page)

    outputStream = file(path, "wb")
    pdfWriter.write(outputStream)

    outputStream.close()

#######################
# Prints details about the cropping
#######################
def printCropDetails(box, widthAmount, heightAmount):
    """ Prints details about the cropping"""

    print "[INFO] --> ... cropping new PDF page ... "
    print "[INFO] --> CROP-BOX before: ", box
    print "[INFO] --> Desired width amount: ", widthAmount * 100,"%"
    print "[INFO] --> Desired height amount: ", heightAmount * 100,"%"
    print "[INFO] --> Page upper right (X,Y): (", box.getUpperRight_x(), ",", box.getUpperRight_y(), ")"

#######################
# Crop page to size amounts. For details see docstring.
#######################
def cropPDFPageFreePDF(page, widthAmount, heightAmount):
    """
        Crops the given PDF page to the given size amounts (numbers between 0 and
        1 that specify the amount of the current width and height for the crop
        operation) - assuming it comes from FreePDF.

        Two issues have to be noticed:
        - It is assumed the page was printed by FreePDF and it therefore contains
        an additional border that is also cropped.
        - Even if FreePDF creates a left to right oriented page (DIN A4,
        width > height), it virtually is treated by pyPDF / PDF as being
        "normal" A4 (height > width). Therefore, width has to be treated as height
        in this function and vice-versa. This has the effect that the page
        X coordinates are scaled or shifted with the height scaling factor or
        frame height, the Y coordinates are scaled or shifted with the width
        scaling factor or frame width.

        Note that for pyPDF, a cropBox, mediaBox, artBox or bleedBox is a rectangle
        with following points:

        lower left (X, Y) ++++++++++++++++++++++++++++++ lower right (X, Y)
        +                                                                 +
        +                              box                                +
        +                                                                 +
        upper left (X, Y) ++++++++++++++++++++++++++++++ upper right (X, Y)
    """

    printCropDetails(page.cropBox, widthAmount, heightAmount)

    # FreePDF creates a static white frame around the printed figure:
    #    top: 1 cm, bottom: 1 cm, left: 2,2 cm, right: 2,2 cm.
    #    Here is the transformation from centimeters into points which is the
    #    unit that pyPDF expects (1 cm equals approximately 28,35 points)
    cmToPoints = 28.35

    # The border width and hights are taken 1 millimeter smaller than their real
    # size to avoid clipping effects.
    staticBorderWidth = 2.1 * cmToPoints
    staticBorderHeight = 0.9 * cmToPoints

    # (1) Scale the page width and height according to given amounts.
    #    Only the pages "payload" is scaled, i.e. what is within the frame
    #    that FreePDF added.
    widthToScale = page.cropBox.getUpperRight_x() - staticBorderHeight * 2
    heightToScale = page.cropBox.getUpperRight_y() - staticBorderWidth * 2

    # Scale by setting the pages upper right (=bottom right) coordinate which
    # automatically affects the whole box, because the crop box must always be
    # a rectangle.
    page.cropBox.setUpperRight([
        widthToScale * heightAmount + staticBorderHeight,
        heightToScale * widthAmount + staticBorderWidth
        ])

    # (2) Set lower left (i.e. top left of the page) to eliminate the FreePDF
    #     generated borders
    page.cropBox.setLowerLeft([
        staticBorderHeight,
        staticBorderWidth
        ])

#######################
# Crop page to size amounts. For details see docstring.
#######################
def cropPDFPagePowerpoint(page, widthAmount, heightAmount):
    """
        Crops the given PDF page to the given size amounts (numbers between 0 and
        1 that specify the amount of the current width and height for the crop
        operation) - Assuming it comes from PowerPoint.

        Differences to FreePDF Case:
        - The coordinate system is strangle flipped: 

        upper left (0, height) ++++++++++++++++ upper right (width, height)
        +                                                                 +
        +                         original PPT                            +
        +                                                                 +
        lower left (0, 0) ++++++++++++++++++++++++++ lower right (width, 0)
    """

    printCropDetails(page.cropBox, widthAmount, heightAmount)
    
    # Scale the page width and height according to given amounts.
    #    Only the pages "payload" is scaled, i.e. what is within the frame
    #    that FreePDF added.
    widthToScale = page.cropBox.getUpperRight_x()
    heightToScale = page.cropBox.getUpperRight_y()

    page.cropBox.setLowerLeft([
        0,
        heightToScale * (1 - heightAmount),
        ])
    page.cropBox.setLowerRight([
        widthToScale * widthAmount,
        heightToScale * (1 - heightAmount),
        ])  

#######################
# Return page sizing amounts. For details see docstring.
#######################
def getPageSizingAmounts(pageText):
    """
        Extracts the page sizing amounts which have to be encoded in the given
        page text in the format '##SIZE=[<width>;<height>]##'. Width and height must
        be given in percentages of the overall page width and height.
    """
    sizeHeader = "##SIZE=\["
    sizeFooter = "\]##"
    sizeSeparator = ";"
    sizeRegExp = sizeHeader + "[0-9]+" + sizeSeparator + "[0-9]+" + sizeFooter

    # Find the single matching string
    figureSizeTupel = findall(sizeRegExp, pageText)
    
    if len(figureSizeTupel) == 0:
        return None, None
    
    pageSize = figureSizeTupel[0]

    # Strip header and footer delimiters of the string
    pageSize = pageSize.strip(sizeHeader + sizeFooter)

    # Convert percentages to amounts between 0 and 1
    widthAmount = float(pageSize.split(sizeSeparator, 1)[0]) / 100
    heightAmount = float(pageSize.split(sizeSeparator, 1)[1]) / 100

    return widthAmount, heightAmount

#######################
# Return page file name. For details see docstring.
#######################
def getFileNameFromPage(pageText):
    """
        Extracts the destination file name which has to be encoded in the given
        page text in the format '<part>_<chapter>_<name>.pdf'. <part> must be a roman number up to 38,
        e.g. I, II, III, X, XXXV, <chapter> is an arbitrary number.
    """
    pageRegExp = "[IVX]+_.*?\.pdf"
    
    pageTextTupel = findall(pageRegExp, pageText)

    if len(pageTextTupel) == 0:
        return None

    # The page title
    return pageTextTupel[0]

#######################
# Determines page PDF file output path. For details see docstring.
#######################
def getOutputPath(outputFolderBase, fileName):
    """
        Determines the absolute path of the output file to create. It is a
        combination of the output base folder, another folder derived from the file
        name and the plain file name itself.
    """
    return join(outputFolderBase,
                fileName)

#######################
# Main function of this script. For details see docstring.
#######################
def main():
    """
        Script main function.
    """

    if len(argv) < 3:
        print "Usage: python PDFCropper <output_folder> <file_path>"
        exit(-1)

    outputFolderBase = argv[1]

    print "#####################"
    print "   Cropping PDFs!"
    print "#####################"
    print ""
    print "[INFO] Output Folder:", outputFolderBase

    for index in range(2, len(argv)):
        print "[INFO] Mangling file:", argv[index]

        input = PdfFileReader(file(argv[index], "rb"))

        for currentPageIndex in range(input.numPages):
            currentPage = input.getPage(currentPageIndex)

            pageText = currentPage.extractText()

            pageWidthAmount, pageSizeAmount = getPageSizingAmounts(pageText)
            
            print "[INFO] - ( Page", currentPageIndex+1, "of", input.numPages,") of file", argv[index]
            
            if pageWidthAmount is None:
                print "[INFO] - Skipping page, as no size definition found" 
            
            else:
                pageFileName = getFileNameFromPage(pageText)
                
                if pageFileName is None:
                    print "[INFO] - Skipping page, as no valid filename found"
    
                else:
                    print "[INFO] - Output filename of current page:", pageFileName
    
                    cropPDFPagePowerpoint(currentPage, pageWidthAmount, pageSizeAmount)
                    outputPath = getOutputPath(outputFolderBase, pageFileName)
                    writePDFPage(outputPath, currentPage)
                    print "[INFO] - Done current page:", pageFileName
                    print 


#######################
# Conduct script
#######################
main()

if __name__ == '__main__':
    pass