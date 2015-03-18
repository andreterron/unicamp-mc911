%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

char *concat(int count, ...);
void debugResult(const char* type, const char* result);
void fPrintFile(char *fileName, FILE *output);

int debug = 0;
char *savedTitle = NULL;

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

%token <str> T_CHAR T_WHITESPACE T_BREAK
%type  <str> text whitespace word char element graphic file command skip_blank whitespaces body bold italic maketitle
%start file
//content item items list 

%%

/* Grammar rules */

/*** Quebrei em dois, um com e outro sem o maketitle.. O com maketitle, imprimi o titulo, o
sem nao faz nada. 

Tentei adaptar o seu printFile do header e footer pra um *readFile* e ficar mais bonitinho, mas nao consegui haha 
se quiser tentar pra deixar mais organizado, mas nao eh necessario, funciona feio  assim hehe

Ele salva num arquivo teste.html o codigo agora
***/
file		: skip_blank header T_BEGIN_DOC skip_blank body skip_blank T_END_DOC skip_blank {
				   // Sem maketitle
               FILE *T = fopen("teste.html", "w");

				fPrintFile("header.html", T);
               
               // Salva no arquivo html
/*** Meti esse alinhamento justificado pra ficar igual o tex ***/
				fprintf(T, "<p align=\"justify\">%s\n</p>", $5);
					
				fPrintFile("footer.html", T);
				fclose(T);
/***	Antiga impressao direta no terminal				printf("%s\n", $1, $5); ***/
				};

/* Header */

header		: title skip_blank			{ debugResult("header", "");}
			;

title		: T_TITLE '{' text '}'		{ savedTitle = strdup($3); debugResult("title", $3);}//   }
			;

/* Body */

body		: element 					{ $$ = $1;}
			| body whitespaces element	{ $$ = concat(3, $1, $2, $3);}
			;


element		: word						{ $$ = $1; debugResult("word", $1);}
			| command					{ $$ = $1; }
			;


command		: maketitle					{ $$ = $1; }
			//| list						{ $$ = $1; debugResult("command list", $1);}
			| graphic					{ $$ = $1; debugResult("command graphic", $1);}
			| bold						{ $$ = $1; debugResult("command bold", $1); }
			| italic					{ $$ = $1; debugResult("command italic", $1); }
			| '{' text '}'				{ $$ = concat(3, "{", $2, "}"); debugResult("command", $$); }
			;


maketitle	: T_MAKETITLE				{ $$ = concat(3, "<h1 align=\"center\">", savedTitle, "</h1>"); debugResult("maketitle", savedTitle);}
			;

/*list		: T_BEGIN_ITEM skip_blank items T_END_ITEM { $$ = concat(3, "<ul>", $3, "</ul>"); debugResult("list", $3); }
			;

items		: item						{ $$ = $1; }
			| items item		{ $$ = concat(2, $1, $2); }
			;

item		: T_ITEM content skip_blank	{ $$ = concat(3, "<li>", $2, "</li>"); debugResult("item", $2);}
			;

content		: skip_blank text	{ $$ = $2; debugResult("content", $2); }
			;*/

graphic		: T_GRAPHIC '{' text '}'	{ $$ = concat(3, "<img src=\"", $3, "\"/>"); }
			;

bold		: T_BOLD '{' text '}'		{ $$ = concat(3, "<b>", $3, "</b>");}
			;

italic		: T_ITALIC '{' text '}'		{ $$ = concat(3, "<i>", $3, "</i>");}
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
			| whitespaces				{ $$ = $1; debugResult("skip_blank", $1);}
			;
			
whitespaces : whitespace				{ $$ = $1; debugResult("skip_blank", $1);}
			| whitespaces whitespace	{ $$ = concat(2, $1, $2); debugResult("skip_blank", $$);}
			;

whitespace	: T_WHITESPACE				{ $$ = " ";}
//              | T_BREAK T_BREAK     { $$ = "<br>\n";}
//              | T_BREAK             { $$ = " \n";}
			| T_BREAK     { $$ = "<BR>\n";}
			;

word		: char
			| word char					{ $$ = concat(2, $1, $2); }
			;

char		: T_CHAR
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

void fPrintFile(char *fileName, FILE *output) {
	int c;
	FILE *file;
	file = fopen(fileName, "r");
	if (file) {
		while ((c = getc(file)) != EOF)
			fputc(c, output);
		fclose(file);
	}
}

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
/*** Passei esses  prints pra dentro de um arquivo .html de uma vez ***/
//	printFile("header.html");
//	putchar('\n');
	yyparse();
//	printFile("footer.html");
//	putchar('\n');
	return 0;
}
