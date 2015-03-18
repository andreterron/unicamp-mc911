# Lab01 - Text to HTML translator

## Requirements

Na primeira etapa ser&aacute; implementado um parser que converter&aacute; um subconjunto de latex para html.

O cabe&ccedil;alho do arquivo latex &eacute; tudo que est&aacute; antes do \begin{document} e poder&aacute; conter:

* `\documentclass[...]{nomedocumento}`: dever&aacute; ser ignorado
* `\usepackage[...]{nomepacote}`: dever&aacute; ser ignorado
* `\title{titulo}`: ver sobre o comando \maketitle adiante
* `\author{nomeautor}`: dever&aacute; ser ignorado

O corpo do documento inicia-se com \begin{document} e encerra-se com \end{document} e poder&aacute; conter:

* textos
* texto sob o modo matem&aacute;tico
* comandos

No texto presente no corpo do documento, linhas em branco devem ser consideradas e inseridas no HTML gerado.

O modo matem&aacute;tico ser&aacute; demarcado somente pelo sinal cifr&atilde;o. Caso queira mostrar o caracter $ ao inv&eacute;s de entrar no modo matem&aacute;tico, ser&aacute; necess&aacute;rio digitar \$. O seu conte&uacute;do deve ser renderizado como o modo matem&aacute;tico do Latex (existem bibliotecas javascript que podem ajudar nesta parte).

Os comandos latex que poder&atilde;o estar inclu&iacute;dos no arquivo ser&atilde;o:

* `\maketitle`: mostra o t&iacute;tulo descrito pelo \title no cabe&ccedil;alho (voc&ecirc; pode supor que sempre que um \maketitle estiver presente, existe um \title tamb&eacute;m).
* `\textbf{texto1}`: aplica negrito em texto1
* `\textit{texto1}`: aplica it&aacute;lico em texto1
* `\begin{itemize} \item texto1 \item texto2 \end{itemize}`: gera uma lista n&atilde;o enumerada com os itens texto1 e texto2 (pode haver encadeamento de lista)
* `\includegraphics{figura1}`: mostrar a imagem figura1
* `\cite{ref1}`: adiciona o n&uacute;mero da refer&ecirc;ncia ref1, que se encontra na se&ccedil;&atilde;o de 'thebibliography'
* `\begin{thebibliography} \bibitem{ref1} Referencia 1 \bibitem{ref2} Referencia 2. \end{thebibliography}`: adiciona as refer&ecirc;ncias bibliogr&aacute;ficas; o corpo do documento poder&aacute; conter apenas uma se&ccedil;&atilde;o 'thebibliography'.

O projeto ser&aacute; implementado utilizando a ferramenta Flex e Bison. Note que a especifica&ccedil;&atilde;o do projeto est&aacute; consideravalmente livre, ent&atilde;o use sua criatividade para implementar o proposto.

## Synopsis

`./parse [<input.tex>] [-t|--token] [-d|--debug] [-o|--output <path/to/ouput.html>]`

The program will translate the tex file, which can be the `<input.tex>`
or the `stdin` if `<input.tex>` isn't specified, into a html file,
which can be specified with the --output option or the `stdout` if not specified.

## Options

* `-t, --token`	Will only display the tokens matched, no rules will be evaluated (for debug purposes)

* `-d, --debug`	Will activate debug prints

* `-o, --output <file.html>`	Places the output into <file.html>. If not present, it will be written to `stdout`
