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

		for(String king : TonccGame.KINGS) {
			scoreLabels.put(king, new JLabel("0")); 
			tokenLabels.put(king, new JLabel(""+INITIAL_TOKENS));
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
			add(new JLabel(undecidedIcon));
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
		label.repaint();

		// Update number of used tokens
		for(String color : TonccGame.KINGS) {
			int nOwned = toncc.getNOwned(color);
			JLabel lab = tokenLabels.get(color);
			lab.setText(""+(INITIAL_TOKENS - nOwned));
			lab.repaint();
		}
	}

	private final Toncc toncc;
	private ImageIcon decidedIcon, undecidedIcon;
	private Map<String, JLabel> scoreLabels = new HashMap<>(),
		                    tokenLabels = new HashMap<>();
}
