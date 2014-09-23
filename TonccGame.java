package toncc;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** The main frontend using the Toncc class: displays a
 * Toncc table and allows calculating kings' scores for a
 * particular configuration.
 *
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
public class TonccGame extends TonccRenderer {
	
	public TonccGame(final Toncc toncc, int... sizes) {
		super(toncc, sizes);
		
		score = new JPanel(new GridLayout(4, 2, 3, 1));
		score.add(new JLabel("King"));
		score.add(new JLabel("Score"));
		score.add(new JLabel("<html><font color='blue'>Blue</font></html>"));
		score.add(blueScore);
		score.add(new JLabel("<html><font color='red'>Red</font></html>"));
		score.add(redScore);
		score.add(new JLabel("<html><font color='#FFCC00'>Yellow</font></html>"));
		score.add(yellowScore);

		for(Map.Entry<String,TonccCellRenderer> entry : cellRenderers.entrySet()) {
			final TonccCellRenderer tcr = entry.getValue();
			final String id = entry.getKey();
			if(id.equals("MIND")) continue;
			final JPopupMenu popup = new JPopupMenu("Set cell owner");
			JMenuItem item = new JMenuItem("Free");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tcr.setState(TonccCell.State.FREE);
					kgCellRenderers.get(id).setOwner(null);	
					toncc.getCell(id).setOwner(null);
					tcr.repaint();
					kgCellRenderers.get(id).repaint();
					updateScore();
				}
			});
			popup.add(item);
			for(final String king : "Blue Red Yellow".split(" ")) {
				item = new JMenuItem(king);
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tcr.setState(TonccCell.State.CAPTURED);
						String prevOwner = kgCellRenderers.get(id).getCell().getOwner();
						kgCellRenderers.get(id).setOwner(king);	
						toncc.getCell(id).setOwner(king);
						tcr.repaint();
						kgCellRenderers.get(id).repaint();
						updateScore(king);
						if(prevOwner != null)
							updateScore(prevOwner);
					}
				});
				popup.add(item);
			}
			tcr.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
	}

	public final JPanel getScore() { return score; }

	public static void main(String[] args) {
		int cs = -1, kcs = -1;
		if(args.length > 0) {
			if(args.length > 1)
				kcs = new Integer(args[1]);
			cs = new Integer(args[0]);
		}
		JFrame frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		TonccGame renderer = null;
		if(cs != -1 || kcs != -1)
			renderer = new TonccGame(new Toncc(), cs, kcs);
		else
			renderer = new TonccGame(new Toncc());

		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 0;
		frame.add(renderer, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
		frame.add(renderer.getKingdoms(), c);
		c.gridy = 1;
		frame.add(renderer.score, c);
		SwingConsole.run(frame, "Toncc Renderer");
	}

	private void updateScore() {
		updateScore("Blue");
		updateScore("Red");
		updateScore("Yellow");
	}

	private void updateScore(final String king) {
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
		switch(king.charAt(0)) {
		case 'B':
			blueScore.setText(""+score);
			blueScore.repaint();
			break;
		case 'R':
			redScore.setText(""+score);
			redScore.repaint();
			break;
		case 'Y':
			yellowScore.setText(""+score);
			yellowScore.repaint();
			break;
		}
	}

	private JPanel score;
	private JLabel 	blueScore = new JLabel("0"), 
			redScore = new JLabel("0"),
			yellowScore = new JLabel("0");
}
