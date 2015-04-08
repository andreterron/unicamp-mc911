

SRC=$(wildcard *.s)
OBJ=$(SRC:.s=.o)

compile: $(OBJ)
	gcc *.o -o banco

%.o: %.s
	llc $< -o $(basename $<).asm
	as $(basename $<).asm -o $@
	
clean:	
	rm *.asm *.o banco


