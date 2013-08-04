
import java.util.ArrayList;


public class CPU {
	Instruction NOP=new Instruction();
	Instruction ADD;
	Instruction SUB;
	Instruction STOP;
	Instruction LOAD;
	Instruction LDD;
	Instruction LDK;
	Instruction WK;
	Instruction CPY;
	Instruction OUT;
	Instruction CMP;
	Instruction JMP;
	Instruction JPE;
	Instruction JNE;
	Instruction MOD;
	Instruction DIV;
	Instruction MOV;
	Instruction JPG;
	Instruction MUL;
	public Instruction[] instructions;
	ArrayList<String> console;
	private int line;
	
	private final int DBG=0;
	
	RegisterFile regs=new RegisterFile();
	
	public CPU()
	{
		initInstructions();
		instructions=new Instruction[]{NOP,ADD,SUB,MOD,DIV,LOAD,LDK,LDD,WK,CPY,OUT,CMP,JMP,JPE,JNE,STOP,MOV,JPG,MUL};
		regs.pc=0;
		line=0;
	}
	
	private void initInstructions()
	{
		ADD=new Instruction("Add Two Integer Registers","ADD",4);
		ADD.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg0]=regs.int_Regs[arg1]+regs.int_Regs[arg2];
				regs.pc+=4;
			}
		});
		
		SUB=new Instruction("Substract Two Integer Registers","SUB",4);
		SUB.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg0]=regs.int_Regs[arg1]-regs.int_Regs[arg2];
				regs.pc+=4;
			}
		});
		MOD=new Instruction("Modulo","MOD",4);
		MOD.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg0]=regs.int_Regs[arg1]%regs.int_Regs[arg2];
				regs.pc+=4;
			}
		});
		DIV=new Instruction("Integer Division","DIV",4);
		DIV.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg0]=regs.int_Regs[arg1]/regs.int_Regs[arg2];
				regs.pc+=4;
			}
		});
		MOV=new Instruction("Copy into Second Register","MOV",3);
		MOV.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg1]=regs.int_Regs[arg0];
				regs.pc+=3;
			}
		});
		STOP=new Instruction("Stop the processor","STOP",1);
		STOP.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.status=255;
			}
		});
		LOAD=new Instruction("Load into First Register from Adress at Second integer Register","LOAD",3);
		LOAD.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int adress=regs.int_Regs[arg1];
				regs.int_Regs[arg0]=RAM[adress]<<24|RAM[adress]<<16|RAM[adress]<<8|RAM[adress];
				regs.pc+=3;
			}
		});
		LDK=new Instruction("Load Constant to Register","LDK",6);
		LDK.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int pc=regs.pc;
				Byte[] parts=new Byte[]{RAM[pc+2],RAM[pc+3],RAM[pc+4],RAM[pc+5]};
				regs.int_Regs[arg0]=byteToInt(parts);
				regs.pc+=6;
			}
		});
		LDD=new Instruction("Load from Adress","LDD",6);
		LDD.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int pc=regs.pc;
				Byte[] parts=new Byte[]{RAM[pc+2],RAM[pc+3],RAM[pc+4],RAM[pc+5]};
				int adress=byteToInt(parts);
				parts[0]=RAM[adress];
				parts[1]=RAM[adress+1];
				parts[2]=RAM[adress+2];
				parts[3]=RAM[adress+3];
				regs.int_Regs[arg0]=byteToInt(parts);
				regs.pc+=6;
			}
		});
		WK=new Instruction("Write Constant to Adress in Register","WK",6);
		WK.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int pc=regs.pc;
				Byte[] parts=new Byte[]{RAM[pc+2],RAM[pc+3],RAM[pc+4],RAM[pc+5]};
				int adress=regs.int_Regs[arg0];
				//System.err.println(adress);
				RAM[adress]=parts[0];
				RAM[adress+1]=parts[1];
				RAM[adress+2]=parts[2];
				RAM[adress+3]=parts[3];
				regs.int_Regs[arg0]=byteToInt(parts);
				regs.pc+=6;
			}
		});
		CPY=new Instruction("Copy Register to RAM","CPY",6);
		CPY.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int pc=regs.pc;
				Byte[] parts=new Byte[]{RAM[pc+2],RAM[pc+3],RAM[pc+4],RAM[pc+5]};
				int adress=byteToInt(parts);
				parts[0]=(byte)(regs.int_Regs[arg0]>>>24);
				parts[1]=(byte)(regs.int_Regs[arg0]>>>16);
				parts[2]=(byte)(regs.int_Regs[arg0]>>>8);
				parts[3]=(byte)(regs.int_Regs[arg0]);
				RAM[adress]=parts[0];
				RAM[adress+1]=parts[1];
				RAM[adress+2]=parts[2];
				RAM[adress+3]=parts[3];
				regs.pc+=6;
			}
		});
		OUT=new Instruction("Output Register to Terminal","OUT",2);
		OUT.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				console.add(new Integer(regs.int_Regs[arg0]).toString());
				regs.pc+=2;
			}
		});
		CMP=new Instruction("Compare two Registers","CMP",3);
		CMP.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int a=regs.int_Regs[arg0];
				int b=regs.int_Regs[arg1];
				if (a==b)
				{
					regs.compareResult=0;
				}
				else
				{
					if (a>b)
					{
						regs.compareResult=1;
					}
					else
					{
						regs.compareResult=2;
					}
				}
				regs.pc+=3;
			}
		});
		JMP=new Instruction("Jump to Adress","JMP",5);
		JMP.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				int pc = regs.pc;
				Byte[] parts=new Byte[]{RAM[pc+1],RAM[pc+2],RAM[pc+3],RAM[pc+4]};
				regs.pc=byteToInt(parts);
			}
		});
		JPE=new Instruction("Jump if equal","JPE",5);
		JPE.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				if (regs.compareResult==0)
				{
					int pc = regs.pc;
					Byte[] parts=new Byte[]{RAM[pc+1],RAM[pc+2],RAM[pc+3],RAM[pc+4]};
					regs.pc=byteToInt(parts);
				}
				else
				{
					regs.pc+=5;
				}
			}
		});
		JNE=new Instruction("Jump if not equal","JNE",5);
		JNE.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				if (regs.compareResult!=0)
				{
					int pc = regs.pc;
					Byte[] parts=new Byte[]{RAM[pc+1],RAM[pc+2],RAM[pc+3],RAM[pc+4]};
					regs.pc=byteToInt(parts);
				}
				else
				{
					regs.pc+=5;
				}
			}
		});
		JPG=new Instruction("Jump if greater","JPG",5);
		JPG.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				if (regs.compareResult==1)
				{
					int pc = regs.pc;
					Byte[] parts=new Byte[]{RAM[pc+1],RAM[pc+2],RAM[pc+3],RAM[pc+4]};
					regs.pc=byteToInt(parts);
				}
				else
				{
					regs.pc+=5;
				}
			}
		});
		MUL=new Instruction("Multiply Two Integer Registers","MUL",4);
		MUL.setExecute(new Execution(){
			void execute(final RegisterFile regs, final Byte[] RAM, final int arg0, final int arg1, final int arg2)
			{
				regs.int_Regs[arg0]=regs.int_Regs[arg1]*regs.int_Regs[arg2];
				regs.pc+=4;
			}
		});
	}
	
	public String readline()
	{
		if (line<console.size())
		{
			line++;
			return console.get(line-1);
		}
		else
		{
			return null;
		}
	}
	
	private int byteToInt(Byte[] b)
	{
		return (b[0]&0xFF)<<24|(b[1]&0xFF)<<16|(b[2]&0xFF)<<8|(b[3]&0xFF);
	}
	
	public void execute(Byte[] RAM)
	{
		regs.pc=0;
		line=0;
		console=new ArrayList<String>();
		while (regs.status==0)
		{
			step(RAM);
			//System.out.println(regs.pc);
			for (int i=0;i<32;i++)
			{
				debug(RegisterFile.int_names[i]+":"+new Integer(regs.int_Regs[i]).toString()+"; ");
				if ((i+1)%8==0)
				{
					debug("\n");
				}
			}
			for (int i=0;i<RAM.length;i++)
			{
				debug((int)(RAM[i]&0xFF)+";");
				if ((i+1)%16==0)
				{
					debug("\n");
				}
			}
		}
	}
	
	public void step(Byte[] RAM)
	{
		if (regs.pc>=RAM.length)
		{
			regs.status=1;
		}
		else
		{
			int pc=regs.pc;
			int instr=RAM[pc];
			int offset=instructions[instr].getLength();
			//System.out.print(instructions[instr].getSymbol()+" ");
			debug(instructions[instr].getSymbol()+" ");
			switch (offset)
			{
				case 0:
				{
					regs.status=2;
					break;
				}
				case 1:
				{
					instructions[instr].execute(regs, RAM, 0,0,0);
					//System.out.print("\n");
					debug("\n");
					break;
				}
				case 2:
				{
					instructions[instr].execute(regs, RAM ,RAM[pc+1],0,0);
					//System.out.println(RegisterFile.int_names[RAM[pc+1]]+";");
					debug(RegisterFile.int_names[RAM[pc+1]]+";\n");
					break;
				}
				case 3:
				{
					instructions[instr].execute(regs, RAM ,RAM[pc+1],RAM[pc+2],0);
					//System.out.println(RegisterFile.int_names[RAM[pc+1]]+" "+RegisterFile.int_names[RAM[pc+2]]+";");
					debug(RegisterFile.int_names[RAM[pc+1]]+" "+RegisterFile.int_names[RAM[pc+2]]+";\n");
					break;
				}
				case 4:
				{
					instructions[instr].execute(regs, RAM ,RAM[pc+1],RAM[pc+2],RAM[pc+3]);
					//System.out.println(RegisterFile.int_names[RAM[pc+1]]+" "+RegisterFile.int_names[RAM[pc+2]]+" "+RegisterFile.int_names[RAM[pc+3]]+";");
					debug(RegisterFile.int_names[RAM[pc+1]]+" "+RegisterFile.int_names[RAM[pc+2]]+" "+RegisterFile.int_names[RAM[pc+3]]+";\n");
					break;
				}
				default:
				{
					instructions[instr].execute(regs, RAM ,RAM[pc+1],RAM[pc+2],RAM[pc+3]);
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void debug(String s)
	{
		if (DBG>0)
		{
			System.out.print(s);
		}
	}
}
