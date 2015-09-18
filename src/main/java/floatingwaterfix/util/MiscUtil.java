package floatingwaterfix.util;

public class MiscUtil {
	public static Integer stringToInt(String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			
		}
		return null;
	}
	
	public static Float stringToFloat(String s) {
		try {
			return Float.parseFloat(s);
		}
		catch (NumberFormatException e) {
			
		}
		return null;
	}
	
	public static Double stringToDouble(String s) {
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e) {
			
		}
		return null;
	}
}
