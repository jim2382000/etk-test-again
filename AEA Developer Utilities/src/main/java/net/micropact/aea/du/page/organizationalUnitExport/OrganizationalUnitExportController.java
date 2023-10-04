package net.micropact.aea.du.page.organizationalUnitExport;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;

import net.entellitrak.aea.du.DuServiceFactory;
import net.micropact.aea.du.page.organizationalUnitImport.OrganizationalUnitImportController;

/**
 * This class serves as the controller code for a page which exports Organizational Unit data from one site to another.
 *
 * @author Zachary.Miller
 * @see OrganizationalUnitImportController
 */
@HandlerScript(type = PageController.class)
public class OrganizationalUnitExportController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        try {
            return etk.createFileResponse("organizationalUnits.json",
                    DuServiceFactory.getOrganizationalHierarchyMigrationService(etk).exportToStream());
        } catch (final Exception e) {
            throw new ApplicationException(e);
        }
    }
}
