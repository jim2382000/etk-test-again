package net.micropact.aea.du.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.ioUtility.FileUtils;
import net.micropact.aea.utility.Utility;

/**
 * Class containing utility functions related to dealing with the App Server logs.
 *
 * @author zmiller
 */
public final class LogUtility {

	/**
	 * The file extensions which are used by the log files.
	 */
	private static final String[] LOG_EXTENSIONS = {"log", "out", "txt"};

	/** Hide constructor for Utility Class. */
	private LogUtility(){}

	/**
	 * This method attempts to determine the location of the log directory where the
	 * {@link com.entellitrak.logging.Logger} writes to.
	 * I do not know how the log directory is actually determined, however I have programmed in a best-guess which
	 * appears to be good enough for most sites.
	 *
	 * @param etk entellitrak execution context
	 * @return The location of the log file
	 */
	public static String getLogPath(final ExecutionContext etk){
		// This is the default path which seems to work for all ITOPs hosted sites and be the default for tomcat.
		final String defaultLogDirectory = System.getProperty("catalina.base") + "/logs";

		try{
			final String loggingConfigFile = getLoggingConfigFile();
			final String logFilePathFromConfigFile = determineLogPathFromConfigFile(loggingConfigFile);
			return Utility.nvl(logFilePathFromConfigFile, defaultLogDirectory);
		}catch(final RuntimeException e){
			etk.getLogger().error("Error occurred attempting to determine log path", e);
			return defaultLogDirectory;
		}
	}

	/**
	 * This method attempts to parse the logging config file and guess the location of the logging directory.
	 * It is not close to perfect because I don't actually know the format of the config file.
	 * If it doesn't find one, it returns null.
	 *
	 * @param loggingConfigFile location of the logging config file
	 * @return The path of the logging file, or null if it could not guess one
	 */
	private static String determineLogPathFromConfigFile(final String loggingConfigFile) {
		// The lines in the file which specify the directory locations have this format
		final Pattern pattern = Pattern.compile(".*\\.directory\\s*=\\s*(\\S+)\\s*");

		try (FileInputStream fileInputStream = new FileInputStream(loggingConfigFile);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
				Reader fileReader = new BufferedReader(inputStreamReader);
				BufferedReader bufferedReader = new BufferedReader(fileReader)){

			String line = null;

			/* We are going to look for the FIRST line which seems to contain a directory and replace it.
			 * There might be other lines which match our regular expression so the regex may need to be made more
			 * strict */
			while((line = bufferedReader.readLine()) != null){
				final Matcher matcher = pattern.matcher(line);
				if(matcher.find()){
					// We found the matching line, so we have to replace the variables
					return replaceProperties(matcher.group(1));
				}
			}
			return null;
		} catch (final IOException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	/**
	 * The logging config file has vairables of the form ${systemPropertyName}. This method replaces those variables
	 * with their actual values.
	 *
	 * @param string A string containing property variables
	 * @return The string with the property variables replaced with their values
	 */
	private static String replaceProperties(final String string){
		// This is the pattern of the variables
		final Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");

		// This is the final string we will be replacing
		String currentString = string;

		/* This variable indicates whether we are done replacing variables. We have to try to run the replacement at
		 * least once */
		boolean done = false;
		while(!done){
			final Matcher matcher = pattern.matcher(currentString);
			if(matcher.find()){
				// This is the name of the property we are going to replace
				final String property = matcher.group(1);

				// Replace the property name with its value
				currentString = currentString.substring(0, matcher.start())
						+ System.getProperty(property)
						+ currentString.substring(matcher.end());
			}else{
				done = true;
			}
		}

		return currentString;
	}

	/**
	 * This method returns the location of the logging config file. The logging config file tells apache what
	 * type of logging is enabled. This is the file which will contain the logging paths.
	 *
	 * @return The location of the logging config file.
	 */
	private static String getLoggingConfigFile(){
		return System.getProperty("java.util.logging.config.file");
	}

	/**
	 * Gets data about the files in a directory which have particular file extensions.
	 *
	 * @param etk entellitrak execution context.
	 * @return Map of file information of the form:
	 *      <pre>
	 *          [{name: String,
	 *            size: Number,
	 *            lastModified: Number}]
	 *      </pre>
	 */
	public static Collection<Map<String, Object>> getLogFileInfos(final ExecutionContext etk) {
		final String path = getLogPath(etk);
		final File dir = new File(path);

		final Collection<Map<String, Object>> fileInfos;

		if (dir.exists() && dir.isDirectory()) {
			fileInfos = FileUtils.listFiles(dir, LOG_EXTENSIONS)
					.stream()
					.sorted()
					.map(file
							-> Utility.arrayToMap(String.class, Object.class, new Object[][]{
								{"name", file.getName()},
								{"size", FileUtils.byteCountToDisplaySize(file.length())},
								{"lastModified", file.lastModified()}}))
					.collect(Collectors.toList());
		}else{
			fileInfos = Collections.emptyList();
		}

		return fileInfos;
	}

	/**
	 * Get a list of the valid log file names.
	 *
	 * @param etk entellitrak execution context
	 * @return the log file names
	 */
	private static Set<String> getValidLogFileNames(final ExecutionContext etk){
		return getLogFileInfos(etk)
				.stream()
				.map(fileInfo -> (String) fileInfo.get("name"))
				.collect(Collectors.toSet());
	}

	/**
	 * Ensures that a file name is a valid log file.
	 *
	 * <p>
	 *  Throws an exception of the file name is not a valid log file.
	 * </p>
	 *
	 * @param etk entellitrak execution context
	 * @param fileName the file name to validate
	 */
	public static void ensureFileNameIsValidLogFile(final ExecutionContext etk, final String fileName) {
		if(!getValidLogFileNames(etk).contains(fileName)) {
			throw new GeneralRuntimeException(String.format("File \"%s\" is not currently recognized as a valid log file.",
					fileName));
		}
	}
}
