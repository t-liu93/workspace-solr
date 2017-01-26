package solr.analysis;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class TokenizerTest {

	public static void main(String[] args) {

		String tmp = "Patch Set 1: Code-Review+1 No score; I would prefer that you didn't submit "
				+ "this The way this is solved in the rest of the code is to prefix "
				+ "strings that might start with \"-\" with \"x\". This is a practice "
				+ "that is explicitly recommended in the POSIX spec, IIRC. And there "
				+ "is an example of it one line up, even!;";
		
		String feature = "i would";
		
		StreamTokenizer tf = new StreamTokenizer(new StringReader(tmp));
		tf.lowerCaseMode(true);
		
		try {
			
			while (tf.nextToken() != StreamTokenizer.TT_EOF) {
				System.out.println(tf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
