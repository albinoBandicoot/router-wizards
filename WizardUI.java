import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class WizardUI extends JFrame implements ActionListener {

	public static WizardUI ui;
	public static Wizard[] wizards;

	public Wizard active_wizard;
	public ArrayList<Invocation> invoc;
	public ArrayList<Command> commands;

	public JPanel button_panel;
	public JButton[] wiz_buttons;

	public JButton generate;
	public JTextArea command_view;

	public WizardUI () throws IOException, FileNotFoundException, ArgumentException {
		super ("Router Wizards");
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		setLayout (null);

		button_panel = new JPanel();
		button_panel.setLayout (null);
		button_panel.setBounds (610,10,200,600);
		
		generate = new JButton ("Generate Commands");
		generate.setBounds (0,0,200,45);
		generate.addActionListener (this);
		generate.setActionCommand ("generate");
		button_panel.add (generate);

		File[] specs = new File("spec/").listFiles();
		wizards = new Wizard[specs.length];
		wiz_buttons = new JButton[specs.length];
		for (int i=0; i < specs.length; i++) {
			wizards[i] = new Wizard (specs[i], this);
			wizards[i].setBounds (5,5,600,600);
			wiz_buttons[i] = new JButton (wizards[i].name);
			wiz_buttons[i].setBounds (0,50+30*i,150,25);
			wiz_buttons[i].addActionListener (this);
			wiz_buttons[i].setActionCommand (i + "");
			button_panel.add (wiz_buttons[i]);
		}

		commands = new ArrayList<Command> ();

		command_view = new JTextArea ();
		command_view.setBounds (300, 20, 300, 600);
		
		invoc = new ArrayList<Invocation>();

		add (button_panel);
		add (command_view);

		pack();
		setSize (830,650);
		setVisible(true);
	}

	public static void main (String[] args) throws IOException, FileNotFoundException, ArgumentException{
		Wizard.readGlobals();
		ui = new WizardUI ();
	}

	public void setActiveWizard (Wizard w) {
		if (w == null) {
			remove (active_wizard);
			command_view.setVisible(true);
			button_panel.setVisible(true);
		} else {
			add (w);
			command_view.setVisible(false);
			button_panel.setVisible(false);
		}
		active_wizard = w;
		repaint();
	}

	public void actionPerformed (ActionEvent e) {
		String c = e.getActionCommand();
		System.out.println("Got actionEvent with command " + c);
		if (c.equals ("Cancel")) {
			setActiveWizard (null);
		} else if (c.equals ("OK")) {
			invoc.add (new Invocation (active_wizard));
			setActiveWizard (null);
		} else if (c.equals ("generate")) {
			commands.clear();
			for (Invocation i : invoc) {
				try {
					i.generate();
				} catch (ArgumentException ex) {
					System.out.println(ex);
					ex.printStackTrace();
				}
				commands.addAll (i.commands);
			}
			StringBuffer sb = new StringBuffer();
			for (Command m : commands) {
				sb.append (m + "\n");
			}
			command_view.setText (sb.toString());
		} else {
			try {
				int i = Integer.parseInt (c);
				setActiveWizard (wizards[i]);
			} catch (NumberFormatException ex) {
			}
		}
	}
}
