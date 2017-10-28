(TeX-add-style-hook
 "CrossfunctionalAspects"
 (lambda ()
   (add-to-list 'LaTeX-verbatim-environments-local "lstlisting")
   (add-to-list 'LaTeX-verbatim-macros-with-braces-local "lstinline")
   (add-to-list 'LaTeX-verbatim-macros-with-delims-local "lstinline")
   (LaTeX-add-labels
    "sec:BasicAspects"
    "sec:GeneralErrorHandling"
    "sec:AbnormalEvengtVsOperationErrors"
    "sec:ErrorHandlingApproaches"
    "sec:ErrorHandlingApproachesAllgDes"
    "sec:LoggingLibName"
    "sec:Konfiguration"
    "sec:PackageNamingConventions"
    "sec:JavaNamingConventions"
    "sec:CoreLibrary"
    "sec:Extensions"
    "sec:ProjectNamingandStructure"
    "fig:5_3_SCH_ProjStruct"
    "sec:ModuleStructureandDependencies")))

