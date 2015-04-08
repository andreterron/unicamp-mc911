# Ex01 - Flex & Bison SQL Select

## Requirements

Dado o exemplo Flex e Bison com comandos SQL mostrado em sala e dispon&iacute;vel na p&aacute;gina da disciplina, implementar o m&eacute;todo SELECT , com as poss&iacute;veis varia&ccedil;&otilde;es:

	SELECT * FROM tab1; 			    //retorna o conte&uacute;do de todas as colunas
	SELECT col1, col2, coln FROM tab1; 	//retorna o conte&uacute;do das colunas na ordem solicitada


### Observa&ccedil;&otilde;es:

A entrada deste laborat&oacute;rio deve ser um arquivo chamado 'sql.zip', contendo os arquivos do flex (.l), do bison (.y) e um Makefile capaz gerar o parser.
O execut&aacute;vel deve ter o nome de parsersql e deve ser capaz de receber redirecionamento:

	./parsersql < input.sql 

Segue um exemplo de arquivo de entrada: select-input.sql ; e a sa&iacute;da correspondente: select-output.sql
O Susy apenas receber&aacute; o pacote; n&atilde;o realizar&aacute; corre&ccedil;&otilde;es


## Running

`./parsersql`

The program will read from `stdin` and execute each command it finds. The
tables will be written to files with the name of the table. For exmaple, the
command `CREATE TABLE example (...)` will create a new file named `example`.

