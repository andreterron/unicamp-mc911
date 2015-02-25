/* Flex Definitions */

%{
#include "parser.h"
%}

ANY .

/* Token rules */
%%

"\\begin{document}"         {return T_BEGIN_DOC}  
"\\begin{itemize}"          {return BEGIN_ITEM} 
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

"\\documentclass"         {return T_DOCUMENT_CLASS}
"\\usepackage"         	  {return T_USE_PACKAGE} 
"\\author"         	  {return T_AUTHOR} 

ANY {
		yylval.str = (char *) strdup(yytext);
		return T_CHAR
	}
