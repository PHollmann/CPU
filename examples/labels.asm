#Sample program to show the use of labels
#based on the variables example
#use LDD to access data from a variable
#expected output is 0 20 40
.int start
start 10
LDD R1 start
ADD R1 R1 R1
CPY R1 start
LDD R2 start
SUB R1 R1 R1
OUT R1
OUT R2
JMP lord
end: OUT R1
jmp ende
lord: ADD R1 R2 R2
jmp end
ende: NOP
