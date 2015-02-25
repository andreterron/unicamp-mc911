/* Flex Definitions */

WHITESPACE [ \t\n\r]+
ANY       .

%%

/* Token rules */
"\\begin{document}"         {return T_BEGIN_DOC}  
"\\begin{itemize}"          {return BEGIN_ITEM 
"\\begin{thebibliography}"  {return T_BEGIN_BIB}

"\\end{document}"         {return T_END_DOC}  
"\\end{itemize}"          {return T_END_ITEM} 
"\\end{thebibliography}"  {return T_END_BIB}  

"\\maketitle"             {return T_MAKETITLE} 
"\\title"                 {return T_TITLE}
"\\textbf"                {return T_BOLD}
"\\textid"                {return T_ITALIC}    
"\\item"                  {return T_ITEM}      
"\\includegraphics"       {return T_GRAPHIC}   
"\\cite"                  {return T_CITE}      
"\\bibitem"               {return T_BIB_ITEM}  

ANY {return T_CHAR}
