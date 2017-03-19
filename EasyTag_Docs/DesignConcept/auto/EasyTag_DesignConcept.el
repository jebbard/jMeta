(TeX-add-style-hook
 "EasyTag_DesignConcept"
 (lambda ()
   (TeX-add-to-alist 'LaTeX-provided-package-options
                     '(("inputenc" "latin1") ("fontenc" "T1") ("xcolor" "table")))
   (add-to-list 'LaTeX-verbatim-environments-local "lstlisting")
   (add-to-list 'LaTeX-verbatim-macros-with-braces-local "lstinline")
   (add-to-list 'LaTeX-verbatim-macros-with-delims-local "lstinline")
   (TeX-run-style-hooks
    "{"
    "Introduction/Introduction"
    "Part_I"
    "Part_II"
    "Part_III"
    "{Appendix/Literature}"
    "moreverb"
    "float"
    "fancyhdr"
    "nameref"
    "inputenc"
    "fontenc"
    "longtable"
    "pdflscape"
    "makeidx"
    "listings"
    "xcolor")
   (TeX-add-symbols
    "LibName"
    "LibVersion"
    "Lib"
    "JavaVersion"
    "COMtitle"
    "COMsubtitle"
    "COMversion"
    "COMstatus")
   (LaTeX-add-counters
    "UseCase")))

