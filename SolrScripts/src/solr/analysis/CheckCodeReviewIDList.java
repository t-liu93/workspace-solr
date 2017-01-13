package solr.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import solr.utils.HtmlRequests;

public class CheckCodeReviewIDList {

	public static void printAllCodeReviewsIndexed() {

		String url = "http://localhost:8983/solr/gettingstarted/select?fl=_number&indent=on&q=*:*&wt=csv&rows=20000&start=";

		List<String> allCodeReviewIDs = new ArrayList<String>();
		
		for (int start = 0; start < 160000; start = start + 20000) {

			String urlRequest = url + start;
			
			System.out.println(urlRequest);

			List<String> codeReviewIDs = HtmlRequests.excuteSolrRequest(urlRequest);
			
			System.out.println(codeReviewIDs.size());
			
			for (String id : codeReviewIDs) {
				
				String solrIndexCmd = "java -Dc=gettingstarted -Dauto=yes -Ddata=files -Drecursive=yes -jar example/exampledocs/post.jar "
						+ "C:/Users/Felipe/Documents/code-reviews/details/" + id + ".json \n";
				
				allCodeReviewIDs.add(solrIndexCmd);
			}
		}
		
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("./results/code-review-solr-index-cmd.txt"), "UTF-8"))) {
			writer.write(allCodeReviewIDs.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println("Done...");
	}

	public static void checkMissingCodeReviews() {

		List<String> codeReviewIDs = new ArrayList<>();

		BufferedReader br = null;
		try {
			// TODO check this URL and file
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
			// TODO check this URL and file
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

			System.out.println(t);
		}
	}

	public static void main(String[] args) {

		// checkMissingCodeReviews();

		printAllCodeReviewsIndexed();
	}
}
