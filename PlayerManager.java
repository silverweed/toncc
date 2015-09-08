package toncc;

import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.util.*;

class PlayerManager extends JPanel {

	public PlayerManager(final TonccGame tonccGame) {

		this.tonccGame = tonccGame;
		toncc = tonccGame.toncc;

		// King : Decided : Score : Remaining tokens
		setLayout(new GridLayout(4, 4, 20, 3));
		
		// Load decided/undecided icons
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

		for(King king : tonccGame.kings) {
			// Load kings' sprites
			try {
				add(new JLabel(new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/" + king.getColorString().toLowerCase() + ".png"))
					.getScaledInstance(-1, TonccGame.KING_SIZE, Image.SCALE_SMOOTH))));
			} catch(IOException e) {
				System.err.println("[PlayerManager] Error loading image:");
				e.printStackTrace();
			}
			// Add labels to the gridlayout
			scoreLabels.put(king, new JLabel("<html><font color=\"blue\">0</font></html>"));
			scoreLabels.get(king).setFont(new Font("Sans", Font.BOLD, 18));
			tokenLabels.put(king, new JLabel("<html><font color=\"blue\">"+TonccGame.INITIAL_TOKENS+"</font></html>"));
			tokenLabels.get(king).setFont(new Font("Sans", Font.BOLD, 18));
			moveLabels.put(king, new JLabel(undecidedIcon));
			add(moveLabels.get(king));
			add(scoreLabels.get(king));
			add(tokenLabels.get(king));
		}
	}

	void updateScore() {
		for(King k : tonccGame.kings) {
			if (!k.gameOver)
				updateScore(k);
		}
	}

	void updateScore(final King king) {
		int score = 0;
		for(Set<TonccCell.Id> kingdom : toncc.getKingdoms(king)) {
			for(TonccCell.Id id : kingdom) {
				switch(id.toString().charAt(0)) {
				case 'B':
					if(king == King.BLUE) score += 1;
					else if(king == King.RED) score += 2;
					else score += 3;
					break;
				case 'R':
					if(king == King.RED) score += 1;
					else if(king == King.YELLOW) score += 2;
					else score += 3;
					break;
				case 'Y':
					if(king == King.YELLOW) score += 1;
					else if(king == King.BLUE) score += 2;
					else score += 3;
					break;
				}
			}
		}
		king.score = score;
		JLabel label = scoreLabels.get(king);
		label.setText("<html><font color=\"blue\">"+score+"</font></html>");
		SwingUtilities.invokeLater(() -> label.repaint());

		// Update number of used tokens
		for(King k : tonccGame.kings) {
			JLabel lab = tokenLabels.get(k);
			lab.setText("<html><font color=\"blue\">"+k.getTokens()+"</font></html>");
			SwingUtilities.invokeLater(() -> lab.repaint());
		}
	}

	public void finalizeScore(final King king, final int extra) {
		updateScore(king);
		king.score += extra;
		scoreLabels.get(king).setText(""+king.score);
	}

	void selectMove(final King king, final Direction direction) {
		selectedMove.put(king, direction);
		if(selectedMove.size() == tonccGame.activeKings.size()) {
			moveKings();
			tonccGame.checkCaptures();
			tonccGame.checkKingsGameOver();
		} else {
			moveLabels.get(king).setIcon(decidedIcon);
			SwingUtilities.invokeLater(() -> moveLabels.get(king).repaint());
		}
	}

	private void moveKings() {
		for(Map.Entry<King, Direction> pair : selectedMove.entrySet()) {
			final King king = pair.getKey();
			final Direction d = pair.getValue();
			final int kidx = Arrays.binarySearch(tonccGame.kings, king);
			king.move(d);
		}
		selectedMove.clear();
		SwingUtilities.invokeLater(() -> {
			for (Map.Entry<King, JLabel> entry : moveLabels.entrySet()) {
				entry.getValue().setIcon(undecidedIcon);

				// Move king's sprite
				final King king = entry.getKey();
				final int idx = king.position.asCellIndex();
				final Rectangle bounds = tonccGame.cells.get(idx).getBounds();
				final int kidx = Arrays.binarySearch(tonccGame.kings, king);
				if (!king.gameOver) {
					king.setBounds(
							bounds.x + TonccGame.kingXOffset[kidx],
							bounds.y + TonccGame.kingYOffset[kidx],
							TonccGame.KING_SIZE, TonccGame.KING_SIZE);
					king.repaint();
				}
			}
		});
	}

	private final TonccGame tonccGame;
	private final Toncc toncc;
	private ImageIcon decidedIcon, undecidedIcon;

	private Map<King, JLabel> scoreLabels = new EnumMap<>(King.class),
		                  tokenLabels = new EnumMap<>(King.class),
				  moveLabels  = new EnumMap<>(King.class);
	private Map<King, Direction> selectedMove = new EnumMap<>(King.class);
}
