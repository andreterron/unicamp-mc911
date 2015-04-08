%class.Poupanca = type { %class.Conta, i32 }
%class.Conta = type { i32, i32 }

define void @__depositar_conta(%class.Conta* %this, i32 %value) {
	%saldo_addr = getelementptr %class.Conta* %this, i32 0, i32 1
	%saldo = load i32* %saldo_addr
	%tmp1 = add i32 %saldo, %value
	store i32 %tmp1, i32 * %saldo_addr
	ret void
}
define void @__sacar_conta(%class.Conta* %this, i32 %value) {
	%saldo_addr = getelementptr %class.Conta* %this, i32 0, i32 1
	%saldo = load i32*%saldo_addr
	%tmp1 = sub i32 %saldo, %value
	store i32 %tmp1, i32 * %saldo_addr
	ret void
}
define i32  @__consultar_conta(%class.Conta* %this) {
	%saldo_addr = getelementptr %class.Conta* %this, i32 0, i32 1
	%saldo = load i32* %saldo_addr
	ret i32 %saldo
}
define void @__atualizarSaldo_poupanca(%class.Poupanca* %this, i32 %value) {
	%super = getelementptr %class.Poupanca* %this, i32 0, i32 0
	%saldo_addr = getelementptr %class.Conta* %super, i32 0, i32 1
	%saldo = load i32*%saldo_addr
	%t1 = mul i32 %saldo, %value
	%t2 = sdiv i32 %t1, 100
	%t3 = add i32 %saldo, %t2
	store i32 %t3, i32 * %saldo_addr
	ret void
}