package solr.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import solr.utils.Const;

public class BuildCSVFromExcel {

	public static void buildCSVFromExcel(String excelFileLocation, String excelFileName) {

		Workbook workbook = null;

		try {

			StringBuffer sbOutput = new StringBuffer();
			
			sbOutput.append(Const.ID + Const.COMMA + Const.CONFUSION + Const.COMMA + Const.COMMENT + Const.NEW_LINE);

			workbook = Workbook.getWorkbook(new File(excelFileLocation + excelFileName));

			Sheet sheet = workbook.getSheet(0);

			for (int i = 0; i < 396; i++) {
				
				Cell cell1 = sheet.getCell(0, i);

				String id = cell1.getContents();

				Cell cell2 = sheet.getCell(1, i);

				String label = cell2.getContents();

				Cell cell3 = sheet.getCell(2, i);

				String comment = cell3.getContents();

				comment = comment.replaceAll(Const.NEW_LINE, Const.SPACE);

				comment = comment.replaceAll(Const.DOUBLE_QUOTES, Const.EMPTY_STRING);

				sbOutput.append(id + Const.COMMA + label + Const.COMMA + Const.DOUBLE_QUOTES + comment + Const.DOUBLE_QUOTES + Const.NEW_LINE);
			}

			String output = excelFileName.substring(0, 17);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(excelFileLocation + output + Const._CSV), Const._UTF_8))) {
				writer.write(sbOutput.toString());
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

		System.out.println("Done with buildCSVFromExcel...");
	}

	public static void main(String[] args) {

		String excelFilePath = "C:/Users/febert/Dropbox/fifo/Doutorado/Papers Published/ICSME 2017 - Code Reviews/"
				+ "code-review/data - manual labeling/general-comments/hedges/";

		String excelFileName = "classified-hedges.xls";

		buildCSVFromExcel(excelFilePath, excelFileName);
	}
}
