
public class RegisterFile {
	public final static String[] int_names= new String[]{"R00","R01","R02","R03","R04","R05","R06","R07",
									 "R08","R09","R10","R11","R12","R13","R14","R15",
									 "R16","R17","R18","R19","R20","R21","R22","R23",
									 "R24","R25","R26","R27","R28","R29","R30","R31"};
	public final static String[] float_names= new String[]{"F00","F01","F02","F03","F04","F05","F06","F07",
			 						 "F08","F09","F10","F11","F12","F13","F14","F15",
			 						 "F16","F17","F18","F19","F20","F21","F22","F23",
			 						 "F24","F25","F26","F27","F28","F29","F30","F31"};
	public int[] int_Regs=new int[int_names.length];
	public double[] float_regs=new double[float_names.length];
	public int pc=0;
	public int status=0;
	public int compareResult=0;
	
	public RegisterFile()
	{
		
	}
}
