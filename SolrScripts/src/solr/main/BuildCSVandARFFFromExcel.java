package solr.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import solr.basics.Tuple;
import solr.utils.Const;

public class BuildCSVandARFFFromExcel {

	public static String replaceBreakLine(String str) {
		String replaced = str;
		if (replaced.contains("\n") || str.contains("\r")) {
			replaced = str.replaceAll("\\r\\n|\\r|\\n", " ");
		}
		return replaced;
	}

	public static String replaceSingleQuotes(String str) {
		String replaced = str;
		if (str.contains("'")) {
			replaced = str.replaceAll("'", "’");
		}
		return replaced;
	}

	public static String removeGreaterThan(String str) {
		String replaced = str;
		if (str.contains(">")) {
			replaced = str.replaceAll(">", "");
		}
		return replaced;
	}

	public static String replaceDoubleQuotes(String str) {
		String replaced = str;
		if (replaced.contains("\"")) {
			replaced = str.replaceAll("\"", "\\\\\"");
		}
		return replaced;
	}

	public static String replaceCommitSha1(String str) {
		return str.replaceAll("\\b[0-9a-f]{5,40}\\b", "COMMIT");
	}

	public static String replaceNumbers(String str) {
		return str.replaceAll("-?\\+?\\d+", "NUMBER");
	}

	public static String replaceNames(String str) {

		HashSet<String> nameList = readNameListFiles();

		String[] words = str.split(" ");

		for (int i = 0; i < words.length; i++) {
			if (nameList.contains(words[i])) {
				str = str.replaceAll(words[i], "@USERNAME");
			}
		}
		
		//TODO fix the "Do", "Set", etc...

		return str;
	}

	public static HashSet<String> readNameListFiles() {
		String path = "C:/Users/febert/Documents/genderComputer/nameLists/";
		HashSet<String> set = new HashSet<String>();
		try {
			List<File> filesInFolder = new ArrayList<>();
			filesInFolder.addAll(Files.walk(Paths.get(path)).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toList()));
			for (File file : filesInFolder) {
				List<String> list = Files.readAllLines(file.toPath());
				for (String line : list) {
					String[] array = line.split(";");
					set.add(array[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static String replaceUsers(String str) {
		if (Pattern.compile(Pattern.quote("danalbert's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)danalbert's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("cferris'"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)cferris'", "@USER");
		}
		if (Pattern.compile(Pattern.quote("host's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)host's", "@HOSTS");
		}
		if (Pattern.compile(Pattern.quote("Mark's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Mark's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("api's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)api's", "@APIS");
		}
		if (Pattern.compile(Pattern.quote("change's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)change's", "@CHANGES");
		}
		if (Pattern.compile(Pattern.quote("scripts'"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)scripts'", "@SCRIPTS");
		}
		if (Pattern.compile(Pattern.quote("Google's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Google's", "@GOOGLES");
		}
		if (Pattern.compile(Pattern.quote("restorecon's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)restorecon's", "@RESTORECONS");
		}
		if (Pattern.compile(Pattern.quote("Daniel's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Daniel's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("jarjar'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)jarjar'd", "@JARJARD");
		}
		if (Pattern.compile(Pattern.quote("Android's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Android's", "@ANDROIDS");
		}
		if (Pattern.compile(Pattern.quote("Dan's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Dan's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("libbase's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)libbase's", "@LIBBASES");
		}
		if (Pattern.compile(Pattern.quote("people's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)people's", "@PEOPLES");
		}
		if (Pattern.compile(Pattern.quote("neil's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)neil's", "@USER");
		}
		return str;
	}

	public static String replaceURLs(String text) {
		String str = text;
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			String url = text.substring(urlMatcher.start(0), urlMatcher.end(0));
			str = str.replace(url, "@URL");
		}
		return str;
	}

	public static void buildCSVandARFFFromExcel(String excelFileLocation, String excelFileName) {

		Workbook workbook = null;

		try {

			StringBuffer sbCsv = new StringBuffer();

			sbCsv.append("comment,confusion\n");

			StringBuffer sbArff = new StringBuffer();

			sbArff.append("@relation hedges\n\n" + "@attribute comment string\n"
					+ "@attribute confusionclass {confusion, no_confusion}\n\n" + "@data\n");

			workbook = Workbook.getWorkbook(new File(excelFileLocation + excelFileName));

			Sheet sheet = workbook.getSheet(0);

			for (int i = 0; i < 396; i++) {

				Cell cell1 = sheet.getCell(0, i);

				String comment = cell1.getContents();

				// remove breaklines: "\\r\\n|\\r|\\n" ==> " "
				comment = replaceBreakLine(comment);

				// replace single quotes
				comment = replaceSingleQuotes(comment);

				// replace greater than
				comment = removeGreaterThan(comment);

				// replace users
				comment = replaceUsers(comment);

				// replace names
				comment = replaceNames(comment);

				// replace URLs
				comment = replaceURLs(comment);

				// replace commits SHA1
				comment = replaceCommitSha1(comment);

				// replace numbers
				comment = replaceNumbers(comment);

				// escape double quotes: " ==> \"
				comment = replaceDoubleQuotes(comment);

				// remove the weird chars:
				// http://www.cafeconleche.org/books/xmljava/chapters/ch03s03.html
				// http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html

				Cell cell2 = sheet.getCell(1, i);

				String label = cell2.getContents();

				sbCsv.append("\"" + comment + "\"" + "," + label + "\n");
				sbArff.append("\"" + comment + "\"" + "," + label + "\n");
			}

			String outputCsv = excelFileName.substring(0, 17);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(excelFileLocation + outputCsv + Const._CSV), Const._UTF_8))) {
				writer.write(sbCsv.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

			String outputArff = excelFileName.substring(0, 17);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(excelFileLocation + outputArff + Const._ARFF), Const._UTF_8))) {
				writer.write(sbArff.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (IOException | BiffException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		System.out.println("Done with buildCSVandARFFFromExcel...");
	}

	public static void main(String[] args) {

		String excelFilePath = "C:/Users/febert/Dropbox/fifo/Doutorado/Papers Published/ICSME 2017 - Code Reviews/"
				+ "code-review/data - manual labeling/general-comments/hedges/";

		String excelFileName = "classified-hedges.xls";

		buildCSVandARFFFromExcel(excelFilePath, excelFileName);
	}
}
