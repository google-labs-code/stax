/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.planck.planck.domain.monitoringapp.notifications;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.planck.planck.config.MonitoringScheduleConfig;
import com.planck.planck.entitities.UserDeletionStatus;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("monitoring")
// Due to authentication requirements, this service is not currently used. It remains for future
// use, in case there's a way to send mails with service role based authentication.
public class GmailNotificationService implements UserDeletionNotificationService {

  private static final Logger logger = LoggerFactory.getLogger(GmailNotificationService.class);

  private final MonitoringScheduleConfig config;
  private Gmail gmailService;

  @Value("${google.credentials_path:}")
  private String credentialsPath;

  public GmailNotificationService(MonitoringScheduleConfig config) {
    this.config = config;
    initializeGmailService();
  }

  private void initializeGmailService() {
    try {
      GoogleCredentials credentials;

      if (credentialsPath != null && !credentialsPath.isEmpty()) {
        // Load credentials from file path
        credentials =
            GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(GmailScopes.GMAIL_SEND);
      } else {
        // Try to load from resources folder as fallback
        try {
          credentials =
              GoogleCredentials.fromStream(getClass().getResourceAsStream("/credentials.json"))
                  .createScoped(GmailScopes.GMAIL_SEND);
        } catch (Exception e) {
          logger.warn(
              "Could not load credentials from resources, trying Application Default Credentials",
              e);
          // Use Application Default Credentials as final fallback
          credentials =
              GoogleCredentials.getApplicationDefault().createScoped(GmailScopes.GMAIL_SEND);
        }
      }

      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

      this.gmailService =
          new Gmail.Builder(
                  new NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
              .setApplicationName("Planck Monitoring")
              .build();

    } catch (Exception e) {
      logger.error("Failed to initialize Gmail service", e);
      throw new RuntimeException("Failed to initialize Gmail service", e);
    }
  }

  public void sendStaleUserDeletionNotification(List<UserDeletionStatus> staleUserDeletions) {
    if (!config.getEmail().isEnabled() || staleUserDeletions.isEmpty()) {
      return;
    }

    try {
      String htmlContent = createHtmlEmailContent(staleUserDeletions);
      Message message = createMessage(htmlContent);

      Message sentMessage = gmailService.users().messages().send("me", message).execute();
      logger.info(
          "Stale user deletion notification sent successfully. Message ID: {}",
          sentMessage.getId());

    } catch (GoogleJsonResponseException e) {
      GoogleJsonError error = e.getDetails();
      if (error.getCode() == 403) {
        logger.error("Unable to send message: {}", e.getDetails());
      } else {
        logger.error("Error sending Gmail message", e);
      }
    } catch (Exception e) {
      logger.error("Error sending stale user deletion notification", e);
    }
  }

  private String createHtmlEmailContent(List<UserDeletionStatus> staleUserDeletions) {
    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>");
    html.append("<html><head>");
    html.append("<style>");
    html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
    html.append("h2 { color: #d32f2f; }");
    html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
    html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
    html.append("th { background-color: #f5f5f5; font-weight: bold; }");
    html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
    html.append(".count { font-size: 18px; font-weight: bold; color: #d32f2f; }");
    html.append("</style>");
    html.append("</head><body>");

    html.append("<h2>🚨 Stale User Deletion Alert</h2>");
    html.append("<p>Found <span class=\"count\">")
        .append(staleUserDeletions.size())
        .append("</span> stale user deletion(s) that require attention.</p>");

    html.append("<table>");
    html.append("<thead>");
    html.append("<tr>");
    html.append("<th>User ID</th>");
    html.append("<th>Request ID</th>");
    html.append("<th>Last Failure Step</th>");
    html.append("<th>Last Failure Message</th>");
    html.append("</tr>");
    html.append("</thead>");
    html.append("<tbody>");

    for (UserDeletionStatus status : staleUserDeletions) {
      html.append("<tr>");
      html.append("<td>").append(escapeHtml(status.getUserId())).append("</td>");
      html.append("<td>").append(escapeHtml(status.getRequestId())).append("</td>");
      html.append("<td>")
          .append(
              escapeHtml(status.getLastFailureStep() != null ? status.getLastFailureStep() : "N/A"))
          .append("</td>");
      html.append("<td>")
          .append(
              escapeHtml(
                  status.getLastFailureMessage() != null ? status.getLastFailureMessage() : "N/A"))
          .append("</td>");
      html.append("</tr>");
    }

    html.append("</tbody>");
    html.append("</table>");

    html.append(
        "<p><strong>Action Required:</strong> Please investigate and retry these failed user deletions.</p>");
    html.append(
        "<p><em>This is an automated notification from the Planck monitoring system.</em></p>");

    html.append("</body></html>");

    return html.toString();
  }

  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private Message createMessage(String htmlContent) throws MessagingException, IOException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);
    email.setFrom(new InternetAddress(config.getEmail().getFromAddress()));

    // Add recipients
    for (String toAddress : config.getEmail().getToAddresses()) {
      email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toAddress));
    }

    email.setSubject(config.getEmail().getSubject());
    email.setContent(htmlContent, "text/html; charset=utf-8");

    // Encode and wrap the MIME message into a Gmail message
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    email.writeTo(buffer);
    byte[] rawMessageBytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

    Message message = new Message();
    message.setRaw(encodedEmail);

    return message;
  }
}
