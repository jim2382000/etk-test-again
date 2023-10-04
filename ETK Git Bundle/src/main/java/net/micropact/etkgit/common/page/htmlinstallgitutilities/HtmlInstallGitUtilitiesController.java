package net.micropact.etkgit.common.page.htmlinstallgitutilities;

import org.apache.velocity.tools.generic.EscapeTool;

import com.entellitrak.ApplicationException;
import com.entellitrak.InputValidationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.dynamic.SciSourceControlSettings;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;

public class HtmlInstallGitUtilitiesController implements PageController {
	private PageExecutionContext etk;

	@Override
	public Response execute(final PageExecutionContext executionContext) throws ApplicationException {
		this.etk = executionContext;

        final var response = etk.createTextResponse();

        response.setTitle("ETK Git Bundle - Install");

        final var escapeTool = new EscapeTool();

        try {
        	//Insert RDO defaults.
            insertUpdateProperty("repositoryUrl", null);

            //Turn on the system event listener
            etk.createSQL("update ETK_SYSTEM_EVENT_LISTENER set ACTIVE = 1 where BUSINESS_KEY = 'systemEventListener.sciCommitToSourceControl'")
            	.execute();

            response.put("out", escapeTool.html("ETK Git Bundle installed properly"));
        } catch (final InputValidationException e) {
            response.put("out", escapeTool.html("Error installing the SonarQube Utility component: " + e.getMessage()));
            etk.getLogger().error("Error installing the SonarQube Utility component", e);
        }

        return response;
	}

    private void insertUpdateProperty(final String code, final String defaultValue) throws InputValidationException {
        final var dynamicObjectService = etk.getDynamicObjectService();
        final var dataElementService = etk.getDataElementService();

        final var codeElement = dataElementService.getDataElementByBusinessKey("object.sciSourceControlSettings.element.code");

        final var criteria = etk.createCriteria();
        criteria.add(criteria.equals(codeElement, code));

        final boolean recordAlreadyExists = dynamicObjectService.exists(SciSourceControlSettings.class, criteria);

        if(!recordAlreadyExists) {
            final var newSetting = dynamicObjectService.createNew(SciSourceControlSettings.class);
            newSetting.setCode(code);
            newSetting.setValue(defaultValue);

            try {
                dynamicObjectService.createSaveOperation(newSetting)
                	.setExecuteEvents(false)
                	.save();
            } catch (final InputValidationException e) {
                throw new InputValidationException(e);
            }
        }
    }
}