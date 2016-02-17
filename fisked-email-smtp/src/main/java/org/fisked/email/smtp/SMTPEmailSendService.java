package org.fisked.email.smtp;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.fisked.email.service.Email;
import org.fisked.email.service.EmailProfile;
import org.fisked.email.service.IEmailSendService;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides(specifications = IEmailSendService.class)
public class SMTPEmailSendService implements IEmailSendService {
	@ServiceProperty(name = "protocol", value = "smtp")
	private String _protocol;

	@Override
	public void sendEmail(Email email, EmailProfile profile, IEmailSendCallback callback) {
		SMTPServerConfiguration server = (SMTPServerConfiguration) profile.getSendServer();

		if (server == null) {
			callback.error(email, profile, new NullPointerException("No SMTP server found for profile."));
			return;
		}

		Thread.currentThread().setContextClassLoader(Message.class.getClassLoader());

		if (server.getHost() == null) {
			callback.error(email, profile, new NullPointerException("No host provided."));
			return;
		}

		Properties properties = System.getProperties();

		properties.setProperty("mail.smtp.host", server.getHost());
		properties.setProperty("mail.smtp.port", server.getPort());

		properties.setProperty("mail.smtp.auth", server.hasAuth() ? "true" : "false");
		properties.setProperty("mail.smtp.starttls.enable", server.hasTls() ? "true" : "false");

		Session session;

		if (server.hasAuth()) {
			if (server.getUsername() == null || server.getPassword() == null) {
				callback.error(email, profile, new NullPointerException("No credentials provided."));
			}
			session = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(server.getUsername(), server.getPassword());
				}
			});
		} else {
			session = Session.getInstance(properties);
		}

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(profile.getName() + " <" + email.getFrom() + ">"));

			for (String to : email.getReceivers()) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}

			for (String cc : email.getCcList()) {
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}

			message.setSubject(email.getSubject());

			if (email.getAttachments().size() > 0) {
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(email.getBody());

				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				for (File file : email.getAttachments()) {
					BodyPart messageFilePart = new MimeBodyPart();
					String filename = file.getName();
					DataSource source = new FileDataSource(file);
					messageFilePart.setDataHandler(new DataHandler(source));
					messageFilePart.setFileName(filename);
					multipart.addBodyPart(messageFilePart);
				}
				message.setContent(multipart);
			} else {
				message.setText(email.getBody());
			}

			Transport.send(message);
			callback.success(email, profile);
		} catch (MessagingException e) {
			callback.error(email, profile, e);
		}
	}

}
