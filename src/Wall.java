/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Subreddit Wallpaper mass downloader 
*/

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.net.URL;
import java.io.File;
import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class Wall {
	private final String imgur_url = "http://www.imgur.com/";

	private String xmlUrl;
	private String path;

	// for xml elements
	private Elements hashes;
	private Elements exts;
	private Elements widths;
	private Elements heights;

	public static void main(String[] args) {

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

		// popular subreddits for wallpapers
		String[] subs = {"space", "earth", "sky", "animal", "winter",
						 "city", "adrenaline", "food", "map", "history"};
		String sub = (String)JOptionPane.showInputDialog(
							null,
							"Enter desired image subreddit:",
							"Reddit image downloader",
							JOptionPane.PLAIN_MESSAGE,
							null,
							subs,
							"space");

		// append for coreect url
		if(sub != null) { sub = "r/" + sub + "porn"; }
		// if none selected then exit
		if(sub == null) { System.exit(0); }

		Wall wall = new Wall(sub, path);

		wall.getXMLPage();
		wall.getImages();
	}

	public Wall(String sub, String path) {
		this.path = path;
		xmlUrl = imgur_url + sub + ".xml";
	}

	public void getXMLPage() {
		Document doc;
		try {
			doc = Jsoup.connect(xmlUrl).get();

			// get elements for the following tags
			hashes = doc.select("hash");
			exts = doc.select("ext");
			widths = doc.select("width");
			heights = doc.select("height");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(
				null,
				"Error retrieving page",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void getImages() {
		try {
			// for every image referenced in the XML
			for(int i = 0; i < hashes.size(); i++) {
				int width = Integer.parseInt(widths.get(i).ownText());
				int height = Integer.parseInt(heights.get(i).ownText());

				// only download images that meet the min. resolution
				if((width >= 1920) || (height >= 1080)) {
					// 7-char hash associated with image
					String hash = hashes.get(i).ownText();
					// file extension
					String ext = exts.get(i).ownText();
					// url image is located at
					URL url = new URL(imgur_url + hash + ext);
					// download image and convert for save
					Image image = ImageIO.read(url);
					BufferedImage img = toBufferedImage(image);
					// create output file and write image
					File outputfile = new File(path + hash + ext);
					// don't overwrite if it already exists
					if(!outputfile.exists()) {
						ImageIO.write(img, ext.substring(1), outputfile);
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
				null,
				"Error retrieving Images",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static BufferedImage toBufferedImage(Image img) {
	    if (img instanceof BufferedImage) {
	        return (BufferedImage) img;
	    }
	    // create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // return the buffered image
	    return bimage;
	}
}