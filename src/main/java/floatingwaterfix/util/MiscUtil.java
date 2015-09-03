package floatingwaterfix.util;

public class MiscUtil {
	public static int stringToInt(String s) {
		int i = 0;
		
		try {
			i = Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			System.err.println("Tried to parse string to int.");
			e.printStackTrace();
		}
		
		return i;
	}
}
