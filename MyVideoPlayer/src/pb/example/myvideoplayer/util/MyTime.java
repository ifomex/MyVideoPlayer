package pb.example.myvideoplayer.util;

/**
 * Tøída pro uložení a zobrazení èasu
 * @author Petr  Blatny
 * e-mail: xblatn03@stud.fit.vutbr.cz
 *
 */
public class MyTime {

	int sec;
	int min;
	int hour;
	
	public MyTime(long millis) {
		this.sec = 0;
		this.min = 0;
		this.hour = 0;
		
		int pres = 0;
		pres = (int) (millis / 1000); //v sekundach
		
		if (pres == 0) {	//nema vic nez sekundu
			return;
		}
		
		this.sec = pres % 60;
		pres = pres / 60; //v minutach
		if (pres == 0)		//nema vic nez minutu
			return;
		
		this.min = pres % 60;
		this.hour = pres / 60;
	}
	
	public String toString() {
		String hourString;
		String minuteString;
		String secondString;

		
		hourString = Integer.toString(this.hour);

		if (this.min < 10) {
			minuteString = "0" + this.min;
		} else {
			minuteString = Integer.toString(this.min);
		}
		if (this.sec < 10) {
			secondString = "0" + this.sec;
		} else {
			secondString = Integer.toString(this.sec);
		}
		
		
		return  (this.hour > 0) ? (hourString + ":" + minuteString + ":" + secondString)
				: (minuteString + ":" + secondString);
	}
	
}
