%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

char *concat(int count, ...);

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
%type  <str> text text2 whitespace word

%%

/* Grammar rules */

text		: word text2;

text2		: /* empty */			{ $$ = ""; }
			| whitespace word text2 { $$ = concat(3, $1, $2, $3); }
			;

whitespace	: T_WHITESPACE	{ $$ = $1; }
			;

word		: /* empty */ { $$ = ""; }
			| T_CHAR word { $$ = concat(2, $1, $2); }
			;


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

int main(int argc, char** argv)
{
	 printFile("header.html");
	 putchar('\n');
     yyparse();
	 printFile("footer.html");
	 putchar('\n');
     return 0;
}