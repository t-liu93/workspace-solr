package solr.utils;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class WriteExcelTemplate {
	
	/**
	 * Source: http://poi.apache.org/spreadsheet/how-to.html
	 * 
	 * */

	public static void main(String[] args) {

		try {

			FileOutputStream out = new FileOutputStream("./workbook.xls");

			Workbook wb = new HSSFWorkbook();

			Sheet s = wb.createSheet();

			wb.setSheetName(0, "Test");

			Row r = null;

			Cell c = null;
			
			for (int row = 0; row < 10; row++) {
				
				r = s.createRow(row);
				
				for (int cell = 0; cell < 4; cell++) {
					
					c = r.createCell(cell);
					c.setCellValue("abs");
				}
			}


			wb.write(out);

			out.close();
			
			wb.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done...");
	}
}
