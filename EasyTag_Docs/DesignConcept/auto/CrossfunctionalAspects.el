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
    "sec:CoreLibrary"
    "sec:Extensions")))

