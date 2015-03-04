/* Flex Definitions */

%{
/* C code to be copied verbatim */
#include <stdio.h>
#include "parser.h"

%}

DOCUMENT_CLASS \\documentclass(\[.*\])?\{.*\}
USE_PACKAGE    \\usepackage(\[.*\])?\{.*\}
AUTHOR         \\author\{.*\}
WHITESPACE     [ \n\r\t]+
ANY .

/* Token rules */
%%

"\\begin{document}"         {return T_BEGIN_DOC;} //debugToken("begin{document}"); 
"\\begin{itemize}"          ;//{debugToken("begin{itemize}");}
"\\begin{thebibliography}"  ;//{debugToken("begin{thebibliography}");}

"\\end{document}"           {return T_END_DOC;}//{return T_END_DOC;} - debugToken("end{document}"); 
"\\end{itemize}"            ;//{return T_END_ITEM;}
"\\end{thebibliography}"    ;//{return T_END_BIB;}

"\\maketitle"               ;//{debugToken("maketitle");}
"\\title"                   {return T_TITLE;}
"\\textbf"                  ;//{debugToken("BOLD"); return T_BOLD;}
"\\textit"                  ;//{return T_ITALIC;}
"\\item"                    ;//{return T_ITEM;}
"\\includegraphics"         {return T_GRAPHIC;}
"\\cite"                    ;//{return T_CITE;}
"\\bibitem"                 ;//{return T_BIB_ITEM;}

{DOCUMENT_CLASS}            ;//{debugToken("DOCUMENT_CLASS");}// OK - {return T_DOCUMENT_CLASS;}
{USE_PACKAGE}               ;//{debugToken("USE_PACKAGE");}// OK - {return T_USE_PACKAGE;}
{AUTHOR}                    ;//{debugToken("AUTHOR");}// OK - {return T_AUTHOR;}


"{"							{return '{'; }
"}"							{return '}'; }

{WHITESPACE}  {
		yylval.str = (char *) strdup(yytext);
		return T_WHITESPACE;
	}
{ANY} {
		yylval.str = (char *) strdup(yytext);
		return T_CHAR;
	}
%%

YYSTYPE yylval;
extern debug;

int setFlags(int argc, char** argv) {
	int i, token = 0;
	char *c;
	for (i = 1; i < argc; i++) {
		if (argv[i][0] == '-') {
			for (c = &(argv[i][1]); *c != '\0'; c++) {
				if (*c == 't') token = 1;
				if (*c == 'd') debug = 1;
			}
		}
	}
	return token;
}

int main(int argc, char** argv)
{
	int tFlag = setFlags(argc, argv);
	if (tFlag) {
		int token;
		while ((token = yylex()) != 0)
			printf("Token: %d (%s)\n", token, yytext);
		return 0;
	} else {
		return parseMain(argc, argv);
	}
     return 0;
}