package toncc;

/** Class representing a Toncc cell.
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
class TonccCell {
	
	public enum State { FREE, CAPTURED };

	public TonccCell(final String id) {
		this.id = id;
	}

	public final String id() { return id; }
	public final State getState() { return state; }
	public final String getOwner() { return owner; }

	public void setState(final State state) {
		this.state = state;
	}
	public void setOwner(final String owner) {
		this.owner = owner;
	}

	protected final String id;
	protected State state = State.FREE;
	protected String owner;
}
