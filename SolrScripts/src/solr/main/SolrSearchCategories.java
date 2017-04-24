package solr.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class SolrSearchCategories {

	@SuppressWarnings("unchecked")
	public static void solrSearchCategories(String framework, String commentType) {

		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		System.out.println("Searching for framework: " + frameworkPath);

		List<Tuple> listTuples = new ArrayList<Tuple>();

		List<String> listIDs = new ArrayList<String>();

		StringBuffer sbFeatures = new StringBuffer();

		sbFeatures.append(Const.CSV_HEADLINE);

		sbFeatures.append(Const.NEW_LINE);

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			long totalFeatures = 0;

			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

				query.setSort(SortClause.asc(Const.ID));

				query.setFields(Const.ID, Const.MESSAGE);

				query.setRows(Const._5000);

				String cursorMark = CursorMarkParams.CURSOR_MARK_START;

				boolean done = false;

				long numFeatures = 0;

				long numComments = 0;

				while (!done) {

					query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

					QueryResponse response = solr.query(query);

					String nextCursorMark = response.getNextCursorMark();

					SolrDocumentList results = response.getResults();

					numComments = results.getNumFound();

					for (int j = 0; j < results.size(); ++j) {

						SolrDocument codeReview = results.get(j);

						String id = (String) codeReview.getFieldValue(Const.ID);

						if (!listIDs.contains(id)) {

							listIDs.add(id);

							listTuples.add(new Tuple(feature, id));
						}

						String comment = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

						numFeatures = numFeatures + Utils.countWordFrequency(feature, comment);
					}

					if (cursorMark.equals(nextCursorMark)) {
						done = true;
					}

					cursorMark = nextCursorMark;
				}

				sbFeatures.append(feature + Const.SEMICOLON + numComments + Const.SEMICOLON + numFeatures);

				sbFeatures.append(Const.NEW_LINE);

				totalFeatures = totalFeatures + numFeatures;
			}

			sbFeatures.append(Const.TOTAL + Const.SEMICOLON + listIDs.size() + Const.SEMICOLON + totalFeatures);

			sbFeatures.append(Const.NEW_LINE);

			Utils.writeCSVOutputFile(framework, commentType, sbFeatures);

			Utils.writeIDsOutputFile(framework, commentType, listIDs);

			Utils.writeTuplesOutputFile(framework, commentType, listTuples);

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with solrSearchCategories...");
	}

	public static void main(String[] args) {

		String commentType = Const.INLINE;

		/** HEDGES */
		String hedges = Const.HEDGES;
		solrSearchCategories(hedges, commentType);

		/** HYPOTHETICALS */
		String hypotheticals = Const.HYPOTHETICALS;
		solrSearchCategories(hypotheticals, commentType);

		/** PROBABLES */
		String probables = Const.PROBABLES;
		solrSearchCategories(probables, commentType);

		/** I_STATEMENTS */
		String I_Statements = Const.I_STATEMENTS;
		solrSearchCategories(I_Statements, commentType);

		/** NONVERBALS */
		String nonverbals = Const.NONVERBALS;
		solrSearchCategories(nonverbals, commentType);

		/** META */
		String meta = Const.META;
		solrSearchCategories(meta, commentType);
	}
}
