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

		System.out.println("End compileQuestionResults...");
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

				List<String> ids = Files.readAllLines(Paths.get(file));

				for (String id : ids) {

					listIDs.add(id);
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

		String commentType = Const.INLINE;

		compileQuestionResults(commentType);
	}
}
