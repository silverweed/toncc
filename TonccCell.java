package toncc;

/** Class representing a Toncc cell.
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
class TonccCell {
	
	public enum State { FREE, CAPTURED };

	public enum Id {
		YI,
		Yd,
		Ycd,
		Bcd,
		BcII,
		BII,
		RII,
		Rdd,
		Rsdd,
		Ysdd,
		YsIII,
		YIII,
		BIII,
		Bddd,
		Btddd,
		Rtddd,
		RtI,
		RI,
		MIND
	}

	public TonccCell(final Id id) {
		this.id = id;
	}

	public final Id id() { return id; }
	public final State getState() { return state; }
	public final King getOwner() { return owner; }

	public void setState(final State state) {
		this.state = state;
	}

	public void setOwner(final King owner) {
		this.owner = owner;
		if (owner == null)
			state = State.FREE;
		else
			state = State.CAPTURED;
	}

	protected final Id id;
	protected State state = State.FREE;
	protected King owner;
}
