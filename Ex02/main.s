
%class.Poupanca = type { %class.Conta, i32 }
%class.Conta = type { i32, i32 }

@.str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Este exemplo:
; 1. aloca um objeto Poupanca
; 2. atribui 0 em num_conta
; 3. atribui 0 em saldo
; 4. deposita 500
; 5. saca 200
; 6. atualiza o saldo com o rendimento de 10%
; 7. consulta o saldo
; 8. Saida experada do printf: 330

define i32 @main() {
  %t1 = alloca %class.Poupanca
  %tmp1 = bitcast %class.Poupanca* %t1 to %class.Conta*
  %tmp2 = getelementptr %class.Conta* %tmp1, i32 0, i32 0
  store i32 0, i32* %tmp2
  %tmp3 = getelementptr %class.Conta* %tmp1, i32 0, i32 1
  store i32 0, i32* %tmp3
  call void @__depositar_conta(%class.Conta* %tmp1, i32 500)
  call void @__sacar_conta(%class.Conta* %tmp1, i32 200)
  call void @__atualizarSaldo_poupanca(%class.Poupanca* %t1, i32 10)
  %tmp4 = call i32 @__consultar_conta(%class.Conta* %tmp1)
  %tmp5 = call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @.str, i32 0, i32 0), i32 %tmp4)
  ret i32 0
}

; Nomes mangling para você utilizar no arquivo "classes.s"
declare void @__depositar_conta(%class.Conta*, i32)
declare void @__sacar_conta(%class.Conta*, i32)
declare i32  @__consultar_conta(%class.Conta*)
declare void @__atualizarSaldo_poupanca(%class.Poupanca*, i32)
declare i32  @printf(i8*, ...)







