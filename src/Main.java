import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Create CPU");
		CPU cpu=new CPU();
		System.out.println("CPU Created");
		Compiler c=new Compiler();
		//String[] source =new String[]{".int i","i 5","LDK R1 @252", "LDK R2 @248","SUB R0 R1 R2","ADD R0 R0 R0", "ADD R1 R1 R1","STOP"};
		String[] source;
		if (args.length>0)
		{
			if (args[0].equalsIgnoreCase("-C"))
			{
				source=getSource(args[1]);
				Byte[] b=c.Compile(source, 256);
				saveBinary(b, args[1]);
			}
			else if((args[0].equalsIgnoreCase("-B")))
			{
				Byte[] b=readBinary(args[1]);
				cpu.execute(b);
			}
			else
			{
				source = getSource(args[0]);
				Byte[] b=c.Compile(source, 256);
				for (int i=0;i<b.length;i++)
				{
					System.out.print((int)(b[i]&0xFF)+";	");
					if ((i+1)%16==0)
					{
						System.out.print("\n");
					}
				}
				System.out.print("\n");
				cpu.execute(b);
			}
		}
		else
		{
			MainGUI m=new MainGUI();
			m.setVisible(true);
		}
	}
	
	private static String[] getSource(String path)
	{
		ArrayList<String> out=new ArrayList<String>();
		File testFile = new File(path);
		try {
			BufferedReader input=new BufferedReader(new FileReader(testFile));
			String line=input.readLine();
			while (line!=null)
			{
				out.add(line);
				line=input.readLine();
			}
			return out.toArray(new String[out.size()]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[]{"#Error"};
	}
	
	private static void saveBinary(Byte[] RAM, String path)
	{
		// Write to disk with FileOutputStream
		FileOutputStream f_out;
		try {
			f_out = new 
				FileOutputStream(path+".bin");
			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new
				ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject (RAM);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Byte[] readBinary(String path)
	{
		// Read from disk using FileInputStream
		FileInputStream f_in;
		try {
			f_in = new 
				FileInputStream(path);
			// Read object using ObjectInputStream
			ObjectInputStream obj_in = 
				new ObjectInputStream (f_in);

			// Read an object
			Object obj = obj_in.readObject();

			if (obj instanceof Byte[])
			{
				return (Byte[])obj;
			}
			else
			{
				return new Compiler().Compile(new String[]{"STOP"},16);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Compiler().Compile(new String[]{"STOP"},16);
	}
}
