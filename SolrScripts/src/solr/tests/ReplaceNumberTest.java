package solr.tests;

import java.util.ArrayList;
import java.util.List;

public class ReplaceNumberTest {

	public static String replaceNumbers(String str) {

		List<Character> number = new ArrayList<Character>();

		int start = -1;
		int end = -1;
		
		for (int i = 0; i < str.length(); i++) {
			
			char c = str.charAt(i);

			if (Character.isDigit(c)) {

				
			} else {
				
			}
		}
		
		// str = str.replace(c, 'N');

		return str;
	}

	public static void main(String[] args) {

		String str = "Patch Set 1:  is this true? i CANT tell any difference in transfer speed with or without this patch. "
				+ "i still get roughly these numbers from \"adb sync\"  a -B build of bionic:  syncing /system... ... "
				+ "32 files pushed. 41492 files skipped. 1002 KB/s (1876139 bytes in 1.827s) syncing /data... ... "
				+ "5 files pushed. 132 files skipped. 3997 KB/s (3579643 bytes in 0.874s)";

		str = replaceNumbers(str);

		System.out.println(str);

	}
}
