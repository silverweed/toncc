package toncc;

import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.util.*;

/** Graphically renders a Toncc class; only works correctly
 * for a regular Toncc table (i.e with 18 cells and a 3-4-5-4-3 
 * layout)
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class TonccRenderer extends JLayeredPane {

	private final static int CELL_SIZE = 54; // pixels
	private final static int KINGDOMS_CELL_SIZE = 34; // pixels

	/** @param toncc The Toncc table to render
	 *  @param sizes [optional] cell_size, kingdoms_cell_size (-1 means default)
	 */
	public TonccRenderer(final Toncc toncc, int... sizes) {
		this.toncc = toncc;
		
		/* Here I prefer explicit all components' bounds rather than using
		 * a LayoutManager, since the cells have a fixed size, and creating
		 * the correct layout with a LayoutManager would be difficult.
		 */
		int[] ncells = { 3, 4, 5, 4, 3 };
		int count = 0;
		cellSize = CELL_SIZE;
		kingdomsCellSize = KINGDOMS_CELL_SIZE;
		if(sizes.length > 0) {
			if(sizes.length > 1) {
				if(sizes[1] > 0)
					kingdomsCellSize = sizes[1];
			}
			if(sizes[0] > 0)
				cellSize = sizes[0];
		}
		for(int line = 0; line < 5; ++line) {
			for(int i = 0; i < ncells[line]; ++i) {
				TonccCellRenderer cell = null;
				if(line == 2 && i == 2) {
					// this is the MIND
					if(cellSize != CELL_SIZE)
						cell = new TonccCellRenderer(new TonccCell("MIND"), cellSize);
					else
						cell = new TonccCellRenderer(new TonccCell("MIND"));
				} else {
					if(cellSize != CELL_SIZE)
						cell = new TonccCellRenderer(toncc.getCell(count++), cellSize);
					else
						cell = new TonccCellRenderer(toncc.getCell(count++));
				}
				int x = line == 2 
					? cellSize * i
					: line % 2 == 0 
						? cellSize * (i + 1)
						: cellSize / 2 + cellSize * i,
				    y = line * cellSize,
				    w = cellSize;
				cell.setBounds(x, y, w, w);
				cellRenderers.put(cell.getCell().id(), cell);
				add(cell, new Integer(1));
			}
		}

		// create kingdoms occupation panel
		kingdoms = new JPanel(true) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(kingdomsCellSize * 8, kingdomsCellSize * 8);
			}
		};
		kingdoms.setLayout(null);
		/* Here we must deal with the different order of the cells in the MIND;
		 * we must display cells line-by-line, for ease of code,
		 * but in the MIND they're ordered by kingdom 
		 * (starting from YI, counter-clockwise); therefore we do a simple
		 * trick to display them correctly in the triangle.
		 */
		count = 0;
		// first line (a single cell)
		TonccCellRenderer cell = new TonccCellRenderer(new TonccCell(toncc.MIND[count++]), kingdomsCellSize);
		cell.setBounds(kingdomsCellSize * 3, 0, kingdomsCellSize, kingdomsCellSize);
		kgCellRenderers.put(cell.getCell().id(), cell);
		kingdoms.add(cell);
		// mid lines (2 cells per line)
		for(int line = 1; line < 6; ++line) {
			for(int i = 0; i < 2; ++i) {
				cell = 
					new TonccCellRenderer(new TonccCell(
						i == 0 ? toncc.MIND[count] : toncc.MIND[toncc.TONCC_CELLS_NUM - count]),
						kingdomsCellSize);
				cell.setBounds(	(int)(kingdomsCellSize * (3 - line / 2. + line * i)), 
						kingdomsCellSize * line,
						kingdomsCellSize,
						kingdomsCellSize);
				kgCellRenderers.put(cell.getCell().id(), cell);
				kingdoms.add(cell);			
				if(i == 1) ++count;
			}
		}
		// last line (seven cells, not two)
		for(int i = 0; i < 7; ++i) {
			cell = new TonccCellRenderer(new TonccCell(toncc.MIND[count++]), kingdomsCellSize);
			cell.setBounds(	(int)(kingdomsCellSize * i), 
					kingdomsCellSize * 6,
					kingdomsCellSize,
					kingdomsCellSize);
			kgCellRenderers.put(cell.getCell().id(), cell);
			kingdoms.add(cell);			
		}
	}

	public final JPanel getKingdoms() { return kingdoms; }
	public final int getCellSize() { return cellSize; }
	public final int getKingdomsCellSize() { return kingdomsCellSize; }

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(cellSize * 8, cellSize * 8);
	}

	public static void main(String[] args) {
		int cs = -1, kcs = -1;
		if(args.length > 0) {
			if(args.length > 1)
				kcs = new Integer(args[1]);
			cs = new Integer(args[0]);
		}
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(1,2));
		TonccRenderer renderer = null;
		if(cs != -1 || kcs != -1)
			renderer = new TonccRenderer(new Toncc(), cs, kcs);
		else
			renderer = new TonccRenderer(new Toncc());
		frame.add(renderer);
		frame.add(renderer.kingdoms);
		SwingConsole.run(frame, "Toncc Renderer");
	}

	protected Map<String,TonccCellRenderer> cellRenderers = new HashMap<>();
	protected Map<String,TonccCellRenderer> kgCellRenderers = new HashMap<>();
	protected final Toncc toncc;
	private final JPanel kingdoms;
	private int cellSize, kingdomsCellSize;
}
