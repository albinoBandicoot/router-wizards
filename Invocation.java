import java.util.*;
public class Invocation {

		public Wizard wiz;
		public PList params;
		public ArrayList<Invocation> children;

		public List<Command> commands;

		public Invocation (Wizard w) {
				wiz = w;
				params = new PList (w.params);	// copy current values, without GUI elements attached.
		}

		public void generate () {
				for (Invocation i : children) {
						i.generate();
				}
				wiz.setParams (params);
				commands = wiz.generate(children);
		}
}
