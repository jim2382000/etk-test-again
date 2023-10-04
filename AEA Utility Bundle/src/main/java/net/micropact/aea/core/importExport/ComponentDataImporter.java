package net.micropact.aea.core.importExport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.page.FileStream;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.core.utility.StringEscapeUtils;

/**
 * This class is intended to capture much of the shared logic among the Component Data Import pages.
 *
 * @author zmiller
 */
public final class ComponentDataImporter {

    /**
     * Utility classes do not need constructors.
     */
    private ComponentDataImporter(){}

    /**
     * This method does the job of extracting parameters from a {@link PageExecutionContext} and calling an object
     * which is capable of doing the actual import with the file data. The page controller should have parameters
     * <code>update</code> and a file named <code>importFile</code>.
     * The text response it returns contains keys <code>errors</code> and <code>importCompleted</code>.
     *
     * @param etk entellitrak execution context
     * @param importPageName the name of the import page
     * @param importPageUrl the relative URL of the import page
     * @param importLogic An object which is capable of actually ingesting the file contents and updating the database
     *          with the correct values.
     * @return The response that the page should return.
     * @throws ApplicationException If there was an underlying {@link Exception}
     */
    public static TextResponse performExecute(final PageExecutionContext etk,
            final String importPageName,
            final String importPageUrl,
            final IImportLogic importLogic) throws ApplicationException{
        final Parameters parameters = etk.getParameters();

        final boolean update = "1".equals(parameters.getSingle("update"));
        Boolean importCompleted = false;

        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                new SimpleBreadcrumb(
                        StringEscapeUtils.escapeHtml(importPageName),
                        importPageUrl));

        final List<String> errors = new ArrayList<>();

        if(update){
            PageUtility.validateCsrfToken(etk);

            final FileStream fileParameter = parameters.getFile("importFile");

            if(fileParameter == null){
                errors.add("You must upload a file");
            }else{
                try (InputStream fileStream = fileParameter.getInputStream()) {
                    importLogic.performImport(fileStream);
                    importCompleted = true;
                } catch (final IOException e) {
                    throw new GeneralRuntimeException(e);
                }
            }
        }

        final Gson gson = new Gson();
        response.put("errors", gson.toJson(errors));
        response.put("importCompleted", gson.toJson(importCompleted));
		response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

        return response;
    }
}
