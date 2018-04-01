package be.algielen.randompicture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

import darrylbu.icon.StretchIcon;

public class ImagePanel extends MouseAdapter {
	static GraphicsDevice device = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getScreenDevices()[0];
	private boolean isFullscreen = false;
	JFrame fullscreenFrame;

	private File filepath;
	private StretchIcon myPicture;
	private File myPicturePath = filepath;
	private final Vector<StretchIcon> nextPictures;
	private Vector<File> nextPicturesPathes;
	private JLabel picLabel;
	ImageIcon imgicon;

	ImageFetcher fetcher;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new ImagePanel();
	}

	@SuppressWarnings("serial")
	ImagePanel() {
		// properties
		String defaultImage = null;
		Properties props = new Properties();
		try (InputStream input = ImagePanel.class.getResourceAsStream("/props.properties")) {
			props.load(input);
			filepath = new File(props.getProperty("path"));
			defaultImage = props.getProperty("defaultImage");

		} catch (Exception e2) {
			System.err.println("Couldn't properly load props.properties, verify your JAR.");
			System.err.println(e2.getMessage());
		}
		nextPictures = new Vector<>();
		nextPicturesPathes = new Vector<>();

		JFrame fenetre = new JFrame("RandomPicture");
		JFrame fullscreenFrame = new JFrame();

		fullscreenFrame.setUndecorated(true);

		fenetre.setSize(1280, 720);
		fenetre.setLocationRelativeTo(null);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setLayout(new BorderLayout());
		fenetre.add(panel);

		// myPicture = new ImageIcon();

		imgicon = new ImageIcon(ImagePanel.class.getResource("/" +  defaultImage));
		picLabel = new JLabel(imgicon);
		panel.add(picLabel, BorderLayout.CENTER);
		Action actionRefresh = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				nextImage();
			}
		};

		Action actionFullscreen = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isFullscreen) {
					fullscreenFrame.add(panel);
					device.setFullScreenWindow(fullscreenFrame);
					fenetre.setVisible(false);
					fullscreenFrame.setVisible(true);
					isFullscreen = true;
				} else {
					fenetre.add(panel);
					device.setFullScreenWindow(null);
					fullscreenFrame.setVisible(false);
					fenetre.setVisible(true);
					isFullscreen = false;
				}
			}
		};
		picLabel.addMouseListener(this);
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"actionRefresh");
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				"actionRefresh");
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				"actionRefresh");
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
				"fullscreen");
		panel.getActionMap().put("actionRefresh", actionRefresh);
		panel.getActionMap().put("fullscreen", actionFullscreen);
		fenetre.setVisible(true);

		fetcher = new ImageFetcher(nextPictures, filepath, nextPicturesPathes);
		fetcher.run();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) { // clic gauche
			nextImage();
			// clic droit
		} else if (arg0.getButton() == MouseEvent.BUTTON3 && !isFullscreen && myPicture != null) { 
			JPanel savepopup = new JPanel();
			JTextField textfield = new JTextField(myPicturePath.getPath());
			textfield.setEditable(false);
			textfield.setRequestFocusEnabled(true);
			JScrollPane scrollpane = new JScrollPane(textfield);
			savepopup.add(scrollpane);
			JButton button = new JButton("Show in Explorer.");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (SystemUtils.IS_OS_WINDOWS) {
						try {
							Runtime.getRuntime().exec(
									"explorer.exe /select,"
											+ myPicturePath.getPath());
						} catch (IOException e1) {
							System.err.println("Couldn't open "+myPicturePath.getPath()+ e1.getMessage());
						}
					} else {
						Desktop desktop = null;
						if (Desktop.isDesktopSupported()) {
							desktop = Desktop.getDesktop();
						}
						try {
							desktop.open(myPicturePath);
						} catch (IOException e1) {
							System.err.println("Couldn't open "+myPicturePath.getPath()+ e1.getMessage());
						}
					}
				}
			});
			savepopup.add(button);
			JOptionPane.showOptionDialog(null, savepopup, "Picture path",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, null, null);
		}
	}

	private void nextImage() {
		fetchNewImages();
		picLabel.setIcon(myPicture);
		picLabel.repaint();
	}

	private void fetchNewImages() {
		synchronized (nextPictures) {
			if (!nextPictures.isEmpty()) {
				myPicture = nextPictures.remove(0);
				myPicturePath = nextPicturesPathes.remove(0);
				nextPictures.notifyAll();
			} else {
				// JOptionPane.showMessageDialog(null,
				// "Pas de nouvelle image.");
				//TODO
			}
		}
	}


}

