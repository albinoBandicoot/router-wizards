import java.util.*;
public class Invocation {

	public Wizard wiz;
	public PList params;
	public PList globals;
	public ArrayList<Invocation> children;

	public List<Command> commands;

	public Invocation (Wizard w) {
		wiz = w;
		params = new PList (w.params);	// copy current values, without GUI elements attached.
		globals = new PList (w.globals);
	}

	public void generate () throws ArgumentException{
		for (Invocation i : children) {
			i.generate();
		}
		wiz.setParams (params, globals);
		commands = wiz.generate(children);
	}
}
