(TeX-add-style-hook
 "EnhancedCommands"
 (lambda ()
   (TeX-add-symbols
    '("SpecialTerm" 2)
    '("SpecialTermBasic" 1)
    '("BasicTermLink" 2)
    '("SectionLink" 1)
    '("DesLink" 1)
    '("DD" 5)
    '("labelname" 1)
    "OpenIssue"
    "DES")
   (LaTeX-add-labels
    "#1")
   (LaTeX-add-environments
    '("Guideline" 1)
    "TAGcapt"
    "TAGcrit"
    "IDTAGcapt"
    "IDTAGcrit"
    "UCcapt"
    "UCdesc"
    "TABLEcapt"
    "TABLEdesc"
    "Packdesc")
   (LaTeX-add-counters
    "OpenIssueCounter"
    "DesignDecisionCounter")))

