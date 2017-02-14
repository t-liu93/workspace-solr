package solr.main;

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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import solr.utils.Const;

public class SolrSearch {

	public static void countFeaturesOccurrences(String framework, String commentType) {

		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		System.out.println("Searching for framework: " + frameworkPath);

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			StringBuffer sbFeaturesOutput = new StringBuffer();

			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				String excludeBots = " NOT author._account_id:" + Const.ANDROID_BOT_TREEHUGGER
						+ " NOT author._account_id:" + Const.ANDROID_BOT_DECKARD + " NOT author._account_id:"
						+ Const.ANDROID_BOT_ANONYMOUS + " NOT author._account_id:" + Const.ANDROID_BOT_BIONIC
						+ " NOT author._account_id:" + Const.ANDROID_BOT_ANDROID_MERGER + " NOT author._account_id:"
						+ Const.ANDROID_BOT_ANDROID_DEVTOOLS + " NOT author._account_id:\\" + Const.ANDROID_BOT_GERRIT;

				System.out.println(solrQueryString + excludeBots);

				System.out.println("Searching for feature: " + solrQueryString);

				sbFeaturesOutput.append(feature + Const.SEMICOLON);

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + excludeBots);

				query.setFields(Const.ID);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				long numCommentsFound = results.getNumFound();

				System.out.println("Number of " + commentType + " comments => " + numCommentsFound);

				sbFeaturesOutput.append(numCommentsFound + Const.NEW_LINE);

				System.out.println("===========================");
			}

			String filePath = "";

			if (commentType.equalsIgnoreCase("general")) {

				filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + framework + Const._OUT + Const._CSV;

			} else if (commentType.equalsIgnoreCase("inline")) {

				filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + framework + Const._OUT + Const._CSV;
			}

			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {
				writer.write(sbFeaturesOutput.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");

	}

	@SuppressWarnings("unchecked")
	public static void printSomeExamples(String framework, String commentType, int nExamples) {

		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		System.out.println("Searching for examples for framework: " + frameworkPath);

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			StringBuffer sbExamplesHtmlOutput = new StringBuffer();

			sbExamplesHtmlOutput.append(printHeadHtml(framework));

			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				String excludeBots = " NOT author._account_id:" + Const.ANDROID_BOT_TREEHUGGER
						+ " NOT author._account_id:" + Const.ANDROID_BOT_DECKARD + " NOT author._account_id:"
						+ Const.ANDROID_BOT_ANONYMOUS + " NOT author._account_id:" + Const.ANDROID_BOT_BIONIC
						+ " NOT author._account_id:" + Const.ANDROID_BOT_ANDROID_MERGER + " NOT author._account_id:"
						+ Const.ANDROID_BOT_ANDROID_DEVTOOLS + " NOT author._account_id:\\" + Const.ANDROID_BOT_GERRIT;

				System.out.println("Searching for feature: " + solrQueryString);

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + excludeBots);

				if (commentType.equalsIgnoreCase("general")) {

				} else if (commentType.equalsIgnoreCase("inline")) {

					query.setFields(Const.CODE_REVIEW_ID, Const.MESSAGE, Const.PATCH_SET, Const.FILE, Const.LINE);
				}

				query.setRows(nExamples);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				for (int i = 0; i < results.size(); ++i) {

					SolrDocument codeReview = results.get(i);

					List<Long> codeReviewID = ((List<Long>) codeReview.getFieldValue(Const.CODE_REVIEW_ID));

					List<String> message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE));

					List<String> file = ((List<String>) codeReview.getFieldValue(Const.FILE));

					List<Long> line = ((List<Long>) codeReview.getFieldValue(Const.LINE));

					List<Long> patchSet = ((List<Long>) codeReview.getFieldValue(Const.PATCH_SET));
					
					//TODO handle when it is General Comment

					String url = Const.URL_GERRIT + codeReviewID.get(0) + Const.SLASH + patchSet.get(0) + Const.SLASH
							+ file.get(0) + Const.AT + line.get(0);

					sbExamplesHtmlOutput.append(printBodyHtml(String.valueOf(codeReviewID.get(0)), message.get(0), feature, url));
					

				}
			}

			sbExamplesHtmlOutput.append("</ul>\n</body>\n</html>");

			String filePath = "";

			if (commentType.equalsIgnoreCase("general")) {

				filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + framework + Const._HTML;

			} else if (commentType.equalsIgnoreCase("inline")) {

				filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + framework + Const._HTML;
			}

			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {
				writer.write(sbExamplesHtmlOutput.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");

	}

	private static Object printBodyHtml(String codeReviewID, String msg, String feature, String url) {

		String html = "<li><b>Code Review ID:</b> <a href=\"" + url + "\">" + codeReviewID + "</a></li>\n";

		html = html + "<li><b>Feature:</b> " + feature + "</li>\n";
		
		html = html + "<li><b>Comment:</b><br>" + msg + "</li><br><br>\n";

		return html;
	}

	private static Object printHeadHtml(String framework) {

		String html = "<!DOCTYPE html>\n<html>\n<head>\n<title>\n Category: " + framework;

		html = html + "</title>\n</head>\n<body>\n<H2> Features examples of the" + framework + " category!</H2>\n<ul>";

		return html;
	}

	public static void main(String[] args) {

		String framework = "hedges";

		String commentType = "inline";

		// countFeaturesOccurrences(framework, commentType);

		printSomeExamples(framework, commentType, 3);
	}
}
