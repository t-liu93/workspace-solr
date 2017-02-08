package solr.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import solr.utils.HtmlRequests;
import solr.utils.Utils;

public class CheckSolrGC {

	public static void printJsonGCIDs() {
		
		List<String> gcIDs = new ArrayList<String>();

		List<String> codeReviewIDs = Utils.getCodeReviewIDs();

		for (String codeReviewID : codeReviewIDs) {

			try {
				
				String content = new String(Files.readAllBytes(
						Paths.get("C:/Users/Felipe/Documents/code-reviews/details/" + codeReviewID + ".json")));
				
				JsonObject jsonGC = (JsonObject) new JsonParser().parse(content);
				
				JsonArray generalComments = (JsonArray) jsonGC.get("messages");
				
				for (JsonElement comment : generalComments) {
					
					gcIDs.add(comment.getAsJsonObject().get("id").getAsString());
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("finished reading...");

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("./results/general-comments-id-list.txt"))) {

			for (String id : gcIDs) {
				bw.write(id + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("finished printing!!!");
		
		System.out.println("# of general comments: " + gcIDs.size());
	}
	
	public static void printAllCodeReviewsIndexed() {

		String url = "http://localhost:8983/solr/gettingstarted/select?fl=_number&indent=on&q=*:*&wt=csv&rows=20000&start=";

		List<String> allCodeReviewIDs = new ArrayList<String>();

		for (int start = 0; start < 160000; start = start + 20000) {

			String urlRequest = url + start;

			List<String> codeReviewIDs = HtmlRequests.excuteSolrRequest(urlRequest);

			for (String id : codeReviewIDs) {
				allCodeReviewIDs.add(id + "\n");
			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("./results/code-review-id-solr.txt"))) {

			for (String id : allCodeReviewIDs) {
				bw.write(id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void createSolrIndexingCmds() {

		List<String> codeReviewIDs = Utils.getCodeReviewIDs();

		List<String> solrIndexCmds = new ArrayList<String>();

		for (String id : codeReviewIDs) {

			String line = "java -Dc=gettingstarted -Dauto=yes -Ddata=files -Drecursive=yes -jar "
					+ "C:/Solr/solr-6.3.0-2/example/exampledocs/post.jar "
					+ "C:/Users/Felipe/Documents/code-reviews/details/" + id + ".json";

			solrIndexCmds.add(line + "\n");
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("./results/code-review-solr-index-cmds.txt"))) {

			for (String cmd : solrIndexCmds) {
				bw.write(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void runSolrIndexCmds() {

		try {

			List<String> solrCmds = Files.readAllLines(Paths.get("./results/code-review-solr-index-cmds.txt"));

			for (String cmd : solrCmds) {

				Runtime.getRuntime().exec(cmd);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void checkMissingCodeReviews() {

		List<String> codeReviewIDs = Utils.getCodeReviewIDs();

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

		for (String id : codeReviewIDs) {
			String t = "java -Dc=gettingstarted -Dauto=yes -Ddata=files -Drecursive=yes -jar example/exampledocs/post.jar "
					+ "C:/Users/Felipe/Documents/code-reviews/details/" + id + ".json";

			System.out.println(t);
		}
	}

	public static void main(String[] args) {

		// printAllCodeReviewsIndexed();

		// checkMissingCodeReviews();

		// createSolrIndexingCmds();

		// runSolrIndexCmds();

		printJsonGCIDs();
	}
}
