/* Flex Definitions */

%{
/* C code to be copied verbatim */
#include <stdio.h>
#include "parser.h"

%}

DOCUMENT_CLASS \\documentclass\[.*\]\{.*\}
USE_PACKAGE    \\usepackage(\[.*\])?\{.*\}
AUTHOR         \\author\{.*\}
WHITESPACE     [ \n\r\t]+
ANY .

/* Token rules */
%%

"\\begin{document}"         ;//{return T_BEGIN_DOC;}
"\\begin{itemize}"          ;//{return T_BEGIN_ITEM;}
"\\begin{thebibliography}"  ;//{return T_BEGIN_BIB;}

"\\end{document}"           ;//{return T_END_DOC;}
"\\end{itemize}"            ;//{return T_END_ITEM;}
"\\end{thebibliography}"    ;//{return T_END_BIB;}

"\\maketitle"               ;//{return T_MAKETITLE;}
"\\title"                   ;//{return T_TITLE;}
"\\textbf"                  ;//{return T_BOLD;}
"\\textid"                  ;//{return T_ITALIC;}
"\\item"                    ;//{return T_ITEM;}
"\\includegraphics"         ;//{return T_GRAPHIC;}
"\\cite"                    ;//{return T_CITE;}
"\\bibitem"                 ;//{return T_BIB_ITEM;}

{DOCUMENT_CLASS}            ;//{return T_DOCUMENT_CLASS;}
{USE_PACKAGE}               ;//{return T_USE_PACKAGE;}
{AUTHOR}                    ;//{return T_AUTHOR;}


WHITESPACE  {
		yylval.str = (char *) strdup(yytext);
		return T_WHITESPACE;
	}
ANY {
		yylval.str = (char *) strdup(yytext);
		return T_CHAR;
	}
