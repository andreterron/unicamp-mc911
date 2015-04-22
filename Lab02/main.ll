; ModuleID = 'teste.c'
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-redhat-linux-gnu"

; Function Attrs: nounwind uwtable
define i32 @main() #0 {
  %1 = alloca i32, align 4
  %x = alloca [10 x i32], align 16
  %y = alloca i32, align 4
  store i32 0, i32* %1
  %2 = getelementptr inbounds [10 x i32]* %x, i32 0, i64 1
  store i32 1, i32* %2, align 4
  store i32 4, i32* %y, align 4
  br label %3

; <label>:3                                       ; preds = %8, %0
  %4 = getelementptr inbounds [10 x i32]* %x, i32 0, i64 1
  %5 = load i32* %4, align 4
  %6 = load i32* %y, align 4
  %7 = icmp slt i32 %5, %6
  br i1 %7, label %8, label %12

; <label>:8                                       ; preds = %3
  %9 = getelementptr inbounds [10 x i32]* %x, i32 0, i64 1
  %10 = load i32* %9, align 4
  %11 = add nsw i32 %10, 1
  store i32 %11, i32* %9, align 4
  br label %3

; <label>:12                                      ; preds = %3
  ret i32 0
}

attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"clang version 3.5.0 (tags/RELEASE_350/final)"}
