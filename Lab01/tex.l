/* Flex Definitions */
BEGIN_DOC  \\begin{document}
BEGIN_ITEM \\begin{itemize}
BEGIN_BIB  \\begin{thebibliography}
WHITESPACE [ \t\n\r]+

END_DOC  \\end{document}
END_ITEM \\end{itemize}
END_BIB  \\end{thebibliography}

MAKETITLE \\maketitle
TITLE     \\title
BOLD      \\textbf
ITALIC    \\textid
ITEM      \\item
GRAPHIC   \\includegraphics
CITE      \\cite
BIB_ITEM  \\bibitem

ANY       .

%%

/* Token rules */
