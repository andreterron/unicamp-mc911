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

%token <str> T_CHAR T_WHITESPACE T_BREAK
%type  <str> text whitespace word char element graphic file command skip_blank header title body bold italic

%%

/* Grammar rules */

/*** Quebrei em dois, um com e outro sem o maketitle.. O com maketitle, imprimi o titulo, o
sem nao faz nada. 

Tentei adaptar o seu printFile do header e footer pra um *readFile* e ficar mais bonitinho, mas nao consegui haha 
se quiser tentar pra deixar mais organizado, mas nao eh necessario, funciona feio  assim hehe
***/
file		: header skip_blank T_BEGIN_DOC skip_blank body skip_blank T_END_DOC skip_blank {
				   // Sem maketitle
               FILE *T = fopen("teste.html", "w");
               FILE *H = fopen("header.html", "r");
               FILE *F = fopen("footer.html", "r");
               char header[1000], footer[1000], linha[1000];
               strcpy(header, "");
               strcpy(footer, "");

               // Le header 
               fscanf(H, "%s", header);
               strcat(header, "\n"); 
					while(fscanf(H,"%s",linha) == 1)  
               {  
                  strcat(header, linha);
                  strcat(header, "\n"); 
               }
              
               // Le footer                
               fscanf(F, "%s", footer);
               strcat(footer, "\n"); 
					while(fscanf(F,"%s",linha) == 1)  
               {  
                  strcat(footer, linha);
                  strcat(footer, "\n"); 
               }
               
               // Salva no arquivo html
/*** Meti esse alinhamento justificado pra ficar igual o tex ***/
					fprintf(T, "%s\n<p align=\"justify\">%s\n</p>%s", header, $5, footer);
					
					fclose(T);
					fclose(H);
					fclose(F);
/***	Antiga impressao direta no terminal				printf("%s\n", $1, $5); ***/
				} |
				header skip_blank T_BEGIN_DOC skip_blank T_MAKETITLE skip_blank body skip_blank T_END_DOC skip_blank {
				   // Com maketitle
               FILE *T = fopen("teste.html", "w");
               FILE *H = fopen("header.html", "r");
               FILE *F = fopen("footer.html", "r");
               char header[1000], footer[1000], linha[1000];
               strcpy(header, "");
               strcpy(footer, "");

               // Le header 
               fscanf(H, "%s", header);
               strcat(header, "\n"); 
					while(fscanf(H,"%s",linha) == 1)  
               {  
                  strcat(header, linha);
                  strcat(header, "\n"); 
               }
              
               // Le footer                
               fscanf(F, "%s", footer);
               strcat(footer, "\n"); 
					while(fscanf(F,"%s",linha) == 1)  
               {  
                  strcat(footer, linha);
                  strcat(footer, "\n"); 
               }
               
               // Salva no arquivo html]
/*** Meti esse alinhamento justificado pra ficar igual o tex ***/
					fprintf(T, "%s%s\n<p align=\"justify\">\n%s\n</p>%s", header, $1, $7, footer);
					
					fclose(T);
					fclose(H);
					fclose(F);
/***	Antiga impressao direta no terminal				printf("%s\n%s\n", $1, $5); ***/
				}
			;

/* Header */

header		: skip_blank title			{ $$ = $2; debugResult("header", $2);}
			;

title		: T_TITLE '{' text '}'		{ $$ = concat(3, "<h1 align=\"center\">", $3, "</h1>"); debugResult("title", $3); }
			;

/* Body */

body		: element 					{ $$ = $1;}
			| body skip_blank element	{ $$ = concat(3, $1, $2, $3);}
			;


element		: word						{ $$ = $1; debugResult("word", $1);}
			| command					{ $$ = $1; }
			;


command		: graphic					{ $$ = $1; debugResult("command graphic", $1);}
			| bold						{ $$ = $1; debugResult("command bold", $1); }
   		| italic						{ $$ = $1; debugResult("command italic", $1); }
			| '{' text '}'				{ $$ = concat(3, "{", $2, "}"); debugResult("command", $$); }
   		;

graphic		: T_GRAPHIC '{' text '}'	{ $$ = concat(3, "<img src=\"", $3, "\"/>"); }
			;

bold		: T_BOLD '{' text '}'		{ $$ = concat(3, "<b>", $3, "</b>");}
			;
			
italic	: T_ITALIC '{' text '}'		{ $$ = concat(3, "<i>", $3, "</i>");}
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
