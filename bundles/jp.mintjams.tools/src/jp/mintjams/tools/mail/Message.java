/*
 * Copyright (c) 2021 MintJams Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jp.mintjams.tools.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import jp.mintjams.tools.internal.mail.Charsets;
import jp.mintjams.tools.internal.mail.SessionBuilder;

public class Message {

	private final MimeMessage fMessage;
	private final List<Part> fParts = new ArrayList<>();

	private Message(MimeMessage message) throws MessagingException {
		fMessage = message;
		prepare(fMessage);
	}

	public static Message create() throws MessagingException {
		Session session;
		try {
			session = SessionBuilder.create().build();
		} catch (Throwable ex) {
			throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
		}
		MimeMessage message = new MimeMessage(session);
		return new Message(message);
	}

	public static Message from(javax.mail.Message message) throws MessagingException {
		Objects.requireNonNull(message);
		return new Message((MimeMessage) message);
	}

	public static Message from(InputStream in) throws MessagingException {
		Objects.requireNonNull(in);
		try (in) {
			Session session;
			try {
				session = SessionBuilder.create().build();
			} catch (Throwable ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
			MimeMessage message = new MimeMessage(session, in);
			return new Message(message);
		} catch (IOException ex) {
			throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
		}
	}

	private String defaultString(String value) {
		return defaultString(value, "");
	}

	private String defaultString(String value, final String def) {
		return (value == null) ? def : value;
	}

	private boolean isEmpty(String value) {
		return (value == null || value.trim().isEmpty());
	}

	private boolean containsEncoded(String value) {
		if (value == null) {
			return false;
		}

		int p = value.indexOf("=?");
		return (p != -1 && value.indexOf("?=", p + 2) != -1);
	}

	private String decodeText(String value) throws MessagingException {
		if (value == null) {
			return null;
		}

		if (containsEncoded(value)) {
			try {
				value = MimeUtility.decodeText(value);
			} catch (UnsupportedEncodingException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}

		return value;
	}

	private void prepare(Part part) throws MessagingException {
		String contentType = defaultString(part.getContentType());

		if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			fParts.add(part);
			return;
		}

		if ((part.isMimeType("text/*") || contentType.startsWith("text/"))) {
			fParts.add(part);
			return;
		}

		if (part.isMimeType("multipart/*") || contentType.startsWith("multipart/")) {
			Multipart multiPart;
			try {
				multiPart = (Multipart) part.getContent();
			} catch (IOException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
			for (int i = 0; i < multiPart.getCount(); i++) {
				prepare(multiPart.getBodyPart(i));
			}
			return;
		}

		if (part.isMimeType("message/rfc822")) {
			try {
				prepare((Part) part.getContent());
			} catch (IOException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
			return;
		}
	}

	private Address[] fixPersonal(Address[] addresses) throws MessagingException {
		String charset = Charsets.from(fMessage);
		for (Address a : addresses) {
			if (a instanceof InternetAddress) {
				InternetAddress ia = (InternetAddress) a;
				try {
					String personal = ia.getPersonal();
					if (personal != null) {
						if (containsEncoded(personal)) {
							personal = decodeText(personal);
						}
						ia.setPersonal(new String(personal.getBytes(charset), charset), charset);
					}
				} catch (UnsupportedEncodingException ex) {
					throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
				}
			}
		}
		return addresses;
	}

	private byte[] toByteArray(Part part) throws MessagingException, IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		try (InputStream in = new BufferedInputStream(part.getInputStream())) {
			try (OutputStream out = new BufferedOutputStream(bo)) {
				byte[] buf = new byte[8192];
				for (;;) {
					int size = in.read(buf);
					if (size == -1) {
						break;
					}
					out.write(buf, 0, size);
				}
				out.flush();
			}
		}
		return bo.toByteArray();
	}

	public String[] getHeader(String name) throws MessagingException {
		String[] values = fMessage.getHeader(name);
		for (int i = 0; i < values.length; i++) {
			try {
				values[i] = MimeUtility.decodeText(values[i]);
			} catch (UnsupportedEncodingException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}
		return values;
	}

	public int getMessageNumber() {
		return fMessage.getMessageNumber();
	}

	public String getMessageId() throws MessagingException {
		return fMessage.getHeader("Message-Id", "");
	}

	public String getContentType() throws MessagingException {
		return fMessage.getContentType();
	}

	public java.util.Date getSentDate() throws MessagingException {
		return fMessage.getSentDate();
	}

	public Message setSentDate(java.util.Date sent) throws MessagingException {
		fMessage.setSentDate(sent);
		return this;
	}

	public java.util.Date getReceivedDate() throws MessagingException {
		{
			java.util.Date value = fMessage.getReceivedDate();
			if (value != null) {
				return value;
			}
		}

		{
			String[] values = fMessage.getHeader("received");
			if (values != null) {
				String value = values[0];
				int p = value.lastIndexOf(";");
				if (p != -1) {
					value = value.substring(p).trim();
				}
				try {
					java.util.Date receivedDate = new MailDateFormat().parse(value);
					if (receivedDate != null) {
						return receivedDate;
					}
				} catch (ParseException ignore) {}
			}
		}

		{
			String[] values = fMessage.getHeader("date");
			if (values != null) {
				String value = values[0];
				try {
					java.util.Date dateDate = new MailDateFormat().parse(value);
					if (dateDate != null) {
						return dateDate;
					}
				} catch (ParseException ignore) {}
			}
		}

		return null;
	}

	public Address[] getFrom() throws MessagingException {
		Address[] a = fMessage.getFrom();
		return (a != null) ? fixPersonal(a) : new Address[0];
	}

	public Message setFrom(Address address) throws MessagingException {
		fMessage.setFrom(address);
		return this;
	}

	public Address[] getTo() throws MessagingException {
		Address[] a = fMessage.getRecipients(RecipientType.TO);
		return (a != null) ? fixPersonal(a) : new Address[0];
	}

	public Message setTo(Address[] addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.TO, addresses);
		return this;
	}

	public Address[] getCc() throws MessagingException {
		Address[] a = fMessage.getRecipients(RecipientType.CC);
		return (a != null) ? fixPersonal(a) : new Address[0];
	}

	public Message setCc(Address[] addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.CC, addresses);
		return this;
	}

	public Address[] getBcc() throws MessagingException {
		Address[] a = fMessage.getRecipients(RecipientType.BCC);
		return (a != null) ? fixPersonal(a) : new Address[0];
	}

	public Message setBcc(Address[] addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.BCC, addresses);
		return this;
	}

	public Address[] getReplyTo() throws MessagingException {
		Address[] a = fMessage.getReplyTo();
		return (a != null) ? fixPersonal(a) : new Address[0];
	}

	public Message setReplyTo(Address[] addresses) throws MessagingException {
		fMessage.setReplyTo(addresses);
		return this;
	}

	public String getSubject() throws MessagingException {
		return decodeText(fMessage.getSubject());
	}

	public Message setSubject(String subject) throws MessagingException {
		fMessage.setSubject(subject, Charsets.UTF8);
		return this;
	}

	public String[] getInReplyTo() throws MessagingException {
		return fMessage.getHeader("In-Reply-To");
	}

	public Message setInReplyTo(String[] inReplyTo) throws MessagingException {
		fMessage.removeHeader("In-Reply-To");
		for (String v : inReplyTo) {
			fMessage.addHeader("In-Reply-To", v);
		}
		return this;
	}

	public String[] getReferences() throws MessagingException {
		return fMessage.getHeader("References");
	}

	public Message setReferences(String[] references) throws MessagingException {
		fMessage.removeHeader("References");
		for (String v : references) {
			fMessage.addHeader("References", v);
		}
		return this;
	}

	public String[] getIdentifiers() throws MessagingException {
		List<String> l = new ArrayList<>();
		for (String[] sa : new String[][] { new String[] { getMessageId() }, getInReplyTo(), getReferences() }) {
			if (sa != null) {
				for (String id : sa) {
					if (!isEmpty(id) && !l.contains(id)) {
						l.add(id);
					}
				}
			}
		}
		return l.toArray(new String[l.size()]);
	}

	public String getPriority() throws MessagingException {
		try {
			String[] priority = fMessage.getHeader("Priority");
			if (priority != null && priority.length > 0) {
				return priority[0].toLowerCase();
			} else {
				priority = fMessage.getHeader("X-Priority");
				if (priority != null && priority.length > 0) {
					if (Integer.parseInt(priority[0]) < 3) {
						return "urgent";
					} else if (Integer.parseInt(priority[0]) > 3) {
						return "non-urgent";
					} else {
						return "normal";
					}
				}
			}
			return null;
		} catch (MessagingException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
		}
	}

	public Message setPriority(String priority) throws MessagingException, IOException {
		fMessage.setHeader("Priority", priority);
		return this;
	}

	public String getContent(String mimeType) throws MessagingException, IOException {
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				continue;
			}

			ContentType type;
			try {
				type = new ContentType(p.getContentType());
			} catch (Throwable ignore) {
				continue;
			}

			if (type.match(mimeType)) {
				return new String(toByteArray(p), Charsets.from(p));
			}
		}
		return null;
	}

	public Attachment[] getAttachments() throws MessagingException, IOException {
		List<Attachment> l = new ArrayList<>();
		for (Part p : fParts) {
			if (!Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				continue;
			}

			l.add(new AttachmentImpl(p));
		}
		return l.toArray(new Attachment[l.size()]);
	}

	public boolean hasContent(String mimeType) throws MessagingException, IOException {
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				continue;
			}

			ContentType type;
			try {
				type = new ContentType(p.getContentType());
			} catch (Throwable ignore) {
				continue;
			}

			if (type.match(mimeType)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAttachments() throws MessagingException, IOException {
		int n = 0;
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				n++;
			}
		}
		return (n > 0);
	}

	public boolean isAnswered() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.ANSWERED);
	}

	public boolean isDeleted() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.DELETED);
	}

	public boolean isDraft() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.DRAFT);
	}

	public boolean isFlagged() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.FLAGGED);
	}

	public boolean isRecent() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.RECENT);
	}

	public boolean isSeen() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.SEEN);
	}

	public boolean isUser() throws MessagingException {
		return fMessage.getFlags().contains(Flags.Flag.USER);
	}

	public Message setAnswered(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.ANSWERED, value);
		return this;
	}

	public Message setDeleted(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.DELETED, value);
		return this;
	}

	public Message setDraft(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.DRAFT, value);
		return this;
	}

	public Message setFlagged(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.FLAGGED, value);
		return this;
	}

	public Message setRecent(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.RECENT, value);
		return this;
	}

	public Message setSeen(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.SEEN, value);
		return this;
	}

	public Message setUser(boolean value) throws MessagingException {
		fMessage.setFlag(Flags.Flag.USER, value);
		return this;
	}

	public void saveChanges() throws MessagingException {
		fMessage.saveChanges();
	}

	public MimeMessage toMimeMessage() {
		return fMessage;
	}

	public InputStream toInputStream() throws MessagingException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fMessage.writeTo(out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	public interface Attachment {
		ContentType getContentType() throws MessagingException;

		String getMimeType() throws MessagingException;

		String getCharset() throws MessagingException;

		String getFilename() throws MessagingException;

		void writeTo(OutputStream out) throws MessagingException, IOException;
	}

	private class AttachmentImpl implements Attachment {
		private final Part fPart;

		private AttachmentImpl(Part part) {
			fPart = part;
		}

		@Override
		public ContentType getContentType() throws MessagingException {
			if (fPart.getContentType() == null) {
				return null;
			}
			return new ContentType(fPart.getContentType());
		}

		@Override
		public String getMimeType() throws MessagingException {
			return getContentType().getBaseType();
		}

		@Override
		public String getCharset() throws MessagingException {
			return Charsets.from(fPart);
		}

		@Override
		public String getFilename() throws MessagingException {
			String filename = fPart.getFileName();
			if (containsEncoded(filename)) {
				filename = decodeText(filename);
			}
			return filename;
		}

		@Override
		public void writeTo(OutputStream out) throws MessagingException, IOException {
			try (InputStream in = new BufferedInputStream(fPart.getInputStream())) {
				byte[] buf = new byte[8192];
				for (;;) {
					int size = in.read(buf);
					if (size == -1) {
						break;
					}
					out.write(buf, 0, size);
				}
				out.flush();
			}
		}
	}

}
