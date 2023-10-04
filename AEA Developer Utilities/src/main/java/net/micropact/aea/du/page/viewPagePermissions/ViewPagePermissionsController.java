package net.micropact.aea.du.page.viewPagePermissions;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.user.Role;
import com.entellitrak.user.User;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This controller is for viewing the page permissions in ways which are currently inconvenient within entellitrak.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class ViewPagePermissionsController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
    	final RoleService roleService = etk.getRoleService();
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("View Page Permissions",
                    "page.request.do?page=du.page.viewPagePermissions")));

		response.put("roles", new Gson().toJson(roleService.getRoles()
            .stream()
            .sorted(Comparator.comparing(Role::getName).thenComparing(Role::getId))
            .map(role -> Map.of(
                "ROLE_ID", role.getId(),
                "NAME", role.getName()))
            .collect(Collectors.toList())));

        response.put("users", new Gson().toJson(etk.getUserService().getUsers()
            .stream()
            .sorted(Comparator.comparing(User::getAccountName).thenComparing(User::getId))
            .map(user -> Map.of(
                "USER_ID", user.getId(),
                "USERNAME", user.getAccountName()))
            .collect(Collectors.toList())));

        response.put("groups",
            etk.createSQL("SELECT g.GROUP_ID \"GROUP_ID\", g.GROUP_NAME \"GROUP_NAME\" FROM etk_group g ORDER BY 2, 1")
                .fetchJSON());

        response.put("pages", etk.createSQL("SELECT PAGE_ID \"PAGE_ID\", NAME \"NAME\" FROM etk_page ORDER BY 2, 1")
            .fetchJSON());

        response.put("pagePermissions", etk.createSQL(
            "SELECT pp.PAGE_ID \"PAGE_ID\", sop.IS_EDIT \"IS_EDIT\", sop.IS_EXECUTE \"IS_EXECUTE\", sop.IS_DISPLAY \"IS_DISPLAY\", sop.ROLE_ID \"ROLE_ID\", sop.SUBJECT_ID \"SUBJECT_ID\", sop.IS_ALL_USERS \"IS_ALL_USERS\" FROM etk_page_permission pp JOIN etk_shared_object_permission sop ON sop.shared_object_permission_id = pp.page_permission_id")
            .fetchJSON());

        return response;
    }
}
