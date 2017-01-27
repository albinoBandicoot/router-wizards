import javax.swing.*;
public class RealParam extends Param {

	public double value;
	public double default_value;

	public RealParam (double val, boolean gen_gui) {
		value = val;
		default_value = val;
		if (gen_gui) createGUI();
	}

	public RealParam (double val, double def, boolean gen_gui) {
		value = val;
		default_value = def;
		if (gen_gui) createGUI();
	}

	public Param copy () {
		return new RealParam (value, default_value, false);
	}

	public void setValue (Param p) throws ArgumentException {
		if (p instanceof RealParam) {
			value = ((RealParam) p).value;
			updateGUI();
		} else {
			throw new ArgumentException ("Not a Realparam!");
		}
	}

	public void createGUI () {
		ui = new JTextField (value + "");
	}

	public void updateGUI () {
		if (ui == null) return;
		((JTextField) ui).setText (value + "");
	}

	public void readGUI () {
		if (ui == null) return;	// or throw?
		value = Double.parseDouble (((JTextField) ui).getText());
	}

	public String toString () {
		return "" + value;
	}
}
