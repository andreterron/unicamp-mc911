@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = xor i1 true, true
  br i1 %tmp1, label %true7, label %end7
true7:
  %tmp2 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp3 = call i32 (i8 *, ...)* @printf(i8 * %tmp2, i32 1)
  %tmp4 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp5 = call i32 (i8 *, ...)* @printf(i8 * %tmp4, i32 2)
  br label %end7
end7:
  %tmp6 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp7 = call i32 (i8 *, ...)* @printf(i8 * %tmp6, i32 3)
  %tmp8 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp9 = call i32 (i8 *, ...)* @printf(i8 * %tmp8, i32 4)
  %tmp10 = load i32 * %tmp0
  ret i32 %tmp10
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
