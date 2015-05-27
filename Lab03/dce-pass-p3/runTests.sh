
if [ -z $1 ]; then
  path=dce-tests/minijava/*
elif [ "$1" == "-b" ]; then
  path=dce-tests/minijava/*
else
  path=$1
fi

FILES=$path

make
count=0
correct=0
echo "" > output.txt
echo "" > error.txt
echo "" > diff.txt
for f in $FILES
do
  echo "------------------------------" >> output.txt
  echo "| Processing $f" >> output.txt
  echo "------------------------------" >> output.txt
  echo "------------------------------" >> error.txt
  echo "| Processing $f" >> error.txt
  echo "------------------------------" >> error.txt
  echo "------------------------------" >> diff.txt
  echo "| Processing $f" >> diff.txt
  echo "------------------------------" >> diff.txt
  filepath=$(basename "${f%.*}")
  out=out/$filepath.opt.ll
  RES=0
  make testLive NAME=$filepath 2>> error.txt &&
  make testDefUse NAME=$filepath 2>> error.txt &&
  diff -u dce-tests/output/liveness/$filepath.opt.ll dce-tests/output/defuse/$filepath.opt.ll >> diff.txt
  echo "----- Running test now -----" >> output.txt
  echo "----- Running test now -----" >> error.txt
  #echo "----- Running test now -----"
  #lli $out >> output.txt 2>> error.txt &&
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
