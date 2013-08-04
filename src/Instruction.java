
public class Instruction {
	private String description="No Operation";	//What is it doing
	private String Symbol="NOP";				//Asm Symbol
	private int length=1;
	
	Execution action;
	
	void execute(RegisterFile regs, Byte[] RAM, int arg0, int arg1, int arg2)
	{
		if (action==null)
		{
			regs.pc++;
		}
		else
		{
			action.execute(regs, RAM, arg0, arg1, arg2);
		}
	}
	
	void setExecute(Execution e)
	{
		this.action=e;
	}
	
	public Instruction(String desc, String symbol, int _length)
	{
		description=desc;
		Symbol=symbol;
		length=_length;
	}
	
	public Instruction()
	{
	}
	
	public String getSymbol()
	{
		return Symbol;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public String getDesciption()
	{
		return description;
	}
}
