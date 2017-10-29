(TeX-add-style-hook
 "Part_III"
 (lambda ()
   (TeX-run-style-hooks
    "Part_III/CrossfunctionalAspects"
    "Part_III/COMPUtility"
    "Part_III/COMPcompRegistry"
    "Part_III/COMPextManagement"
    "Part_III/COMPmedia")
   (LaTeX-add-labels
    "sec:Design"
    "sec:SUBSUtilitydes"
    "sec:SUBSLowLeveldes")))

