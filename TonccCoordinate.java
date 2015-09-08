package toncc;

/** A Toncc coordinate is a 3-ple { x, y, z } such that
 *    z = y - x
 * and mod(x, y, z) === |x| + |y| + |z| <= 4.
 * The geometry of the Toncc is the following:
 * - the MIND has coordinates (0, 0, 0);
 * - moving in TOP_LEFT direction decrements x and y by 1 (moving BOTTOM_RIGHT increments);
 * - moving in TOP_RIGHT direction decrements only x by 1 (moving BOTTOM_LEFT increments);
 * - moving in LEFT direction decrements only y by 1 (moving RIGHT increments);
 * - if a move would result in a coordinate with |x|, |y| or |z| greater than 2, the
 *   following boundary conditions apply:
 *   1) if direction was TOP_RIGHT or BOTTOM_LEFT, swap original x and z;
 *   2) if direction was TOP_LEFT or BOTTOM_RIGHT, swap original x and y, and flip their sign;
 *   3) if direction was LEFT or RIGHT, swap original y and z, and flip their sign.
 * As an extra constraint, a king cannot step back onto the MIND once it exited it.
 *
 * @author Giacomo Parolini
 * @license GNU GPL v3
 */
public class TonccCoordinate {
	public TonccCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
		z = y - x;
	}
	
	void move(Direction d) {
		int origX = x, origY = y, origZ = y - x;
		switch(d) {
		case TOP_LEFT:
			--y;
			--x;
			break;
		case TOP_RIGHT:
			--x;
			break;
		case LEFT:
			--y;
			break;
		case RIGHT:
			++y;
			break;
		case BOTTOM_LEFT:
			++x;
			break;
		case BOTTOM_RIGHT:
			++y;
			++x;
		}
		// Avoid stepping on the MIND after exiting it
		if (x == 0 && y == 0) move(d);
		z = y - x;
		if (Math.abs(z) > 2 || Math.abs(x) > 2 || Math.abs(y) > 2) {
			switch(d) {
			case TOP_LEFT:
			case BOTTOM_RIGHT:
				{
					int tmp = origY;
					y = -origX;
					x = -tmp;
					break;
				}
			case TOP_RIGHT:
			case BOTTOM_LEFT:
				x = origZ;
				break;
			default:
				y = -origZ;
			}
		}
		z = y - x;
	}

	public int getX() { return x; }
	public int getY() { return y; }

	/** @return the current position as the cell index in the Toncc */
	public int asCellIndex() {
		return 9 + y + (int)((Math.abs(x) == 1 ? 4 : 3.5) * x);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + "; idx = " + asCellIndex() + ")";
	}

	private int mod() {
		return Math.abs(x) + Math.abs(y) + Math.abs(z);
	}

	private int x, y, z;
}
