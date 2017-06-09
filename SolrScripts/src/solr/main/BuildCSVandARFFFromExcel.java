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
import solr.utils.Const;

public class BuildCSVandARFFFromExcel {

	public static String replaceBreakLines(String str) {
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
		return str.replaceAll("\\b\\d+\\b", "NUMBER");
	}

	public static String replaceNames(String str) {
		HashSet<String> nameList = readNameListFiles();
		String[] words = str.split("[\\s.,;:\n!?()]+");
		for (int i = 0; i < words.length; i++) {
			for (String name : nameList) {
				if (name.equalsIgnoreCase(words[i])) {
					// System.out.println("|| array[0].equalsIgnoreCase(\"" + name + "\")");
					str = str.replaceAll("\\b" + words[i] + "\\b", "@USERNAME");
				}
			}
		}
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

					if (array[0].equalsIgnoreCase("Set") || array[0].equalsIgnoreCase("And")
							|| array[0].equalsIgnoreCase("In") || array[0].equalsIgnoreCase("How")
							|| array[0].equalsIgnoreCase("To") || array[0].equalsIgnoreCase("Be")
							|| array[0].equalsIgnoreCase("A") || array[0].equalsIgnoreCase("That")
							|| array[0].equalsIgnoreCase("Over") || array[0].equalsIgnoreCase("By")
							|| array[0].equalsIgnoreCase("Any") || array[0].equalsIgnoreCase("The")
							|| array[0].equalsIgnoreCase("On") || array[0].equalsIgnoreCase("Side")
							|| array[0].equalsIgnoreCase("Even") || array[0].equalsIgnoreCase("Maybe")
							|| array[0].equalsIgnoreCase("For") || array[0].equalsIgnoreCase("Best")
							|| array[0].equalsIgnoreCase("Do") || array[0].equalsIgnoreCase("You")
							|| array[0].equalsIgnoreCase("Non") || array[0].equalsIgnoreCase("An")
							|| array[0].equalsIgnoreCase("Log") || array[0].equalsIgnoreCase("I")
							|| array[0].equalsIgnoreCase("Master") || array[0].equalsIgnoreCase("Or")
							|| array[0].equalsIgnoreCase("numbers") || array[0].equalsIgnoreCase("Very")
							|| array[0].equalsIgnoreCase("Run") || array[0].equalsIgnoreCase("Java")
							|| array[0].equalsIgnoreCase("Lot") || array[0].equalsIgnoreCase("Me")
							|| array[0].equalsIgnoreCase("So") || array[0].equalsIgnoreCase("One")
							|| array[0].equalsIgnoreCase("Due") || array[0].equalsIgnoreCase("Have")
							|| array[0].equalsIgnoreCase("Ah") || array[0].equalsIgnoreCase("Alias")
							|| array[0].equalsIgnoreCase("Api") || array[0].equalsIgnoreCase("Are")
							|| array[0].equalsIgnoreCase("Ask") || array[0].equalsIgnoreCase("Big")
							|| array[0].equalsIgnoreCase("C") || array[0].equalsIgnoreCase("cherry")
							|| array[0].equalsIgnoreCase("Constant") || array[0].equalsIgnoreCase("D")
							|| array[0].equalsIgnoreCase("Dear") || array[0].equalsIgnoreCase("desire")
							|| array[0].equalsIgnoreCase("Done") || array[0].equalsIgnoreCase("Else")
							|| array[0].equalsIgnoreCase("File") || array[0].equalsIgnoreCase("Final")
							|| array[0].equalsIgnoreCase("Fine") || array[0].equalsIgnoreCase("G")
							|| array[0].equalsIgnoreCase("Gerrit") || array[0].equalsIgnoreCase("Git")
							|| array[0].equalsIgnoreCase("Given") || array[0].equalsIgnoreCase("Go")
							|| array[0].equalsIgnoreCase("Great") || array[0].equalsIgnoreCase("Happy")
							|| array[0].equalsIgnoreCase("Has") || array[0].equalsIgnoreCase("He")
							|| array[0].equalsIgnoreCase("J") || array[0].equalsIgnoreCase("Jar")
							|| array[0].equalsIgnoreCase("Jenkins") || array[0].equalsIgnoreCase("Kind")
							|| array[0].equalsIgnoreCase("L") || array[0].equalsIgnoreCase("Light")
							|| array[0].equalsIgnoreCase("Like") || array[0].equalsIgnoreCase("Line")
							|| array[0].equalsIgnoreCase("Little") || array[0].equalsIgnoreCase("Long")
							|| array[0].equalsIgnoreCase("Mailing") || array[0].equalsIgnoreCase("Make")
							|| array[0].equalsIgnoreCase("May") || array[0].equalsIgnoreCase("Mean")
							|| array[0].equalsIgnoreCase("Media") || array[0].equalsIgnoreCase("More")
							|| array[0].equalsIgnoreCase("My") || array[0].equalsIgnoreCase("N")
							|| array[0].equalsIgnoreCase("Name") || array[0].equalsIgnoreCase("Native")
							|| array[0].equalsIgnoreCase("Nice") || array[0].equalsIgnoreCase("Ok")
							|| array[0].equalsIgnoreCase("Os") || array[0].equalsIgnoreCase("Phone")
							|| array[0].equalsIgnoreCase("Pick") || array[0].equalsIgnoreCase("ping")
							|| array[0].equalsIgnoreCase("Pretty") || array[0].equalsIgnoreCase("Ready")
							|| array[0].equalsIgnoreCase("Real") || array[0].equalsIgnoreCase("Said")
							|| array[0].equalsIgnoreCase("Say") || array[0].equalsIgnoreCase("Silly")
							|| array[0].equalsIgnoreCase("Skip") || array[0].equalsIgnoreCase("Sorry")
							|| array[0].equalsIgnoreCase("Special") || array[0].equalsIgnoreCase("Sure")
							|| array[0].equalsIgnoreCase("Tar") || array[0].equalsIgnoreCase("Than")
							|| array[0].equalsIgnoreCase("Them") || array[0].equalsIgnoreCase("Tie")
							|| array[0].equalsIgnoreCase("Time") || array[0].equalsIgnoreCase("Too")
							|| array[0].equalsIgnoreCase("Tricky") || array[0].equalsIgnoreCase("Try")
							|| array[0].equalsIgnoreCase("Us") || array[0].equalsIgnoreCase("Valid")
							|| array[0].equalsIgnoreCase("With") || array[0].equalsIgnoreCase("Zero")
							|| array[0].equalsIgnoreCase("Key") || array[0].equalsIgnoreCase("Ran")
							|| array[0].equalsIgnoreCase("Im") || array[0].equalsIgnoreCase("Okay")
							|| array[0].equalsIgnoreCase("Guy") || array[0].equalsIgnoreCase("Beta")
							|| array[0].equalsIgnoreCase("grant") || array[0].equalsIgnoreCase("Made")
							|| array[0].equalsIgnoreCase("Mark") || array[0].equalsIgnoreCase("Job")
							|| array[0].equalsIgnoreCase("Core") || array[0].equalsIgnoreCase("bar")
							|| array[0].equalsIgnoreCase("bill") || array[0].equalsIgnoreCase("Let")
							|| array[0].equalsIgnoreCase("Late") || array[0].equalsIgnoreCase("Less")
							|| array[0].equalsIgnoreCase("Dev") || array[0].equalsIgnoreCase("Sets")
							|| array[0].equalsIgnoreCase("Ids") || array[0].equalsIgnoreCase("Trees")
							|| array[0].equalsIgnoreCase("Per") || array[0].equalsIgnoreCase("Rom")
							|| array[0].equalsIgnoreCase("Hammer") || array[0].equalsIgnoreCase("MD")
							|| array[0].equalsIgnoreCase("Bat")) {

						continue;

					} else {
						set.add(array[0]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static String removeCorruptedChars(String str) {
		str = str.replaceAll("[^\\x00-\\x7F]", "");
		return str;
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
				comment = replaceBreakLines(comment);

				// remove corrupted chars
				comment = removeCorruptedChars(comment);

				// replace single quotes
				comment = replaceSingleQuotes(comment);

				// replace URLs
				comment = replaceURLs(comment);

				// replace greater than
				comment = removeGreaterThan(comment);

				// replace users
				comment = replaceUsers(comment);

				// replace names
				comment = replaceNames(comment);

				// replace commits SHA1
				comment = replaceCommitSha1(comment);

				// replace numbers
				comment = replaceNumbers(comment);

				// escape double quotes: " ==> \"
				comment = replaceDoubleQuotes(comment);

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
