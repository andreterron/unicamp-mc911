; ModuleID = 'BinarySearch.bc'

%class.BS = type { [6 x i8*], { i32, i32* }*, i32 }
%class.BinarySearch = type {}

@.formatting.string = private constant [4 x i8] c"%d\0A\00"

define i32 @main() {
entry0:

label1: ; preds = %entry0
  %c = add 0, 0
  %a = add 0, 0
  %b = add i32 %a, 1
  %c = add i32 %b, %c
  %a = mul i32 %b, 2
  
  br i1 %tmp6, label %label1, label %cont1
 
cont1: ; preds = %entry0
  ret i32 %c

}
