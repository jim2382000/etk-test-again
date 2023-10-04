package net.entellitrak.aea.du;

import com.entellitrak.ExecutionContext;
import com.entellitrak.menu.MenuExecutionContext;

import net.entellitrak.aea.du.service.IMenuItemService;
import net.entellitrak.aea.du.service.IOrganizationalHierarchyMigrationService;
import net.entellitrak.aea.du.service.ISystemPreferenceMigrationService;
import net.micropact.aea.du.service.MenuItemService;
import net.micropact.aea.du.service.OrganizationalHierarchyMigrationService;
import net.micropact.aea.du.service.SystemPreferenceMigrationService;

/**
 * This class is part of the public API to grant access to any Services related to the Developer Utility in much the
 * same way that the ExecutionContext in entellitrak is used to access most other core services.
 *
 * @author zachary.miller
 */
public final class DuServiceFactory {

    /**
     * Utility classes do not need public constructors.
     */
    private DuServiceFactory(){}

    /**
     * Get access to a service for migrating system preferences from one site to another.
     *
     * @param etk entellitrak execution context
     * @return An {@link ISystemPreferenceMigrationService}
     */
    public static ISystemPreferenceMigrationService getSystemPreferenceMigrationService(final ExecutionContext etk){
        return new SystemPreferenceMigrationService(etk);
    }

    /**
     * Get access to a service for migrating organizational hierarchy from one site to another.
     *
     * @param etk entellitrak execution context
     * @return An {@link IOrganizationalHierarchyMigrationService}
     */
    public static IOrganizationalHierarchyMigrationService getOrganizationalHierarchyMigrationService(final ExecutionContext etk){
        return new OrganizationalHierarchyMigrationService(etk);
    }

    /**
     * Get access to a service for dealing with menu items.
     *
     * @param etk entellitrak execution context
     * @return  the An {@link IMenuItemService}
     */
    public static IMenuItemService getMenuItemService(final MenuExecutionContext etk) {
        return new MenuItemService(etk);
    }
}
