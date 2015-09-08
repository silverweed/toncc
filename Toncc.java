package toncc;

import java.util.*;

/** Class representing a Toncc table.
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class Toncc {
	
	/** Array containing all possible cell types */
	public static final TonccCell.Id[] MIND = new TonccCell.Id[] {
		TonccCell.Id.YI,
		TonccCell.Id.Yd,
		TonccCell.Id.Ycd,
		TonccCell.Id.Bcd,
		TonccCell.Id.BcII,
		TonccCell.Id.BII,
		TonccCell.Id.RII,
		TonccCell.Id.Rdd,
		TonccCell.Id.Rsdd,
		TonccCell.Id.Ysdd,
		TonccCell.Id.YsIII,
		TonccCell.Id.YIII,
		TonccCell.Id.BIII,
		TonccCell.Id.Bddd,
		TonccCell.Id.Btddd,
		TonccCell.Id.Rtddd,
		TonccCell.Id.RtI,
		TonccCell.Id.RI
	};
	public static final int TONCC_CELLS_NUM = MIND.length;

	/** Create a random Toncc table. */
	public Toncc() {
		TonccCell.Id[] randCells = getShuffled(MIND);
		for(int i = 0; i < randCells.length; ++i) 
			cells[i] = new TonccCell(randCells[i]);
	}

	public final TonccCell[] getCells() { return cells; }
	public final TonccCell getCell(final int i) { return cells[i]; }
	public final TonccCell getCell(final TonccCell.Id id) {
		for (TonccCell cell : cells)
			if (cell.id() == id)
				return cell;
		return null;
	}
	public final int getPosition(final String id) {
		for(int i = 0; i < cells.length; ++i)
			if(cells[i].id().equals(id)) return i;
		return -1;
	}

	/** @return map { owner: [ { cell1, cell2, ...}, { cell1, ... }, ... ], ... } of kingdoms */
	public final Map<King, List<Set<TonccCell.Id>>> getKingdoms() {
		Map<King, List<Set<TonccCell.Id>>> kingdoms = new EnumMap<>(King.class);
		Set<King> owners = EnumSet.noneOf(King.class);
		for(TonccCell cell : cells) {
			if(cell.getOwner() != null) {
				owners.add(cell.getOwner());
			}
		}
		for(King owner : owners) 
			kingdoms.put(owner, getKingdoms(owner));

		return kingdoms;
	}

	public final List<Set<TonccCell.Id>> getKingdoms(final King owner) {
		List<Set<TonccCell.Id>> kingdoms = new ArrayList<>();
		// for each possible kingdom, check if it's entirely owned by someone
		for(int i = 0; i < TONCC_CELLS_NUM - 1; ++i) {
			// {5,6,7}, {11,12,13} and {17,0,1} are not real kingdoms!
			if(i == 5 || i == 11 || i == 17) continue;
			boolean owned = true;
			for(int j = 0; j < 3; ++j) {
				final King cellOwner = getCell(MIND[(i+j) % TONCC_CELLS_NUM]).getOwner();
				if(cellOwner == null || cellOwner != owner) {
					owned = false;
					break;
				}
			}
			if(owned) {
				kingdoms.add(EnumSet.of(MIND[i], MIND[i + 1], MIND[(i + 2) % TONCC_CELLS_NUM]));
			}
		}
		return kingdoms;
	}

	/** Returns number of cells owned by `owner` */
	public final int getNOwned(final King owner) {
		int n = 0;
		for(final TonccCell cell : cells) {
			if(cell.getOwner() != null && cell.getOwner() == owner)
				++n;
		}
		return n;
	}

	/////// PRIVATE METHODS AND FIELDS ///////

  	private static <T> T[] getShuffled(final T[] arr) {
		Random rng = new Random();
		T[] newarr = Arrays.copyOf(arr, arr.length);
		for(int i = newarr.length - 1; i > 0; --i) {
			int index = rng.nextInt(i + 1);
			T a = newarr[index];
			newarr[index] = newarr[i];
			newarr[i] = a;
		}
		return newarr;
	}

	private TonccCell[] cells = new TonccCell[TONCC_CELLS_NUM];
}
