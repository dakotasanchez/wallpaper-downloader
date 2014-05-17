/* 	
*	Dakota sanchez
*	26 April 2014
*	
*	Imgur wallpaper mass downloader 
*/

import javax.swing.JProgressBar;

public class ProgressBarUpdater implements Runnable {
	private JProgressBar jpb = null;
	private Integer value = null;

	public ProgressBarUpdater (JProgressBar jpb) {
		this.jpb = jpb;
		jpb.setMaximum(100);
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public void run() {
		do {
			if(value != null) {
				jpb.setValue((int)Math.round(Math.floor(value.intValue())));
			}
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		} while (value == null || value.intValue() <= jpb.getMaximum());
	}
}