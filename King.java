package toncc;

import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;

enum King {
	RED, BLUE, YELLOW;

	King() {
		final String color = this.toString().toLowerCase();
		try {
			sprite = new JLabel(new ImageIcon(
					ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("toncc/images/" + color + ".png"))
					.getScaledInstance(-1, TonccGame.KING_SIZE, Image.SCALE_SMOOTH)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public King getMediumColor() {
		switch (this) {
		case RED:
			return BLUE;
		case BLUE:
			return YELLOW;
		}
		return RED;
	}

	public King getWeakColor() {
		switch (this) {
		case RED:
			return YELLOW;
		case BLUE:
			return RED;
		}
		return BLUE;
	}

	public void repaint() {
		sprite.repaint();
	}

	public void setBounds(int x, int y, int w, int h) {
		sprite.setBounds(x, y, w, h);
	}

	public boolean prevailsOn(final King other, final TonccCell.Id cellId) {
		King col = null;
		switch (cellId.toString().charAt(0)) {
		case 'R': col = King.RED; break;
		case 'B': col = King.BLUE; break;
		case 'Y': col = King.YELLOW; break;
		}
		return this == col ||
			(getMediumColor() == col
			 && other != col);
	}

	public JLabel getSprite() { return sprite; }

	public void move(TonccGame.Direction d) {
		position.move(d);
	}

	public void decTokens() { --tokens; }
	public int getTokens() { return tokens; }

	public Color getColor() {
		switch (this) {
		case RED: return Color.RED;
		case BLUE: return Color.BLUE;
		case YELLOW: return new Color(0xFFCC00);
		}
		return null;
	}

	public String getColorString() {
		switch (this) {
		case RED: return "Red";
		case BLUE: return "Blue";
		case YELLOW: return "Yellow";
		}
		return "";
	}

	int score;
	boolean gameOver;
	TonccCoordinate position = new TonccCoordinate(0, 0);

	private JLabel sprite;
	private int tokens = TonccGame.INITIAL_TOKENS; 
}
