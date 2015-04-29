
if [ -z $1 ]; then
  path=test/smaller
else
  path=$1
fi

FILES=$path/*

make
count=0
correct=0
echo "" > output.txt
echo "" > error.txt
for f in $FILES
do
  #echo "Processing $f file..."
  
  RES=0
  make run INPUT=$f OUTPUT=teste.s >> output.txt 2>> error.txt &&
  lli teste.s >> output.txt 2>> error.txt &&
  RES=1 && ((correct+=1))
  if [ $RES == 0 ]; then
	echo "TEST $f FAILED"
  fi
  # take action on each file. $f store current file name
  # cat $f
  ((count+=1))
  #if [ $count -ge 3 ]; then
  #  break
  #fi
done

echo "Correct = $correct/$count"