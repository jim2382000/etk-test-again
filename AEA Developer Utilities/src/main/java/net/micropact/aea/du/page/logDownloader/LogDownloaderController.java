package net.micropact.aea.du.page.logDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.FileResponse;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;

import net.micropact.aea.du.utility.LogUtility;

/**
 * Controller code for a page which downloads a file from the container's log directory.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class LogDownloaderController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        try {
            final Parameters parameters = etk.getParameters();

            final String fileName = parameters.getSingle("fileName");

            LogUtility.ensureFileNameIsValidLogFile(etk, fileName);

            final File file = new File(LogUtility.getLogPath(etk), fileName);

            final FileResponse fileResponse = etk.createFileResponse(fileName, new FileInputStream(file));

            fileResponse.setContentType(ContentType.OCTET_STREAM);
            fileResponse.setHeader("Content-disposition",
                    String.format("attachment;filename=\"%s.txt\"", fileName));

            return fileResponse;
        } catch (final FileNotFoundException e) {
            throw new ApplicationException(e);
        }
    }
}
