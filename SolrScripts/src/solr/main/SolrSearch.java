package solr.main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import solr.utils.Const;
import solr.utils.Utils;

/**
 * Solr - Escaping Special Characters
 * 
 * The current list special characters are:
 * 
 * * + - && || ! ( ) { } [ ] ^ " ~ * ? : \
 * 
 * To escape these character use the \ before the character.
 * 
 */

public class SolrSearch {

	public static void countAllFeatures(String commentType) {

		System.out.println("Started countAllFeatures...");
		
		StringBuffer sbResults = new StringBuffer();

		sbResults.append(Const.CSV_HEADLINE);

		sbResults.append(Const.NEW_LINE);

		long totalFeatures = 0;

		FeatureResult hedges = countFeatures(Const.HEDGES, commentType);
		
		sbResults.append(Const.HEDGES + Const.SEMICOLON + hedges.getTotalNumCommentsFound() + Const.SEMICOLON
				+ hedges.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);
		
		System.out.println("Finished countFeatures for hedges...");

		FeatureResult hypo = countFeatures(Const.HYPOTHETICALS, commentType);
		
		sbResults.append(Const.HYPOTHETICALS + Const.SEMICOLON + hypo.getTotalNumCommentsFound() + Const.SEMICOLON
				+ hypo.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for hypotheticals...");
		
		FeatureResult probables = countFeatures(Const.PROBABLES, commentType);
		
		sbResults.append(Const.PROBABLES + Const.SEMICOLON + probables.getTotalNumCommentsFound() + Const.SEMICOLON
				+ probables.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for probables...");
		
		FeatureResult I_statements = countFeatures(Const.I_STATEMENTS, commentType);
		
		sbResults.append(Const.I_STATEMENTS + Const.SEMICOLON + I_statements.getTotalNumCommentsFound()
				+ Const.SEMICOLON + I_statements.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for I-statements...");
		
		FeatureResult nonverbals = countFeatures(Const.NONVERBALS, commentType);
		
		sbResults.append(Const.NONVERBALS + Const.SEMICOLON + nonverbals.getTotalNumCommentsFound() + Const.SEMICOLON
				+ nonverbals.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for nonverbals...");
		
		FeatureResult meta = countFeatures(Const.META, commentType);
		
		sbResults.append(Const.META + Const.SEMICOLON + meta.getTotalNumCommentsFound() + Const.SEMICOLON
				+ meta.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for meta...");
		
		FeatureResult questions = countQuestionFeatures(Const.QUESTIONS, commentType);
		
		sbResults.append(Const.QUESTIONS + Const.SEMICOLON + questions.getTotalNumCommentsFound() + Const.SEMICOLON
				+ questions.getTotalNumFeaturesFound());
		
		sbResults.append(Const.NEW_LINE);

		System.out.println("Finished countFeatures for questions...");
		
		totalFeatures = hedges.getTotalNumFeaturesFound() + hypo.getTotalNumFeaturesFound()
				+ I_statements.getTotalNumFeaturesFound() + meta.getTotalNumFeaturesFound()
				+ nonverbals.getTotalNumFeaturesFound() + probables.getTotalNumFeaturesFound()
				+ questions.getTotalNumFeaturesFound();

		List<String> uniqueIDs = getUniqueIDs(commentType, hedges.getListIDs(), hypo.getListIDs(), I_statements.getListIDs(),
				meta.getListIDs(), nonverbals.getListIDs(), probables.getListIDs(), questions.getListIDs());

		sbResults.append(Const.TOTAL + Const.SEMICOLON + uniqueIDs.size() + Const.SEMICOLON + totalFeatures);
		sbResults.append(Const.NEW_LINE);

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.GENERAL + Const._RESULTS + Const._CSV;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const.INLINE + Const._RESULTS + Const._CSV;
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {
			writer.write(sbResults.toString());
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Done with countAllFeatures...");
	}

	@SuppressWarnings("unchecked")
	public static FeatureResult countFeatures(String framework, String commentType) {

		FeatureResult result = new FeatureResult();

		List<String> listIDs = new ArrayList<String>();

		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		// System.out.println("Searching for framework: " + frameworkPath);

		StringBuffer sbFeaturesOutput = new StringBuffer();

		sbFeaturesOutput.append(Const.CSV_HEADLINE);

		sbFeaturesOutput.append(Const.NEW_LINE);

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			long totalNumFeaturesFound = 0;

			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

				query.setSort(SortClause.asc(Const.ID));

				query.setFields(Const.ID, Const.MESSAGE);

				query.setRows(Const._5000);

				String cursorMark = CursorMarkParams.CURSOR_MARK_START;

				boolean done = false;

				long numFeaturesFound = 0;

				long numCommentsFound = 0;

				while (!done) {

					query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

					QueryResponse response = solr.query(query);

					String nextCursorMark = response.getNextCursorMark();

					SolrDocumentList results = response.getResults();

					numCommentsFound = results.getNumFound();

					for (int j = 0; j < results.size(); ++j) {

						SolrDocument codeReview = results.get(j);

						String id = (String) codeReview.getFieldValue(Const.ID);

						if (!listIDs.contains(id)) {

							listIDs.add(id);
						}

						String comment = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

						numFeaturesFound = numFeaturesFound + Utils.countWordFrequency(feature, comment);
					}

					if (cursorMark.equals(nextCursorMark)) {
						done = true;
					}

					cursorMark = nextCursorMark;
				}

				sbFeaturesOutput
						.append(feature + Const.SEMICOLON + numCommentsFound + Const.SEMICOLON + numFeaturesFound);

				sbFeaturesOutput.append(Const.NEW_LINE);

				totalNumFeaturesFound = totalNumFeaturesFound + numFeaturesFound;
			}

			sbFeaturesOutput
					.append(Const.TOTAL + Const.SEMICOLON + listIDs.size() + Const.SEMICOLON + totalNumFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			result.setFramework(framework);

			result.setListIDs(listIDs);

			result.setTotalNumCommentsFound(listIDs.size());

			result.setTotalNumFeaturesFound(totalNumFeaturesFound);

			writeCSVOutputFile(framework, commentType, sbFeaturesOutput);

			writeIDsOutputFile(framework, commentType, listIDs);

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		// System.out.println("Done...");

		return result;
	}

	@SuppressWarnings("unchecked")
	public static FeatureResult countQuestionFeatures(String framework, String commentType) {

		FeatureResult result = new FeatureResult();

		List<String> listIDs = new ArrayList<String>();

		// System.out.println("Searching for framework: " + framework);

		StringBuffer sbFeaturesOutput = new StringBuffer();

		sbFeaturesOutput.append(Const.CSV_HEADLINE);

		sbFeaturesOutput.append(Const.NEW_LINE);

		try {

			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, parse");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			String queryString = Const.STAR_TWODOTS_START + Const.EXCLUDE_BOTS;

			query.setQuery(queryString);

			query.setSort(SortClause.asc(Const.ID));

			query.setFields(Const.ID, Const.MESSAGE);

			query.setRows(Const._5000);

			String cursorMark = CursorMarkParams.CURSOR_MARK_START;

			boolean done = false;

			long numSBARQFeaturesFound = 0;

			long numSQFeaturesFound = 0;

			long numSBARQCommentsFound = 0;

			long numSQCommentsFound = 0;

			while (!done) {

				query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

				QueryResponse response = solr.query(query);

				String nextCursorMark = response.getNextCursorMark();

				SolrDocumentList results = response.getResults();

				for (int i = 0; i < results.size(); i++) {

					SolrDocument codeReview = results.get(i);

					String id = (String) codeReview.getFieldValue(Const.ID);

					String comment = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

					Annotation document = new Annotation(comment);

					pipeline.annotate(document);

					List<CoreMap> sentences = document.get(SentencesAnnotation.class);

					for (CoreMap sentence : sentences) {

						Tree tree = sentence.get(TreeAnnotation.class);
						Tree c = tree.getChild(0);

						if (c.label().toString().equalsIgnoreCase(Const.SBARQ)) {

							numSBARQFeaturesFound = numSBARQFeaturesFound + 1;

							if (!listIDs.contains(id)) {

								numSBARQCommentsFound = numSBARQCommentsFound + 1;

								listIDs.add(id);
							}

						} else if (c.label().toString().equalsIgnoreCase(Const.SQ)) {

							numSQFeaturesFound = numSQFeaturesFound + 1;

							if (!listIDs.contains(id)) {

								numSQCommentsFound = numSQCommentsFound + 1;

								listIDs.add(id);
							}
						}
					}
				}

				if (cursorMark.equals(nextCursorMark)) {
					done = true;
				}

				cursorMark = nextCursorMark;
			}

			sbFeaturesOutput.append(
					Const.SBARQ + Const.SEMICOLON + numSBARQCommentsFound + Const.SEMICOLON + numSBARQFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			sbFeaturesOutput
					.append(Const.SQ + Const.SEMICOLON + numSQCommentsFound + Const.SEMICOLON + numSQFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			long totalNumFeaturesFound = numSQFeaturesFound + numSBARQFeaturesFound;

			sbFeaturesOutput
					.append(Const.TOTAL + Const.SEMICOLON + listIDs.size() + Const.SEMICOLON + totalNumFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			result.setFramework(framework);

			result.setListIDs(listIDs);

			result.setTotalNumCommentsFound(listIDs.size());

			result.setTotalNumFeaturesFound(totalNumFeaturesFound);

			writeCSVOutputFile(framework, commentType, sbFeaturesOutput);

			writeIDsOutputFile(framework, commentType, listIDs);

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		// System.out.println("Done...");

		return result;
	}

	public static List<String> getUniqueIDs(String commentType, List<String> hedgesIDs, List<String> hypoIDs, List<String> I_statementsIDs,
			List<String> metaIDs, List<String> nonverbalsIDs, List<String> probablesIDs, List<String> questionsIDs) {

		List<String> uniqueIDs = hedgesIDs;

		for (String id : hypoIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}

		for (String id : I_statementsIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}
		
		for (String id : metaIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}
		
		for (String id : nonverbalsIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}
		
		for (String id : probablesIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}
		
		for (String id : questionsIDs) {

			if (!uniqueIDs.contains(id)) {

				uniqueIDs.add(id);
			}
		}

		writeIDsOutputFile(Const.ALL, commentType, uniqueIDs);
		
		return uniqueIDs;
	}

	public static void writeIDsOutputFile(String framework, String commentType, List<String> listIDs) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + framework + Const._ID + Const._TXT;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + framework + Const._ID + Const._TXT;
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {

			for (String string : listIDs) {

				writer.write(string);

				writer.write(Const.NEW_LINE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void writeCSVOutputFile(String framework, String commentType, StringBuffer sbFeaturesOutput) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + framework + Const._OUT + Const._CSV;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + framework + Const._OUT + Const._CSV;
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {

			writer.write(sbFeaturesOutput.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void countAllFeaturesOccurences(String commentType) {

		List<String> listIDs = countAllFeaturesOccurrencesExceptQuestions(commentType);

		try {

			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, parse");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			String queryString = Const.STAR_TWODOTS_START + Const.EXCLUDE_BOTS;

			query.setQuery(queryString);

			query.setRows(Const._3000);

			query.setSort(SortClause.asc(Const.ID));

			query.setFields(Const.ID, Const.MESSAGE);

			String cursorMark = CursorMarkParams.CURSOR_MARK_START;

			boolean done = false;

			while (!done) {

				query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

				QueryResponse response = solr.query(query);

				String nextCursorMark = response.getNextCursorMark();

				SolrDocumentList results = response.getResults();

				for (int i = 0; i < results.size(); i++) {

					SolrDocument codeReview = results.get(i);

					String id = (String) codeReview.getFieldValue(Const.ID);

					List<String> message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE));

					Annotation document = new Annotation(message.get(0));

					pipeline.annotate(document);

					List<CoreMap> sentences = document.get(SentencesAnnotation.class);

					for (CoreMap sentence : sentences) {

						Tree tree = sentence.get(TreeAnnotation.class);
						Tree c = tree.getChild(0);

						if (c.label().toString().equalsIgnoreCase(Const.SBARQ)
								|| c.label().toString().equalsIgnoreCase(Const.SQ)) {

							// System.out.println("sentence: " + sentence);
							// System.out.println("parse tree: " + tree);
							// System.out.println("root label: " + c.label());
							// System.out.println("=========================");

							if (!listIDs.contains(id)) {

								listIDs.add(id);
							}
						}
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

		System.out.println("Done...");

		System.out.println("Total os comments: " + listIDs.size());
	}

	public static List<String> countAllFeaturesOccurrencesExceptQuestions(String commentType) {

		List<String> listIDs = new ArrayList<String>();

		for (int i = 0; i < Const.features.length; i++) {

			String framework = Const.features[i];

			String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

			System.out.println("Searching for framework: " + frameworkPath);

			try {

				SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

				String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

				List<String> features = Files.readAllLines(Paths.get(frameworkPath));

				for (String feature : features) {

					String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

					SolrQuery query = new SolrQuery();

					query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

					query.setFields(Const.ID, Const.MESSAGE);

					query.setRows(Const._50000);

					QueryResponse response = solr.query(query);

					SolrDocumentList results = response.getResults();

					long numCommentsFound = results.getNumFound();

					System.out.println("Number of comments for " + feature + " => " + numCommentsFound);

					for (int j = 0; j < results.size(); ++j) {

						SolrDocument codeReview = results.get(j);

						String id = (String) codeReview.getFieldValue(Const.ID);

						if (!listIDs.contains(id)) {

							listIDs.add(id);
						}
					}
				}

				// TODO write the IDs in a separate file?! (only the IDs?)

				// String filePath = "";
				//
				// if (commentType.equalsIgnoreCase("general")) {
				//
				// filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH +
				// framework + Const._OUT + Const._CSV;
				//
				// } else if (commentType.equalsIgnoreCase("inline")) {
				//
				// filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH +
				// framework + Const._OUT + Const._CSV;
				// }
				//
				// try (Writer writer = new BufferedWriter(
				// new OutputStreamWriter(new FileOutputStream(filePath),
				// Const._UTF_8))) {
				// writer.write(sbFeaturesOutput.toString());
				// } catch (Exception e) {
				// System.out.println(e);
				// }

			} catch (SolrServerException | IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Done...");

		System.out.println("Total os comments (except question): " + listIDs.size());

		return listIDs;
	}

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

				System.out.println(solrQueryString + Const.EXCLUDE_BOTS);

				System.out.println("Searching for feature: " + solrQueryString);

				sbFeaturesOutput.append(feature + Const.SEMICOLON);

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

				query.setFields(Const.ID);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				long numCommentsFound = results.getNumFound();

				System.out.println("Number of " + commentType + " comments => " + numCommentsFound);

				sbFeaturesOutput.append(numCommentsFound + Const.NEW_LINE);

				System.out.println("===========================");
			}

			writeCSVOutputFile(framework, commentType, sbFeaturesOutput);

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

				System.out.println("Searching for feature: " + solrQueryString);

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

				if (commentType.equalsIgnoreCase(Const.GENERAL)) {

				} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

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

					// TODO handle when it is General Comment

					String url = Const.URL_GERRIT + codeReviewID.get(0) + Const.SLASH + patchSet.get(0) + Const.SLASH
							+ file.get(0) + Const.AT + line.get(0);

					sbExamplesHtmlOutput
							.append(printBodyHtml(String.valueOf(codeReviewID.get(0)), message.get(0), feature, url));

				}
			}

			sbExamplesHtmlOutput.append("</ul>\n</body>\n</html>");

			String filePath = "";

			if (commentType.equalsIgnoreCase(Const.GENERAL)) {

				filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + framework + Const._HTML;

			} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

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

	@SuppressWarnings("unchecked")
	public static void printSomeQuestionFeatureExamples() {

		try {

			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, parse");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.STAR_TWODOTS_START;

			SolrQuery query = new SolrQuery();

			query.setQuery(solrQuery + Const.EXCLUDE_BOTS);

			query.setFields(Const.MESSAGE);

			query.setRows(500);

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			for (int j = 0; j < results.size(); ++j) {

				SolrDocument codeReview = results.get(j);

				List<String> message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE));

				Annotation document = new Annotation(message.get(0));

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				for (CoreMap sentence : sentences) {

					Tree tree = sentence.get(TreeAnnotation.class);
					Tree c = tree.getChild(0);

					// SBARQ and SQ

					if (c.label().toString().equalsIgnoreCase(Const.SBARQ)
							|| c.label().toString().equalsIgnoreCase(Const.SQ)) {

						System.out.println("sentence: " + sentence);
						System.out.println("parse tree: " + tree);
						System.out.println("root label: " + c.label());
						System.out.println("=========================");
					}
				}
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void main(String[] args) {

		// String framework = "nonverbals";
		// countFeaturesOccurrences(framework, commentType);

		String commentType = Const.INLINE;

		// countAllFeaturesOccurrencesExceptQuestions(commentType);

		// countAllFeaturesOccurences(commentType);

		// printSomeExamples(framework, commentType, 3);

		countAllFeatures(commentType);
	}
}
