package solr.analysis;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class TokenizerTest {

	public static void main(String[] args) {

		String tmp = "mm, oh my! I didn't understand, why did you do that?;";
		
		String[] feature = "I didn't understand".split("\\s");
		
		int featureSize = feature.length;
		
		StreamTokenizer tf = new StreamTokenizer(new StringReader(tmp));
		
		tf.lowerCaseMode(true);
		
		try {
			
			String[] lastTokens = new String[featureSize];
			
			while (tf.nextToken() != StreamTokenizer.TT_EOF) {
				
				if (tf.sval != null) {
					
					for (int i = 0; i < feature.length; i++) {
						
						System.out.println(tf.sval + " == " + feature[i]);
						
						
						if (tf.sval.equalsIgnoreCase(feature[i])) {
							
							lastTokens[i] = tf.sval;
							
							tf.nextToken();
							
							if (lastTokens[featureSize - 1] != null) {
								System.out.println("found!!!");
								lastTokens = new String[featureSize];
							}
							
						} else if (!tf.sval.equalsIgnoreCase(feature[i])) {
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
