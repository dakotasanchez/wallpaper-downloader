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
	private boolean exit = false;

	public ProgressBarUpdater (JProgressBar jpb) {
		this.jpb = jpb;
		jpb.setMaximum(100);
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public void exit() {
		exit = true;
	}

	public void run() {
		do {
			if(value != null) {
				// increment progress bar
				jpb.setValue((int)Math.round(Math.floor(value.intValue())));
			}
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(exit) break;
		} while (value == null || value.intValue() <= jpb.getMaximum());
	}
}
