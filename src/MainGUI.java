import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;


public class MainGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea area = new JTextArea(20,120);
	private JTextArea console = new JTextArea(20,40);
	private JTextArea RAM = new JTextArea(20,80);
	private JTextArea lines;
	
	private Panel dbgComponents = new Panel();

    private JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));

    private String currentFile = "Untitled";

    private boolean changed = false;
    
    private final int ramsize=256;
    
    ActionMap m = area.getActionMap();

    Action Cut = m.get(DefaultEditorKit.cutAction);

    Action Copy = m.get(DefaultEditorKit.copyAction);

    Action Paste = m.get(DefaultEditorKit.pasteAction);
	
	public MainGUI()
	{
		FileFilter filter1 = new ExtensionFileFilter("ASM and BIN", new String[] { "ASM", "BIN" });
		dialog.setFileFilter(filter1);
		
		area.setFont(new Font("Monospaced",Font.PLAIN,12));
		area.setForeground(Color.YELLOW);
		area.setBackground(Color.BLACK);
        JScrollPane scroll = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scroll,BorderLayout.CENTER);
        //Line numbering
        lines = new JTextArea("1");
		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);
		area.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int caretPosition = area.getDocument().getLength();
				Element root = area.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
			}
 
			@Override
			public void insertUpdate(DocumentEvent de) {
				lines.setText(getText());
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {
				lines.setText(getText());
			}
 
		});
		scroll.getViewport().add(area);
		scroll.setRowHeaderView(lines);
		//###############################################################################################
        RAM.setEditable(false);
        RAM.setForeground(Color.WHITE);
        RAM.setBackground(Color.BLUE);
        console.setEditable(false);
        console.setForeground(Color.WHITE);
        console.setBackground(Color.BLUE);
		dbgComponents.setLayout(new BorderLayout());
        JScrollPane scrollcmd = new JScrollPane(console,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        dbgComponents.add(scrollcmd, BorderLayout.WEST);
        JScrollPane scrollRAM = new JScrollPane(RAM,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        dbgComponents.add(scrollRAM, BorderLayout.EAST);
        add(dbgComponents,BorderLayout.SOUTH);
        JMenuBar JMB = new JMenuBar();
        setJMenuBar(JMB);
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMB.add(file); 
        JMB.add(edit);
        file.add(New);
        file.add(Open);
        file.add(Save);
        file.add(Quit);
        file.add(SaveAs);
        file.addSeparator();
        for(int i=0; i<4; i++)
                file.getItem(i).setIcon(null);
        edit.add(Cut);
        edit.add(Copy);
        edit.add(Paste);
        edit.getItem(0).setText("Cut out");
        edit.getItem(1).setText("Copy");
        edit.getItem(2).setText("Paste");
        JToolBar tool = new JToolBar();
        add(tool,BorderLayout.NORTH);
        tool.add(New);
        tool.add(Open);
        tool.add(Save);
        tool.addSeparator();
        JButton cut = tool.add(Cut), cop = tool.add(Copy),pas = tool.add(Paste);
        tool.addSeparator();
        JButton bin = tool.add(mkBin);
        JButton run = tool.add(Run);
        JButton help = tool.add(Help);
        bin.setText("Create Binary");
        run.setText("Run");
        help.setText("Help");
        cut.setText("Cut");
        cop.setText("Copy");
        pas.setText("Paste");
        Save.setEnabled(false);
        SaveAs.setEnabled(false);        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        area.addKeyListener(k1);
        setTitle(currentFile);
        setVisible(true);
	}
	
	private void saveFileAs() {

        if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)

                saveFile(dialog.getSelectedFile().getAbsolutePath());

	}
	
	private void saveOld() {

        if(changed) {

                if(JOptionPane.showConfirmDialog(this, "Would you like to save "+ currentFile +" ?","Save",JOptionPane.YES_NO_OPTION)== JOptionPane.YES_OPTION)

                        saveFile(currentFile);

        }

	}
	
	private void readInFile(String fileName) {

        try {

                FileReader r = new FileReader(fileName);
                
                BufferedReader input=new BufferedReader(r);
                
                area.setText("");
                
                try {
                	String line = null;
                    while (( line = input.readLine()) != null){
                        area.append(line);
                        area.append(System.getProperty("line.separator"));
                    }
                }
                finally {
                    input.close();
                }
                
                //area.read(r,null);

                r.close();

                currentFile = fileName;

                setTitle(currentFile);

                changed = false;

        }

        catch(IOException e) {

                Toolkit.getDefaultToolkit().beep();

                JOptionPane.showMessageDialog(this,"Editor can't find the file called "+fileName);

        }

	}
	
	private void saveFile(String fileName) {

        try {
        		if (!fileName.endsWith(".asm"))
        		{
        			fileName+=".asm";
        		}

                FileWriter w = new FileWriter(fileName);

                area.write(w);

                w.close();

                currentFile = fileName;

                setTitle(currentFile);

                changed = false;

                Save.setEnabled(false);

        }

        catch(IOException e) {

        }

	}
	
	private KeyListener k1 = new KeyAdapter() {

        public void keyPressed(KeyEvent e) {

                changed = true;

                Save.setEnabled(true);

                SaveAs.setEnabled(true);

        }

	};
	
	Action Open = new AbstractAction("Open") {

        public void actionPerformed(ActionEvent e) {

                saveOld();

                if(dialog.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {

                    if (dialog.getSelectedFile().getAbsolutePath().endsWith(".asm"))
                    {
                    	readInFile(dialog.getSelectedFile().getAbsolutePath());
                    }

                }

                SaveAs.setEnabled(true);

        }

	};
	
	Action Save = new AbstractAction("Save") {

        public void actionPerformed(ActionEvent e) {

                if(!currentFile.equals("Untitled"))

                        saveFile(currentFile);

                else

                        saveFileAs();

        }

	};
	
	Action SaveAs = new AbstractAction("Save as...") {

        public void actionPerformed(ActionEvent e) {

                saveFileAs();

        }

	};
	
	Action Quit = new AbstractAction("Quit") {

        public void actionPerformed(ActionEvent e) {

                saveOld();

                System.exit(0);

        }

	};
	
	Action New = new AbstractAction("New") {

        public void actionPerformed(ActionEvent e) {

                area.setText("");
                currentFile="Untitled";
                changed=false;
                Save.setEnabled(false);
                SaveAs.setEnabled(false);
        }

	};
	
	Action mkBin = new AbstractAction("Create Binary") {

        public void actionPerformed(ActionEvent e) {

                Compiler c=new Compiler();
                String[] lines = area.getText().split("\\n");
                c.setMessageCallback(new Callback()
                {
                	public void run(String s)
                	{
                		RAM.append(s);
                	}
                });
                Byte[] b=c.Compile(lines, ramsize);
                String s=c.readline();
                if (s!=null)
                {
                	RAM.setForeground(Color.RED);
	                while (s!=null)
	                {
	                	RAM.append(s);
	                	s=c.readline();
	                }
	                RAM.append("Errors found!\n Aborting...");
                }
                else
                {
	                if(dialog.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
	                {
	                    String path=dialog.getSelectedFile().getAbsolutePath();
	                    saveBinary(b,path);
	                }
                }
        }

	};
	
	Action Run = new AbstractAction("Run") {

        public void actionPerformed(ActionEvent e) {
        		console.setText("");
        		RAM.setText("");
                Compiler c=new Compiler();
                c.setMessageCallback(new Callback()
                {
                	public void run(String s)
                	{
                		RAM.append(s);
                	}
                });
                String[] lines = area.getText().split("\\n");
                final Byte[] b=c.Compile(lines, ramsize);
                String s=c.readline();
                RAM.setForeground(Color.WHITE);
                if (s!=null)
                {
                	RAM.setForeground(Color.RED);
	                while (s!=null)
	                {
	                	RAM.append(s);
	                	s=c.readline();
	                }
	                RAM.append("Errors found!\n Aborting...");
                }
                else
                {
                	String[] gen=c.getSource();
                	RAM.append("Generated Code:\n");
                	for (int i=0;i<gen.length;i++)
                	{
                		RAM.append(gen[i]);
                	}
                	RAM.append("\nGenerated Binary:\n");
	                for (int i=0;i<b.length;i++)
	    			{
	    				RAM.append(i+": "+(int)(b[i]&0xFF)+";	");
	    				if ((i+1)%8==0)
	    				{
	    					RAM.append("\n");
	    				}
	    			}
	                final CPU cpu=new CPU();
	                Thread t=new Thread()
	                {
	                	public void run()
	                	{
	                		cpu.execute(b);
	                		getOutput(cpu);
	                	}
	                };
	                t.start();
                }
        }
        
        private void getOutput(CPU cpu)
        {
        	String line= cpu.readline();
            while (line!=null)
            {
            	console.append(line+"\n");
            	line=cpu.readline();
            }
        }

	};
	
	Action Help = new AbstractAction("Help") {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
        		console.setText("");
        		RAM.setText("");
                Compiler c=new Compiler();
                String[] s=c.getHelp();
                for (int i=0;i<s.length;i++)
                {
                	RAM.append(s[i]+"\n");
                }
        }

	};
	
	private static void saveBinary(Byte[] RAM, String path)
	{
		// Write to disk with FileOutputStream
		FileOutputStream f_out;
		try {
			if (!path.endsWith(".bin"))
			{
				path+=".bin";
			}
			f_out = new 
				FileOutputStream(path);
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
	
	class ExtensionFileFilter extends FileFilter {
		  String description;

		  String extensions[];

		  public ExtensionFileFilter(String description, String extension) {
		    this(description, new String[] { extension });
		  }

		  public ExtensionFileFilter(String description, String extensions[]) {
		    if (description == null) {
		      this.description = extensions[0];
		    } else {
		      this.description = description;
		    }
		    this.extensions = (String[]) extensions.clone();
		    toLower(this.extensions);
		  }

		  private void toLower(String array[]) {
		    for (int i = 0, n = array.length; i < n; i++) {
		      array[i] = array[i].toLowerCase();
		    }
		  }

		  public String getDescription() {
		    return description;
		  }

		  public boolean accept(File file) {
		    if (file.isDirectory()) {
		      return true;
		    } else {
		      String path = file.getAbsolutePath().toLowerCase();
		      for (int i = 0, n = extensions.length; i < n; i++) {
		        String extension = extensions[i];
		        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
		          return true;
		        }
		      }
		    }
		    return false;
		  }
		}
}
