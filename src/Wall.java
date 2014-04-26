import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class Wall {
	private final String imgur_url = "http://www.imgur.com/r/";
	private final String imgur_image_url = "http://www.i.imgur.com/";

	private String xmlUrl;
	private String path;

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

		if(returnVal == 1) { System.exit(0); }

		String path = fc.getSelectedFile().getPath();
		char end = path.charAt(path.length() - 1);

		if((System.getProperty("os.name")).startsWith("Windows")) {
			if(end != '\\')
				path += "\\";
		} else {
			if(end != '/')
				path += "/";
		}

		System.out.println(path);

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

		if(sub != null) { sub = sub + "porn"; }
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
			System.exit(0);
		}
	}

	public void getImages() {
		try {
			for(int i = 0; i < hashes.size(); i++) {
				int width = Integer.parseInt(widths.get(i).ownText());
				int height = Integer.parseInt(heights.get(i).ownText());
				if((width >= 1366) || (height >= 768)) {
					String hash = hashes.get(i).ownText();
					String ext = exts.get(i).ownText();

					URL url = new URL(imgur_image_url + hash + ext);

					Image image = ImageIO.read(url);

					File outputfile = new File(path + hash + ext);

					BufferedImage img = toBufferedImage(image);

					ImageIO.write(img, ext.substring(1), outputfile);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
				null,
				"Error retrieving Images",
				"Error",
				JOptionPane.ERROR_MESSAGE);
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