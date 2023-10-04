package net.micropact.aea.du.page.convertSubreportExpressions;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.legacy.report.ReportService;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.report.ReportType;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.dao.SavedReport;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.core.xml.XmlUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/* For jrxml look into expressions of the type:
 * <subreportExpression>
 *   <![CDATA[JasperCompileManager.compileReport("myFile.jrxml")]]>
 * </subreportExpression> */

/*
 * subreport formats below
 *
 * Jaspersoft Studio:
 *
 *     <subreport>
 *       ...
 *       <subreportExpression><![CDATA["reports/zmiller/Adventure Status Pie Chart.jasper"]]></subreportExpression>
 *     </subreport>
 *
 * entellitrak:
 *
 *     old version:
 *     <subreport>
 *       ...
 *       <subreportExpression class="net.sf.jasperreports.engine.JasperReport">
 *          <![CDATA[SubReportLoader.getReportByBusinessKey("report.username.reportName")]]>
 *       </subreportExpression>
 *     </subreport>
 *
 *     new version:
 *     <subreport>
 *       ...
 *       <subreportExpression>
 *          <![CDATA[SubReportLoader.getReportByBusinessKey("report.username.reportName")]]>
 *       </subreportExpression>
 *     </subreport>
 *
 * */

/**
 * This is the controller code for a page which can be used to transform the subreport expressions used within XML
 * reports between the format used by jaspersoft studio and entellitrak.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class ConvertSubreportExpressionsController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final Parameters parameters = etk.getParameters();
		final ReportService reportService = etk.getReportService();

		// The subreport type to convert to will be passed in as the name of the enum.
		final String targetSubreportType = parameters.getSingle("subreportType");
		final List<String> reportsToUpdate = parameters.getField("reports");

		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Convert Subreport Expressions",
								"page.request.do?page=du.page.convertSubreportExpressions")));

		if(targetSubreportType != null){
			PageUtility.validateCsrfToken(etk);

			final SubreportExpressionType targetType = SubreportExpressionType.valueOf(targetSubreportType);

			final Map<SubreportExpressionType, ISubreportMetadataCalculator> subreportCalculators =
					Utility.arrayToMap(SubreportExpressionType.class, ISubreportMetadataCalculator.class,
							new Object[][]{
						{SubreportExpressionType.ETK_BUSINESS_KEY, new EtkReportBusinessKeySubreport(etk)},
						{SubreportExpressionType.JASPERSTUDIO_DEFAULT_STRING, new JasperStudioDefaultSubreport(etk)},
					});

			final ISubreportMetadataCalculator metadataCalculator = subreportCalculators.get(targetType);

			reportsToUpdate
			.forEach(businessKey
					-> updateReport(etk, subreportCalculators, metadataCalculator, businessKey));
		}

		final Gson gson = new Gson();
		response.put("csrfToken", gson.toJson(etk.getCSRFToken()));
		response.put("subreportTypes", gson.toJson(Stream.of(SubreportExpressionType.values())
				.map(subreportExpressionType -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
					{"display", subreportExpressionType.toString()},
					{"value", subreportExpressionType.name()}}))
				.collect(Collectors.toList())));
		response.put("reports", gson.toJson(reportService.getReportsByType(ReportType.XML).stream()
				.map(report -> Map.of("NAME", report.getName(), "BUSINESS_KEY", report.getBusinessKey()))
				.collect(Collectors.toList())));

		return response;
	}

	/**
	 * Update a particular report.
	 *
	 * @param etk entellitrak execution context
	 * @param subreportCalculators the subreport calculators
	 * @param metadataCalculator the metadata calculator
	 * @param savedReportId the saved report id
	 */
	private static void updateReport(final ExecutionContext etk,
			final Map<SubreportExpressionType, ISubreportMetadataCalculator> subreportCalculators,
			final ISubreportMetadataCalculator metadataCalculator, final String businessKey) {
		try {
			final var sourceReportInfo = etk.getReportService().getReportByBusinessKey(businessKey);
			final String xmlDocument = sourceReportInfo.getXMLDesign();
			final Document document = XmlUtility.convertStringToDocumentWithoutValidation(xmlDocument);

			final XPath xPath = XPathFactory.newInstance().newXPath();

			final NodeList subReportExpressions = (NodeList) xPath.compile("//subreport/subreportExpression")
					.evaluate(document, XPathConstants.NODESET);

			if(subReportExpressions.getLength() > 0){
				for(int i = 0; i < subReportExpressions.getLength(); i++){
					final Element node = (Element) subReportExpressions.item(i);

					final String textContent = node.getTextContent().strip();

					final SavedReport savedReport = findSavedReport(subreportCalculators, textContent);

					final Element newNode = document.createElement("subreportExpression");

					final String cDataFragment = metadataCalculator.generateSubreportExpression(savedReport);

					newNode.appendChild(document.createCDATASection(cDataFragment));

					node.getParentNode().replaceChild(newNode, node);
				}

				final String newData = convertDocumentToString(document);

				etk.createSQL("UPDATE etk_saved_report SET report = :report WHERE business_key = :reportId")
				.setParameter("report", newData)
				.setParameter("reportId", businessKey)
				.execute();
			}
		} catch (XPathExpressionException | RuntimeException e) {
			throw new GeneralRuntimeException(String.format("Problem encountered processing report with business_key %s",
					businessKey),
					e);
		}
	}

	/**
	 * Find the saved report described by the textContent and attributeValue.
	 *
	 * @param subreportCalculators the subreport calculators
	 * @param textContent the text content
	 * @param attributeValue the attribute value
	 * @return the saved report, or null if one is not found
	 */
	private static SavedReport findSavedReport(
			final Map<SubreportExpressionType, ISubreportMetadataCalculator> subreportCalculators,
			final String textContent) {
		return subreportCalculators.values()
				.stream()
				.map(subreportMetadataCalculator -> subreportMetadataCalculator.getReportInfo(textContent))
				.filter(Objects::nonNull)
				.findAny()
				.orElseThrow(() -> new RuntimeException(
						String.format("Could not find report described by textContent %s",
								textContent)));
	}

	/**
	 * This class represents the different types of subreport expressions.
	 *
	 * @author zmiller
	 */
	private enum SubreportExpressionType {
		ETK_BUSINESS_KEY("entellitrak"),
		JASPERSTUDIO_DEFAULT_STRING("Jaspersoft Studio");

		private final String display;

		/**
		 * Constructor for SubereportExpressionType.
		 *
		 * @param displayName A user-readable representation of the type of subreport expression.
		 */
		SubreportExpressionType(final String displayName){
			display = displayName;
		}

		@Override
		public String toString(){
			return display;
		}
	}

	/**
	 * This interface represents objects which can read and write the data for a particular type of subreport
	 * expression type.
	 *
	 * @author zmiller
	 */
	private interface ISubreportMetadataCalculator{

		/**
		 * This method gets a SavedReport by textContent if this object knows how to read
		 * it. If this object does not know how to read it, it returns null.
		 *
		 * @param textContent the content of the CDATA element in the XML
		 * @return The SavedReport that is described by the class and text, otherwise null
		 */
		SavedReport getReportInfo(String textContent);

		/**
		 * This method calculates the information that needs to be in a newly generated subreport expression tag
		 * for this type of subreport expression.
		 *
		 * @param reportInfo The information about the report which should be converted.
		 * @return The CDATA content of the subreportExpression
		 */
		String generateSubreportExpression(SavedReport reportInfo);
	}

	/**
	 * This class encompasses the logic specific to subreport expressions which entellitrak uses to find reports by
	 * business key.
	 *
	 * @author zmiller
	 */
	private static class EtkReportBusinessKeySubreport implements ISubreportMetadataCalculator{

		private final ExecutionContext etk;

		/**
		 * Constructor for EtkReportBusinessKeySubreport.
		 *
		 * @param executionContext entellitrak execution context
		 */
		EtkReportBusinessKeySubreport(final ExecutionContext executionContext){
			etk = executionContext;
		}

		@Override
		public SavedReport getReportInfo(final String textContent) {
			try {
				final SavedReport returnValue;

				final Pattern pattern =
						Pattern.compile("^SubReportLoader\\.getReportByBusinessKey\\(\"([\\w\\.]+)\"\\)$");
				final Matcher matcher = pattern.matcher(textContent);

				if(!matcher.find()){
					returnValue = null;
				}else{
					final String reportBusinessKey = matcher.group(1);
					returnValue = SavedReport.ReportService.loadReportByBusinessKey(etk, reportBusinessKey);
				}

				return returnValue;
			} catch (final IncorrectResultSizeDataAccessException e) {
				throw new GeneralRuntimeException(e);
			}
		}

		@Override
		public String generateSubreportExpression(final SavedReport reportInfo) {
			return String.format("SubReportLoader.getReportByBusinessKey(\"%s\")",
					reportInfo.getBusinessKey());
		}
	}

	/**
	 * This class encompasses the logic specific for subreprot expressions which are jaspersoft studio's default.
	 *
	 * @author zmiller
	 */
	private static class JasperStudioDefaultSubreport implements ISubreportMetadataCalculator{

		private final ExecutionContext etk;

		/**
		 * Constructor for JasperStudioDefaultSubreport.
		 *
		 * @param executionContext entellitrak execution context
		 */
		JasperStudioDefaultSubreport(final ExecutionContext executionContext){
			etk = executionContext;
		}

		@Override
		public SavedReport getReportInfo(final String textContent) {
			try {
				// This validation section could probably be done using regular expressions a little easier.
				if(!textContent.startsWith("\"reports/")){
					etk.getLogger().debug("Subreport expression %s does not start with \"reports/\". It is not a format supported by JasperStudioDefaultSubreport.");
					return null;
				}

				final int expectedFilePartLengths = 3;

				final String[] fileParts = textContent.split("/");
				if(fileParts.length != expectedFilePartLengths){
					throw new GeneralRuntimeException(String.format("Report file path (%s) did not have the expected number (2) of forward slashes", textContent));
				}

				final String jasperPart = fileParts[2];

				if(!jasperPart.endsWith(".jasper\"")){
					throw new GeneralRuntimeException(String.format("The Report File Path does not end with \".jasper\"\". %s", jasperPart));
				}

				final String reportName = jasperPart.substring(0, jasperPart.length() - ".jasper\"".length());

				/* We could select on the username as well, except that the core rules for this are ridiculous
				 * and runReportAsByteArray actually assumes that the report names are unique anyway. */
				// APIFUTURE
				final long referencedReportId = etk.createSQL("SELECT SAVED_REPORT_ID FROM etk_saved_report report JOIN etk_user u ON u.user_id = report.user_id WHERE report.name = :reportName")
						.setParameter("reportName", reportName)
						.fetchLong();

				return SavedReport.ReportService.loadReportById(etk, referencedReportId);
			} catch (final IncorrectResultSizeDataAccessException e) {
				throw new GeneralRuntimeException(e);
			}
		}

		@Override
		public String generateSubreportExpression(final SavedReport reportInfo) {
			return String.format("\"reports/%s/%s.jasper\"",
					reportInfo.getUsername(),
					reportInfo.getName());
		}
	}

	/**
	 * This method converts the XML document to a String.
	 * It has the known issue of not having newlines following the
	 * topmost XML declaration, and after jaspersoft's comment starting out the document,
	 * however this shouldn't be an issue.
	 *
	 * Jaspersoft Studio reformats the report for you, so we will
	 * try not to make our output make too many changes to whitespace
	 * to be as version-control friendly as we can.
	 *
	 * @param document the document
	 * @return the string
	 */
	private static String convertDocumentToString(final Document document) {
		try(StringWriter stringWriter = new StringWriter()) {
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			final Transformer transformer = transformerFactory.newTransformer();

			final StreamResult result = new StreamResult(stringWriter);
			transformer.transform(new DOMSource(document), result);
			final String stringFromTransformer = stringWriter.toString();
			return addLinebreakAfterXmlDeclarationIfNecessary(stringFromTransformer);
		} catch (final IOException | TransformerException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	/* The java XML transformers are no longer adding line breaks after the XML declarations.
	 * We would like our output to match what jasperreports does by default, even if it is not necessary.
	 * See https://stackoverflow.com/questions/11275988/how-to-force-java-xml-dom-to-produce-newline-after-xml-version-1-0-encoding
	 * 		for some discussion from other users about the newline issue. */
	private static String addLinebreakAfterXmlDeclarationIfNecessary(final String inputXml) {
		/* Determine whether we would want to insert windows or unix line endings. */
		final boolean isUsingWindowsLineEndings = inputXml.contains("\r\n");
		final String lineEnding = isUsingWindowsLineEndings ? "\r\n" : "\n";

		final String firstLine = inputXml.lines().findFirst().orElse("");

		/* Create a regular expression to match the first line against.
		 * We capture the part that looks like the xml declaration, as well as whatever comes after the xml declaration. */

		/* Suppress warning about regular expression capture groups not being used.
		 * The capture group is being used.*/
		@SuppressWarnings("java:S5860")
		final String firstLineFormatToReplace = "^(?<xmlDeclaration><\\?xml [^>]*>)(?<restOfLine>.+)";

		final String returnXml;

		/* Out of an abundance of caution, we check that the first line matches our regex.
		 * This would be just in case the first line doesn't match, but some other part of the document did match. */
		if(firstLine.matches(firstLineFormatToReplace)) {
			/* The first line matched, so now we know that replaceFirst will find a match on the first line
			 * (as opposed to finding a match later in the document)*/

			final String replacementString = "${xmlDeclaration}" + lineEnding + "${restOfLine}";
			returnXml = inputXml.replaceFirst(firstLineFormatToReplace, replacementString);
		} else {
			returnXml = inputXml;
		}

		return returnXml;
	}
}
