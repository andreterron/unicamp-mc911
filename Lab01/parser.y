%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

char *concat(int count, ...);
void debugResult(const char* type, const char* result);

int debug = 0;

%}

/* Bison Declarations */

%union{
	char *str;
}


%token T_BEGIN_DOC  
%token T_BEGIN_ITEM 
%token T_BEGIN_BIB

%token T_END_DOC  
%token T_END_ITEM 
%token T_END_BIB  

%token T_MAKETITLE 
%token T_TITLE
%token T_BOLD
%token T_ITALIC    
%token T_ITEM      
%token T_GRAPHIC   
%token T_CITE      
%token T_BIB_ITEM  

//%token T_DOCUMENT_CLASS
//%token T_USE_PACKAGE 
//%token T_AUTHOR

%token <str> T_CHAR T_WHITESPACE
%type  <str> text whitespace word char element graphic file command skip_blank header title body bold

%%

//skip_blank command free_text ft2 file element command

/* Grammar rules */

//file		: text skip_blank file2			{ $$ = concat(3, $1, $2, $3); }
//			//| command skip_blank file2		{ $$ = concat(3, $1, $2, $3); }
//			;

//file2		: /* empty */			{ $$ = ""; }
//			| command skip_blank file2 { $$ = concat(2, $1, $2); }
//			| text skip_blank file2 { $$ = concat(2, $1, $2); }
//			;

/*free_text	: free_text ft2		{ $$ = concat(2, $1, $2); }
			| ft2			{ $$ = $1; }
			;

ft2			//: 			{ $$ = ""; }
			: skip_blank text		{ $$ = concat(2, $1, $2); debugResult("ft2 text", $$); }
			| skip_blank command		{ $$ = concat(2, $1, $2); debugResult("ft2 cmd", $$); }
			;

*/

//file		: skip_blank element { $$ = concat(2, $1, $2); }
//			| file skip_blank element { $$ = concat(3, $1, $2, $3); }
//			;

//file		: element			{ $$ = $1; }
//			| file element		{ $$ = $1; }
//			;

file		: header skip_blank T_BEGIN_DOC skip_blank body skip_blank T_END_DOC skip_blank {
		printf("%s\n", $5);
	}
			;


			
header		: skip_blank title			{ $$ = $1; debugResult("header", $1);}
			;

title		: T_TITLE '{' text '}'		{ $$ = $3; debugResult("title", $3); }
			;

body		: element 					{ $$ = $1;}
			| body skip_blank element	{ $$ = concat(3, $1, $2, $3);}
			;
			
//body		: element body2				{ $$ = concat(2, $1, $2);}
//			;
			
//body2		: /* empty */				{ $$ = ""; }
//			| skip_blank element body2	{ $$ = concat(3, $1, $2, $3);}
//			;
			
element		: word						{ $$ = $1; debugResult("word", $1);}
			| command					{ $$ = $1; }
			;


command		: graphic					{ $$ = $1; debugResult("command graphic", $1);}
			| bold						{ $$ = $1; debugResult("command bold", $1); }
			| '{' text '}'				{ $$ = concat(3, "{", $2, "}"); debugResult("command", $$); }
			;

graphic		: T_GRAPHIC '{' text '}'	{ $$ = concat(3, "<img src=\"", $3, "\"/>"); }
			;

bold		: T_BOLD '{' text '}'		{ $$ = concat(3, "<b>", $3, "</b>");}
			;

text		: word
			| text whitespace word		{ $$ = concat(3, $1, $2, $3); debugResult("text", $$);}
			;

/*text		: word text2				{ $$ = concat(2, $1, $2); debugResult("text", $$);}
			;

text2		: /* empty 					{ $$ = ""; debugResult("text2", $$); }
			| whitespace word text2 	{ $$ = concat(3, $1, $2, $3); debugResult("text2", $$); }
			;*/
			
skip_blank	: /* empty */				{ $$ = ""; debugResult("skip_blank", "");}
			| whitespace skip_blank		{ $$ = concat(2, $1, $2); debugResult("skip_blank", $$);}
			;

whitespace	: T_WHITESPACE				{ $$ = $1; }
			;

word		: char
			| word char					{ $$ = concat(2, $1, $2); }
			;
			
char		: T_CHAR
			;

//word2		: /* empty */				{ $$ = ""; }
//			| T_CHAR word2				{ $$ = concat(2, $1, $2); }
//			;



%%
 
char* concat(int count, ...)
{
    va_list ap;
    int len = 1, i;

    va_start(ap, count);
    for(i=0 ; i<count ; i++)
        len += strlen(va_arg(ap, char*));
    va_end(ap);

    char *result = (char*) calloc(sizeof(char),len);
    int pos = 0;

    // Actually concatenate strings
    va_start(ap, count);
    for(i=0 ; i<count ; i++)
    {
        char *s = va_arg(ap, char*);
        strcpy(result+pos, s);
        pos += strlen(s);
    }
    va_end(ap);

    return result;
}

void debugResult(const char* type, const char* result) {
	if (debug)
		printf("\nMATCH: %s (%s);\n", type, result);
}

void debugToken(const char* token) {
	if (debug)
		printf("\nTOKEN FOUND: %s;\n", token);
}


int yyerror(const char* errmsg)
{
	printf("\n*** Erro: %s\n", errmsg);
}
 
int yywrap(void) { return 1; }

void printFile(char *fileName) {
	int c;
	FILE *file;
	file = fopen(fileName, "r");
	if (file) {
		while ((c = getc(file)) != EOF)
			putchar(c);
		fclose(file);
	}
}

int parseMain(int argc, char** argv)
{
	printFile("header.html");
	putchar('\n');
	yyparse();
	printFile("footer.html");
	putchar('\n');
	return 0;
}