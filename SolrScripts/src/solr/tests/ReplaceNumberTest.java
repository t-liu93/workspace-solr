package solr.tests;

public class ReplaceNumberTest {
	
	public static String removeCorruptedChars(String str) {
		
		str = str.replaceAll("[^\\x00-\\x7F]", "");
		
		return str;
	}

	
	public static void main(String[] args) {
		
//		String str = "Patch Set 1:  is this true? i can�t tell any difference in transfer speed with or without this patch. "
//				+ "i still get roughly these numbers from \"adb sync\"  a -B build of bionic:  syncing /system... ... "
//				+ "32 files pushed. 1492 files skipped. 1002 KB/s (COMMIT bytes in 1.827s) syncing /data... ... "
//				+ "5 files pushed. 132 files skipped. 3997 KB/s (COMMIT bytes in 0.874s)";

		
//		String str = "Mike, How are Ana\n Patch Set 1: Reviewers (and experts in LatinIME and l18n): "
//				+ "how practical is it. Josh! ,going, to be to merge this into our internal "
//				+ "tree in a way that won't get stomped over by any l18n scripts?";
		
		String str = "NUMBER:  Sureâ€¦ @USERNAME reviewï¼Ÿ function â€˜parse_operandsâ€™: error: â€˜firsttype$definedâ€™ @USERNAME";

		str = removeCorruptedChars(str);

		System.out.println(str);

	}
}
