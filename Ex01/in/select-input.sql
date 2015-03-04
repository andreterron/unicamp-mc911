
CREATE TABLE tab1 (
	nome,
	endereco,
	cidade,
	profissao
);

CREATE TABLE tab2 (
	aluno,
	RA,
	curso
);

INSERT INTO tab1 VALUES (
	Roberto, Rua_1, Campinas, Engenheiro
);


INSERT INTO tab1 VALUES (
	Luiz, Rua_30, Indaiatuba, Arquiteto
);

SELECT * FROM tab1;

SELECT nome, cidade FROM tab1;

