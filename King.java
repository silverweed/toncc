package toncc;

import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;

class King {
	public enum Color {
		RED, BLUE, YELLOW;
		
		public King.Color getMediumColor() {
			switch (this) {
			case RED:
				return Color.YELLOW;
			case BLUE:
				return Color.RED;
			}
			return Color.BLUE;
		}

		public King.Color getWeakColor() {
			switch (this) {
			case RED:
				return Color.BLUE;
			case BLUE:
				return Color.YELLOW;
			}
			return Color.RED;
		}
	}

	public King(King.Color color) {
		this.color = color;
		try {
			sprite = new JLabel(new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/" + color.toString().toLowerCase() + ".png"))
					.getScaledInstance(-1, TonccGame.KING_SIZE, Image.SCALE_SMOOTH)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JLabel getSprite() { return sprite; }
	
	private King.Color color;
	private JLabel sprite;
}
