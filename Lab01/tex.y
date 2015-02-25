%{

%}

/* Bison Declarations */

%union {
    char *str;
    int intval;
}

%token <inteval> T_DIGIT
%token <str> T_STRING


%%


/* Grammar rules */

stmt: T_STRING '=' T_DIGIT { printf{"%s"}, $1 }

text: T_WORD text


%%