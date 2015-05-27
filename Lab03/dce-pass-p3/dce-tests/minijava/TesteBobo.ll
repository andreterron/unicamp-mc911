@.formatting.string = private constant [4 x i8] c"%d\0A\00"

define i32 @main() {
entry0:
   br label %label1
label1: 
  %c = add i32 0, 0
  %a = add i32 0, 0
  %b = add i32 %a, 1
  %c2 = add i32 %b, %c
  %c3 = add i32 %b, %c2
  %c4 = add i32 %b, %c3
  %c5 = add i32 %b, %c4
  %a2 = mul i32 %b, 2
  
  ret i32 %c2

}
