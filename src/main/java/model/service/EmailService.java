package model.service;

import jakarta.mail.*;
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

    // Colori Brand
    private static final String COLOR_PRIMARY = "#FB8C00"; // Arancione Foundly
    private static final String COLOR_BG = "#F4F6F8";      // Sfondo Grigio Chiaro
    private static final String COLOR_CARD = "#FFFFFF";    // Sfondo Card
    private static final String COLOR_TEXT = "#333333";    // Testo Scuro
    private static final String COLOR_MUTED = "#757575";   // Testo Secondario

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        this.mittente = "foundly.app@gmail.com";
        final String username = "foundly.app@gmail.com";
        final String password = System.getenv("SMTP_PASSKEY");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    // =================================================================================
    //  METODI PUBBLICI DI INVIO
    // =================================================================================

    // 1. RECUPERO PASSWORD
    public void inviaCodiceRecuperoPassword(String destinatario, String codice) {
        String oggetto = "🔐 Codice di Recupero Password - Foundly";
        String contenuto =
                "<p>Hai richiesto di reimpostare la tua password. Utilizza il codice seguente per completare la procedura:</p>" +

                        // Box Codice
                        "<div style='text-align: center; margin: 30px 0;'>" +
                        "  <div style='display: inline-block; background-color: #FFF3E0; color: " + COLOR_PRIMARY + "; font-size: 32px; font-weight: 800; letter-spacing: 6px; padding: 15px 40px; border-radius: 8px; border: 1px dashed " + COLOR_PRIMARY + ";'>" +
                        codice +
                        "  </div>" +
                        "</div>" +

                        "<p style='color: " + COLOR_MUTED + "; font-size: 14px;'>Se non hai richiesto tu il recupero della password, ti invitiamo a ignorare questa email.</p>";

        inviaEmail(destinatario, oggetto, "Recupero Password", contenuto);
    }

    // 2. APPROVAZIONE DROP-POINT (Verso il Drop-Point)
    public void inviaAccettazioneDropPoint(String destinatario, String nomeAttivita) {
        String oggetto = "🎉 Drop-Point Approvato! Benvenuto in Foundly";
        String contenuto =
                "<p>Ciao <strong>" + nomeAttivita + "</strong>,</p>" +
                        "<p>Siamo felici di informarti che la tua richiesta di affiliazione è stata <strong>APPROVATA</strong>.</p>" +
                        "<p>La tua attività è ora ufficialmente un Drop-Point di Foundly. Gli utenti potranno visualizzare il tuo negozio sulla mappa e selezionarlo per depositi e ritiri sicuri.</p>" +

                        "<div style='margin-top: 25px; padding-top: 20px; border-top: 1px solid #EEE;'>" +
                        "  <p style='margin: 0; font-weight: 600;'>Prossimi passi:</p>" +
                        "  <p style='margin: 5px 0 0 0;'>Accedi alla tua area riservata sulla piattaforma per iniziare a gestire le operazioni.</p>" +
                        "</div>";

        inviaEmail(destinatario, oggetto, "Richiesta Approvata", contenuto);
    }

    // 3. NUOVO RECLAMO RICEVUTO (Verso il Finder)
    public void inviaNotificaNuovoReclamo(String destinatario, String titoloOggetto, String usernameRichiedente) {
        String oggetto = "📩 Nuovo Reclamo per: " + titoloOggetto;
        String contenuto =
                "<p>Hai ricevuto una nuova richiesta di restituzione.</p>" +

                        // Info Box
                        "<div style='background-color: #FAFAFA; border-left: 4px solid " + COLOR_PRIMARY + "; padding: 15px; margin: 20px 0; border-radius: 4px;'>" +
                        "  <div style='margin-bottom: 8px; font-size: 13px; color: " + COLOR_MUTED + "; text-transform: uppercase;'>Oggetto</div>" +
                        "  <div style='font-size: 16px; font-weight: bold; color: " + COLOR_TEXT + "; margin-bottom: 12px;'>" + titoloOggetto + "</div>" +
                        "  <div style='margin-bottom: 8px; font-size: 13px; color: " + COLOR_MUTED + "; text-transform: uppercase;'>Richiedente</div>" +
                        "  <div style='font-size: 16px; font-weight: 600; color: " + COLOR_PRIMARY + ";'>@" + usernameRichiedente + "</div>" +
                        "</div>" +

                        "<p>L'utente ha risposto alle tue domande di sicurezza. Accedi alla sezione <strong>'Le mie Segnalazioni'</strong> per verificare le risposte e decidere se accettare la richiesta.</p>";

        inviaEmail(destinatario, oggetto, "Nuova Richiesta", contenuto);
    }

    // 4. RECLAMO ACCETTATO (Verso l'Owner/Richiedente)
    public void inviaReclamoAccettatoOwner(String destinatario, String titoloOggetto, String nomeFinder, String eventualeCodice) {
        String oggetto = "✅ Il tuo reclamo è stato accettato!";

        String bloccoCodice = "";
        if (eventualeCodice != null && !eventualeCodice.isEmpty()) {
            bloccoCodice =
                    "<div style='text-align: center; margin: 25px 0;'>" +
                            "  <div style='font-size: 12px; text-transform: uppercase; color: " + COLOR_MUTED + "; margin-bottom: 5px;'>Codice di Ritiro</div>" +
                            "  <div style='display: inline-block; background-color: #E8F5E9; color: #2E7D32; font-size: 28px; font-weight: 700; letter-spacing: 4px; padding: 10px 30px; border-radius: 8px; border: 1px dashed #2E7D32;'>" +
                            eventualeCodice +
                            "  </div>" +
                            "  <p style='font-size: 13px; color: " + COLOR_MUTED + "; margin-top: 10px;'>Mostra questo codice al Drop-Point per ritirare l'oggetto.</p>" +
                            "</div>";
        } else {
            bloccoCodice =
                    "<div style='background-color: #E3F2FD; color: #1565C0; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center; font-weight: 500;'>" +
                            "  Contatta il Finder per accordarvi sulla consegna diretta." +
                            "</div>";
        }

        String contenuto =
                "<p>Buone notizie! L'utente <strong>" + nomeFinder + "</strong> ha verificato le tue risposte e confermato che l'oggetto è tuo.</p>" +

                        // Info Oggetto
                        "<div style='margin: 15px 0; font-size: 16px;'>" +
                        "  Oggetto: <strong>" + titoloOggetto + "</strong>" +
                        "</div>" +

                        bloccoCodice +

                        "<p>Accedi alla tua area personale per visualizzare i dettagli completi e i contatti.</p>";

        inviaEmail(destinatario, oggetto, "Congratulazioni!", contenuto);
    }

    // 5. CONFERMA AZIONE ACCETTAZIONE (Verso il Finder)
    public void inviaConfermaAccettazioneFinder(String destinatario, String titoloOggetto, String nomeRichiedente) {
        String oggetto = "👍 Hai accettato il reclamo per: " + titoloOggetto;
        String contenuto =
                "<p>Hai confermato che l'oggetto <strong>" + titoloOggetto + "</strong> appartiene a <strong>" + nomeRichiedente + "</strong>.</p>" +
                        "<p>Grazie per la tua onestà! Abbiamo inviato una notifica all'utente con le istruzioni per procedere al ritiro.</p>" +
                        "<p style='color: " + COLOR_MUTED + "; font-size: 14px; margin-top: 20px;'>Puoi monitorare lo stato della consegna direttamente dalla tua dashboard.</p>";

        inviaEmail(destinatario, oggetto, "Azione Confermata", contenuto);
    }

    // 6. NUOVA RICHIESTA DROP-POINT (Verso Admin)
    public void inviaNotificaAdminNuovoDropPoint(String destinatario, String nomeAttivita, String citta) {
        String oggetto = "🔔 Nuova richiesta Drop-Point: " + nomeAttivita;
        String contenuto =
                "<p>Un nuovo Drop-Point ha richiesto l'iscrizione alla piattaforma Foundly.</p>" +

                        "<div style='background-color: #FAFAFA; border: 1px solid #EEEEEE; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                        "  <ul style='list-style: none; padding: 0; margin: 0;'>" +
                        "    <li style='margin-bottom: 8px;'><strong>Attività:</strong> " + nomeAttivita + "</li>" +
                        "    <li style='margin-bottom: 0;'><strong>Città:</strong> " + citta + "</li>" +
                        "  </ul>" +
                        "</div>" +

                        "<p>Accedi al pannello di amministrazione per esaminare i dettagli completi e approvare o rifiutare la richiesta.</p>";

        inviaEmail(destinatario, oggetto, "Notifica Admin", contenuto);
    }

    // =================================================================================
    //  LOGICA INTERNA E TEMPLATE
    // =================================================================================

    private void inviaEmail(String destinatario, String oggetto, String preHeader, String contenutoHtml) {
        // ESEGUE IL THREAD QUI - NON c'è bisogno di farlo fuori
        new Thread(() -> {
            try {
                MimeMessage message = new MimeMessage(session);
                try {
                    message.setFrom(new InternetAddress(mittente, nomeMittente, StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    message.setFrom(new InternetAddress(mittente));
                }
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                message.setSubject(oggetto, StandardCharsets.UTF_8.name());

                String templateCompleto = costruisciTemplateHtml(preHeader, contenutoHtml);

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(templateCompleto, "text/html; charset=UTF-8");

                MimeMultipart multipart = new MimeMultipart("alternative");
                multipart.addBodyPart(htmlPart);

                message.setContent(multipart);
                Transport.send(message);
                System.out.println("✅ Email inviata con successo a: " + destinatario);

            } catch (MessagingException e) {
                e.printStackTrace();
                System.err.println("❌ Errore invio email a " + destinatario + ": " + e.getMessage());
            }
        }).start();
    }

    private String costruisciTemplateHtml(String titoloPrincipale, String body) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; background-color: " + COLOR_BG + "; font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif; color: " + COLOR_TEXT + ";'>" +
                "  <table width='100%' cellpadding='0' cellspacing='0' style='padding: 40px 0;'>" +
                "    <tr>" +
                "      <td align='center'>" +
                "        " +
                "        <table width='600' cellpadding='0' cellspacing='0' style='background-color: " + COLOR_CARD + "; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #EAEAEA;'>" +
                "          " +
                "          " +
                "          <tr>" +
                "            <td style='background-color: " + COLOR_CARD + "; padding: 30px 40px; border-bottom: 4px solid " + COLOR_PRIMARY + "; text-align: center;'>" +
                "              <h1 style='color: " + COLOR_PRIMARY + "; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;'>Foundly</h1>" +
                "            </td>" +
                "          </tr>" +
                "          " +
                "          " +
                "          <tr>" +
                "            <td style='padding: 40px;'>" +
                "              <h2 style='color: " + COLOR_TEXT + "; margin-top: 0; margin-bottom: 20px; font-size: 22px; font-weight: 700;'>" + titoloPrincipale + "</h2>" +
                "              <div style='font-size: 16px; line-height: 1.6; color: #555555;'>" +
                body +
                "              </div>" +
                "            </td>" +
                "          </tr>" +
                "          " +
                "          " +
                "          <tr>" +
                "            <td style='background-color: #FAFAFA; padding: 25px 40px; text-align: center; border-top: 1px solid #EEEEEE;'>" +
                "              <p style='margin: 0; font-size: 12px; color: #999999; line-height: 1.5;'>" +
                "                Questa è una notifica automatica inviata da Foundly.<br>" +
                "                Ti preghiamo di non rispondere direttamente a questo messaggio." +
                "              </p>" +
                "              <p style='margin: 10px 0 0 0; font-size: 12px; color: #BBBBBB;'>" +
                "                &copy; 2025 Foundly Team" +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }
}