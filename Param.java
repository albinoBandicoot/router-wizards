import javax.swing.*;
public abstract class Param {

		public JComponent ui;

		public abstract Param copy ();

		public abstract void setValue (Param p) throws ArgumentException;

		public abstract void createGUI ();
		public abstract void updateGUI ();
		public abstract void readGUI ();


}

