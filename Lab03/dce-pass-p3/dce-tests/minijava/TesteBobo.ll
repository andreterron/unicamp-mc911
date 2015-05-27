@.formatting.string = private constant [4 x i8] c"%d\0A\00"

define i32 @main() {
entry0:
   br label %label1
label1: 
  %c = add i32 0, 0
  %a = add i32 0, 0
  %b = add i32 %a, 1
  %c2 = add i32 %b, %c
  %a2 = mul i32 %b, 2
  
  %a3 = add i1 0, 0
  br i1 %a3, label %label1, label %cont1
 
cont1:
  ret i32 %c2

}
