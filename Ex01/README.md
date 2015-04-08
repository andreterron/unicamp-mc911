# Ex01 - Flex & Bison SQL Select

## Requirements

Dado o exemplo Flex e Bison com comandos SQL mostrado em sala e dispon&amp;iacute;vel na p&amp;aacute;gina da disciplina, implementar o m&amp;eacute;todo SELECT , com as poss&amp;iacute;veis varia&amp;ccedil;&amp;otilde;es:

	SELECT * FROM tab1; 			    //retorna o conte&amp;uacute;do de todas as colunas
	SELECT col1, col2, coln FROM tab1; 	//retorna o conte&amp;uacute;do das colunas na ordem solicitada


### Observa&amp;ccedil;&amp;otilde;es:

A entrada deste laborat&amp;oacute;rio deve ser um arquivo chamado 'sql.zip', contendo os arquivos do flex (.l), do bison (.y) e um Makefile capaz gerar o parser.
O execut&amp;aacute;vel deve ter o nome de parsersql e deve ser capaz de receber redirecionamento:

	./parsersql &amp;lt; input.sql 

Segue um exemplo de arquivo de entrada: select-input.sql ; e a sa&amp;iacute;da correspondente: select-output.sql
O Susy apenas receber&amp;aacute; o pacote; n&amp;atilde;o realizar&amp;aacute; corre&amp;ccedil;&amp;otilde;es


## Running

`./parsersql`

The program will read from `stdin` and execute each command it finds. The
tables will be written to files with the name of the table. For exmaple, the
command `CREATE TABLE example (...)` will create a new file named `example`.

