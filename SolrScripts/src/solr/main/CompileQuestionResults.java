package solr.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import solr.utils.Const;
import solr.utils.Utils;

public class CompileQuestionResults {

	public static void compileQuestionResults(String commentType) {

		String filePath = Const.DIR_RESULTS;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = filePath + Const._GC + Const.SLASH + Const.DIR_SEARCHQUESTION;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = filePath + Const._IC + Const.SLASH + Const.DIR_SEARCHQUESTION;
		}

		compileIDs(filePath, commentType, Const._ID_TXT);

		compileIDs(filePath, commentType, Const._TUPLESID_TXT);

		compileCSV(filePath, commentType);

		System.out.println("End compileQuestionResults...");
	}

	public static void compileCSV(String filePath, String commentType) {

		List<String> filesIDs = new ArrayList<String>();

		File[] allFiles = new File(filePath).listFiles();

		for (File file : allFiles) {

			if (file.isFile()) {

				if (file.getName().contains(Const._OUT_CSV)) {

					filesIDs.add(file.getPath());
				}
			}
		}

		StringBuffer sbOutput = new StringBuffer();

		sbOutput.append(Const.CSV_HEADLINE + Const.NEW_LINE);

		int commentsSBARQ = 0;

		int featuresSBARQ = 0;

		int commentsSQ = 0;

		int featuresSQ = 0;

		int commentsTotal = 0;

		int featuresTotal = 0;

		for (String file : filesIDs) {

			try {

				List<String> lines = Files.readAllLines(Paths.get(file));

				String[] sbarq = lines.get(Const._1).split(Const.SEMICOLON);
				int comSQARB = Integer.valueOf(sbarq[1]);
				int feaSQARB = Integer.valueOf(sbarq[2]);
				commentsSBARQ = commentsSBARQ + comSQARB;
				featuresSBARQ = featuresSBARQ + feaSQARB;

				String[] sq = lines.get(Const._2).split(Const.SEMICOLON);
				int comSQ = Integer.valueOf(sq[1]);
				int feaSQ = Integer.valueOf(sq[2]);
				commentsSQ = commentsSQ + comSQ;
				featuresSQ = featuresSQ + feaSQ;

				String[] total = lines.get(Const._3).split(Const.SEMICOLON);
				int comTotal = Integer.valueOf(total[1]);
				int feaTotal = Integer.valueOf(total[2]);
				commentsTotal = commentsTotal + comTotal;
				featuresTotal = featuresTotal + feaTotal;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		sbOutput.append(
				Const.SBARQ + Const.SEMICOLON + commentsSBARQ + Const.SEMICOLON + featuresSBARQ + Const.NEW_LINE);

		sbOutput.append(Const.SQ + Const.SEMICOLON + commentsSQ + Const.SEMICOLON + featuresSQ + Const.NEW_LINE);

		sbOutput.append(Const.TOTAL + Const.SEMICOLON + commentsTotal + Const.SEMICOLON + featuresTotal);

		Utils.writeCSVOutputFile(Const.QUESTIONS, commentType, sbOutput);
	}

	public static void compileIDs(String filePath, String commentType, String fileType) {

		List<String> listIDs = new ArrayList<String>();

		List<String> filesIDs = new ArrayList<String>();

		File[] allFiles = new File(filePath).listFiles();

		for (File file : allFiles) {

			if (file.isFile()) {

				if (file.getName().contains(fileType)) {

					filesIDs.add(file.getPath());
				}
			}
		}

		for (String file : filesIDs) {

			try {

				List<String> lines = Files.readAllLines(Paths.get(file));

				for (String line : lines) {

					listIDs.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String newFilePath = Const.DIR_RESULTS;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			newFilePath = newFilePath + Const._GC + Const.SLASH + Const.QUESTIONS;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			newFilePath = newFilePath + Const._IC + Const.SLASH + Const.QUESTIONS;

		}

		if (fileType.equals(Const._ID_TXT)) {

			newFilePath = newFilePath + Const._ID + Const._TXT;

		} else if (fileType.equals(Const._TUPLESID_TXT)) {

			newFilePath = newFilePath + Const._TUPLES_ID + Const._TXT;
		}

		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(newFilePath), Const._UTF_8))) {

			for (String string : listIDs) {

				writer.write(string);

				writer.write(Const.NEW_LINE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {

		String commentType = Const.GENERAL;

		compileQuestionResults(commentType);
	}
}
