package misc;
import java.util.Date;

public class Watchdog extends Thread {
	private long last;
	private long timeout; 
	private WatchdogListener listener;

	public Watchdog (long timeout, WatchdogListener listener) {
		this.timeout = timeout;
		this.listener = listener;
		this.last = new Date().getTime();
	}

	@Override
	public void run () {
		boolean exit = false;
		long now, diff, sleep;

		while (!exit) {
			now = new Date().getTime();
			diff = now-this.last;
			if (diff > this.timeout) {
				this.listener.timeout();
				exit = true;
			}
			sleep = this.timeout-diff;
			if (sleep > 0) {
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
					exit = true;
				}
			}
		}
	}
	
	public void kick() {
		this.last = new Date().getTime();
	}
}
