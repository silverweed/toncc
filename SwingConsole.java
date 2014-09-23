package toncc;

import javax.swing.*;

/** Utility class to run JFrame-based GUI classes.
 *
 * @author Bruce Eckel, Giacomo Parolini
 */
class SwingConsole {

	private static void prepare(final JFrame f) {
		f.setTitle(f.getClass().getSimpleName());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void run(final JFrame f,final int width,final int height) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prepare(f);
				f.setSize(width,height);
				f.setVisible(true);
			}
		});
	}

	/** Don't manually set size, but pack. */
	public static void run(final JFrame f) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prepare(f);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	/** Don't manually set size, but pack. */
	public static void run(final JFrame f,final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setTitle(title);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	public static void run(final JFrame f,final int width,final int height,final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setTitle(title);
				f.setSize(width,height);
				f.setVisible(true);
			}
		});
	}

	public static void runFullScreen(final JFrame f) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prepare(f);
				f.setExtendedState(JFrame.MAXIMIZED_BOTH);
				f.setVisible(true);
			}
		});
	}

	public static void setSystemLookAndFeel() {
		try {
			// Set system L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ee) {
			System.err.println("Caught exception while setting LookAndFeel: "+ee);
		}
	}

}

