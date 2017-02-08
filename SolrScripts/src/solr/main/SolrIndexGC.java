package solr.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
import solr.utils.Utils;

public class SolrIndexGC {

	public static void indexGeneralComments(int start, int end) {

		SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

		Log log = new Log("indexGC-[" +  start + "]-" + end);

		long totalGC = 0;

		List<String> codeReviewIDs = Utils.getCodeReviewIDs();

		for (int i = start; i < end; i++) {

			try {

				String codeReviewID = codeReviewIDs.get(i);

				log.doInfoLogging("indexing file: " + codeReviewID);
				
				String content = new String(Files.readAllBytes(
						Paths.get("C:/Users/Felipe/Documents/code-reviews/tmp/" + codeReviewID + ".json")));
				
				JsonObject jsonGC = (JsonObject) new JsonParser().parse(content);

				String changeID = jsonGC.get("change_id").getAsString();
				
				String status = jsonGC.get("status").getAsString();
				
				String project = jsonGC.get("project").getAsString();
				
				int insertions = jsonGC.get("insertions").getAsInt();
				
				int deletions = jsonGC.get("deletions").getAsInt();
				
				JsonArray messages = jsonGC.get("messages").getAsJsonArray();
				
				for (int j = 0; j < messages.size(); j++) {
				
					JsonObject message = messages.get(j).getAsJsonObject();

					SolrInputDocument document = new SolrInputDocument();

					document.addField("code_review", codeReviewID);

					document.addField("id", message.get("id").getAsString());
					
					document.addField("changeID", changeID);
					
					document.addField("status", status);
					
					document.addField("project", project);
					
					document.addField("insertions", insertions);
					
					document.addField("deletions", deletions);

					document.addField("date", message.get("date").getAsString());

					document.addField("message", message.get("message").getAsString());
					
					document.addField("_revision_number", message.get("_revision_number").getAsInt());
					
					if (message.get("tag") != null) {
						document.addField("tag", message.get("tag").getAsString());
					} else {
						document.addField("tag", "");
					}
					
					JsonObject author = (JsonObject) message.get("author");

					if (author != null) {
						
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
					} else {
						document.addField("author.name", "");
						document.addField("author.email", "");
						document.addField("author._account_id", "");
					}
					
					UpdateResponse response = solr.add(document);

					int responseStatus = response.getStatus();

					if (responseStatus == 0) {
						log.doInfoLogging("response: " + responseStatus + " for GC: " + message.get("id"));
					} else {
						log.doSevereLogging("ERROR: response: " + responseStatus + " for GC: " + message.get("id"));
					}

					solr.commit();

					totalGC = totalGC + 1;

					log.doInfoLogging("finished GC: " + message.get("id"));
				}
				
				log.doInfoLogging("finished file: " + codeReviewIDs.get(i));
				
			} catch (SolrServerException | IOException e) {
				e.printStackTrace();
			}
		}

		log.doInfoLogging("finished all!!!");

		log.doInfoLogging("total of general comments: " + totalGC);
	}

	public static void main(String[] args) {

		int start = 0;

		int end = 3;

		indexGeneralComments(start, end);
	}
}
