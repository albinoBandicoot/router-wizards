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
		name = sc.nextLine().replace('_',' ');
		File img_file = new File ("images/" + sc.nextLine());
		BufferedImage bi = ImageIO.read (img_file);
		image = new JLabel(new ImageIcon(bi));
		image.setBounds (PWD, 20, XS-PWD, YS - 40);

		while (sc.hasNextLine()) {
			Scanner ls = new Scanner(sc.nextLine());
			String pname = ls.next().replace('_',' ');
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
		Command.travel_fr = g.getDouble("travel feed");
		Command.cut_fr = g.getDouble("cut feed");
		Command.retract_fr = g.getDouble("retract feed");
		Command.plunge_fr = g.getDouble("plunge feed");
		Command.safe_z = g.getDouble("safe z");
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
		} else if (name.equals("Slot")) {
			return generate_slot();
		} else if (name.equals("Arc Slot")) {
			return generate_arcslot();
		} else if (name.equals("Face")) {
			return generate_face();
		} else if (name.equals("Surface")) {
			return generate_surface();
		} else if (name.equals("Circular Pocket")) {
			return generate_circular_pocket();
		} else if (name.equals("Rectangular Pocket")) {
			return generate_rect_pocket();
			/*
		} else if (name.equals("Edgefind")) {
			return generate_edgefind();
		} else if (name.equals("Centerfind")) {
			return generate_centerfind();
		} else if (name.equals("Edgefind+Rotation")) {
			return generate_edgefind_rotation();
			*/
		} else if (name.equals("Linear Pattern")) {
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
		double peck_depth = getDouble("Peck depth");
		double retract_len = getDouble ("Retract length");
		if (depth <= 0) throw new ArgumentException ("depth must be positive");
		if (peck_depth <= 0) throw new ArgumentException ("peck depth must be positive");
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

	private List<Command> generate_circular_pocket () throws ArgumentException {
		List<Command>res = new ArrayList<Command>();
		double x = getDouble("X");
		double y = getDouble("Y");
		double z = getDouble("Z");
		double depth = getDouble("Depth");
		double radius = getDouble("Radius");
		double toold = getDouble("Tool Diameter");
		double overlap = getDouble("%Overlap") * 0.01;
		double doc = getDouble("Depth of Cut");
		boolean fill = getBoolean ("Mill Interior");
		boolean climb = getBoolean ("Climb");
		if (depth <= 0) throw new ArgumentException ("depth must be positive");
		if (doc <= 0) throw new ArgumentException ("depth of cut must be positive");
		if (toold <= 0) throw new ArgumentException ("tool diameter must be positive");
		if (overlap < 0.01 || overlap > 1) throw new ArgumentException ("%overlap must be between 1 and 100");
		res.add (Command.lift());
		res.add (Command.rapid(x,y));
		res.add (Command.rapid(x,y,z));

		int nlayers = (int) (depth / doc);
		double residual = depth - nlayers*doc;

		double offset = toold * (1-overlap);
		double dr = radius - toold/2;
		int nrings = (int) (dr / offset);
		double residual_r = dr - nrings*offset;
		
		if (!fill || (nrings == 0 && radius < toold)) {
			// helix
			double pathr = radius - toold/2;
			double dtheta = depth/doc * 2 * Math.PI;
			if (climb) dtheta = -dtheta;
			res.add (Command.cut(x+pathr,y));
			res.add (new Command ("mhlx", pathr, 0, dtheta, doc));
			res.add (new Command ("marc", pathr, dtheta, 2*Math.PI * (climb ? -1 : 1), Command.cut_fr));
			Command.setpos (x + pathr*Math.cos(dtheta), y + pathr*Math.sin(dtheta), z - depth);
			res.add (Command.rapid (x,y));
			res.add (Command.retract (depth));
			return res;
		}

		for (int i=0; i < nlayers; i++) {
			res.add (Command.plunge (doc));
			for (int j=0; j < nrings; j++) {
				res.add (Command.cut (x + (j+1)*offset,y));
				res.add (new Command("marc", (j+1)*offset, 0, 2*Math.PI * (climb ? -1 : 1), Command.cut_fr));
			}
			if (residual_r > 0.01) {
				res.add (Command.cut (x + dr, y));
				res.add (new Command("marc", dr, 0, Math.PI * (climb ? -2 : 2), Command.cut_fr));
			}
		}
		res.add (Command.rapid (x,y,z));
		return res;
	}

	private List<Command> generate_rect_pocket () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
		return res;
	}

	private List<Command> generate_ngon_pocket () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
		return res;
	}
	private List<Command> generate_surface () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
		return res;
	}

	private List<Command> generate_slot () throws ArgumentException {
		List<Command>res = new ArrayList<Command>();
		double[] x = new double[] {getDouble("Start X"), getDouble("End X")};
		double[] y = new double[] {getDouble("Start Y"), getDouble("End Y")};
		double z  = getDouble("Z");
		double depth = getDouble ("Depth");
		double doc = getDouble("Depth of Cut");

		if (depth < 0) throw new ArgumentException ("Depth must be positive");
		if (doc < 0.01) throw new ArgumentException ("Depth of cut must be > 0.01");
		
		int npasses = (int) (depth / doc);
		double residual = depth - npasses * doc;
		
		if (npasses > 500) throw new ArgumentException ("Number of passes is > 500");
		
		res.add (Command.lift());
		res.add (Command.rapid(x[0],y[0]));
		res.add (Command.rapid(x[0],y[0],z));

		double d = 0;
		int side = 0;
		while (d+doc < depth) {
			side = 1-side;
			res.add (Command.plunge(doc));
			res.add (Command.cut (x[side], y[side]));
			d += doc;
		}
		if (residual > 0.01) {
			side = 1-side;
			res.add (Command.plunge(residual));
			res.add (Command.cut (x[side], y[side]));
		}
		return res;
	}

	private List<Command> generate_arcslot () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
		double x = getDouble("Center X");
		double y = getDouble("Center Y");
		double stheta = Math.toRadians(getDouble ("Start Theta"));
		double etheta = Math.toRadians(getDouble ("End Theta"));
		double r = getDouble ("Radius");
		double z = getDouble("Z");
		double dtheta = etheta - stheta;
		double depth = getDouble ("Depth");
		double doc = getDouble ("Depth of Cut");

		if (depth < 0) throw new ArgumentException ("Depth must be positive");
		if (doc < 0.01) throw new ArgumentException ("Depth of cut must be > 0.01");
		
		int npasses = (int) (depth / doc);
		double residual = depth - npasses * doc;
		if (npasses > 500) throw new ArgumentException ("Number of passes is > 500");

		double sx = x + r*Math.cos(stheta);
		double sy = y + r*Math.sin(stheta);


		res.add (Command.lift());
		res.add (Command.rapid (sx, sy));
		res.add (Command.rapid (sx, sy, z));

		int side = 0;
		double d = 0;
		while (d+doc < depth) {
			res.add (Command.plunge(doc));
			res.add (Command.arccut (r, stheta + side*dtheta, (side*2-1)*dtheta));
			side = 1-side;
			d += doc;
		}
		if (residual > 0.01) {
			res.add (Command.plunge (residual));
			res.add (Command.arccut (r, stheta + side*dtheta, (side*2-1)*dtheta));
			side = 1-side;
		}
		res.add (Command.lift());
		if (side != 0) {
			res.add (Command.arc (r, etheta, -dtheta, Command.travel_fr));
		}
		return res;
	}
	
	private List<Command> generate_face () throws ArgumentException {
		List<Command> res = new ArrayList<Command>();
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
