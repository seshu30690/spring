package me.fullstacker.gateway.service;

import me.fullstacker.util.admin.domain.User;

public interface MailService {

	void sendActivationEmail(User user);

	void sendCreationEmail(User user);

	void sendPasswordResetMail(User user);

	void sendEmailFromTemplate(User user, String templateName, String titleKey);

	void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);

}
