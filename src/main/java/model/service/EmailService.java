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

    // Palette Colori
    private static final String COLOR_PRIMARY = "#FB8C00"; // Arancione
    private static final String COLOR_ACCENT  = "#FFF3E0"; // Arancione Chiaro
    private static final String COLOR_TEXT    = "#333333"; // Grigio Scuro
    private static final String COLOR_BG      = "#F9F9F9"; // Sfondo Pagina

    // COSTANTI AGGIUNTE (che mancavano)
    private static final String COLOR_CARD    = "#FFFFFF"; // Bianco Card
    private static final String COLOR_MUTED   = "#757575"; // Testo Secondario (Grigio)

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
    //  METODI DI INVIO (SOLO TESTO/HTML INFORMATIVO - NO LINK)
    // =================================================================================

    // 1. RECUPERO PASSWORD
    public void inviaCodiceRecuperoPassword(String destinatario, String codice) {
        String oggetto = "🔐 Codice di Recupero - Foundly";
        String body =
                "<p>Hai richiesto il ripristino della password. Utilizza il codice sottostante per completare la procedura nell'applicazione:</p>" +
                        getBoxCodice(codice) +
                        "<p style='font-size:13px; color:" + COLOR_MUTED + ";'>Se non hai richiesto tu il ripristino, ignora questo messaggio.</p>";

        inviaEmail(destinatario, oggetto, "Recupero Password", body);
    }

    // 2. APPROVAZIONE DROP-POINT (Verso il Drop-Point)
    public void inviaAccettazioneDropPoint(String destinatario, String nomeAttivita) {
        String oggetto = "🎉 Drop-Point Approvato!";
        String body =
                "<p>Gentile <strong>" + nomeAttivita + "</strong>,</p>" +
                        "<p>Siamo lieti di informarti che la tua richiesta di affiliazione è stata <strong>accettata</strong>.</p>" +
                        "<div style='background-color:#E8F5E9; border-left: 5px solid #4CAF50; padding: 15px; margin: 20px 0; color:#2E7D32;'>" +
                        "  <strong>Stato Attuale:</strong> ATTIVO<br>" +
                        "  La tua attività è ora visibile sulla mappa come Drop-Point ufficiale." +
                        "</div>" +
                        "<p>Puoi ora accedere alla piattaforma per gestire depositi e ritiri.</p>";

        inviaEmail(destinatario, oggetto, "Benvenuto in Foundly", body);
    }

    // 3. NUOVO RECLAMO RICEVUTO (Verso il Finder)
    public void inviaNotificaNuovoReclamo(String destinatario, String titoloOggetto, String usernameRichiedente) {
        String oggetto = "📩 Nuovo Reclamo Ricevuto";
        String body =
                "<p>Un utente ha inviato una richiesta di restituzione per un oggetto che hai segnalato.</p>" +
                        getInfoBox("Oggetto", titoloOggetto) +
                        getInfoBox("Richiedente", "@" + usernameRichiedente) +
                        "<p>Accedi all'applicazione per verificare le risposte di sicurezza e decidere se accettare la richiesta.</p>";

        inviaEmail(destinatario, oggetto, "Nuova Richiesta", body);
    }

    // 4. RECLAMO ACCETTATO (Verso l'Owner - "Hai vinto!")
    public void inviaReclamoAccettatoOwner(String destinatario, String titoloOggetto, String nomeFinder, String eventualeCodice) {
        String oggetto = "✅ Reclamo Accettato!";

        String bloccoConsegna;
        if (eventualeCodice != null && !eventualeCodice.isBlank()) {
            bloccoConsegna =
                    "<p>Recati al Drop-Point indicato e mostra questo codice per il ritiro:</p>" +
                            getBoxCodice(eventualeCodice);
        } else {
            bloccoConsegna =
                    "<div style='background-color:#E3F2FD; padding:15px; border-radius:8px; border:1px solid #BBDEFB; color:#0D47A1; text-align:center; margin:20px 0;'>" +
                            "  <strong>Consegna Diretta</strong><br>" +
                            "  Accedi all'app per visualizzare i contatti del Finder e concordare l'incontro." +
                            "</div>";
        }

        String body =
                "<p>Ottime notizie! <strong>" + nomeFinder + "</strong> ha confermato che l'oggetto è tuo.</p>" +
                        getInfoBox("Oggetto Recuperato", titoloOggetto) +
                        bloccoConsegna;

        inviaEmail(destinatario, oggetto, "Congratulazioni!", body);
    }

    // 5. CONFERMA AZIONE ACCETTAZIONE (Verso il Finder - "Hai accettato")
    public void inviaConfermaAccettazioneFinder(String destinatario, String titoloOggetto, String nomeRichiedente, String codice, String nomeDropPoint) {
        String oggetto = "👍 Hai accettato il reclamo";

        String istruzioni;
        if (codice != null && !codice.isBlank()) {
            // Caso Drop-Point
            istruzioni =
                    "<p>Hai scelto di utilizzare un <strong>Drop-Point</strong> (" + nomeDropPoint + ").</p>" +
                            "<p>Per completare la restituzione:</p>" +
                            "<ol style='color:#555; line-height:1.6;'>" +
                            "  <li>Recati al Drop-Point selezionato portando l'oggetto.</li>" +
                            "  <li>Consegna l'oggetto all'operatore del negozio.</li>" +
                            "  <li>Comunica il seguente codice di sicurezza:</li>" +
                            "</ol>" +
                            getBoxCodice(codice) +
                            "<p style='font-size:13px; color:" + COLOR_MUTED + ";'>L'operatore registrerà il deposito e noi avviseremo il proprietario per il ritiro.</p>";
        } else {
            // Caso Diretta
            istruzioni =
                    "<p>Hai scelto la <strong>Consegna Diretta</strong>.</p>" +
                            "<p>L'utente è stato notificato. Accedi all'app per visualizzare i suoi contatti, concordare un luogo sicuro e confermare lo scambio.</p>";
        }

        String body =
                "<p>Hai confermato correttamente che l'oggetto appartiene a <strong>" + nomeRichiedente + "</strong>.</p>" +
                        getInfoBox("Oggetto", titoloOggetto) +
                        "<hr style='border:0; border-top:1px solid #EEE; margin:20px 0;'>" +
                        istruzioni;

        inviaEmail(destinatario, oggetto, "Azione Registrata", body);
    }

    // 6. NUOVA RICHIESTA DROP-POINT (Verso Admin)
    public void inviaNotificaAdminNuovoDropPoint(String destinatario, String nomeAttivita, String citta) {
        String oggetto = "🔔 Nuova richiesta Drop-Point";
        String body =
                "<p>È stata ricevuta una nuova richiesta di affiliazione.</p>" +
                        "<div style='background-color:#FFF; border:1px solid #EEE; padding:15px; border-radius:8px; margin:15px 0;'>" +
                        "  <ul style='list-style:none; padding:0; margin:0; color:#555;'>" +
                        "    <li style='margin-bottom:5px;'><strong>Attività:</strong> " + nomeAttivita + "</li>" +
                        "    <li><strong>Città:</strong> " + citta + "</li>" +
                        "  </ul>" +
                        "</div>" +
                        "<p>Accedi al pannello di amministrazione per valutare la richiesta.</p>";

        inviaEmail(destinatario, oggetto, "Notifica Admin", body);
    }

    // =================================================================================
    //  HELPER GRAFICI & TEMPLATE
    // =================================================================================

    private String getBoxCodice(String codice) {
        return "<div style='text-align:center; margin:25px 0;'>" +
                "  <span style='font-size:28px; font-weight:800; letter-spacing:4px; color:" + COLOR_PRIMARY + "; background:" + COLOR_ACCENT + "; padding:15px 30px; border-radius:8px; border:2px dashed " + COLOR_PRIMARY + "; display:inline-block;'>" +
                codice +
                "  </span>" +
                "</div>";
    }

    private String getInfoBox(String label, String value) {
        return "<div style='margin-bottom:15px;'>" +
                "  <span style='font-size:12px; color:#888; text-transform:uppercase; display:block;'>" + label + "</span>" +
                "  <span style='font-size:16px; font-weight:600; color:" + COLOR_TEXT + "; display:block;'>" + value + "</span>" +
                "</div>";
    }

    private void inviaEmail(String destinatario, String oggetto, String preHeader, String bodyHtml) {
        if (destinatario == null || destinatario.isBlank()) {
            System.err.println("⚠️ Impossibile inviare email: Destinatario nullo o vuoto.");
            return;
        }

        // Esecuzione in thread separato
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

                String htmlFinale = getTemplateHtml(preHeader, bodyHtml);

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlFinale, "text/html; charset=UTF-8");

                MimeMultipart multipart = new MimeMultipart("alternative");
                multipart.addBodyPart(htmlPart);

                message.setContent(multipart);
                Transport.send(message);

                System.out.println("✅ Email inviata a: " + destinatario + " | Oggetto: " + oggetto);

            } catch (MessagingException e) {
                System.err.println("❌ Errore invio email a " + destinatario + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private String getTemplateHtml(String titolo, String contenuto) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='margin:0; padding:0; background-color:" + COLOR_BG + "; font-family:Helvetica, Arial, sans-serif; color:" + COLOR_TEXT + ";'>" +
                "  <table width='100%' cellpadding='0' cellspacing='0' style='padding:40px 0;'>" +
                "    <tr><td align='center'>" +
                "      <table width='600' cellpadding='0' cellspacing='0' style='background-color:" + COLOR_CARD + "; border-radius:12px; overflow:hidden; box-shadow:0 4px 10px rgba(0,0,0,0.05);'>" +
                "        " +
                "        <tr><td style='background-color:" + COLOR_PRIMARY + "; padding:25px; text-align:center;'>" +
                "          <h1 style='color:#FFFFFF; margin:0; font-size:24px; letter-spacing:1px;'>Foundly</h1>" +
                "        </td></tr>" +
                "        " +
                "        <tr><td style='padding:40px;'>" +
                "          <h2 style='color:" + COLOR_PRIMARY + "; margin-top:0; font-size:20px; border-bottom:1px solid #EEE; padding-bottom:10px;'>" + titolo + "</h2>" +
                "          <div style='font-size:15px; line-height:1.6; color:#444;'>" +
                contenuto +
                "          </div>" +
                "        </td></tr>" +
                "        " +
                "        <tr><td style='background-color:#FAFAFA; padding:20px; text-align:center; border-top:1px solid #EEE; font-size:12px; color:#999;'>" +
                "          &copy; 2025 Foundly. Messaggio automatico, non rispondere." +
                "        </td></tr>" +
                "      </table>" +
                "    </td></tr>" +
                "  </table>" +
                "</body></html>";
    }
}