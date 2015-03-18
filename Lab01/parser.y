%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

char *concat(int count, ...);
void debugResult(const char* type, const char* result);
void fPrintFile(char *fileName, FILE *output);

int debug = 0;
int bib_id = 0;
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
%type  <str> text whitespace word char graphic file command skip_blank whitespaces body bold italic maketitle item items list text2 trim_text bib bib_item bib_items cite
%start file


%%

/* Grammar rules */
file		: skip_blank header T_BEGIN_DOC skip_blank body T_END_DOC skip_blank {
					FILE *T = fopen("teste.html", "w");

					// Imprime os headers no arquivo html
					fPrintFile("header.html", T);
               
					// Imprime o body no arquivo html
					fprintf(T, "<p align=\"justify\">%s\n</p>", $5);
					
					// Imprime o footer no arquivo html
					fPrintFile("footer.html", T);
					
					fclose(T);
				};

/* Header */

header		: title skip_blank				{ debugResult("header", "");}
			;

title		: T_TITLE '{' trim_text '}'		{ savedTitle = strdup($3); debugResult("title", $3);}//   }
			;

/* Body */

body		: /**/							{ $$ = ""; }
			| text							{ $$ = $1; }
			| command skip_blank body		{ $$ = concat(3, $1, $2, $3); }
			| text command skip_blank body	{ $$ = concat(4, $1, $2, $3, $4); }
			;


command		: maketitle						{ $$ = $1; }
			| list							{ $$ = $1; debugResult("command list", $1);}
			| cite							{ $$ = $1; }
			| bib							{ $$ = $1; }//debugResult("command list", $1);}
			| graphic						{ $$ = $1; }//debugResult("command graphic", $1);}
			| bold							{ $$ = $1; }//debugResult("command bold", $1); }
			| italic						{ $$ = $1; }//debugResult("command italic", $1); }
			| '{' trim_text '}'				{ $$ = concat(3, "{", $2, "}"); debugResult("command", $$); }
			;

cite		: T_CITE '{' trim_text '}'  	{ $$ = concat(3, "<a class=\"cite\" data-ref=\"", $3, "\"></a>"); }
			;

bib			: T_BEGIN_BIB skip_blank bib_items T_END_BIB { $$ = concat(3, "<h1>References</h1><ol start=\"0\">", $3, "</ol>"); debugResult("bib", $3); }
			;

bib_items	: bib_item						{ $$ = $1; }
			| bib_items bib_item			{ $$ = concat(2, $1, $2); }
			;

bib_item	: T_BIB_ITEM '{' trim_text '}' trim_text		{
					char id[100];
					sprintf(id, "%d", bib_id++);
					$$ = concat(7, "<li id=\"", $3 ,"\" data-id=\"", id, "\">", $5, "</li>"); debugResult("bib_item", $5);
				}
			;

maketitle	: T_MAKETITLE					{ $$ = concat(3, "<h1 align=\"center\">", savedTitle, "</h1>"); debugResult("maketitle", savedTitle);}
			;

list		: T_BEGIN_ITEM whitespaces items T_END_ITEM { $$ = concat(3, "<ul>", $3, "</ul>"); debugResult("list", $3); }
			;

items		: item							{ $$ = $1; }
			| items item					{ $$ = concat(2, $1, $2); }
			;

item		: T_ITEM trim_text				{ $$ = concat(3, "<li>", $2, "</li>"); debugResult("item", $2);}
			| list skip_blank				{ $$ = $1; }
			;

graphic		: T_GRAPHIC '{' trim_text '}'	{ $$ = concat(3, "<img src=\"", $3, "\"/>"); }
			;

bold		: T_BOLD '{' trim_text '}'		{ $$ = concat(3, "<b>", $3, "</b>");}
			;

italic		: T_ITALIC '{' trim_text '}'	{ $$ = concat(3, "<i>", $3, "</i>");}
			;

trim_text	: skip_blank text				{ $$ = concat(2, $1, $2); debugResult("trim_text", $$);}
			;

text		: word text2					{ $$ = concat(2, $1, $2); }//debugResult("text", $$);}
			;

text2		: whitespaces word text2		{ $$ = concat(3, $1, $2, $3);}
			| whitespaces					{ $$ = $1;}
			| /* empty */					{ $$ = "";}
			;

skip_blank	: /* empty */					{ $$ = ""; }// debugResult("skip_blank", "");}
			| whitespaces					{ $$ = $1; }//debugResult("skip_blank", $1);}
			;

whitespaces : whitespace					{ $$ = $1; }//debugResult("skip_blank", $1);}
			| whitespaces whitespace		{ $$ = concat(2, $1, $2); }//debugResult("skip_blank", $$);}
			;

whitespace	: T_WHITESPACE					{ $$ = " ";}
			| T_BREAK						{ $$ = "<br/>\n";}
			;

word		: char
			| word char						{ $$ = concat(2, $1, $2); }
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
	yyparse();
	return 0;
}
