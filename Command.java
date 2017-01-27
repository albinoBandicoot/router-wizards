public class Command {

	public static double travel_fr;
	public static double cut_fr;
	public static double retract_fr;
	public static double plunge_fr;
	public static double safe_z;

	public static double x, y, z;	// current location

	public String opcode;
	public double[] args;

	public Command (String op, double... a) {
		opcode = op;
		args = new double[4];
		for (int i=0; i < Math.min(4, a.length); i++) {
			args[i] = a[i];
		}
	}


	public static Command move (double x, double y, double z, double feed) {
		Command.x = x;
		Command.y = y;
		Command.z = z;
		return new Command ("mova", x, y, z, feed);
	}


	public static Command lift () {
		return move (x, y, safe_z, travel_fr);
	}

	public static Command movx (double x, double feed) {
		return move (x, y, z, feed);
	}

	public static Command movy (double y, double feed) {
		return move (x, y, z, feed);
	}

	public static Command movz (double z, double feed) {
		return move (x, y, z, feed);
	}

	public static Command movxy (double x, double y, double feed) {
		return move (x, y, z, feed);
	}

	public static Command cut (double x, double y, double z) {
		return move (x, y, z, cut_fr);
	}

	public static Command cut (double x, double y) {
		return cut (x, y, z);
	}

	public static Command rapid (double x, double y, double z) {
		return move (x,y,z,travel_fr);
	}

	public static Command rapid (double x, double y) {
		return move (x,y,z,travel_fr);
	}

	public static Command plunge (double depth) {
		return move (x,y, z - depth, plunge_fr);
	}

	public static Command retract (double amt) {
		return move (x, y, z + amt, retract_fr);
	}

	public String toString () {
		return opcode + " X" + args[0] + " Y" + args[1] + " Z" + args[2] + " F" + args[3];
	}
}
