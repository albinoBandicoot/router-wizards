import javax.swing.*;
public class BooleanParam extends Param {

	public boolean value;
	public boolean default_value;

	public BooleanParam (boolean val, boolean gen_gui) {
		value = val;
		default_value = val;
		if (gen_gui) createGUI();
	}

	public BooleanParam (boolean val, boolean def, boolean gen_gui) {
		value = val;
		default_value = def;
		if (gen_gui) createGUI();
	}

	public Param copy () {
		return new BooleanParam (value, default_value, false);
	}

	public void setValue (Param p) throws ArgumentException {
		if (p instanceof BooleanParam) {
			value = ((BooleanParam) p).value;
			updateGUI();
		} else {
			throw new ArgumentException ("Not a Boolean param!");
		}
	}

	public void createGUI () {
		ui = new JCheckBox ("");
	}

	public void updateGUI () {
		if (ui == null) return;
		((JCheckBox) ui).setSelected (value);
	}

	public void readGUI () {
		if (ui == null) return;	// or throw?
		value = ((JCheckBox) ui).isSelected();
	}

	public String toString () {
		return "" + value;
	}
}
