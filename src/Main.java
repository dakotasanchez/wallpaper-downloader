/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Imgur wallpaper mass downloader 
*/

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

// series of dialogs for user input
public class Main {

	private static ProgressBarUpdater pbu;
	private static JFrame frame;
	private static JProgressBar pb;

	public static void main(String[] args) {
		setLAF();

		// get number of images to download from user
		Integer[] limits = new Integer[200];
		for(int i = 0; i < limits.length; i++) { limits[i] = i + 1; }
		Integer imageLimit = (Integer)JOptionPane.showInputDialog(
								null,
								"Choose number of images\nto be downloaded:",
								"",
								JOptionPane.PLAIN_MESSAGE,
								null,
								limits,
								"1");
		
		// if none selected then exit
		if(imageLimit == null) { System.exit(0); }

		// popular imgur sections for wallpapers, get user choice
		String[] subs = {"earth", "space", "sky", "city", "animal",
						 "winter", "history", "food", "culinary", 
						 "car", "architecture", "sports", "future",
						 "macro", "exposure", "destruction", "art",
						 "fractal", "map", "village", "adrenaline" };
		String sub = (String)JOptionPane.showInputDialog(
							null,
							"Choose desired image category:",
							"Imgur image downloader",
							JOptionPane.PLAIN_MESSAGE,
							null,
							subs,
							"space");

		// if none selected then exit
		if(sub == null) { System.exit(0); }

		// create directory chooser for download path
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Please choose a directory for download");
		fc.setApproveButtonText("Choose");
		fc.setCurrentDirectory(new File(System.getProperty("user.home")));
		int returnVal = fc.showOpenDialog(null);

		// exit if no directory chosen
		if(returnVal == 1) { System.exit(0); }

		// get path chosen and grab the last char
		String path = fc.getSelectedFile().getPath();
		char end = path.charAt(path.length() - 1);

		// check os for correct path delimiter
		// then append one if it's not there
		if((System.getProperty("os.name")).startsWith("Windows")) {
			if(end != '\\')
				path += "\\";
		} else {
			if(end != '/')
				path += "/";
		}

		// set up window frame
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 80);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setContentPane(new JPanel());

		// grammar
		String labelStr = "Getting " + imageLimit + " newest ";
		if(imageLimit == 1) 
			labelStr += "image ";
		else
			labelStr += "images ";
		labelStr += "from " + sub + " wallpaper source";

		// add info text to panel
		frame.getContentPane().add(new JLabel(labelStr));
		
		// add progress bar to panel
		pb = new JProgressBar();
		frame.getContentPane().add(pb);

		// create new updater and offload to thread
		pbu = new ProgressBarUpdater(pb);
		new Thread(pbu).start();

		frame.setVisible(true);

		// append for correct url
		sub = "r/" + sub + "porn/";

		// start downloading
		new Wall(sub, path, imageLimit, pbu);

		// close progress bar
		frame.dispose();

		try {
			// open folder after completion
			File downloadDest = new File(path);
			Desktop desktop = Desktop.getDesktop();
			desktop.open(downloadDest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// change Look & Feel of user interface to nimbus
	public static void setLAF() {
		try{
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
