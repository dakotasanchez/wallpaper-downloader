/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Imgur wallpaper mass downloader 
*/

import java.net.URL;
import java.io.File;
import javax.imageio.ImageIO;
import javax.imageio.IIOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import javax.swing.JOptionPane;

public class Wall {
	private final String imgur_url = "http://www.imgur.com/";
    private final String imgur_extension = "new/day/page/";

	private String xmlUrl;
	private String path;
    private int page;
    private int imageCount;
    private int imageLimit;
    private int pageErrorCount;
    private int imageErrorCount;

	// for xml elements
	private Elements hashes;
	private Elements exts;
	private Elements widths;
	private Elements heights;

	private static ProgressBarUpdater pbu;

	public Wall(String sub, String path, int imageLimit, ProgressBarUpdater pbu) {
		this.path = path;
		this.imageLimit = imageLimit;

		imageErrorCount = 0;
		pageErrorCount = 0;
		imageCount = 0;
        page = 0;

        this.pbu = pbu;
		xmlUrl = imgur_url + sub + imgur_extension;

		updateXMLPage();
		getImages();
	}

	public void updateXMLPage() {
		Document doc;
		try {
			HttpConnection c = (HttpConnection)Jsoup.connect(xmlUrl + page + ".xml");
			// 10 second timeout
			c.timeout(10000);
			doc = c.get();
			pageErrorCount = 0;

			// get elements for the following tags
			hashes = doc.select("hash");
			exts = doc.select("ext");
			widths = doc.select("width");
			heights = doc.select("height");

		} catch (Exception e) {
			pageErrorCount++;
			if (pageErrorCount >= 3) {
				showError("Error retrieving page", e);
			}
			updateXMLPage();
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
						Image image;
						try {
							image = ImageIO.read(url);
							// reset error count
							imageErrorCount = 0;
						} catch (IIOException e1) {
							imageErrorCount++;
							if (imageErrorCount >= 3) {
								showError("Error retrieving images", e1);
							}
							continue;
						}

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
						System.exit(0);
					}						
				}
				page++;
				updateXMLPage();
			}
		} catch (Exception e2) {
			e2.printStackTrace();
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

	public void showError(String message, Exception e) {
		JOptionPane.showMessageDialog(
				null,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
	}
}
