package net.micropact.aea.du.page.smtpTester;

import java.util.Optional;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.mail.Mail;
import com.entellitrak.mail.MailException;
import com.entellitrak.mail.MailService;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * <p>
 *  This controller is for testing whether emails are sending correctly from the system.
 * </p>
 * <p>
 *  If entellitrak is not connected to the SMTP server then there will be an exception, or no email will be received.
 * </p>
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class SmtpTesterController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk)
            throws ApplicationException {
        final Parameters parameters = etk.getParameters();

        final TextResponse response = etk.createTextResponse();

        final String formAction = Optional.ofNullable(parameters.getSingle("formAction")).orElse("initial");
        final String recipient = parameters.getSingle("recipient");

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("SMTP Tester",
                                "page.request.do?page=du.page.smtpTester")));

        String error = null;
        String success = null;

        if("send".equals(formAction)){
            PageUtility.validateCsrfToken(etk);

            if (Utility.isBlank(recipient)){
                error = "Recipient Address should not be blank";
            }else {
                final MailService mailService = etk.getMailService();
                final Mail mail = mailService.createMail();
                mail.setSubject("SMTP Tester");
                mail.setMessage("This is an SMTP test");
                mail.addTo(recipient);
                try {
                    mailService.send(mail);

                    success = "Test Email Sent";
                } catch (final MailException e) {
                    throw new ApplicationException(e);
                }
            }
        }

        final Gson gson = new Gson();
        response.put("success", gson.toJson(success));
        response.put("recipient", gson.toJson(recipient));
        response.put("error", gson.toJson(error));
        response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

        return response;
    }
}
