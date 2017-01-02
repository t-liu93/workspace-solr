package solr.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public static void main(String[] args) {

		String line = "Hello, I didn't understand It appears that it is solved!  i didn't understand...";

		Pattern pattern = Pattern.compile("\\.*[Ii] didn't understand\\.*");

		Matcher matcher = pattern.matcher(line);

		int count = 0;
		while (matcher.find()) {
			count++;
		}
		System.out.println(count);
		
	}
}
