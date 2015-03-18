/* Flex Definitions */

%{
/* C code to be copied verbatim */
#include <stdio.h>
#include "parser.h"

%}

DOCUMENT_CLASS	\\documentclass(\[.*\])?\{.*\}
USE_PACKAGE		\\usepackage(\[.*\])?\{.*\}
AUTHOR			\\author\{.*\}
BREAK			\n\n
WHITESPACE		([ \n\r\t])([ \r\t]+[ \n\r\t])*
ITEM			\\item(\[.*\])?
ANY .

/* Token rules */
%%

"\\begin{document}"         {return T_BEGIN_DOC; }
"\\begin{itemize}"          {return T_BEGIN_ITEM; }
"\\begin{thebibliography}"  {return T_BEGIN_BIB; }

"\\end{document}"           {return T_END_DOC;}
"\\end{itemize}"            {return T_END_ITEM;}
"\\end{thebibliography}"    {return T_END_BIB;}

"\\maketitle"               {return T_MAKETITLE;}
"\\title"                   {return T_TITLE;}
"\\textbf"                  {return T_BOLD;}
"\\textit"                  {return T_ITALIC;}
{ITEM}                      {return T_ITEM;}
"\\includegraphics"         {return T_GRAPHIC;}
"\\cite"                    {return T_CITE;}
"\\bibitem"                 {return T_BIB_ITEM;}

{DOCUMENT_CLASS}            ;
{USE_PACKAGE}               ;
{AUTHOR}                    ;


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
{BREAK} {
		yylval.str = (char *) strdup(yytext);
		return T_BREAK;
	}
%%

YYSTYPE yylval;
extern int debug;
extern int fileOutput;
extern FILE *output;


void yyerror(char *s) {
	printf("%d: %s at %s\n", yylineno, s, yytext);
}

int setFlags(int argc, char** argv) {
	int i, token = 0, expectOutput = 0;
	fileOutput = 0;
	char *c;
	for (i = 1; i < argc; i++) {
		if (argv[i][0] == '-') {
			for (c = &(argv[i][1]); *c != '\0'; c++) {
				if (*c == 't') token = 1;
				if (*c == 'd') debug = 1;
				if (*c == 'o') expectOutput = 1;
			}
		} else if (expectOutput = 1) {
			output = fopen(argv[i], "w");
			fileOutput = 1;
			expectOutput = 0;
		}
	}
	if (!fileOutput) {
		output = stdout;
	}
	if (expectOutput) {
		char msg[256];
		sprintf(msg, "Error: no output file specified with -o.\n\tUsage: %s [-o <path/to/output.html>]", argv[0]);
		yyerror(msg);
		exit(0);
	}
	return token;
}

int main(int argc, char** argv) {
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
