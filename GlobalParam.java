import javax.swing.*;
public class GlobalParam extends Param {

	public boolean override;
	public double local_value;

	public String name;

	private JCheckBox override_ui;
	private JLabel label;
	private JTextField value_ui;


	public GlobalParam (String name, double loc, boolean over, boolean gen_gui) {
		this.name = name;
		local_value = loc;
		override = over;
		if (gen_gui) createGUI();
	}

	public Param copy () {
		return new GlobalParam (name, local_value, override, false);
	}

	public void setValue (Param p) throws ArgumentException{
		if (p instanceof GlobalParam) {
			GlobalParam g = (GlobalParam) p;
			local_value = g.local_value;
			override = g.override;
		} else {
			throw new ArgumentException ("Not a global parameter!");
		}
	}

	public void createGUI () {
		ui = new JPanel();
		try {
			label = new JLabel (name + " (" + Wizard.globals.getDouble(name) + ")");
		} catch (ArgumentException e) {
		}
		label.setBounds (0,0,200, 25);
		override_ui = new JCheckBox();
		override_ui.setBounds (200,0,50,25);
		value_ui = new JTextField (local_value + "");
		value_ui.setBounds (250,0,125,25);
		ui.setLayout(null);
		ui.add (override_ui);
		ui.add (label);
		ui.add (value_ui);
	}

	public void updateGUI () {
		value_ui.setText (local_value + "");
		try {
			label.setText (name + " (" + Wizard.globals.getDouble(name) + ")");
		} catch (ArgumentException e) {
		}
		override_ui.setSelected (override);
	}

	public void readGUI () {
		local_value = Double.parseDouble(value_ui.getText());
		override = override_ui.isSelected();
	}

	public double getValue () {
		if (override) {
			return local_value;
		}
		try {
			return Wizard.globals.getDouble(name);
		} catch (ArgumentException e) {
		}
		return 0;
	}
}
