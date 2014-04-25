import javax.swing.JOptionPane;

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

	public static void main(String[] args) {
		String windowsEx = "Windows: C:\\Users\\Bob\\Pictures\\\n";
		String macEx = "Mac: /Users/bob/Pictures/\n";
		String linuxEx = "Linux: /home/bob/Pictures/\n";

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

		String path = (String)JOptionPane.showInputDialog(
							null,
							"Enter path to download folder:\n\ne.g.\n" + 
								windowsEx + macEx + linuxEx + "\n",
							"Path",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							null);

		if(path == null || path.length() < 1) {
			JOptionPane.showMessageDialog(
				null,
				"No path specified",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
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
				if(width > 1366) {
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