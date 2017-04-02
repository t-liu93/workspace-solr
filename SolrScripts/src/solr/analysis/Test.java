package solr.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {

		String str = "I call on!. acall on calling you becayse CALL_ON is calL ON...";

		int matches = 0;

		String feature = "call on";

		Matcher matcher = Pattern.compile("\\b" + feature + "\\b", Pattern.CASE_INSENSITIVE).matcher(str);

		while (matcher.find()) {
			matches++;
		}

		System.out.println(matches);
	}
}
