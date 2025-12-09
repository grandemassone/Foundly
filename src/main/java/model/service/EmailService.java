package model.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {
    private final Session session;
    private final String mittente;
    private final String nomeMittente = "Foundly";

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        this.mittente = "foundly.app@gmail.com";
        final String username = "foundly.app@gmail.com";
        final String password = System.getenv("SMTP_PASSKEY"); // app password

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void inviaCodiceRecuperoPassword(String destinatario, String codice)
            throws MessagingException {

        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(mittente, nomeMittente, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            message.setFrom(new InternetAddress(mittente));
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject("Codice per il recupero password Foundly", StandardCharsets.UTF_8.name());

        // versione testo semplice
        String testoPlain =
                "Ciao,\n\n" +
                        "hai richiesto il recupero della password per il tuo account Foundly.\n\n" +
                        "Questo è il tuo codice di verifica:\n\n" +
                        codice + "\n\n" +
                        "Inseriscilo nella pagina di recupero password e scegli una nuova password.\n\n" +
                        "Se non hai richiesto tu questo recupero, puoi ignorare questa email.\n\n" +
                        "Il team Foundly";

        // versione HTML più curata
        String testoHtml =
                "<!DOCTYPE html>" +
                        "<html lang=\"it\">" +
                        "<head>" +
                        "  <meta charset=\"UTF-8\">" +
                        "  <title>Codice recupero password Foundly</title>" +
                        "</head>" +
                        "<body style=\"margin:0;padding:0;background-color:#f5f5f5;font-family:Roboto,Arial,sans-serif;\">" +
                        "  <table align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding:24px 0;\">" +
                        "    <tr>" +
                        "      <td>" +
                        "        <table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" " +
                        "               style=\"max-width:520px;width:100%;background-color:#ffffff;border-radius:12px;" +
                        "                      box-shadow:0 4px 16px rgba(0,0,0,0.06);overflow:hidden;\">" +
                        "          <tr>" +
                        "            <td style=\"padding:18px 24px;background:linear-gradient(135deg,#FB8C00,#F57C00);color:#ffffff;\">" +
                        "              <div style=\"font-size:20px;font-weight:700;\">Foundly</div>" +
                        "              <div style=\"font-size:12px;opacity:0.9;\">Recupero password</div>" +
                        "            </td>" +
                        "          </tr>" +
                        "          <tr>" +
                        "            <td style=\"padding:24px 24px 16px 24px;color:#202124;font-size:14px;line-height:1.6;\">" +
                        "              <p style=\"margin:0 0 12px 0;\">Ciao,</p>" +
                        "              <p style=\"margin:0 0 12px 0;\">" +
                        "                hai richiesto il recupero della password per il tuo account <strong>Foundly</strong>." +
                        "              </p>" +
                        "              <p style=\"margin:0 0 8px 0;\">Questo è il tuo codice di verifica:</p>" +
                        "            </td>" +
                        "          </tr>" +
                        "          <tr>" +
                        "            <td style=\"padding:0 24px 16px 24px;\">" +
                        "              <div style=\"text-align:center;margin:8px 0 16px 0;\">" +
                        "                <span style=\"" +
                        "                       display:inline-block;" +
                        "                       padding:12px 24px;" +
                        "                       border-radius:999px;" +
                        "                       background-color:#FFF3E0;" +
                        "                       color:#E65100;" +
                        "                       font-size:22px;" +
                        "                       font-weight:700;" +
                        "                       letter-spacing:4px;" +
                        "                       font-family:monospace;\">" +
                        codice +
                        "                </span>" +
                        "              </div>" +
                        "              <p style=\"margin:0 0 8px 0;font-size:13px;color:#5f6368;text-align:center;\">" +
                        "                Inserisci questo codice nella pagina di <strong>Recupero password</strong> e scegli una nuova password." +
                        "              </p>" +
                        "            </td>" +
                        "          </tr>" +
                        "          <tr>" +
                        "            <td style=\"padding:0 24px 20px 24px;color:#5f6368;font-size:12px;line-height:1.6;\">" +
                        "              <p style=\"margin:0 0 10px 0;\">" +
                        "                Se non hai richiesto tu questo recupero, puoi ignorare questa email in totale sicurezza." +
                        "              </p>" +
                        "              <p style=\"margin:0;\">" +
                        "                A presto,<br>" +
                        "                <span style=\"color:#E65100;font-weight:600;\">Il team Foundly</span>" +
                        "              </p>" +
                        "            </td>" +
                        "          </tr>" +
                        "          <tr>" +
                        "            <td style=\"padding:12px 24px 18px 24px;border-top:1px solid #eee;color:#9e9e9e;font-size:10px;text-align:center;\">" +
                        "              Non rispondere a questa email: il messaggio è stato generato automaticamente." +
                        "            </td>" +
                        "          </tr>" +
                        "        </table>" +
                        "      </td>" +
                        "    </tr>" +
                        "  </table>" +
                        "</body>" +
                        "</html>";

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(testoPlain, StandardCharsets.UTF_8.name());

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(testoHtml, "text/html; charset=UTF-8");

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        message.setContent(multipart);

        message.setHeader("X-Mailer", "Foundly Mailer");
        message.setHeader("Content-Transfer-Encoding", "8bit");

        Transport.send(message);
    }
}
