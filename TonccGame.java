package toncc;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import static java.awt.event.KeyEvent.*;

/** The main frontend using the Toncc class: displays a
 * Toncc table and allows calculating kings' scores for a
 * particular configuration.
 *
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class TonccGame extends TonccRenderer {

	public final static String[] KINGS = { "Red", "Blue", "Yellow" };
	static final int KING_SIZE = 34;

	enum Direction {
		TOP_LEFT,
		TOP,
		TOP_RIGHT,
		LEFT,
		RIGHT,
		BOTTOM_LEFT,
		BOTTOM,
		BOTTOM_RIGHT
	}

	/** { direction: [red, blue, yellow] } */
	private final static Map<Direction, Integer[]> commands = new EnumMap<>(Direction.class);
	static {
		commands.put(Direction.TOP_LEFT,     new Integer[] { VK_Q, VK_U, VK_NUMPAD7 });
		commands.put(Direction.TOP_RIGHT,    new Integer[] { VK_E, VK_O, VK_NUMPAD9 });
		commands.put(Direction.LEFT,         new Integer[] { VK_A, VK_H, VK_NUMPAD4 });
		commands.put(Direction.RIGHT,        new Integer[] { VK_D, VK_K, VK_NUMPAD6 });
		commands.put(Direction.BOTTOM_LEFT,  new Integer[] { VK_Z, VK_B, VK_NUMPAD1 });
		commands.put(Direction.BOTTOM_RIGHT, new Integer[] { VK_C, VK_M, VK_NUMPAD3 });
	}
	
	public TonccGame(final Toncc toncc, int... sizes) {
		super(toncc, sizes);
	
		// Save the references to the cells before we add
		// any other component to this panel
		for (int i = 0; i < getComponentCount(); ++i)
			cells.add(getComponent(i));

		playerManager = new PlayerManager(this);

		king[0] = new King(King.Color.RED);
		king[1] = new King(King.Color.BLUE);
		king[2] = new King(King.Color.YELLOW);

		// get the MIND position
		Rectangle mindBounds = cells.get(9).getBounds();
		for (int i = 0; i < 3; ++i)
			king[i].getSprite().setBounds(
					mindBounds.x + kingXOffset[i], 
					mindBounds.y + kingYOffset[i],
					KING_SIZE, 
					KING_SIZE);

		add(king[0].getSprite(), new Integer(2));
		add(king[1].getSprite(), new Integer(2));
		add(king[2].getSprite(), new Integer(3));

		for(Map.Entry<String,TonccCellRenderer> entry : cellRenderers.entrySet()) {
			final TonccCellRenderer tcr = entry.getValue();
			final String id = entry.getKey();
			if(id.equals("MIND")) continue;
			final JPopupMenu popup = new JPopupMenu("Set cell owner");
			JMenuItem item = new JMenuItem("Free");
			item.addActionListener(e -> {
				tcr.setState(TonccCell.State.FREE);
				kgCellRenderers.get(id).setOwner(null);	
				toncc.getCell(id).setOwner(null);
				SwingUtilities.invokeLater(() -> {
					tcr.repaint();
					kgCellRenderers.get(id).repaint();
				});
				playerManager.updateScore();
			});
			popup.add(item);
			for(final String king : KINGS) {
				item = new JMenuItem(king);
				item.addActionListener(e -> {
					tcr.setState(TonccCell.State.CAPTURED);
					String prevOwner = kgCellRenderers.get(id).getCell().getOwner();
					kgCellRenderers.get(id).setOwner(king);	
					toncc.getCell(id).setOwner(king);
					SwingUtilities.invokeLater(() -> {
						tcr.repaint();
						kgCellRenderers.get(id).repaint();
					});
					playerManager.updateScore(king);
					if(prevOwner != null)
						playerManager.updateScore(prevOwner);
				});
				popup.add(item);
			}
			tcr.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}

		// Position hints
		// TRIGGER WARNING: the following code is horribly hacky, don't judge.

		/// Top left movement
		Rectangle bounds = cells.get(0).getBounds();
		JLabel lab = new JLabel(_join(commands.get(Direction.TOP_LEFT)));
		lab.setBounds((int)(bounds.getX() - cellSize),
				(int)(bounds.getY() - 1.2 * cellSize), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
		// Top right
		bounds = cells.get(2).getBounds();
		lab = new JLabel(_join(commands.get(Direction.TOP_RIGHT)));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY() - 1.2 * cellSize), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
		// Left
		bounds = cells.get(7).getBounds();
		lab = new JLabel(_join(commands.get(Direction.LEFT), ",<br>"));
		lab.setBounds((int)(bounds.getX() - 0.5 * cellSize),
				(int)(bounds.getY() - 0.5 * cellSize), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
		// Right
		bounds = cells.get(11).getBounds();
		lab = new JLabel(_join(commands.get(Direction.RIGHT), ",<br>"));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY() - 0.5 * cellSize), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
		// Bottom left
		bounds = cells.get(16).getBounds();
		lab = new JLabel(_join(commands.get(Direction.BOTTOM_LEFT)));
		lab.setBounds((int)(bounds.getX() - cellSize),
				(int)(bounds.getY()), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
		// Bottom right
		bounds = cells.get(18).getBounds();
		lab = new JLabel(_join(commands.get(Direction.BOTTOM_RIGHT)));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY()), 
				2 * cellSize, 2 * cellSize);
		add(lab, new Integer(2));
	}

	public static void main(String[] args) {
		int cs = -1,  // cell size
		    kcs = -1; // kingdoms (mind) cell size

		for(int i = 0; i < args.length; ++i) {
			final String arg = args[i];
			switch(arg) {
			case "-h":
			case "--help":
				System.err.println("Usage: TonccGame [-t toncc_cell_size] [-k mind_cell_size]");
				return;
			case "-t":
				++i;
				cs = new Integer(args[i]);
				break;
			case "-k":
				++i;
				kcs = new Integer(args[i]);
				break;
			}
		}
		JFrame frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		TonccGame renderer = null;
		if(cs != -1 || kcs != -1)
			renderer = new TonccGame(new Toncc(), cs, kcs);
		else
			renderer = new TonccGame(new Toncc());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(15, 0, 15, 0);
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		frame.add(renderer, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
		frame.add(renderer.getKingdoms(), c);
		c.gridy = 1;
		c.gridheight = 1;
		frame.add(renderer.playerManager, c);
		
		frame.addKeyListener(renderer.playerMovesListener);

		SwingConsole.run(frame, "Play Toncc!");
	}

	private static String _join(Integer[] it) {
		return _join(it, ", ");
	}

	private static String _join(Integer[] it, String del) {
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<font color=\"red\">"+getKeyText(it[0])+"</font>");
		sb.append(del);
		sb.append("<font color=\"blue\">"+getKeyText(it[1])+"</font>");
		sb.append(del);
		sb.append("<font color=\"#fd9100\">"+getKeyText(it[2]).replaceAll("NumPad\\-", "")+"</font>");
		sb.append("</html>");
		return sb.toString();
	}

	private PlayerManager playerManager;
	private KeyListener playerMovesListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			int idx = -1;
			Direction d = null;
outer:
			for(Map.Entry<Direction, Integer[]> pairs : commands.entrySet()) {
				d = pairs.getKey();
				Integer[] codes = pairs.getValue();
				for(int i = 0; i < codes.length; ++i) {
					if(codes[i] == code) {
						idx = i;
						break outer;
					}
				}
			}
			if(idx == -1) return;
			final String king = KINGS[idx];
			playerManager.selectMove(king, d);
		}
	};

	King[] king = new King[3];
	private int[] kingXOffset = new int[] { 0, KING_SIZE*2/3, KING_SIZE/3 };
	private int[] kingYOffset = new int[] { 0, 0, KING_SIZE/2 };

	/** The references to all cells in the toncc, indexed from 0 to TONCC_CELLS_NUM-1 */
	java.util.List<Component> cells = new ArrayList<>();
}
