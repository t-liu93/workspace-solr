package solr.analysis.mallet;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

public class CreateOneFilePerComment {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		String filePath = "C:/Users/febert/Documents/mallet-2.0.8/sample-data/confusion/ic/";

		String commentType = Const.INLINE;

		String emailFilePath = Const.DIR_RESULTS;

		String path = Const.SLASH + Const.DIR_VERIFYING + Const.SLASH + Const.CONFUSION_IDS + Const._TXT;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			emailFilePath = emailFilePath + Const._GC + path;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			emailFilePath = emailFilePath + Const._IC + path;
		}

		String solrQuery = "";

		try {

			List<String> confusionIDs = Files.readAllLines(Paths.get(emailFilePath));

			for (String id : confusionIDs) {
				solrQuery = solrQuery + "id:" + id + " ";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			query.setQuery(solrQuery);

			query.setSort(SortClause.asc(Const.ID));

			query.setFields(Const.ID, Const.MESSAGE);

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

					String message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

					String file = filePath + id + Const._TXT;

					try (Writer writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(file), Const._UTF_8))) {

						writer.write(message);

					} catch (Exception e) {
						System.out.println(e);
					}
				}

				if (cursorMark.equals(nextCursorMark)) {
					done = true;
				}

				cursorMark = nextCursorMark;
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with solrGetEmails...");
	}
}
