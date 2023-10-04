package net.micropact.aea.core.common.export;

import java.io.IOException;
import java.io.InputStream;

import com.entellitrak.ExecutionContext;
import com.entellitrak.file.File;
import com.entellitrak.file.FileService;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.ioUtility.IOUtility;

/**
 * Data Transfer Object for etk_file.
 *
 * @author Zachary.Miller
 */
public class FileDTO {

    private final String name;
    private final String contentType;
    private final byte[] content;

    /**
     * Simple constructor.
     *
     * @param theName the name
     * @param theContentType the content type
     * @param theContent the content
     */
    public FileDTO (final String theName, final String theContentType, final byte[] theContent){
        name = theName;
        contentType = theContentType;
        content = theContent;
    }

    /**
     * Method for generating a File DTO based on an entellitrak file id.
     *
     * @param etk entellitrak execution context
     * @param fileId the file id
     * @return the FileDTO.
     */
    public static FileDTO fromId(final ExecutionContext etk, final long fileId) {
        try {
            final FileService fileService = etk.getFileService();

            final File file = fileService.get(fileId);

            try (InputStream contentStream = file.getContent()){
                final byte[] content = IOUtility.toByteArray(contentStream);

                return new FileDTO(file.getName(), file.getContentType(), content);
            }
        } catch (final IOException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the content.
     *
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }
}
