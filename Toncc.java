package toncc;

import java.util.*;

/** Class representing a Toncc table.
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class Toncc {
	
	/** Array containing all possible cell types */
	public static final String[] MIND = {
		"YI",
		"Yd",
		"Ycd",
		"Bcd",
		"BcII",
		"BII",
		"RII",
		"Rdd",
		"Rsdd",
		"Ysdd",
		"YsIII",
		"YIII",
		"BIII",
		"Bddd",
		"Btddd",
		"Rtddd",
		"RtI",
		"RI",	
	};
	public static final int TONCC_CELLS_NUM = MIND.length;

	/** Create a random Toncc table. */
	public Toncc() {
		String[] randCells = getShuffled(MIND);
		for(int i = 0; i < randCells.length; ++i) 
			cells[i] = new TonccCell(randCells[i]);
	}

	/** Create a Toncc table from a given scheme */
	public Toncc(final String[] scheme) {
		if(scheme.length != TONCC_CELLS_NUM)
			throw new IllegalArgumentException("Given scheme contains "+
				scheme.length+" cells instead of "+TONCC_CELLS_NUM+"!");
		for(int i = 0; i < TONCC_CELLS_NUM; ++i)
			cells[i] = new TonccCell(scheme[i]);
	}
	
	public final TonccCell[] getCells() { return cells; }
	public final TonccCell getCell(final int i) { return cells[i]; }
	public final TonccCell getCell(final String id) {
		for(TonccCell tc: cells) {
			if(tc.id().equals(id))
				return tc;
		}
		return null;
	}
	public final int getPosition(final String id) {
		for(int i = 0; i < cells.length; ++i)
			if(cells[i].id().equals(id)) return i;
		return -1;
	}

	/** @return map { owner: [ { cell1, cell2, ...}, { cell1, ... }, ... ], ... } of kingdoms */
	public final Map<String,List<Set<String>>> getKingdoms() {
		Map<String,List<Set<String>>> kingdoms = new HashMap<>();
		Set<String> owners = new HashSet<>();
		for(TonccCell cell : cells) {
			if(cell.getOwner() != null) {
				owners.add(cell.getOwner());
			}
		}
		for(String owner : owners) 
			kingdoms.put(owner, getKingdoms(owner));

		return kingdoms;
	}

	public final List<Set<String>> getKingdoms(final String owner) {
		List<Set<String>> kingdoms = new ArrayList<>();
		// for each possible kingdom, check if it's entirely owned by someone
		for(int i = 0; i < TONCC_CELLS_NUM - 1; ++i) {
			// {5,6,7}, {11,12,13} and {17,0,1} are not real kingdoms!
			if(i == 5 || i == 11 || i == 17) continue;
			boolean owned = true;
			for(int j = 0; j < 3; ++j) {
				if(	!(getCell(MIND[(i+j) % TONCC_CELLS_NUM]).getOwner() != null &&
					getCell(MIND[(i+j) % TONCC_CELLS_NUM]).getOwner().equals(owner))
				) {
					//System.err.println("cell["+(i+j)%TONCC_CELLS_NUM+"] has owner "+
					//	cells[(i+j)%TONCC_CELLS_NUM].getOwner());
					owned = false;
					break;
				}
			}
			if(owned) {
				//System.err.println("isOwned: " + cells[i].id());
				kingdoms.add(new HashSet<String>(Arrays.asList(
					new String[] { MIND[i], MIND[i + 1], MIND[(i + 2) % TONCC_CELLS_NUM] })));
			}
		}
		//System.err.println(kingdoms);
		return kingdoms;
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

	private int cellIdToNum(final String id) {
		for(int i = 0; i < MIND.length; ++i)
			if(MIND[i].equals(id)) return i;
		return -1;
	}
	
	private TonccCell[] cells = new TonccCell[TONCC_CELLS_NUM];
}
