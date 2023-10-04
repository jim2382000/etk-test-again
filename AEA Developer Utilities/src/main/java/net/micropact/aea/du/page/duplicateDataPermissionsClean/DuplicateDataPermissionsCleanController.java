package net.micropact.aea.du.page.duplicateDataPermissionsClean;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.PageUtility;
/**
 * This serves as the Controller Code for a Page which removes duplicate Page Dashboard Options for users.
 *
 * @author valeriya.myronenko
 */
@HandlerScript(type = PageController.class)
public class DuplicateDataPermissionsCleanController implements PageController {

	@Override
	public Response execute(PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		response.setContentType(ContentType.JSON);

		PageUtility.validateCsrfToken(etk);

		deleteDuplicateDataPermissions(etk);

		response.put("out", "1");

		return response;
	}
	
	private static void deleteDuplicateDataPermissions(final ExecutionContext etk) {
		etk.createSQL("delete from etk_data_permission where data_permission_id in (select permissiontodelete.data_permission_id from etk_data_permission permissionToDelete " + 
				"where exists (select * from etk_data_permission permissionToKeep  " + 
				"where permissionToDelete.data_object_type = permissionToKeep.data_object_type " + 
				"AND (permissionToDelete.data_element_type = permissionToKeep.data_element_type OR (permissionToDelete.data_element_type is NULL AND permissionToKeep.data_element_type is NULL)) " + 
				"AND (permissionToDelete.role_id = permissionToKeep.role_id OR (permissionToDelete.role_id is NULL AND permissionToKeep.role_id is NULL)) " + 
				"AND permissiontodelete.data_permission_id > permissiontokeep.data_permission_id))")
		.execute();
	}
}
