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


public class Main {

	private static ProgressBarUpdater pbu;
	private static JFrame frame;
	private static JProgressBar pb;

	public static void main(String[] args) {
		setLAF();

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
		
		if(imageLimit == null) { System.exit(0); }

		// popular subreddits for wallpapers
		String[] subs = {"space", "earth", "sky", "animal", "winter",
						 "city", "adrenaline", "food", "map", "history"};
		String sub = (String)JOptionPane.showInputDialog(
							null,
							"Choose desired image category:",
							"Imgur image downloader",
							JOptionPane.PLAIN_MESSAGE,
							null,
							subs,
							"space");

		// append for coreect url
		if(sub != null) { sub = "r/" + sub + "porn/"; }
		// if none selected then exit
		if(sub == null) { System.exit(0); }

		// create directory chooser for download
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Please choose a directory for download");
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

		// set up progress bar and offload to thread
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Downloading...");
		frame.setSize(400, 50);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		
		pb = new JProgressBar();
		frame.getContentPane().add(pb);

		pbu = new ProgressBarUpdater(pb);
		new Thread(pbu).start();

		frame.setVisible(true);

		new Wall(sub, path, imageLimit, pbu);
	}

	// change Look & Feel of user interface
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