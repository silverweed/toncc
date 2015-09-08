package toncc;

import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

/** Renders a single TonccCell class.
 * 
 * @license GNU GPL v3
 * @author Giacomo Parolini
 */
class TonccCellRenderer extends JLabel {
	
	public TonccCellRenderer(final TonccCell cell) {
		this.cell = cell;
		try {
			icon = new ImageIcon(ImageIO.read(getClass().getClassLoader()
				.getResourceAsStream("toncc/images/"+cell.id()+".png")));
			setIcon(icon);
		} catch(IOException e) {
			System.err.println("[TonccCellRenderer] Error! Couldn't load icon for cell "+cell.id());
			e.printStackTrace();
		}
	}

	public TonccCellRenderer(final TonccCell cell, final int size) {
		this.cell = cell;
		try {
			icon = new ImageIcon(ImageIO.read(getClass().getClassLoader()
				.getResourceAsStream("toncc/images/"+cell.id()+".png"))
				.getScaledInstance(-1, size, Image.SCALE_SMOOTH));
			setIcon(icon);
		} catch(IOException e) {
			System.err.println("[TonccCellRenderer] Error! Couldn't load icon for cell "+cell.id());
			e.printStackTrace();
		}
	}

	public void setState(final TonccCell.State state) {
		if(cell.id() == TonccCell.Id.MIND) return;
		cell.setState(state);
	}

	public void setOwner(final King owner) {
		if(cell.id() == TonccCell.Id.MIND) return;
		cell.setOwner(owner);
		if (owner == null)
			kingColor = null;
		else
			kingColor = owner.getColor();
	}

	public final TonccCell getCell() { return cell; }

	@Override
	/** If kingColor != null, paint the cell with a color filter of the
	 * king's color; else, if cell is captured, paint it grayscale, else
	 * paint it normally.
	 */
	protected void paintComponent(Graphics gg) {
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		if(kingColor != null) {
			BufferedImage colVersion = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)gg;
			g.setColor(kingColor);

			g.fillRect(0, 0, width, height);
		} else {
			if(cell.getState() == TonccCell.State.FREE)
				gg.drawImage(icon.getImage(), 0, 0, width, height, 0, 0, width, height, null);
			else
				gg.drawImage(GrayFilter.createDisabledImage(
					icon.getImage()), 0, 0, width, height, 0, 0, width, height, null);
		}
	}

	private final TonccCell cell;
	private Color kingColor;
	private ImageIcon icon;

	/** Test method */
	public static void main(String[] args) {
		JFrame panel = new JFrame();
		panel.setLayout(new FlowLayout());
		panel.add(new TonccCellRenderer(new TonccCell(TonccCell.Id.MIND)));
		for(TonccCell.Id id : Toncc.MIND) {
			panel.add(new TonccCellRenderer(new TonccCell(id)));
		}
		SwingConsole.run(panel, "Test TonccCellRenderer");
	}
}
