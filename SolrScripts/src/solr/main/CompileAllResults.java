package solr.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class CompileAllResults {

	public static void compileAllResults(String commentType) {

		String filePath = Const.DIR_RESULTS;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = filePath + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = filePath + Const._IC + Const.SLASH;
		}

		compileIDs(filePath, commentType);

		compileTupleIDs(filePath, commentType);

		compileCSV(filePath, commentType);

		System.out.println("Ended compileAllResults...");

	}

	public static void compileIDs(String filePath, String commentType) {
	
		List<String> listIDs = new ArrayList<String>();
	
		List<String> filesIDs = new ArrayList<String>();
	
		File[] allFiles = new File(filePath).listFiles();
	
		findFiles(filesIDs, allFiles, Const._ID_TXT);
	
		for (String file : filesIDs) {
	
			try {
	
				List<String> lines = Files.readAllLines(Paths.get(file));
	
				for (String line : lines) {
	
					if (!listIDs.contains(line)) {
						listIDs.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		String fileName = filePath + commentType + Const.__ID_TXT;
	
		try {
	
			Path path = Paths.get(fileName);
	
			Files.write(path, listIDs, StandardCharsets.UTF_8);
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished compileIDs...");
	}

	public static void compileTupleIDs(String filePath, String commentType) {

		List<Tuple> listTupleIDs = new ArrayList<Tuple>();

		List<String> filesIDs = new ArrayList<String>();

		File[] allFiles = new File(filePath).listFiles();

		findFiles(filesIDs, allFiles, Const._TUPLESID_TXT);

		for (String file : filesIDs) {

			List<Tuple> tuples = Utils.readTulpes(file);

			for (Tuple tuple : tuples) {

				boolean hasDuplicate = false;

				for (Tuple tmp : listTupleIDs) {

					if (tmp.getCommentID().equals(tuple.getCommentID())) {
						hasDuplicate = true;
						break;
					}
				}

				if (!hasDuplicate) {
					listTupleIDs.add(tuple);
				}
			}
		}

		String fileName = filePath + commentType+ Const.__TUPLESID_TXT;

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), Const._UTF_8))) {

			for (Tuple tuple : listTupleIDs) {

				writer.write(tuple.toString());

				writer.write(Const.NEW_LINE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println("Finished compileTupleIDs...");
	}

	public static void compileCSV(String filePath, String commentType) {
		
		long totalNumberComments = 0;
		long totalNumberFeatures = 0;
		long hedgesCom = 0;
		long hypoCom = 0;
		long I_statementsCom = 0;
		long probablesCom = 0;
		long nonverbalsCom = 0;
		long metaCom = 0;
		long questionsCom = 0;
		
		long hedgesFea = 0;
		long hypoFea = 0;
		long I_statementsFea = 0;
		long probablesFea = 0;
		long nonverbalsFea = 0;
		long metaFea = 0;
		long questionsFea = 0;

		try {

			Path path = Paths.get(filePath + commentType + Const.__ID_TXT);
			totalNumberComments = Files.lines(path).count();

			path = Paths.get(filePath + Const.HEDGES + Const._ID_TXT);
			hedgesCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.HYPOTHETICALS + Const._ID_TXT);
			hypoCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.I_STATEMENTS + Const._ID_TXT);
			I_statementsCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.PROBABLES + Const._ID_TXT);
			probablesCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.NONVERBALS + Const._ID_TXT);
			nonverbalsCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.META + Const._ID_TXT);
			metaCom = Files.lines(path).count();
			
			path = Paths.get(filePath + Const.QUESTIONS + Const._ID_TXT);
			questionsCom = Files.lines(path).count();
			
			int lineHedgeFea = Files.readAllLines(Paths.get(filePath + Const.HEDGES + Const._OUT_CSV)).size();
			String stringHedgeFea = Files.readAllLines(Paths.get(filePath + Const.HEDGES + Const._OUT_CSV)).get(lineHedgeFea - Const._1);
			hedgesFea = Long.valueOf(stringHedgeFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineHypoFea = Files.readAllLines(Paths.get(filePath + Const.HYPOTHETICALS + Const._OUT_CSV)).size();
			String stringHypoFea = Files.readAllLines(Paths.get(filePath + Const.HYPOTHETICALS + Const._OUT_CSV)).get(lineHypoFea - Const._1);
			hypoFea = Long.valueOf(stringHypoFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineI_statementsFea = Files.readAllLines(Paths.get(filePath + Const.I_STATEMENTS + Const._OUT_CSV)).size();
			String stringI_statementsFea = Files.readAllLines(Paths.get(filePath + Const.I_STATEMENTS + Const._OUT_CSV)).get(lineI_statementsFea - Const._1);
			I_statementsFea = Long.valueOf(stringI_statementsFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineProbablesFea = Files.readAllLines(Paths.get(filePath + Const.PROBABLES + Const._OUT_CSV)).size();
			String stringProbablesFea = Files.readAllLines(Paths.get(filePath + Const.PROBABLES + Const._OUT_CSV)).get(lineProbablesFea - Const._1);
			probablesFea = Long.valueOf(stringProbablesFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineNonverbalsFea = Files.readAllLines(Paths.get(filePath + Const.NONVERBALS + Const._OUT_CSV)).size();
			String stringNonverbalsFea = Files.readAllLines(Paths.get(filePath + Const.NONVERBALS + Const._OUT_CSV)).get(lineNonverbalsFea - Const._1);
			nonverbalsFea = Long.valueOf(stringNonverbalsFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineMetaFea = Files.readAllLines(Paths.get(filePath + Const.META + Const._OUT_CSV)).size();
			String stringMetaFea = Files.readAllLines(Paths.get(filePath + Const.META + Const._OUT_CSV)).get(lineMetaFea - Const._1);
			metaFea = Long.valueOf(stringMetaFea.split(Const.SEMICOLON)[Const._2]);
			
			int lineQuestionsFea = Files.readAllLines(Paths.get(filePath + Const.QUESTIONS + Const._OUT_CSV)).size();
			String stringQuestionsFea = Files.readAllLines(Paths.get(filePath + Const.QUESTIONS + Const._OUT_CSV)).get(lineQuestionsFea - Const._1);
			questionsFea = Long.valueOf(stringQuestionsFea.split(Const.SEMICOLON)[Const._2]);
			
			totalNumberFeatures = hedgesFea + hypoFea + I_statementsFea + probablesFea + nonverbalsFea + metaFea + questionsFea;
			
			StringBuffer sbResults = new StringBuffer();

			sbResults.append(Const.CSV_HEADLINE + Const.NEW_LINE);
			sbResults.append(Const.HEDGES + Const.SEMICOLON + hedgesCom + Const.SEMICOLON + hedgesFea + Const.NEW_LINE);
			sbResults.append(Const.HYPOTHETICALS + Const.SEMICOLON + hypoCom + Const.SEMICOLON + hypoFea + Const.NEW_LINE);
			sbResults.append(Const.PROBABLES + Const.SEMICOLON + probablesCom + Const.SEMICOLON + probablesFea + Const.NEW_LINE);
			sbResults.append(Const.QUESTIONS + Const.SEMICOLON + questionsCom + Const.SEMICOLON + questionsFea + Const.NEW_LINE);
			sbResults.append(Const.I_STATEMENTS + Const.SEMICOLON + I_statementsCom + Const.SEMICOLON + I_statementsFea + Const.NEW_LINE);
			sbResults.append(Const.NONVERBALS + Const.SEMICOLON + nonverbalsCom + Const.SEMICOLON + nonverbalsFea + Const.NEW_LINE);
			sbResults.append(Const.META + Const.SEMICOLON + metaCom + Const.SEMICOLON + metaFea + Const.NEW_LINE);
			sbResults.append(Const.TOTAL + Const.SEMICOLON + totalNumberComments + Const.SEMICOLON + totalNumberFeatures);
			
			String fileName = filePath + commentType + Const.__OUT_CSV;
			
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), Const._UTF_8))) {
				writer.write(sbResults.toString());
			} catch (Exception e) {
				System.out.println(e);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished compileCSV...");
	}

	public static void findFiles(List<String> filesIDs, File[] allFiles, String type) {
	
		for (File file : allFiles) {
	
			if (file.isFile()) {
	
				if (file.getName().contains(type)) {
	
					filesIDs.add(file.getPath());
				}
			}
		}
	}

	public static void main(String[] args) {

		String commentType = Const.INLINE;

		compileAllResults(commentType);
	}
}
