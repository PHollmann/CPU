import java.util.ArrayList;


public class Compiler {

	private CPU cpu;
	private Type[] types;
	private int line;
	private int msg_line;
	private ArrayList<Type> variables;
	private ArrayList<Type> labels;
	private ArrayList<String> messages;
	private ArrayList<String> gen_source;
	private Callback errorCall;
	private Callback messageCall;

	private int codesize; 
	
	public Compiler()
	{
		cpu=new CPU();
		initTypes();
		messages=new ArrayList<String>();
		gen_source=new ArrayList<String>();
	}
	
	private void initTypes()
	{
		Type _int=new Type();
		_int.name=".int";
		_int.size=4;
		types=new Type[]{_int};
	}
	
	public void setErrorCallback(Callback c)
	{
		errorCall=c;
	}
	
	public void setMessageCallback(Callback c)
	{
		messageCall=c;
	}
	
	public Byte[] Compile(String[] source, int ramsize)
	{
		messages=new ArrayList<String>();
		gen_source=new ArrayList<String>();
		msg_line=0;
		codesize=0;
		Byte[] RAM=new Byte[ramsize];
		long start = System.nanoTime();
		Byte[] bin=Process(source,ramsize);
		long end = System.nanoTime();

		message("Compilation took: "+((end-start)/1000+0.5)+" µs");
		message("\n");
		for (int i=0; i<bin.length; i++)
		{
			RAM[i]=bin[i];
		}
		for (int i=bin.length; i<RAM.length; i++)
		{
			RAM[i]=0;
		}
		return RAM;
	}
	
	private Byte[] Process(String[] source, int ramsize)
	{
		variables=new ArrayList<Type>();
		labels=new ArrayList<Type>();
		ArrayList<Byte> output=new ArrayList<Byte>();
		ArrayList<unclearStatement> unclear=new ArrayList<unclearStatement>();
		for (line=0;line<source.length;line++)
		{
			if (source[line].length()>0)
			{
				if (source[line].charAt(0)!='#')	//It's  no comment
				{
					String[] Contents=source[line].split(" ");
					if (Contents[0].charAt(0)=='.')	//Variable definition
					{
						if (Contents.length>1)
						{
							if (variables.size()>0)
							{
								for (int var=0;var<variables.size();var++)
								{
									if (variables.get(var).name.equalsIgnoreCase(Contents[1].trim()))
									{
										error("Double definition of variable!");
									}
								}
							}
							if (labels.size()>0)
							{
								for (int var=0;var<labels.size();var++)
								{
									if (labels.get(var).name.equalsIgnoreCase(Contents[1].trim()))
									{
										error("Label with same name exists!");
									}
								}
							}
							Type dummy=new Type();
							dummy.name=Contents[1].trim();
							dummy=getAdress(dummy, Contents[0], ramsize);
							variables.add(dummy);
						}
						else
						{
							error("Variable name expected");
							ArrayList<Byte> b=new ArrayList<Byte>();
							addToBinary("STOP",b);
							return b.toArray(new Byte[output.size()]);
						}
					}
					else
					{
						if (isInstruction(Contents[0]))	//Is it an Instruction?
						{
							for (int arg=1;arg<Contents.length;arg++)
							{
								if (!isRegister(Contents[arg]))
								{
									if (isVariable(Contents[arg]))
									{
										for (int var=0;var<variables.size();var++)
										{
											if (Contents[arg].equalsIgnoreCase(variables.get(var).name))
											{
												Contents[arg]="@"+variables.get(var).size;
											}
										}
									}
									else if (isLabel(Contents[arg]))
									{
										for (int label=0;label<labels.size();label++)
										{
											if (Contents[arg].equalsIgnoreCase(labels.get(label).name))
											{
												Contents[arg]="@"+labels.get(label).size;
											}
										}
									}
									else if (Contents[arg].charAt(0)!='@')
									{
										unclearStatement s=new unclearStatement();
										s.line=line;
										s.Adress=output.size()+1;
										s.name=Contents[arg];
										unclear.add(s);
										Contents[arg]="@0";
									}
								}
							}
							String out="";
							for (int i=0;i<Contents.length;i++)
							{
								out+=Contents[i]+" ";
							}
							addToBinary(out, output);
						}
						else	//Try to use it as a variable
						{
							int var=getVariable(Contents[0]);
							if (var>0)
							{
								if (Contents.length>1)
								{
									addToBinary("CPY R0 @"+new Integer(variables.get(variables.size()-1).size-4).toString(),output);
									addToBinary("LDK R0 @"+Contents[1],output);
									addToBinary("CPY R0 @"+variables.get(var-1).size,output);
									addToBinary("LDD R0 @"+new Integer(variables.get(variables.size()-1).size-4).toString(),output);
								}
								else
								{
									error("Missing Value!");
								}
							}
							else if (Contents[0].endsWith(":"))		//Try to use as Label
							{
								if (getLabel(Contents[0])>=0)
								{
									error("Label already in use : "+Contents[0]);
								}
								else if (Contents.length>1)
								{
									Type label=new Type();
									label.name=Contents[0].replace(":", " ").trim();
									label.size=output.size();
									int i=0;
									while (i<unclear.size())
									{
										if (label.name.equalsIgnoreCase(unclear.get(i).name))
										{
											message("Removing statement: "+unclear.get(i).name+" [Line "+unclear.get(i).line+"]");
											writeInt(unclear.get(i).Adress,label.size,output);
											unclear.remove(i);
										}
										else
										{
											i++;
										}
									}
									labels.add(label);
									String s="";
									for (i=1;i<Contents.length;i++)
									{
										s+=Contents[i]+" ";
									}
									addToBinary(s,output);
								}
							}
							else{
								error("Unknown Statement! "+source[line]);
							}
						}
					}
				}
			}
		}
		if (unclear.size()>0)
		{
			for (int i=0;i<unclear.size();i++)
			{
				error("Unclear statement: "+unclear.get(i).name,unclear.get(i).line);
			}
		}
		addToBinary("STOP", output);
		if (variables.size()>0)
		{
			codesize+=(ramsize-variables.get(variables.size()-1).size);
		}
		if (codesize>ramsize)
		{
			error("Insufficient Space");
		}
		message("Instructions and Constants: "+output.size()+" Bytes");
		if (variables.size()>0)
		{
			message("Variables: "+(ramsize-variables.get(variables.size()-1).size)+" Bytes");
		}
		message("Codesize: "+codesize+" Bytes");
		int used=(int)(codesize*100/ramsize+0.5);
		message(used+"% of Memory used");
		return output.toArray(new Byte[output.size()]);
		//return new String[1];
	}
	
	private void writeInt(int adress, int val, ArrayList<Byte> binary)
	{
		if (adress<=(binary.size()-4))
		{
			Byte[] parts=new Byte[4];
			parts[0]=(byte)(val>>>24);
			parts[1]=(byte)(val>>>16);
			parts[2]=(byte)(val>>>8);
			parts[3]=(byte)(val);
			for (int i=0;i<4;i++)
			{
				binary.set(i+adress, parts[i]);
			}
		}
	}
	
	private class unclearStatement
	{
		public int line;
		//public int content;
		public int Adress;
		public String name;
	}
	
	private void addToBinary(String source, ArrayList<Byte> Binary)
	{
		gen_source.add(source+"\n");
		Byte[] bin=this.Compile(source);
		for (int i=0;i<bin.length;i++)
		{
			Binary.add(bin[i]);
		}
	}
	
	public String[] getSource()
	{
		return gen_source.toArray(new String[gen_source.size()]);
	}
	
	private int getLabel(String s)
	{
		s+=":";
		for (int i=0;i<labels.size();i++)
		{
			if (labels.get(i).name.equalsIgnoreCase(s))
			{
				return i;
			}
		}
		return -1;
	}
	
	private Type getAdress(Type t, String type, int ramsize)
	{
		for (int i=0;i<types.length;i++)
		{
			if (types[i].name.equalsIgnoreCase(type.trim()))
			{
				if (variables.size()==0)
				{
					t.size=ramsize-types[i].size;
				}
				else
				{
					t.size=variables.get(variables.size()-1).size-types[i].size;
				}
				if (t.size<0)
				{
					error("Insufficient Memory!");
				}
				return t;
			}
		}
		error("Unknown Type!");
		return new Type();
	}
	
	private String[] error(String msg)
	{
		String _msg="Error in Line "+new Integer(line+1).toString()+": "+msg;
		System.err.println(_msg);
		messages.add(_msg+"\n");
		writeCallback(_msg+"\n");
		return new String[]{"#"+_msg};
	}
	
	private void message(String s)
	{
		if (messageCall!=null)
		{
			messageCall.run(s+"\n");
		}
		System.out.println(s);
	}
	
	private String[] error(String msg, int _line)
	{
		String _msg="Error in Line "+new Integer(_line+1).toString()+": "+msg;
		System.err.println(_msg);
		messages.add(_msg+"\n");
		writeCallback(_msg+"\n");
		return new String[]{"#"+_msg};
	}
	
	private void writeCallback(String s)
	{
		if (errorCall!=null)
		{
			errorCall.run(s);
		}
	}
	
	private class Type
	{
		public String name;
		public int size;
	}
	
	public String[] getHelp()
	{
		ArrayList<String> out=new ArrayList<String>();
		for (int i=0; i<cpu.instructions.length; i++)
		{
			out.add(cpu.instructions[i].getSymbol()+"	:	"+cpu.instructions[i].getDesciption());
		}
		return out.toArray(new String[out.size()]);
	}
	
	private boolean isInstruction(String s)
	{
		for (int i=0;i<cpu.instructions.length;i++)
		{
			if (cpu.instructions[i].getSymbol().equalsIgnoreCase(s.trim()))
			{
				return true;
			}
		}
		return false;
	}
	
	private int getVariable(String s)
	{
		for (int i=0; i<variables.size();i++)
		{
			if (variables.get(i).name.equalsIgnoreCase(s.trim()))
			{
				return i+1;
			}
		}
		return 0;
	}
	
	public Byte[] Compile(String source)
	{
		ArrayList<Byte> v = new ArrayList<Byte>();
		if (!source.matches("[#].*"))
		{
			for (int i=0; i<cpu.instructions.length;i++)
			{
				String[] Contents=source.split(" ");
				for (int c=0;c<Contents.length;c++)
				{
					Contents[c]=Contents[c].trim();
				}
				if (Contents[0].equalsIgnoreCase(cpu.instructions[i].getSymbol()))
				{
					v.add((byte)i);
					for (int registers=1;registers<Contents.length;registers++)
					{
						Contents[registers]=Contents[registers].trim();
						if (Contents[registers].matches("[@].*"))
						{
							int x=new Integer(Contents[registers].replace("@", ""));
							v.add((byte)(x>>>24));
							v.add((byte)(x>>>16));
							v.add((byte)(x>>>8));
							v.add((byte)(x));
						}
						else
						{
							if (isRegister(Contents[registers]))
							{
								v.add(getRegByte(Contents[registers]));
							}
							else if (isVariable(Contents[registers]))
							{
								String s=Contents[registers];
								int x=0;
								for (int var=0;var<variables.size();var++)
								{
									if (s.equalsIgnoreCase(variables.get(var).name))
									{
										x=variables.get(var).size;
									}
								}
								v.add((byte)(x>>>24));
								v.add((byte)(x>>>16));
								v.add((byte)(x>>>8));
								v.add((byte)(x));
							}
							else if (isLabel(Contents[registers]))
							{
								String s=Contents[registers];
								int x=0;
								for (int var=0;var<labels.size();var++)
								{
									if (s.equalsIgnoreCase(labels.get(var).name))
									{
										x=labels.get(var).size;
									}
								}
								v.add((byte)(x>>>24));
								v.add((byte)(x>>>16));
								v.add((byte)(x>>>8));
								v.add((byte)(x));
							}
							else
							{
								error("Unknown Statement "+Contents[registers]);
							}
						}
					}
					break;
				}
			}
		}
		Byte[] bitstream=v.toArray(new Byte[v.size()]);
		codesize+=bitstream.length;
		return bitstream;
	}
	
	private byte getRegByte(String s)
	{
		s=s.trim();
		for (byte i=0;i<32;i++)
		{
			if (s.equalsIgnoreCase("R"+new Integer(i).toString())||s.equalsIgnoreCase("F"+new Integer(i).toString())||
				s.equalsIgnoreCase(RegisterFile.int_names[i])||s.equalsIgnoreCase(RegisterFile.float_names[i]))
			{
				return i;
			}
		}
		error("Unable to find registername "+s+"!");
		return 0;
	}
	
	private boolean isRegister(String s)
	{
		s=s.trim();
		for (byte i=0;i<32;i++)
		{
			if (s.equalsIgnoreCase("R"+new Integer(i).toString())||s.equalsIgnoreCase("F"+new Integer(i).toString())||
				s.equalsIgnoreCase(RegisterFile.int_names[i])||s.equalsIgnoreCase(RegisterFile.float_names[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isVariable(String s)
	{
		s=s.trim();
		for (int i=0;i<variables.size();i++)
		{
			if (s.equalsIgnoreCase(variables.get(i).name))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isLabel(String s)
	{
		s=s.trim();
		for (int i=0;i<labels.size();i++)
		{
			if (s.equalsIgnoreCase(labels.get(i).name))
			{
				return true;
			}
		}
		return false;
	}
	
	public String readline()
	{
		if (msg_line<messages.size())
		{
			msg_line++;
			return messages.get(msg_line-1);
		}
		else
		{
			return null;
		}
	}
}
