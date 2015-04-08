# Ex02 - LLVM-IR bank account methods

## Requirements

Construam em LLVM-IR uma classe Conta, com as seguintes caracter&iacute;sticas:

* **atributo** num_conta: inteiro, n&atilde;o precisa inicializar
* **atributo** saldo: inteiro, n&atilde;o precisa inicializar
* **m&eacute;todo** sacar, que recebe o valor do saque. N&atilde;o precisa checar saldo negativo.
* **m&eacute;todo** depositar, que recebe o valor do dep&oacute;sito.
* **m&eacute;todo** consultar, que retorna o valor do saldo

Acrescente agora a classe Poupan&ccedil;a herdada de Conta com as seguintes caracter&iacute;sticas:

* **atributo** dia_rendimento: inteiro, n&atilde;o precisa inicializar
* **m&eacute;todo** atualizarSaldo, que recebe a taxa de rendimento (de 0 a 100) e atualiza o saldo.

Seu arquivo &ldquo;classes.s&rdquo; deve conter apenas a implementa&ccedil;&atilde;o dessas classes solicitadas. A fun&ccedil;&atilde;o &ldquo;main&rdquo;, necess&aacute;ria para a execu&ccedil;&atilde;o, ser&aacute; fornecida no conjunto de testes. Este pacote possui um &quot;main.s&quot; de exemplo e um &quot;Makefile&quot;. Adicione seu &quot;classes.s&quot; no mesmo diret&oacute;rio e use o comando &ldquo;make&rdquo;. O execut&aacute;vel resultante ser&aacute; &ldquo;banco&rdquo;. A sa&iacute;da esperada para esse &quot;main.s&quot; &eacute; 330.

### Observa&ccedil;&otilde;es:

* A fim de padronizar o nome das fun&ccedil;&otilde;es, utilize os nomes &ldquo;mangling&rdquo; que est&atilde;o declarados em &quot;main.s.&quot;
* Al&eacute;m das instru&ccedil;&otilde;es aritm&eacute;ticas conhecidas mul, sub e add, no m&eacute;todo atualizarSaldo deve-se utilizar a instru&ccedil;&atilde;o sdiv , que possui a sintaxe id&ecirc;ntica &agrave; mul .
* A entrada deste laborat&oacute;rio &eacute; apenas um arquivo denominado &quot;classes.s&quot; que cont&eacute;m c&oacute;digo em LLVM-IR.
* O Susy apenas receber&aacute; o pacote; n&atilde;o realizar&aacute; corre&ccedil;&otilde;es

## Running

	$ make
	$ ./banco

Build and execute the `banco` file. The expected result with the given files is `330`.
