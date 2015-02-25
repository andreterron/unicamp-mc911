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
	int  *intval;
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

%token T_DOCUMENT_CLASS
%token T_USE_PACKAGE 
%token T_AUTHOR

%token T_EOF

%token <str> T_CHAR
%type  <str> file text
%%



/* Grammar rules */

//text: T_CHAR;

file: text {
	printf("<!DOCTYPE html>\n");
	printf("<html>\n\t<head>\n\t</head>\n\t<body>\n");
	printf("%s", $1);
	printf("\t</body>\n</html>\n");
};

text: T_CHAR {$$ = $1;} | T_CHAR text {
	$$ = concat(2, $1, $2);
};


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
 
int main(int argc, char** argv)
{
     yyparse();
     return 0;
}