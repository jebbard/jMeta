Const POWERPOINT_PDF = 32

if WScript.Arguments.Count <> 2 then
    WScript.Echo "Expecting two parameters: <path to pptx file> and <path to pdf file>"
end if

pptPath = WScript.Arguments.Item(0)
pdfPath = WScript.Arguments.Item(1)

WScript.Echo "Using input PPT file path: " & pptPath
WScript.Echo "Using output PDF file path: " & pdfPath

' Open powerpoint application 
Set powerPointApplication = CreateObject("PowerPoint.Application")
powerPointApplication.Visible = True
' Maximize and force into foreground...
powerPointApplication.WindowState = 2

' Open powerpoint presentation to save as PDF 
Set ppt = powerPointApplication.Presentations.Open(pptPath)

' Save it as PDF, embedding any fonts necessary; it overwrites any existing files
ppt.SaveAs pdfPath, POWERPOINT_PDF, True

ppt.Close

powerPointApplication.Quit


