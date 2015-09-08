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
	static final int INITIAL_TOKENS = 6;

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
			cells.add((TonccCellRenderer)getComponent(i));

		kings[0] = King.RED;
		kings[1] = King.BLUE;
		kings[2] = King.YELLOW;

		playerManager = new PlayerManager(this);

		// get the MIND position
		Rectangle mindBounds = cells.get(9).getBounds();
		for (int i = 0; i < 3; ++i) {
			final JLabel sprite = kings[i].getSprite();
			sprite.setBounds(
					mindBounds.x + kingXOffset[i], 
					mindBounds.y + kingYOffset[i],
					KING_SIZE, 
					KING_SIZE);
			SwingUtilities.invokeLater(() -> sprite.repaint());
			activeKings.add(kings[i]);
		}

		add(kings[0].getSprite(), new Integer(2));
		add(kings[1].getSprite(), new Integer(2));
		add(kings[2].getSprite(), new Integer(3));

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

	void checkCaptures() {
		// Keep track of { cell id => occupier king's index }
		Map<TonccCell.Id, Integer> occupiedBy = new EnumMap<>(TonccCell.Id.class);
		for (int i = 0; i < 3; ++i) {
			final int idx = kings[i].position.asCellIndex();
			if (idx == 9) {
				// ignore the MIND
				continue;
			}
			// Check if idx-th cell is occupied
			TonccCellRenderer cell = cells.get(idx);
			if (cell.getCell().getState() == TonccCell.State.CAPTURED)
				continue;

			final TonccCell.Id cellId =  cell.getCell().id();
			final Integer otheridx = occupiedBy.get(cellId);
			if (otheridx == null) {
				occupiedBy.put(cellId, i);
			} else {
				// Another king is on the cell: check who prevails
				if (kings[i].prevailsOn(kings[otheridx], cellId))
					occupiedBy.put(cellId, i);
			}
		}
		// Capture the cells
		for (Map.Entry<TonccCell.Id, Integer> entry : occupiedBy.entrySet()) {
			final TonccCell.Id cellId = entry.getKey();
			final int kidx = entry.getValue();
			final King king = kings[kidx];
			toncc.getCell(cellId).setOwner(king);
			kgCellRenderers.get(cellId).setOwner(king);
			king.decTokens();
			// Check king gameover
			if (king.getTokens() == 0) {
				int score = 0;
				for (int i = 0; i < 3; ++i) {
					if (!kings[i].gameOver)
						++score;
				}
				king.score += score;
				activeKings.remove(king);
				Rectangle bounds = cells.get(9).getBounds();
				SwingUtilities.invokeLater(() -> {
					king.position = new TonccCoordinate(0, 0);
					king.getSprite().setBounds(
							bounds.x + kingXOffset[kidx],
							bounds.y + kingYOffset[kidx],
							KING_SIZE, KING_SIZE);
					king.repaint();
				});
			}
		}
		playerManager.updateScore();
		int gameOverCnt = 0;
		for (int i = 0; i < 3; ++i) {
			if (kings[i].getTokens() == 0) {
				kings[i].gameOver = true;
				++gameOverCnt;
			}
		}
		if (gameOverCnt == KINGS.length) {
			// game over
			Map.Entry<King, Integer> winner = null;
			Map<King, Integer> scores = new HashMap<>();
			for (King king : kings) {
				int score = king.score;
				if (winner == null || winner.getValue() < score) {
					winner = new AbstractMap.SimpleEntry<>(king, score);
				}
				scores.put(king, score);
			}
			int i = 0;
			boolean draw = false;
			for (int v : scores.values())
				if (v == winner.getValue()) {
					if (++i > 1) {
						draw = true;
						break;
					}
				}

			JOptionPane.showMessageDialog(
					this,
					"Game Over! " + (draw 
						? "It's a draw"
						: "Winner is: " + winner.getKey()),
					"Game Over",
					JOptionPane.INFORMATION_MESSAGE);
		}
		SwingUtilities.invokeLater(() -> {
			kingdoms.repaint();
			repaint();
		});
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
			if (kings[idx].gameOver) return;
			final King king = kings[idx];
			playerManager.selectMove(king, d);
		}
	};

	King[] kings = new King[King.values().length];
	java.util.List<King> activeKings = new ArrayList<>();
	final static int[] kingXOffset = new int[] { 0, KING_SIZE*2/3, KING_SIZE/3 };
	final static int[] kingYOffset = new int[] { 0, 0, KING_SIZE/2 };

	/** The references to all cells in the toncc, indexed from 0 to TONCC_CELLS_NUM-1 */
	java.util.List<TonccCellRenderer> cells = new ArrayList<>();
}
