package solr.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import solr.utils.Const;
import solr.utils.Log;

public class SolrIndexIC {

	public static boolean checkCodeReview(SolrClient solr, int codeReviewID) {

		boolean exists = false;

		try {

			String queryString = "code_review:" + codeReviewID;

			SolrQuery query = new SolrQuery();

			query.setQuery(queryString);

			query.setFields("code_review");

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			long numCodeReviewsFound = results.getNumFound();

			if (numCodeReviewsFound > 0) {
				exists = true;
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		return exists;
	}

	public static void indexInlineComments() {

		SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

		Log log = new Log("indexIC");

		try {

			List<File> filesInFolder = new ArrayList<>();

			filesInFolder.addAll(Files.walk(Paths.get("C:/Users/Felipe/Documents/code-reviews/details/inline-comments"))
					.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList()));

			for (File fileIC : filesInFolder) {

				int codeReviewID = Integer.parseInt(fileIC.getName().substring(0, fileIC.getName().indexOf("-")));

				log.doInfoLogging("indexing file: " + codeReviewID);

				boolean commentExist = checkCodeReview(solr, codeReviewID);

				if (commentExist) {
					
					log.doInfoLogging("the code review already exists: " + codeReviewID);
					
					continue;
				} else {

					String content = new String(Files.readAllBytes(fileIC.toPath()));

					JsonObject jsonIC = (JsonObject) new JsonParser().parse(content);

					Set<Entry<String, JsonElement>> files = jsonIC.entrySet();

					for (Iterator<Entry<String, JsonElement>> iterator = files.iterator(); iterator.hasNext();) {

						Entry<String, JsonElement> file = (Entry<String, JsonElement>) iterator.next();

						JsonArray comments = file.getValue().getAsJsonArray();

						for (JsonElement comment : comments) {

							JsonObject jsonObj = comment.getAsJsonObject();

							SolrInputDocument document = new SolrInputDocument();

							document.addField("id", jsonObj.get("id").getAsString());

							document.addField("file", file.getKey().toString());

							document.addField("code_review", codeReviewID);

							JsonObject author = (JsonObject) jsonObj.get("author");

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

							document.addField("patch_set", jsonObj.get("patch_set").getAsInt());

							if (jsonObj.get("line") != null) {
								document.addField("line", jsonObj.get("line").getAsInt());
							} else {
								document.addField("line", -1);
							}

							JsonObject range = new JsonObject();

							if (jsonObj.get("range") != null) {

								range = jsonObj.getAsJsonObject("range");

								document.addField("range.start_line", range.get("start_line").getAsInt());
								document.addField("range.start_character", range.get("start_character").getAsInt());
								document.addField("range.end_line", range.get("end_line").getAsInt());
								document.addField("range.end_character", range.get("end_character").getAsInt());

							} else {

								document.addField("range.start_line", -1);
								document.addField("range.start_character", -1);
								document.addField("range.end_line", -1);
								document.addField("range.end_character", -1);
							}

							if (jsonObj.get("in_reply_to") != null) {
								document.addField("in_reply_to", jsonObj.get("in_reply_to").getAsString());

							} else {
								document.addField("in_reply_to", "0");
							}

							document.addField("updated", jsonObj.get("updated").getAsString());

							document.addField("message", jsonObj.get("message").getAsString());

							UpdateResponse response = solr.add(document);

							int responseStatus = response.getStatus();

							if (responseStatus == 0) {
								log.doInfoLogging("response: " + responseStatus + " for IC: " + jsonObj.get("id"));
							} else {
								log.doSevereLogging(
										"ERROR: response: " + responseStatus + " for IC: " + jsonObj.get("id"));
							}

							solr.commit();

							log.doInfoLogging("finished IC: " + jsonObj.get("id"));
						}
					}

					log.doInfoLogging("finished file: " + fileIC.getName());
				}
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		log.doInfoLogging("finished all!!!");
	}

	public static void main(String[] args) {

		indexInlineComments();
	}
}
