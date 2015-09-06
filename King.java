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

	public boolean prevailsOn(final King other, final String cellId) {
		Color col = null;
		switch (cellId.charAt(0)) {
		case 'R': col = Color.RED; break;
		case 'B': col = Color.BLUE; break;
		case 'Y': col = Color.YELLOW; break;
		}
		return color == col ||
			(color.getMediumColor() == col
			 && other.color != col);
	}

	public JLabel getSprite() { return sprite; }

	@Override
	public String toString() { return color + " King"; }

	public void setPosition(int cellIdx) {
		position = cellIdx;
	}
	public int getPosition() { return position; }

	public void decTokens() { --tokens; }
	public int getTokens() { return tokens; }

	public void setGameOver(boolean b) { gameOver = b; }
	public boolean isGameOver() { return gameOver; }

	public String getColorString() {
		switch (color) {
		case RED: return "Red";
		case BLUE: return "Blue";
		case YELLOW: return "Yellow";
		}
		return "";
	}
	
	private King.Color color;
	private JLabel sprite;
	private int position;
	private int tokens = TonccGame.INITIAL_TOKENS; 
	private boolean gameOver;
}
