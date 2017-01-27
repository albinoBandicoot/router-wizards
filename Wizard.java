import javax.swing.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
public class Wizard extends JPanel {

	public static PList globals = new PList();	// this will be for editing the global values. These are RealParams

	public String name;
	public PList params;
	public PList global_over;	// this will be for the overrides; all of them should be GlobalParams
	public JPanel param_panel;
	public JLabel image;

	public JButton cancel;
	public JButton ok;

	public WizardUI wiz_ui;

	public static final int XS = 800;
	public static final int YS = 600;
	public static final int PWD = 300;

	public static void readGlobals () throws IOException, ArgumentException {
		Scanner sc = new Scanner (new File ("globals"));
		while (sc.hasNextLine()) {
			Scanner ls = new Scanner (sc.nextLine());
			String name = ls.next();
			double val = ls.nextDouble();
			globals.add (name, val, true);
		}
	}

	public Wizard (File spec, WizardUI wizui) throws FileNotFoundException, IOException, ArgumentException {
		wiz_ui = wizui;
		System.out.println("Working on " + spec);
		params = new PList();
		global_over = new PList();
		param_panel = new JPanel();
		setLayout(null);
		param_panel.setLayout(null);
		param_panel.setBounds (8,8,PWD,YS);
		Scanner sc = new Scanner (spec);
		name = sc.nextLine();
		File img_file = new File ("images/" + sc.nextLine());
		BufferedImage bi = ImageIO.read (img_file);
		image = new JLabel(new ImageIcon(bi));
		image.setBounds (PWD, 20, XS-PWD, YS - 40);

		while (sc.hasNextLine()) {
			Scanner ls = new Scanner(sc.nextLine());
			String pname = ls.next();
			String val = ls.next();
			if (val.equals("true") || val.equals("false")) {
				boolean bval = Boolean.parseBoolean (val);
				params.add (pname, bval, true);
				System.out.println("added parameter " + pname + " = " + bval);
			} else {
				double dval = Double.parseDouble (val);
				params.add (pname, dval, true);
				System.out.println("added parameter " + pname + " = " + dval);
			}
		}
		int i=0;
		for (Map.Entry<String, Param> e : params.params.entrySet()) {
			JLabel label = new JLabel (e.getKey());
			label.setBounds(0, 30*i, 150, 25);
			e.getValue().ui.setBounds(150, 30*i, 150, 25);
			param_panel.add (label);
			param_panel.add (e.getValue().ui);
			i++;
		}
		for (String name : globals.params.keySet()) {
			GlobalParam g = new GlobalParam (name, globals.getDouble(name), false, true);
			global_over.params.put (name, g);
			g.ui.setBounds (0, 30*i, PWD, 25);
			param_panel.add (g.ui);
			i++;
		}

		add (param_panel);
		add (image);

		cancel = new JButton ("Cancel");
		cancel.setBounds (PWD + 5, YS - 32, 120, 25);
		cancel.addActionListener (wiz_ui);
		cancel.setActionCommand ("Cancel");
		ok = new JButton ("OK");
		ok.setBounds (PWD + 150, YS - 32, 120, 25);
		ok.addActionListener (wiz_ui);
		ok.setActionCommand ("OK");
		
		
		add (cancel);
		add (ok);
	}

	public void setParams (PList p, PList g) throws ArgumentException{
		for (String s : p.params.keySet()) {
			Param par = params.get(s);
			if (par == null) {	
				// oops
			} else {
				par.setValue (p.get(s));
			}
		}
		for (String s : g.params.keySet()) {
			Param gl = global_over.get(s);
			gl.setValue (g.get(s));
		}
		Command.travel_fr = g.getDouble("travel_feed");
		Command.cut_fr = g.getDouble("cut_feed");
		Command.retract_fr = g.getDouble("retract_feed");
		Command.plunge_fr = g.getDouble("plunge_feed");
		Command.safe_z = g.getDouble("safe_z");
	}

	public void readGUI () {
		params.readGUI();
		global_over.readGUI();
	}
	public void updateGUI () {
		params.updateGUI();
		global_over.updateGUI();
	}

	public boolean getBoolean (String name) throws ArgumentException {
		return params.getBoolean(name);
	}

	public double getDouble (String name) throws ArgumentException {
		try {
			return params.getDouble(name);
		} catch (ArgumentException ex) {
			return global_over.getDouble(name);
		}
	}

	public List<Command> generate (ArrayList<Invocation> children) throws ArgumentException {
		readGUI();
		if (name.equals("Drill")) {
			return generate_drill();
		} else if (name.equals ("Linear_Pattern")) {
			return generate_linear_pattern (children);
		}
		return null;
	}

	// Individual Wizard Implementations

	private List<Command> generate_drill () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
		double x = getDouble("X");
		double y = getDouble("Y");
		double z = getDouble("Z");
		double depth = getDouble ("Depth");
		double peck_depth = getDouble("Peck_depth");
		double retract_len = getDouble ("Retract_length");
		double plunge_fr = getDouble ("plunge_feed");
		double retract_fr = getDouble ("retract_feed");
		if (depth <= 0) throw new ArgumentException ("depth must be positive");
		if (peck_depth <= 0) throw new ArgumentException ("peck depth must be positive");
		if (plunge_fr <= 0 || retract_fr <= 0) throw new ArgumentException ("Feedrates must be positive");
		res.add (Command.lift());
		res.add (Command.rapid (x,y));
		res.add (Command.rapid (x,y,z));
		int np = (int) (depth / peck_depth);
		double residual_depth = depth - np*peck_depth;
		for (int i=0; i < np; i++) {
			res.add (Command.plunge (peck_depth + (i == 0 ? 0 : retract_len)));
			res.add (Command.retract (retract_len));
		}
		if (residual_depth > 0.01) {
			res.add (Command.plunge (peck_depth + (np == 0 ? 0 : retract_len)));
		}
		res.add (Command.move (x,y,z,Command.retract_fr));
		return res;
	}

	private List<Command> generate_linear_pattern (List<Invocation> children) throws ArgumentException{
		List<Command> res = new ArrayList<Command>();
		double n = params.getInt ("N");
		double dx = params.getDouble ("dx");
		double dy = params.getDouble ("dy");
		for (int i=0; i < n; i++) {
		}

		return null;
	}
}
