package toncc;

import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import static java.awt.event.KeyEvent.*;

/** The main frontend using the Toncc class: displays a
 * Toncc table and allows calculating kings' scores for a
 * particular configuration.
 *
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class TonccGame extends TonccRenderer {

	static final int KING_SIZE = 45;
	static final int INITIAL_TOKENS = 6;

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
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
		// Top right
		bounds = cells.get(2).getBounds();
		lab = new JLabel(_join(commands.get(Direction.TOP_RIGHT)));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY() - 1.2 * cellSize), 
				2 * cellSize, 2 * cellSize);
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
		// Left
		bounds = cells.get(7).getBounds();
		lab = new JLabel(_join(commands.get(Direction.LEFT), ",<br>"));
		lab.setBounds((int)(bounds.getX() - 0.5 * cellSize),
				(int)(bounds.getY() - 0.5 * cellSize), 
				2 * cellSize, 2 * cellSize);
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
		// Right
		bounds = cells.get(11).getBounds();
		lab = new JLabel(_join(commands.get(Direction.RIGHT), ",<br>"));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY() - 0.5 * cellSize), 
				2 * cellSize, 2 * cellSize);
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
		// Bottom left
		bounds = cells.get(16).getBounds();
		lab = new JLabel(_join(commands.get(Direction.BOTTOM_LEFT)));
		lab.setBounds((int)(bounds.getX() - cellSize),
				(int)(bounds.getY()), 
				2 * cellSize, 2 * cellSize);
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
		// Bottom right
		bounds = cells.get(18).getBounds();
		lab = new JLabel(_join(commands.get(Direction.BOTTOM_RIGHT)));
		lab.setBounds((int)(bounds.getX() + 1.2 * cellSize),
				(int)(bounds.getY()), 
				2 * cellSize, 2 * cellSize);
		lab.setFont(new Font("Sans", Font.BOLD, 18));
		add(lab, new Integer(2));
	}

	public static void main(String[] args) {
		// enable anti-aliased text:
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");

		int cs = 70,  // cell size
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

		JLabel lab = new JLabel("<html>Virtual T&oacute;ncc by Giacomo Parolini - v1.1 (2015) " +
				"-- get the source at <font color=\"blue\">https://github.com/silverweed/toncc</font></html>");
		lab.setFont(new Font("Sans", Font.PLAIN, 12));
		frame.add(lab, BorderLayout.SOUTH);

		BackgroundPanel container = new BackgroundPanel();

		try {
			container.setImage(ImageIO.read(
					TonccGame.class.getClassLoader()
					.getResourceAsStream("toncc/images/bg.png")));
		} catch(Exception e) {
			System.err.println("[ ERROR ] Couldn't set background image:");
			e.printStackTrace();
			container.setBackground(new Color(0x607270));
		}
		container.setLayout(new GridBagLayout());
		TonccGame renderer = null;
		renderer = new TonccGame(new Toncc(), cs, kcs);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(15, 0, 15, 0);
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = 1;
		c.weightx = 0.7;
		container.add(renderer, c);
		c.insets = new Insets(0,0,0,0);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
		c.weighty = 0.5;
		c.weightx = 0.3;
		container.add(renderer.getKingdoms(), c);
		c.gridy = 1;
		c.gridheight = 1;
		container.add(renderer.playerManager, c);
	
		frame.add(container);
		frame.addKeyListener(renderer.playerMovesListener);

		SwingConsole.run(frame, 832, 624, "Play Toncc!");
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
		}
		playerManager.updateScore();
	}

	/** When only 1 king is left, this procedure assigns it all the remaining cells. */
	private void autoFinish() {
		// Find out which king is left
		King king = null;
		for (King k : kings)
			if (!k.gameOver) {
				king = k;
				break;
			}
		for (TonccCellRenderer cell : cells) {
			if (cell.getCell().id() == TonccCell.Id.MIND) continue;
			if (cell.getCell().getState() == TonccCell.State.FREE) {
				kgCellRenderers.get(cell.getCell().id()).setOwner(king);
				cell.setState(TonccCell.State.CAPTURED);
			}
		}
		king.setTokens(0);
		checkKingsGameOver();
	}

	/** Checks if any king is out of tokens; if all kings are done,
	 * announce game over.
	 */
	public void checkKingsGameOver() {
		for (int i = 0; i < kings.length; ++i) {
			King king = kings[i];
			if (king.gameOver) continue;
			final int kidx = i;
			if (king.getTokens() == 0) {
				int score = 0;
				for (King k : kings) {
					if (!k.gameOver)
						++score;
				}
				// Finalize this king's score
				playerManager.finalizeScore(king, score);
				activeKings.remove(king);
				// Place the king back on the MIND
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

		// This must be done after the first check to properly count
		// the king points
		int gameOverCnt = 0;
		for (King king : kings) {
			if (king.getTokens() == 0) {
				king.gameOver = true;
				++gameOverCnt;
			}
		}

		if (gameOverCnt == kings.length) {
			// pair with highest score so far
			Map.Entry<King, Integer> winner = null;
			// map { king => score }
			Map<King, Integer> scores = new HashMap<>();
			for (King king : kings) {
				int score = king.score;
				if (winner == null || winner.getValue() < score) {
					winner = new AbstractMap.SimpleEntry<>(king, score);
				}
				scores.put(king, score);
			}
			// If 2 kings have the same score, the dominated one wins.
			// A full draw only happens if all 3 kings have the same score.
			int i = 0;
			boolean draw = false;
			King[] winners = new King[2];
			for (Map.Entry<King, Integer> entry : scores.entrySet()) {
				King king = entry.getKey();
				int score = entry.getValue();
				if (score == winner.getValue()) {
					if (i == 2) {
						draw = true;
						break;
					} 
					winners[i++] = king;
				}
			}
			SwingUtilities.invokeLater(() -> {
				kingdoms.repaint();
				repaint();
			});
			if (draw) {
				JOptionPane.showMessageDialog(this, "Game Over! It's a draw.", "Game Over",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				if (winners[1] != null) {
					winner = new AbstractMap.SimpleEntry<>(
							winners[0].dominates(winners[1]) 
							? winners[1] : winners[0], winner.getValue());
				}
				JOptionPane.showMessageDialog(this, "Game Over! Winner is: " + winner.getKey(), "Game Over",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (gameOverCnt == kings.length - 1) {
			autoFinish();
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
		sb.append("<font color=\"yellow\">"+getKeyText(it[2]).replaceAll("NumPad\\-", "")+"</font>");
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
