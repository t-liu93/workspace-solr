package solr.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <h1>Log</h1>
 * 
 * The Log class contains all logging methods used in the program.
 *
 * @author Felipe Ebert
 * @version 0.1
 * @since 2016-11-25
 */
public class Log {

	// create the Logger instance
	private final Logger logger = Logger.getLogger(Log.class.getName());

	// create the FileHandler
	private FileHandler fh = null;

	/**
	 * This is the Log constructor.
	 * 
	 * @param fileName
	 *            The name of the log file.
	 */
	public Log(String fileName) {

		// create the date format
		SimpleDateFormat format = new SimpleDateFormat(Const.LOG_DATE_FORMAT);
		try {
			// create the log file in the format:
			// "./logs/fileName.y-MM-dd-hhmmss.log"
			fh = new FileHandler(Const.DIR_LOGS + fileName + Const.DOT
					+ format.format(Calendar.getInstance().getTime()) + Const.LOG_EXTENSION);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set the formatter
		fh.setFormatter(new SimpleFormatter());

		// add the log handler to the file
		logger.addHandler(fh);
	}

	/**
	 * This method logs info.
	 * 
	 * @param msg
	 *            The message.
	 */
	public void doInfoLogging(String msg) {
		logger.info(msg);
	}

	/**
	 * This method logs severe.
	 * 
	 * @param msg
	 *            The message.
	 */
	public void doSevereLogging(String msg) {
		logger.severe(msg);
	}

	/**
	 * This method logs severe with the Exception StackTrace.
	 * 
	 * @param msg
	 *            The message.
	 * @param e
	 *            The exception.
	 */
	public void doSevereLogging(String msg, Exception e) {
		logger.log(Level.SEVERE, msg, e);
	}

	/**
	 * This method logs warnings.
	 * 
	 * @param msg
	 *            The message.
	 */
	public void doWarningLogging(String msg) {
		logger.warning(msg);
	}

	/**
	 * This method logs fine.
	 * 
	 * @param msg
	 *            The message.
	 */
	public void doFineLogging(String msg) {
		logger.fine(msg);
	}
}
