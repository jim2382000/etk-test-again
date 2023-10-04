package net.micropact.aea.du.page.codeSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.FormService;
import com.entellitrak.configuration.ViewService;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.legacy.report.ReportService;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.query.QueryService;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.StringUtil;
import net.entellitrak.aea.gl.api.java.map.MapBuilder;
import net.micropact.aea.core.cache.AeaCoreConfiguration;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

@HandlerScript(type = PageController.class)
public class CodeSearchController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final WorkspaceService workspaceService = etk.getWorkspaceService();
		final FormService formService = etk.getFormService();
		final QueryService queryService = etk.getQueryService();
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final ViewService viewService = etk.getViewService();
		final ReportService reportService = etk.getReportService();

		final TextResponse response = etk.createTextResponse();

		setBreadcrumb(response);

		final Gson gson = new Gson();

		final List<ResultGroup> filteredResultGroups;

		final String keyword = StringUtil.toNonEmptyString(etk.getParameters().getSingle("keyword"));
		final boolean isCaseSensitive = "1".equals(etk.getParameters().getSingle("caseSensitive"));
		final String workspaceParameter = etk.getParameters().getSingle("workspace");
		final String workspaceName = Optional.ofNullable(workspaceParameter).orElseGet(() -> workspaceService.getActiveWorkspace().getName());
		if (keyword != null) {
			final Workspace workspace = workspaceService.getWorkspace(workspaceName);

			final List<ResultGroup> resultGroups = new ArrayList<>();

			// Script Objects
			resultGroups.add(new ResultGroup("Script Objects",
					convertMapsToResults(workspaceService.getScripts(workspace)
							.stream()
							.map(script -> new MapBuilder<String, Object>()
									.put("NAME", script.getFullyQualifiedName())
									.put("CODE", workspaceService.getCode(workspace, script))
									.build()),
							"NAME", keyword, "CODE", isCaseSensitive)));

			// Form Instructions
			resultGroups.add(new ResultGroup("Data Forms",
					convertMapsToResults(dataObjectService.getDataObjects()
							.stream()
							.flatMap(dataObject -> formService.getForms(dataObject).stream())
							.map(dataForm -> new MapBuilder<String, Object>()
									.put("NAME", dataForm.getName())
									.put("INSTRUCTIONS", dataForm.getInstructions())
									.build()), "NAME",
							keyword, "INSTRUCTIONS", isCaseSensitive)));

			// View Instructions
			resultGroups.add(new ResultGroup("Data Views",
					convertMapsToResults(dataObjectService.getDataObjects()
							.stream()
							.flatMap(dataObject -> viewService.getViews(dataObject).stream())
							.map(view -> new MapBuilder<String, Object>()
									.put("NAME", view.getName())
									.put("TEXT", view.getInstructions())
									.build()),
							"NAME",
							keyword, "TEXT", isCaseSensitive)));

			// Reports
			resultGroups.add(new ResultGroup("Reports", convertMapsToResults(reportService.getReports()
					.stream()
					.map(report -> new MapBuilder<String, Object>()
							.put("NAME", report.getName())
							.put("REPORT", report.getXMLDesign())
							.build()),
					"NAME",
					keyword, "REPORT", isCaseSensitive)));

			// Queries
			resultGroups.add(new ResultGroup("Queries",
					convertMapsToResults(queryService.getQueries()
							.stream()
							.map(query -> new MapBuilder<String, Object>()
									.put("NAME", query.getName())
									.put("SQL_SCRIPT", query.getSql())
									.build()),
							"NAME", keyword,
							"SQL_SCRIPT", isCaseSensitive)));

			// Stored Procedures
			resultGroups.add(new ResultGroup("Stored Procedures",
					convertMapsToResults(
							getDatabaseObject(etk,
									DatabaseObjectType.STORED_PROCEDURE).stream(),
							"NAME", keyword, "TEXT", isCaseSensitive)));

			// Database Functions
			resultGroups.add(new ResultGroup("Database Functions",
					convertMapsToResults(
							getDatabaseObject(etk, DatabaseObjectType.FUNCTION).stream(),
							"NAME", keyword, "TEXT", isCaseSensitive)));

			// Database Views
			resultGroups.add(new ResultGroup("Database Views",
					convertMapsToResults(getDatabaseViews(etk, isCaseSensitive, keyword).stream(), "NAME",
							keyword, "TEXT", isCaseSensitive)));

			filteredResultGroups = filterExcludedObjectNames(etk, resultGroups);
		} else {
			filteredResultGroups = List.of();
		}

		response.put("workspaces", gson.toJson(workspaceService.getWorkspaceNames().stream().sorted().collect(Collectors.toList())));
		response.put("workspaceName", gson.toJson(workspaceName));
		response.put("keyword", gson.toJson(keyword));
		response.put("caseSensitive", gson.toJson(isCaseSensitive));
		response.put("resultGroups", gson.toJson(filteredResultGroups));

		return response;
	}

	/**
	 * Set the breadcrumb for the response.
	 *
	 * @param response the response
	 */
	private static void setBreadcrumb(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Code Search", "page.request.do?page=du.page.codeSearch")));
	}

	private static List<ResultGroup> filterExcludedObjectNames(final ExecutionContext etk, final List<ResultGroup> resultGroups) {
		final List<String> regexStrings = AeaCoreConfiguration.getCodeSearchObjectNameExclusionRegexes(etk);

		final List<Predicate<String>> regexPredicates = regexStrings.stream().map(string -> Pattern.compile(string).asMatchPredicate()).collect(Collectors.toList());

		return resultGroups.stream()
				.map(resultGroup -> new ResultGroup(resultGroup.getTitle(),
						resultGroup.getResults().stream()
						.filter(result -> regexPredicates.stream().noneMatch(regexPredicate -> regexPredicate.test(result.getName())))
						.collect(Collectors.toList())))
				.collect(Collectors.toList());
	}

	/**
	 * This enum keeps track of the different types of database objects which we can
	 * query for.
	 *
	 * @author zmiller
	 */
	private enum DatabaseObjectType {
		STORED_PROCEDURE("PROCEDURE", new String[] { "P" }, new String[] { "p" }),
		FUNCTION("FUNCTION", new String[] { "FN", "TF" }, new String[] { "f", "a", "w" });

		private final List<String> oracleIdentifier;
		private final List<String> sqlServerIdentifiers;
		private final List<String> postgresIdentifiers;

		/**
		 * Constructor for DatabaseObjectType.
		 *
		 * @param theOracleIdentifier The identifier that Oracle uses in all_source
		 * @param theSqlServerIdentifiers The identifiers that SQL Server uses in sys.objects.type
		 * @param thePostgresIdentifiers  The identifier that Postgres uses in pg_proc
		 */
		DatabaseObjectType(final String theOracleIdentifier, final String[] theSqlServerIdentifiers,
				final String[] thePostgresIdentifiers) {
			oracleIdentifier = Arrays.asList(theOracleIdentifier);
			sqlServerIdentifiers = Arrays.asList(theSqlServerIdentifiers);
			postgresIdentifiers = Arrays.asList(thePostgresIdentifiers);
		}

		/**
		 * Gets the identifiers that Oracle uses in all_source, or SQL Server uses in
		 * sys.objects.type.
		 *
		 * @param etk entellitrak execution context
		 * @return The identifiers that Oracle uses in all_source, or SQL Server uses in sys.objects.type
		 */
		public List<String> getDatabaseIdentifiers(final ExecutionContext etk) {
			if (Utility.isSqlServer(etk)) {
				return sqlServerIdentifiers;
			} else if (Utility.isPostgreSQL(etk)) {
				return postgresIdentifiers;
			}

			return oracleIdentifier;
		}
	}

	/**
	 * This function searches for text for objects within the database such as
	 * stored procedures.
	 *
	 * @param etk entellitrak execution context
	 * @param isCaseSensitive Whether the search is case sensitive
	 * @param queryParameterMap A map containing the default parameters such as those required by {@link #generateLikeClause(ExecutionContext, String, boolean)}
	 * @param databaseObjectType The type of database object to search for
	 * @return A list of matching results with "NAME" and "TEXT" as the map keys
	 */
	private static List<Map<String, Object>> getDatabaseObject(final ExecutionContext etk,
			final DatabaseObjectType databaseObjectType) {

		final List<Map<String, Object>> returnResults;

		final List<String> databaseIdentifiers = databaseObjectType.getDatabaseIdentifiers(etk);

		if (Utility.isSqlServer(etk)) {
			returnResults = etk.createSQL(
					"SELECT objects.name NAME, modules.definition TEXT FROM sys.sql_modules modules JOIN sys.objects objects ON modules.object_id = objects.object_id WHERE objects.type IN(:databaseIdentifiers)")
					.setParameter("databaseIdentifiers", databaseIdentifiers)
					.fetchList();
		} else if (Utility.isPostgreSQL(etk)) {
			returnResults = etk.createSQL(
					"SELECT proname \"NAME\", prosrc \"TEXT\" FROM pg_proc pg join pg_namespace n on pg.pronamespace = n.oid WHERE n.nspname not in ('pg_catalog', 'information_schema') AND pg.prokind::varchar(255) IN(:databaseIdentifiers)")
					.setParameter("databaseIdentifiers", databaseIdentifiers)
					.fetchList();
		} else {
			final List<Map<String, Object>> rawQueryResults = etk.createSQL(
					"SELECT NAME, TEXT FROM all_source allSource WHERE allSource.owner = USER AND allSource.type = :databaseIdentifiers AND EXISTS( SELECT * FROM all_source matchingSource WHERE matchingSource.owner = allSource.owner AND matchingSource.name = allSource.name AND matchingSource.type = allSource.type)")
					.setParameter("databaseIdentifiers", databaseIdentifiers)
					.fetchList();

			returnResults = groupRawOracleQueryResults(rawQueryResults);
		}

		return returnResults;
	}

	/**
	 * Gets all database views which contain keyword in their definitions.
	 *
	 * @param etk entellitrak execution context
	 * @param isCaseSensitive whether the case should be case sensitive
	 * @param keyword the keyword to search for
	 * @return A list of matching results with "NAME" and "TEXT" keys for the maps
	 */
	private static List<Map<String, Object>> getDatabaseViews(final ExecutionContext etk, final boolean isCaseSensitive,
			final String keyword) {
		List<Map<String, Object>> rawQueryResults = null;
		final List<Map<String, Object>> matchingResults = new ArrayList<>();
		final String caseTypedKeyword = (isCaseSensitive ? keyword : keyword.toLowerCase()).trim().replaceAll(" +",
				" ");

		if (Utility.isSqlServer(etk)) {
			rawQueryResults = etk.createSQL(
					"SELECT objects.name NAME, modules.definition TEXT FROM sys.sql_modules modules JOIN sys.objects objects ON modules.object_id = objects.object_id WHERE objects.type IN('V') order by name")
					.fetchList();
		} else if (Utility.isPostgreSQL(etk)) {
			rawQueryResults = etk.createSQL(
					"select viewname as \"NAME\", definition as \"TEXT\" from pg_catalog.pg_views where schemaname NOT IN ('pg_catalog', 'information_schema') order by viewname")
					.fetchList();
		} else {
			rawQueryResults = etk.createSQL("select VIEW_NAME as NAME, TEXT from user_views order by name").fetchList();
		}

		String text = null;

		for (final Map<String, Object> aRawResult : rawQueryResults) {
			if (aRawResult.get("TEXT") != null) {
				text = (isCaseSensitive ? (String) aRawResult.get("TEXT")
						: ((String) aRawResult.get("TEXT")).toLowerCase()).trim().replaceAll(" +", " ");
				aRawResult.put("TEXT", text);

				if (text.contains(caseTypedKeyword)) {
					matchingResults.add(aRawResult);
				}
			}
		}

		return groupRawOracleQueryResults(matchingResults);
	}

	/**
	 * Within the all_sources table, oracle stores each line as a separate record.
	 * This function will combine all the lines into one, however it expects that
	 * the input list has already been sorted primarily by NAME and secondarily by
	 * TEXT.
	 *
	 * @param rawQueryResults The raw query results from etk.createSQL
	 * @return A list of results with "NAME" and "TEXT" keys for the maps
	 */
	private static List<Map<String, Object>> groupRawOracleQueryResults(
			final List<Map<String, Object>> rawQueryResults) {
		final List<Map<String, Object>> returnList = new ArrayList<>();

		int startIndex;
		int currentIndex = 0;

		while (currentIndex < rawQueryResults.size()) {
			startIndex = currentIndex;

			final String name = (String) rawQueryResults.get(startIndex).get("NAME");
			final StringBuilder textBuilder = new StringBuilder((String) rawQueryResults.get(startIndex).get("TEXT"));

			currentIndex = startIndex + 1;
			while (currentIndex < rawQueryResults.size()
					&& name.equals(rawQueryResults.get(currentIndex).get("NAME"))) {
				textBuilder.append(rawQueryResults.get(currentIndex).get("TEXT"));
				currentIndex = currentIndex + 1;
			}

			final Map<String, Object> completeResult = new HashMap<>();
			completeResult.put("NAME", name);
			completeResult.put("TEXT", textBuilder.toString());
			returnList.add(completeResult);
		}

		return returnList;
	}

	/**
	 * Converts maps representing matching result records into an actual
	 * {@link Result} objects.
	 *
	 * @param objects List of objects where each entry represents a matched object.
	 * @param nameKey The key in the Map which contains the name of the result
	 * @param url The object which will determine the URL of the resource given an object
	 * @param keyword The keyword which was searched for
	 * @param codeKey The key in the objects maps which holds the code.
	 * @param isCaseSensitive Whether the search is case sensitive
	 * @return A list of results which represents the passed in list of objects.
	 */
	private static List<Result> convertMapsToResults(final Stream<Map<String, Object>> objects, final String nameKey,
			final String keyword, final String codeKey, final boolean isCaseSensitive) {
		final List<Result> results = new ArrayList<>();
		objects.forEach(object -> {
			final Result result = new Result((String) object.get(nameKey), keyword,
					(String) object.get(codeKey), isCaseSensitive);

			if(!result.getMatches().isEmpty()) {
				results.add(result);
			}
		});

		return results
				.stream()
				.sorted(Comparator.comparing(Result::getName))
				.collect(Collectors.toList());
	}

	/**
	 * This class represents a group of results such as all results related to
	 * Reports. This could instead be an interface that each result type could
	 * implement, but the way this is seems easiest for now.
	 *
	 * @author zmiller
	 */
	public static class ResultGroup {

		/**
		 * The title of the group. For instance "Data Forms".
		 */
		private final String title;
		/**
		 * The matching results for this group.
		 */
		private final List<Result> results;

		/**
		 * Constructor for ResultGroup.
		 *
		 * @param groupTitle   The title of the group.
		 * @param groupResults The matching code repositories in the group.
		 */
		public ResultGroup(final String groupTitle, final List<Result> groupResults) {
			title = groupTitle;
			results = groupResults;
		}

		/**
		 * Gets the title of the group.
		 *
		 * @return The title of the group.
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Gets the results within the group.
		 *
		 * @return The matching results in this group.
		 */
		public List<Result> getResults() {
			return results;
		}
	}

	/**
	 * This class represents a particular matching block of code. Within the
	 * matching block it will have multiple matching keywords. The matches are the
	 * actual lines within the code block which match the keyword.
	 *
	 * @author zmiller
	 */
	public static class Result {
		/**
		 * The name of this block of code.
		 */
		private final String name;
		/**
		 * The matching lines.
		 */
		private final List<LineMatch> matches;

		/**
		 * Constructor for Result.
		 *
		 * @param resultName The name of the code block. The equivalent of a script object or report name.
		 * @param searchKeyword The keyword which is being searhed for
		 * @param resultText The text of the code block
		 * @param isSearchCaseSensitive Whether the search is case sensitive
		 */
		public Result(final String resultName, final String searchKeyword,
				final String resultText, final boolean isSearchCaseSensitive) {
			name = resultName;
			matches = findAllMatches(searchKeyword, resultText, isSearchCaseSensitive);
		}

		/**
		 * Gets the name of the result.
		 *
		 * @return The name of the code block.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the list of matching lines of code.
		 *
		 * @return A list of the matching lines as HTML fragments. The matching word
		 *         itself will be contained in an HTML span called with class=highlight.
		 */
		public List<LineMatch> getMatches() {
			return matches;
		}

		/**
		 * This method takes a block of text, escapes the HTML and wraps items that
		 * match the keyword in an HTML span with class=highlight.
		 *
		 * @param keyword The keyword which is being searched for.
		 * @param text The text which the keyword is located within.
		 * @param isCaseSensitive Whether the search is case sensitive.
		 * @return The list of matching lines.
		 */
		private static List<LineMatch> findAllMatches(final String keyword, final String text,
				final boolean isCaseSensitive) {
			// The pattern depends on case sensitivity
			final Pattern pattern;
			if (isCaseSensitive) {
				pattern = Pattern.compile(Pattern.quote(keyword), 0);
			} else {
				pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
			}

			final List<LineMatch> lineMatches = new ArrayList<>();
			final String[] lines = StringUtil.toNonNullString(text).split("\r\n|\r|\n");
			for (int i = 0; i < lines.length; i++) {
				final String line = lines[i];

				if (pattern.matcher(line).find()) {
					final long lineNumber = i + 1L;
					lineMatches.add(new LineMatch(lineNumber, fragments(line, pattern)));
				}
			}

			return lineMatches;
		}

		private static List<Fragment> fragments(final String string, final Pattern pattern){
			String currentString = string;
			final List<Fragment> fragments = new ArrayList<>();

			while(!currentString.isEmpty()) {
				final Matcher matcher = pattern.matcher(currentString);

				if(matcher.find()) {
					final int start = matcher.start();
					final int end = matcher.end();

					if(start > 0) {
						fragments.add(new Fragment("PLAIN", currentString.substring(0, start)));
					}

					fragments.add(new Fragment("MATCH", currentString.substring(start, end)));
					currentString = currentString.substring(end);
				} else {
					fragments.add(new Fragment("PLAIN", currentString));
					currentString = "";
				}
			}

			return fragments;
		}
	}

	static class LineMatch {
		private final long lineNumber;
		private final List<Fragment> fragments;

		public LineMatch(final long theLineNumber, final List<Fragment> theFragments) {
			lineNumber = theLineNumber;
			fragments = theFragments;
		}

		public long getLineNumber() {
			return lineNumber;
		}

		public List<Fragment> getFragments() {
			return fragments;
		}
	}

	static class Fragment {
		String type;
		String text;

		public Fragment(final String theType, final String theText) {
			type = theType;
			text = theText;
		}

		public String getType() {
			return type;
		}

		public String getText() {
			return text;
		}
	}
}

