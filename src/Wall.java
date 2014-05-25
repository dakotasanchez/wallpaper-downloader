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
import java.awt.color.CMMException;

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
	private String path; // path to download to
    private int page; // current url page
    private int imageCount;
    private int imageLimit;
    private int pageErrorCount; // keep track of exception count retrieving xml
	private int savedi;

	// for xml elements, to extract image urls
	private Elements hashes;
	private Elements exts;
	private Elements widths;
	private Elements heights;

	// update gui progress bar at intervals
	private static ProgressBarUpdater pbu;

	public Wall(String sub, String path, int imageLimit, ProgressBarUpdater pbu) {
		this.path = path;
		this.imageLimit = imageLimit;
		this.pbu = pbu;

		pageErrorCount = 0;
		imageCount = 0;
        page = 0;
		savedi = 0;

		xmlUrl = imgur_url + sub + imgur_extension;

		// retrieve first xml and start downloading
		updateXMLPage();
		getImages();
	}

	public void updateXMLPage() {
		try {
			HttpConnection c = (HttpConnection)Jsoup.connect(xmlUrl + page + ".xml");
			c.timeout(10000); // 10 second timeout
			Document doc = c.get();

			pageErrorCount = 0; // reset error count after successful connection

			// get elements for the following xml tags
			hashes = doc.select("hash");
			exts = doc.select("ext");
			widths = doc.select("width");
			heights = doc.select("height");

		} catch (Exception e) {
			pageErrorCount++;
			System.out.println("pageErrorCount = " + pageErrorCount);
			// after 3 failures, show error and exit
			if (pageErrorCount >= 3) {
				showError("Slow connection, try again later", e);
			}
			updateXMLPage();
		}
	}

	public void getImages() {
		try {
			while(imageCount < imageLimit) {
				// for every image referenced in the XML
				for(int i = savedi; i < hashes.size(); i++) {
					int width = Integer.parseInt(widths.get(i).ownText());
					int height = Integer.parseInt(heights.get(i).ownText());

					// only download images that meet the min. resolution
					if((width >= 1280) || (height >= 720)) {

						String hash = hashes.get(i).ownText(); // 7-char hash associated with image

						String ext = exts.get(i).ownText(); // file extension

						File outputfile = new File(path + hash + ext); // create output file object

						// don't overwrite if it already exists
						if(!outputfile.exists()) {
							URL url = new URL(imgur_url + hash + ext); // url image is located at

							Image image = ImageIO.read(url); // download image and convert for save
							BufferedImage img = toBufferedImage(image);
						
							ImageIO.write(img, ext.substring(1), outputfile);
							imageCount++;

							// update progress bar with correct increment
							Integer barValue = (int) (((imageCount * 1.0) / imageLimit) * 100);
							pbu.setValue(barValue);
						} else {
							System.out.println(path + hash + ext + " already exists");
						}

					}
					// exit once image limit is reached
					if (imageCount >= imageLimit) {
						Thread.sleep(500); // let gui thread update
						pbu.exit(); // end progress bar thread
						return;
					}
					savedi++; // keep track of iterator for loop re-entry					
				}
				savedi = 0;
				// reached end of element list, so retrieve next xml page
				page++;
				updateXMLPage();
			}
		} catch (Exception e) {
			// IIOException when reading is usually related to server stress
			if(e instanceof IIOException) {
				showError("Slow connection, try again later", e);
			} else if(e instanceof CMMException) {
				e.printStackTrace();
				savedi++; // skip corrupted image
				getImages();
			} else {
				showError("Error retrieving images", e);
			}
		}
	}

	public static BufferedImage toBufferedImage(Image img) {
	    if (img instanceof BufferedImage) {
	        return (BufferedImage) img;
	    }

        System.out.println("Converting img to BufferedImage");
	    // create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // return the buffered image
	    return bimage;
	}

	// show error message dialog and exit
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
