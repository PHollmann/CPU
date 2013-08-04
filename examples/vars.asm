#Sample program to show the use of variables
#use LDD to access data from a variable
.int start
start 10
LDD R1 start
ADD R1 R1 R1
CPY R1 start
LDD R2 start
SUB R1 R1 R1
OUT R1
OUT R2