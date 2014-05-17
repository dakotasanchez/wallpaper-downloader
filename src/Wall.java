/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Imgur wallpaper mass downloader 
*/

import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
    private final String imgur_extension = "new/day/page/";

	private String xmlUrl;
	private String path;
    private int page;
    private int imageCount;
    private int imageLimit;

	// for xml elements
	private Elements hashes;
	private Elements exts;
	private Elements widths;
	private Elements heights;

	private ProgressBarUpdater pbu;
	private JFrame frame;
	private JProgressBar pb;

	private Thread t;

	public Wall(String sub, String path, int imageLimit) {
		this.path = path;
		this.imageLimit = imageLimit;
		imageCount = 0;
        page = 0;
		xmlUrl = imgur_url + sub + imgur_extension;

		// set up progress bar and offload to thread
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Downloading...");
		frame.setSize(300, 50);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		
		pb = new JProgressBar();
		frame.getContentPane().add(pb);

		pbu = new ProgressBarUpdater(pb);
		t = new Thread(pbu);
		t.start();

		frame.setVisible(true);
	}

	public void updateXMLPage() {
		Document doc;
		try {
			doc = Jsoup.connect(xmlUrl + page + ".xml").get();

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
			while(imageCount < imageLimit) {
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
							imageCount++;
							Integer barValue = (int) (((imageCount * 1.0) / imageLimit) * 100);
							pbu.setValue(barValue);
						}
					}
					if (imageCount >= imageLimit) {
						Thread.sleep(500);
						frame.dispose();
						System.exit(0);
					}						
				}
				page++;
				updateXMLPage();
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
