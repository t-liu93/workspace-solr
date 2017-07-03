package solr.analysis.email;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import solr.utils.Const;

public class SolrGetEmails {

	@SuppressWarnings("unchecked")
	public static void solrGetEmails(String commentType) {

		String emailFilePath = Const.DIR_RESULTS;

		String path = Const.SLASH + Const.DIR_VERIFYING + Const.SLASH + Const.CONFUSION_IDS + Const._TXT;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			emailFilePath = emailFilePath + Const._GC + path;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			emailFilePath = emailFilePath + Const._IC + path;
		}

		List<String> confusionIDs = new ArrayList<String>();

		try {

			confusionIDs = Files.readAllLines(Paths.get(emailFilePath));

		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> mapNoConfusion = new HashMap<String, String>();

		Map<String, String> mapConfusion = new HashMap<String, String>();

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			query.setQuery(Const.EXCLUDE_BOTS);

			query.setSort(SortClause.asc(Const.ID));

			query.setFields(Const.ID, Const.AUTHOR_NAME, Const.AUTHOR_EMAIL);

			query.setRows(Const._5000);

			String cursorMark = CursorMarkParams.CURSOR_MARK_START;

			boolean done = false;

			while (!done) {

				query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

				QueryResponse response = solr.query(query);

				String nextCursorMark = response.getNextCursorMark();

				SolrDocumentList results = response.getResults();

				for (int j = 0; j < results.size(); ++j) {

					SolrDocument codeReview = results.get(j);

					String id = (String) codeReview.getFieldValue(Const.ID);

					if (id.equals("bc8574df9181b3631561f4e7ebe9c53c24a46b03")) {

						continue;
					}

					boolean isConfusion = confusionIDs.contains(id);

					String name = "";

					if ((List<String>) codeReview.getFieldValue(Const.AUTHOR_NAME) == null) {

						continue;

					} else {

						name = ((List<String>) codeReview.getFieldValue(Const.AUTHOR_NAME)).get(0);

						Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");

						Matcher matcher = pattern.matcher(name);

						if (matcher.find()) {

							continue;
						}
					}

					String email = "";

					if ((List<String>) codeReview.getFieldValue(Const.AUTHOR_EMAIL) == null) {

						continue;

					} else {

						email = ((List<String>) codeReview.getFieldValue(Const.AUTHOR_EMAIL)).get(0);

						Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");

						Matcher matcher = pattern.matcher(email);

						if (matcher.find()) {

							continue;
						}

					}

					
					if (isConfusion) {
						
						if (!mapConfusion.containsKey(email)) {
							
							mapConfusion.put(email, name);
						}
					} else {
						
						if (!mapNoConfusion.containsKey(email)) {
							
							mapNoConfusion.put(email, name);
						}
					}
				}

				if (cursorMark.equals(nextCursorMark)) {
					done = true;
				}

				cursorMark = nextCursorMark;
			}

			String noConfusionFile = Const.EMAILS + Const.SLASH + Const.EMAIL_LIST;
			
			String confusionFile = Const.EMAILS + Const.SLASH + Const.EMAIL_LIST;

			if (commentType.equalsIgnoreCase(Const.GENERAL)) {

				noConfusionFile = noConfusionFile + Const._GC + Const._TXT;
				
				confusionFile = confusionFile + Const._GC + Const.DASH + Const.CONFUSION + Const._TXT;

			} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

				noConfusionFile = noConfusionFile + Const._IC + Const._TXT;
				
				confusionFile = confusionFile + Const._IC + Const.DASH + Const.CONFUSION + Const._TXT;
			}

			
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(noConfusionFile), Const._UTF_8))) {

				for (String key : mapNoConfusion.keySet()) {

					writer.write(key + " ### " + mapNoConfusion.get(key) + " <" + key + ">" + Const.NEW_LINE);
				}
				
			} catch (Exception e) {
				System.out.println(e);
			}

			
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(confusionFile), Const._UTF_8))) {

				for (String key : mapConfusion.keySet()) {

					writer.write(key + " ### " + mapConfusion.get(key) + " <" + key + ">" + Const.NEW_LINE);
				}
				
			} catch (Exception e) {
				System.out.println(e);
			}
			
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with solrGetEmails...");
	}

	public static void main(String[] args) {

		String commentType = Const.INLINE;

		solrGetEmails(commentType);
	}
}
