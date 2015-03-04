%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

struct t_col_list {
	char *name;
	struct t_col_list *prev;
	int index;
	int order;
};

typedef struct t_col_list column;

char *concat(int count, ...);
char *selectFromTable(char *table);
char *selectCols(char *table, column *col);
char *colListToString(column *col);
column *createColumn(char *name, column *list);


%}
 
%union{
	char *str;
	int  *intval;
	column *list_col;
}

%token <str> T_STRING
%token T_SELECT
%token T_FROM
%token T_CREATE
%token T_TABLE
%token T_INSERT
%token T_INTO
%token T_VALUES

%type <str> create_stmt insert_stmt col_list  values_list select_stmt
%type <list_col> select_col_list

%start stmt_list

%error-verbose
 
%%

stmt_list: 	stmt_list stmt 
	 |	stmt 
;

stmt:
		create_stmt ';'	{printf("%s",$1);}
	|	insert_stmt ';'	{printf("%s",$1);}
	|	select_stmt ';' {printf("%s",$1);}

;

select_stmt:
		T_SELECT '*' T_FROM T_STRING		{
								$$ = concat(5, "\nSELECT '*' FROM ", $4, ":\n", selectFromTable($4), "\n");
								}
	|	T_SELECT select_col_list T_FROM T_STRING	{ $$ = concat(7, "\nSELECT ", colListToString($2), " FROM ", $4, ":\n", selectCols($4, $2), "\n\n"); }
;

create_stmt:
	   T_CREATE T_TABLE T_STRING '(' col_list ')' 	{	FILE *F = fopen($3, "w"); 
								fprintf(F, "%s\n", $5);
								fclose(F);
								$$ = concat(5, "\nCREATE TABLE: ", $3, "\nCOL_NAME: ", $5, "\n\n");
							}
;


select_col_list:
		T_STRING 		{ $$ = createColumn($1, NULL); }
	| 	select_col_list ',' T_STRING 	{ $$ = createColumn($3, $1); }
;

col_list:
		T_STRING 		{ $$ = $1; }
	| 	col_list ',' T_STRING 	{ $$ = concat(3, $1, ";", $3); }
;


insert_stmt:
	   T_INSERT T_INTO T_STRING T_VALUES '(' values_list ')' { FILE *F = fopen($3, "a"); 
								  fprintf(F, "%s\n", $6);
								  fclose(F);
								  $$ = concat(5, "\nINSERT INTO TABLE: ", $3, "\nVALUES: ", $6, "\n\n");
							 	}
;

values_list:
		T_STRING 		{ $$ = $1; }
	| 	col_list ',' T_STRING 	{ $$ = concat(3, $1, ";", $3); }
;


 
%%

column *createColumn(char *name, column *list) {
	column *c = malloc(sizeof(column));
	c->name = name;
	c->prev = list;
	if (list == NULL) {
		c->index = 0;
	} else {
		c->index = list->index + 1;
	}
	return c;
}

char *selectCols(char *table, column *col) {
	char line[1024];
	char * result = "", *line_res, *res, *token;
	char s[4] = ";\n";
	char *saveptr;
	int order = 0;
	FILE *file = fopen(table, "r");
	column *aux;
	
	// READS THE COLUMNS
	
	fgets(line, 1024, file);
	
	token = strtok(line, s);
	
	/* walk through other tokens */
	while( token != NULL ) 
	{
		//printf( " %s\n", token );
		aux = col;
		while (aux != NULL) {
			if (!strcmp(token, aux->name))
				aux->order = order;
			aux = aux->prev;
		}
		order++;
		token = strtok(NULL, s);
	}
	
	while (fgets(line, 1024, file) != NULL) {
	
		line_res = NULL;
		for (aux = col; aux != NULL; aux = aux->prev) {
		
			/* get the first token */
			token = strtok_r(line, s, &saveptr);
			
			/* walk through other tokens */
			for (order = 0; order < aux->order && token != NULL; order++) 
			{
				token = strtok_r(NULL, s, &saveptr);
			}
			
			// found token in this order
			res = token;
			
			// concats result
			if (line_res == NULL) {
				line_res = concat(1, res);
			} else {
				line_res = concat(3, res, ";", line_res);
			}
		}
		result = concat(3, result, line_res, "\n");
		
	}
	fclose(file);
	return result;
}

char *colListToString(column *col) {
	char *result = NULL;
	while (col != NULL) {
		if (result == NULL) {
			result = col->name;
		} else {
			result = concat(3, col->name, ", ", result);
		}
		col = col->prev;
	}
	return result;
}

char *selectFromTable(char *table) {
	char line[1024];
	char * result = "";
	FILE *file = fopen(table, "r");
	
	fgets(line, 1024, file);
	while (fgets(line, 1024, file) != NULL) {
		result = concat(2, result, line);
	}
	fclose(file);
	return result;
}

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


