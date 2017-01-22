import java.util.*;
public class PList {

	public Map<String, Param> params;

	public PList () {
		params = new TreeMap<String, Param>();
	}

	public PList (PList p) {
		this();
		for (String s : p.params.keySet()) {
			params.put (s, p.get(s).copy());
		}
	}

	public void add (String name, boolean b, boolean gen_gui) {
		params.put (name, new BooleanParam (b, gen_gui));
	}

	public void add (String name, double d, boolean gen_gui) {
		params.put (name, new RealParam (d, gen_gui));
	}

	public Param get (String name) {
		return params.get(name);
	}

	public boolean getBoolean (String s) throws ArgumentException {
		Param p = params.get(s);
		if (p == null) throw new ArgumentException ("No such parameter: '" + s + "'");
		if (! (p instanceof BooleanParam)) throw new ArgumentException ("Parameter '" + s + "' is not a double");
		return ((BooleanParam) p).value;
	}

	public int getInt (String s) throws ArgumentException {
		double d = getDouble(s);
		if (d != (int) d) throw new ArgumentException ("Parameter '" + s + "' must have an integer value!");
		return (int) d;
	}

	public double getDouble (String s) throws ArgumentException {
		Param p = params.get(s);
		if (p == null) throw new ArgumentException ("No such parameter: '" + s + "'");
		if (p instanceof GlobalParam) return ((GlobalParam) p).getValue();
		if (! (p instanceof RealParam)) throw new ArgumentException ("Parameter '" + s + "' is not a double");
		return ((RealParam) p).value;
	}


	public void updateGUI () {
		for (Param p : params.values()) {
			p.updateGUI();
		}
	}

	public void readGUI () {
		for (Param p : params.values()) {
			p.readGUI();
		}
	}

}
