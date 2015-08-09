package toncc;

import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.util.*;

class PlayerManager extends JPanel {
	private static final int KING_SIZE = 34;
	private static final int INITIAL_TOKENS = 6;

	public PlayerManager(final Toncc toncc) {

		this.toncc = toncc;

		for(String color : TonccGame.KINGS) {
			positions.put(color, new TonccCoordinate(0, 0));
			scoreLabels.put(color, new JLabel("0")); 
			tokenLabels.put(color, new JLabel(""+INITIAL_TOKENS));
			moveLabels.put(color, new JLabel(undecidedIcon));
		}

		// King : Decided : Score : Remaining tokens
		setLayout(new GridLayout(4, 4, 20, 3));
		
		try {
			decidedIcon = new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/decided.png")));
			undecidedIcon = new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/undecided.png")));
		} catch(IOException e) {
			System.err.println("[PlayerManager] Error loading image:");
			e.printStackTrace();
		}

		add(new JLabel("KING"));
		add(new JLabel("MOVED"));
		add(new JLabel("SCORE"));
		add(new JLabel("TOKENS"));

		for(String color : TonccGame.KINGS) {
			try {
				add(new JLabel(new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/" + color.toLowerCase() + ".png"))
					.getScaledInstance(-1, KING_SIZE, Image.SCALE_SMOOTH))));
			} catch(IOException e) {
				System.err.println("[PlayerManager] Error loading image:");
				e.printStackTrace();
			}
			add(moveLabels.get(color));
			add(scoreLabels.get(color));
			add(tokenLabels.get(color));
		}
	}

	void updateScore() {
		for(final String king : TonccGame.KINGS)
			updateScore(king);
	}

	void updateScore(final String king) {
		int score = 0;
		for(Set<String> kingdom : toncc.getKingdoms(king)) {
			for(String id : kingdom) {
				switch(id.charAt(0)) {
				case 'B':
					if(king.equals("Blue")) score += 1;
					else if(king.equals("Red")) score += 2;
					else score += 3;
					break;
				case 'R':
					if(king.equals("Red")) score += 1;
					else if(king.equals("Yellow")) score += 2;
					else score += 3;
					break;
				case 'Y':
					if(king.equals("Yellow")) score += 1;
					else if(king.equals("Blue")) score += 2;
					else score += 3;
					break;
				}
			}
		}
		JLabel label = scoreLabels.get(king);
		label.setText(""+score);
		SwingUtilities.invokeLater(() -> label.repaint());

		// Update number of used tokens
		for(String color : TonccGame.KINGS) {
			int nOwned = toncc.getNOwned(color);
			JLabel lab = tokenLabels.get(color);
			lab.setText(""+(INITIAL_TOKENS - nOwned));
			SwingUtilities.invokeLater(() -> lab.repaint());
		}
	}

	void selectMove(final String king, final TonccGame.Direction direction) {
		selectedMove.put(king, direction);
		if(selectedMove.size() == TonccGame.KINGS.length) {
			moveKings();
		} else {
			moveLabels.get(king).setIcon(decidedIcon);
			SwingUtilities.invokeLater(() -> moveLabels.get(king).repaint());
		}
	}

	private void moveKings() {
		for(Map.Entry<String, TonccGame.Direction> pair : selectedMove.entrySet()) {
			final String col = pair.getKey();
			final TonccGame.Direction d = pair.getValue();
			positions.get(col).move(d);
		}
		selectedMove.clear();
	}

	private final Toncc toncc;
	private ImageIcon decidedIcon, undecidedIcon;

	private Map<String, JLabel> scoreLabels = new HashMap<>(),
		                    tokenLabels = new HashMap<>(),
				    moveLabels  = new HashMap<>();
	private Map<String, TonccGame.Direction> selectedMove = new HashMap<>();
	private Map<String, TonccCoordinate> positions = new HashMap<>();

	/** A Toncc coordinate is a 3-ple { x, y, z } such that
	 *    z = y - x
	 * and mod(x, y, z) === |x| + |y| + |z| <= 4.
	 * The geometry of the Toncc is the following:
	 * - the MIND has coordinates (0, 0, 0);
	 * - moving in TOP_LEFT direction decrements x and y by 1 (moving BOTTOM_RIGHT increments);
	 * - moving in TOP_RIGHT direction decrements only x by 1 (moving BOTTOM_LEFT increments);
	 * - moving in LEFT direction decrements only y by 1 (moving RIGHT increments);
	 * - if a move would result in a coordinate with mod > 4, the
	 *   boundary conditions apply:
	 *   1) if direction was *_LEFT, swap original x and z;
	 *   2) if direction was *_RIGHT, swap original x and y, and flip their sign;
	 *   3) else, just flip all coordinates' signs.
	 */
	private class TonccCoordinate {
		TonccCoordinate(int x, int y) {
			this.x = x;
			this.y = y;
			z = y - x;
		}
		
		void move(TonccGame.Direction d) {
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
			z = y - x;
			if (mod() > 4) {
				switch(d) {
				case TOP_LEFT:
				case BOTTOM_LEFT:
					{
						int tmp = z;
						z = x;
						x = tmp;
						break;
					}
				case TOP_RIGHT:
				case BOTTOM_RIGHT:
					{
						int tmp = y;
						y = -x;
						x = -tmp;
						break;
					}
				default:
					y = -y;
					x = -x;
				}
			}
		}

		public int getX() { return x; }
		public int getY() { return y; }

		@Override
		public String toString() {
			return "(" + x + ", " + y + ", " + z + ")";
		}

		private int mod() {
			return Math.abs(x) + Math.abs(y) + Math.abs(z);
		}

		private int x, y, z;
	}
}
