# Ex02 - LLVM-IR bank account methods

## Requirements

Construam em LLVM-IR uma classe Conta, com as seguintes caracter&amp;iacute;sticas:

* *atributo* num_conta: inteiro, n&amp;atilde;o precisa inicializar
* *atributo* saldo: inteiro, n&amp;atilde;o precisa inicializar
* *m&amp;eacute;todo* sacar, que recebe o valor do saque. N&amp;atilde;o precisa checar saldo negativo.
* *m&amp;eacute;todo* depositar, que recebe o valor do dep&amp;oacute;sito.
* *m&amp;eacute;todo* consultar, que retorna o valor do saldo

Acrescente agora a classe Poupan&amp;ccedil;a herdada de Conta com as seguintes caracter&amp;iacute;sticas:

* *atributo* dia_rendimento: inteiro, n&amp;atilde;o precisa inicializar
* *m&amp;eacute;todo* atualizarSaldo, que recebe a taxa de rendimento (de 0 a 100) e atualiza o saldo.

Seu arquivo &amp;ldquo;classes.s&amp;rdquo; deve conter apenas a implementa&amp;ccedil;&amp;atilde;o dessas classes solicitadas. A fun&amp;ccedil;&amp;atilde;o &amp;ldquo;main&amp;rdquo;, necess&amp;aacute;ria para a execu&amp;ccedil;&amp;atilde;o, ser&amp;aacute; fornecida no conjunto de testes. Este pacote possui um &amp;quot;main.s&amp;quot; de exemplo e um &amp;quot;Makefile&amp;quot;. Adicione seu &amp;quot;classes.s&amp;quot; no mesmo diret&amp;oacute;rio e use o comando &amp;ldquo;make&amp;rdquo;. O execut&amp;aacute;vel resultante ser&amp;aacute; &amp;ldquo;banco&amp;rdquo;. A sa&amp;iacute;da esperada para esse &amp;quot;main.s&amp;quot; &amp;eacute; 330.

Observa&amp;ccedil;&amp;otilde;es:

* A fim de padronizar o nome das fun&amp;ccedil;&amp;otilde;es, utilize os nomes &amp;ldquo;mangling&amp;rdquo; que est&amp;atilde;o declarados em &amp;quot;main.s.&amp;quot;
* Al&amp;eacute;m das instru&amp;ccedil;&amp;otilde;es aritm&amp;eacute;ticas conhecidas mul, sub e add, no m&amp;eacute;todo atualizarSaldo deve-se utilizar a instru&amp;ccedil;&amp;atilde;o sdiv , que possui a sintaxe id&amp;ecirc;ntica &amp;agrave; mul .
* A entrada deste laborat&amp;oacute;rio &amp;eacute; apenas um arquivo denominado &amp;quot;classes.s&amp;quot; que cont&amp;eacute;m c&amp;oacute;digo em LLVM-IR.
* O Susy apenas receber&amp;aacute; o pacote; n&amp;atilde;o realizar&amp;aacute; corre&amp;ccedil;&amp;otilde;es


## Running

	$ make
	$ ./banco

Build and execute the `banco` file. The expected result with the given files is `330`.
