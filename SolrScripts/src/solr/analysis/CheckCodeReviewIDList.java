package solr.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CheckCodeReviewIDList {

	public static void main(String[] args) {

		List<String> codeReviewIDs = new ArrayList<>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("./results/code-review-id-list.txt"));
			String line = br.readLine();

			while (line != null) {
				codeReviewIDs.add(line);
				line = br.readLine();
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}

		BufferedReader br2 = null;
		try {
			br2 = new BufferedReader(new FileReader("./results/code-review-id-solr.txt"));
			String line = br2.readLine();

			while (line != null) {

				if (codeReviewIDs.contains(line)) {
					codeReviewIDs.remove(line);
				}

				line = br2.readLine();
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				br2.close();
			} catch (IOException e) {
			}
		}

		for (String string : codeReviewIDs) {
			 String t = "java -Dc=gettingstarted -Dauto=yes -Ddata=files -Drecursive=yes -jar example/exampledocs/post.jar "
			 + "C:/Users/Felipe/Documents/code-reviews/details/" + string + ".json";

			// String t = "http://localhost:8983/solr/gettingstarted/update?stream.body=<delete><query>_number:" + string
			// 		+ "</query></delete>&commit=true";
			// Log log = new Log("test");
			// HtmlRequests.excuteHttpRequest(t, log);
			
			System.out.println(t);
		}
	}
}
