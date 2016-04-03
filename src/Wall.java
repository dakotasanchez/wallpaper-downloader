/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Imgur wallpaper mass downloader 
*/

import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
				for(int i = 0; i < hashes.size(); i++) {
					int width = Integer.parseInt(widths.get(i).ownText());
					int height = Integer.parseInt(heights.get(i).ownText());

					// only download images that meet the min. resolution
					if((width >= 1280) && (height >= 720)) {

						String hash = hashes.get(i).ownText(); // 7-char hash associated with image

						String ext = exts.get(i).ownText(); // file extension

						File outputfile = new File(path + hash + ext); // create output file object

						// don't overwrite if it already exists
						if(!outputfile.exists()) {
							URL url = new URL(imgur_url + hash + ext); // url image is located at

							InputStream is = url.openStream();
							OutputStream os = new FileOutputStream(outputfile);

							byte[] b = new byte[2048];
							int length;

							// read file into buffer and write to file
							while((length = is.read(b)) != -1) {
								os.write(b, 0, length);
							}

							is.close();
							os.close();

							imageCount++; // increment download counter

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
				}
				// reached end of element list, so retrieve next xml page
				page++;
				updateXMLPage();
			}
		} catch (Exception e) {
			showError("Error retrieving images (possible slow connection)", e);
		}
	}

	// show error message dialog and exit
	public void showError(String message, Exception e) {
		JOptionPane.showMessageDialog(
				null,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(1);
	}
}
