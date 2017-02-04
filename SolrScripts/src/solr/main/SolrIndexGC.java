package solr.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import solr.utils.Const;
import solr.utils.Log;

public class SolrIndexGC {

	public static void indexGeneralComments(int start, int end) {
		
		SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

		Log log = new Log("indexGC");

		File codeReviewsFolder = new File("C:/Users/Felipe/Documents/code-reviews/details/");

		File[] listOfCodeReviews = codeReviewsFolder.listFiles();
		
		long totalGC = 0;

		try {
			for (int i = start; i < end; i++) {

				if (listOfCodeReviews[i].isFile()) {

					String content = new String(Files.readAllBytes(listOfCodeReviews[i].toPath()));

					JsonObject jsonGC = (JsonObject) new JsonParser().parse(content);

					JsonArray messages = jsonGC.get("messages").getAsJsonArray();
					
					int codeReviewID = jsonGC.get("_number").getAsInt();
					
					log.doInfoLogging("indexing file: " + codeReviewID);

					for (int j = 0; j < messages.size(); j++) {
						
						JsonObject message = messages.get(j).getAsJsonObject();
						
						SolrInputDocument document = new SolrInputDocument();
						
						document.addField("code_review", codeReviewID);
						
						document.addField("id", message.get("id").getAsString());
						
						document.addField("date", message.get("date").getAsString());

						document.addField("message", message.get("message").getAsString());
						
						document.addField("message", message.get("message").getAsString());
						
						document.addField("_revision_number", message.get("_revision_number").getAsInt());
						
						if (message.get("tag") != null) {
							document.addField("tag", message.get("tag").getAsString());
						} else {
							document.addField("tag", "");
						}
						
						JsonObject author = (JsonObject) message.get("author");

						if (author.get("name") != null) {
							document.addField("author.name", author.get("name").getAsString());
						} else {
							document.addField("author.name", "");
						}

						if (author.get("email") != null) {
							document.addField("author.email", author.get("email").getAsString());
						} else {
							document.addField("author.email", "");
						}

						if (author.get("_account_id") != null) {
							document.addField("author._account_id", author.get("_account_id").getAsString());
						} else {
							document.addField("author._account_id", "");
						}
						
						
						UpdateResponse response = solr.add(document);

						int responseStatus = response.getStatus();

						if (responseStatus == 0) {
							log.doInfoLogging("response: " + responseStatus + " for GC: " + message.get("id"));
						} else {
							log.doSevereLogging(
									"ERROR: response: " + responseStatus + " for GC: " + message.get("id"));
						}
						
						solr.commit();

						totalGC = totalGC + 1;
						
						log.doInfoLogging("finished GC: " + message.get("id"));
					}
					
					log.doInfoLogging("finished file: " + listOfCodeReviews[i].getName());
				}
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		
		log.doInfoLogging("finished all!!!");
		
		log.doInfoLogging("total of general comments: " + totalGC);
	}

	public static void main(String[] args) {

		int start = 0;

		int end = 10;

		indexGeneralComments(start, end);
	}
}
