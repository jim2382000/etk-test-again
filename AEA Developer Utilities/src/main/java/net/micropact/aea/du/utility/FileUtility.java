package net.micropact.aea.du.utility;

import java.util.List;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataType;
import com.entellitrak.dm.ServiceFactory;
import com.entellitrak.file.File;
import com.entellitrak.file.FileService;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.utility.EtkDataUtils;

/**
 * Generic entellitrak file utilities.
 *
 * @author zmiller
 */
public final class FileUtility {

    /**
     * There is no reason to create a new instance.
     */
    private FileUtility() {
    }

    /**
     * Gets a list of all File Data elements within the system.
     *
     * @param etk
     *            entellitrak execution context
     * @return The list of data elements
     */
    public static List<DataElement> getFileDataElements(final ExecutionContext etk) {
        return EtkDataUtils.getAllDataElements(etk)
            .filter(dataElement -> dataElement.getDataType() == DataType.FILE)
            .collect(Collectors.toList());
    }

    /**
     * Deletes a file.
     *
     * @param etk
     *            entellitrak execution context
     * @param fileId
     *            the file id
     */
    public static void deleteFile(final ExecutionContext etk, final long fileId) {
        try {
        	final FileService fileService = etk.getFileService();
        	final ServiceFactory documentManagementServiceFactory = etk.getDocumentManagementServiceFactory();

        	final File file = fileService.get(fileId);

            if (file.isStoredInDM()) {
				documentManagementServiceFactory.getFileService().delete(fileId);
            } else {
                fileService.delete(file);
            }
        } catch (final Exception e) {
            throw new GeneralRuntimeException(String.format("Problem encountered deleting file with id %s",
            		fileId),
            		e);
        }
    }
}
