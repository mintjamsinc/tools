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
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import jp.mintjams.tools.internal.mail.Charsets;
import jp.mintjams.tools.internal.mail.InputStreamDataSource;
import jp.mintjams.tools.internal.mail.SessionBuilder;
import jp.mintjams.tools.io.Closer;

public class Message implements Closeable {

	private final Closer fCloser = Closer.newCloser();
	private MimeMessage fMessage;
	private MimeHeaders fMimeHeaders;
	private final Map<String, String> fContents = new HashMap<>();
	private final List<Part> fParts = new ArrayList<>();
	private Integer fMessageNumber;

	private Message(MimeMessage message) throws MessagingException, IOException {
		fMessage = message;
		fMimeHeaders = null;
		prepare(fMessage);
	}

	public static Message create() throws MessagingException, IOException {
		MimeMessage message = createEmptyMessage(newSession());
		message.setFlag(Flags.Flag.DRAFT, true);
		return new Message(message);
	}

	public static Message from(javax.mail.Message message) throws MessagingException, IOException {
		Objects.requireNonNull(message);
		return new Message((MimeMessage) message);
	}

	public static Message from(InputStream in) throws MessagingException {
		Objects.requireNonNull(in);
		try (in) {
			MimeMessage message = new MimeMessage(newSession(), in);
			return new Message(message);
		} catch (IOException ex) {
			throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
		}
	}

	public static MimeMessage createEmptyMessage(Session session) throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setSubject("");
		ContentType type = new ContentType("text/plain");
		type.setParameter("charset", Charsets.from(message));
		setText(message, "\n", type);
		return message;
	}

	@Override
	public void close() throws IOException {
		fCloser.close();
	}

	private static void setText(MimePart part, String value, ContentType type) throws MessagingException {
		part.setText(value, type.getParameter("charset"), type.getSubType());
		part.setHeader("Content-Type", type.toString());
		part.setHeader("Content-Transfer-Encoding", "base64");
	}

	private static Session newSession() throws MessagingException {
		try {
			return SessionBuilder.create().build();
		} catch (Throwable ex) {
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

	private String decodeText(String value) throws MessagingException {
		if (value == null) {
			return null;
		}

		for (;;) {
			int i = value.indexOf("=?");
			if (i == -1 || value.indexOf("?=", i + 2) == -1) {
				break;
			}

			try {
				value = value.substring(0, i) + MimeUtility.decodeText(value.substring(i));
			} catch (UnsupportedEncodingException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}

		return value;
	}

	private String getContentTransferEncoding(Part part) throws MessagingException {
		String[] encoding = part.getHeader("Content-Transfer-Encoding");
		if (encoding != null) {
			return encoding[0];
		}
		return null;
	}

	private void prepare(Part part) throws MessagingException, IOException {
		String contentType = defaultString(part.getContentType());

		if (part instanceof MimeMessage) {
			fParts.clear();
			fContents.clear();
		}

		if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			fParts.add(part);
			return;
		}

		if ((part.isMimeType("text/*") || contentType.startsWith("text/"))) {
			ContentType type = new ContentType(contentType);
			if (contentType.toLowerCase().indexOf("charset") == -1) {
				type.setParameter("charset", Charsets.from(part));
				part.setHeader("Content-Type", type.toString());
			}
			if ("8bit".equals(getContentTransferEncoding(part))) {
				String text = decodeText((String) part.getContent());
				fContents.put(type.getBaseType(), text);
			} else {
				fContents.put(type.getBaseType(), (String) part.getContent());
			}

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

	private MimeHeaders getMimeHeaders() throws MessagingException {
		if (fMimeHeaders == null) {
			fMimeHeaders = new MimeHeaders();
		}
		return fMimeHeaders;
	}

	private InternetAddress[] fixPersonal(InternetAddress[] addresses) throws MessagingException {
		String charset = getCharset();
		for (InternetAddress ia : addresses) {
			try {
				String personal = ia.getPersonal();
				if (personal != null) {
					personal = decodeText(personal);
					ia.setPersonal(new String(personal.getBytes(charset), charset), charset);
				}
			} catch (UnsupportedEncodingException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}
		return addresses;
	}

	private void replaceContent(List<Part> contentParts, List<Part> attachmentParts) throws MessagingException, IOException {
		Objects.requireNonNull(contentParts);
		Objects.requireNonNull(attachmentParts);

		String encoding = null;
		try {
			encoding = getMimeHeaders().getDecoded("Content-Transfer-Encoding")[0];
		} catch (Throwable ignore) {}
		String messageCharset = getCharset();

		if (contentParts.size() > 1 || !attachmentParts.isEmpty()) {
			Multipart mp = new MimeMultipart();
			for (Part p : contentParts) {
				if (p instanceof MimeMessage) {
					MimeBodyPart part = new MimeBodyPart();
					ContentType type = new ContentType(p.getContentType());
					type.setParameter("charset", messageCharset);
					p.setHeader("Content-Type", type.toString());
					setText(part, (String) p.getContent(), type);
					mp.addBodyPart(part);
					continue;
				}

				mp.addBodyPart(BodyPart.class.cast(p));
			}
			for (Part p : attachmentParts) {
				mp.addBodyPart(BodyPart.class.cast(p));
			}
			fMessage.setContent(mp);
			fMessage.setHeader("Content-Type", mp.getContentType());
			getMimeHeaders().setValue("Content-Type", mp.getContentType());
		} else if (!contentParts.isEmpty()) {
			Part p = contentParts.get(0);
			ContentType type = new ContentType(p.getContentType());
			type.setParameter("charset", messageCharset);
			p.setHeader("Content-Type", type.toString());
			setText(fMessage, (String) p.getContent(), type);
			getMimeHeaders().setValue("Content-Type", type.toString());
		}

		if (encoding != null) {
			fMessage.setHeader("Content-Transfer-Encoding", encoding);
		}

		prepare(fMessage);
	}

	private java.util.Date[] listDates() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("Received");
		List<java.util.Date> l = new ArrayList<>();
		if (values != null) {
			for (String value : values) {
				int i = value.lastIndexOf(";");
				if (i == -1) {
					continue;
				}
				value = value.substring(i + 1).trim();
				try {
					l.add(new MailDateFormat().parse(value));
				} catch (ParseException ex) {
					throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
				}
			}
			Collections.sort(l);
		} else {
			try {
				l.add(new MailDateFormat().parse(getMimeHeaders().getDecoded("Date")[0]));
			} catch (ParseException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}
		return l.toArray(new java.util.Date[l.size()]);
	}

	private InternetAddress[] toInternetAddress(Address...address) throws MessagingException {
		List<InternetAddress> l = new ArrayList<>();
		for (Address e : address) {
			l.add(InternetAddress.class.cast(e));
		}
		return l.toArray(new InternetAddress[l.size()]);
	}

	public String[] getHeader(String name) throws MessagingException {
		return getMimeHeaders().getDecoded(name);
	}

	public Message setHeader(String name, String value) throws MessagingException {
		fMessage.setHeader(name, value);
		getMimeHeaders().setValue(name, value);
		return this;
	}

	public Message setHeader(String name, String[] values) throws MessagingException {
		for (int i = 0; i < values.length; i++) {
			if (i == 0) {
				fMessage.setHeader(name, values[i]);
			} else {
				fMessage.addHeader(name, values[i]);
			}
		}
		getMimeHeaders().setValue(name, values);
		return this;
	}

	public Message removeHeader(String...names) throws MessagingException {
		for (String name : names) {
			fMessage.removeHeader(name);
			getMimeHeaders().removeValue(name);
		}
		return this;
	}

	public int getMessageNumber() {
		if (fMessageNumber != null) {
			return fMessageNumber;
		}
		return fMessage.getMessageNumber();
	}

	public Message setMessageNumber(int messageNumber) {
		fMessageNumber = messageNumber;
		return this;
	}

	public String getMessageID() throws MessagingException {
		return getMimeHeaders().getDecoded("Message-ID")[0];
	}

	public Message setMessageID(String messageID) throws MessagingException {
		getMimeHeaders().setValue("Message-ID", messageID);
		return this;
	}

	public String getContentType() throws MessagingException {
		return getMimeHeaders().getDecoded("Content-Type")[0];
	}

	public java.util.Date getSentDate() throws MessagingException {
		java.util.Date[] l = listDates();
		if (l.length == 0) {
			return null;
		}
		return l[0];
	}

	public Message setSentDate(java.util.Date value) throws MessagingException {
		fMessage.setSentDate(value);
		getMimeHeaders().setValue("Date", new MailDateFormat().format(value));
		return this;
	}

	public java.util.Date getReceivedDate() throws MessagingException {
		java.util.Date[] l = listDates();
		if (l.length == 0) {
			return null;
		}
		return l[l.length - 1];
	}

	public Address[] getFrom() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("From");
		if (values == null) {
			return new InternetAddress[0];
		}
		InternetAddress[] a = InternetAddress.parse(values[0]);
		return (a != null) ? fixPersonal(a) : new InternetAddress[0];
	}

	public Message setFrom(Address address) throws MessagingException {
		fMessage.setFrom(address);
		getMimeHeaders().setValue("From", InternetAddress.toUnicodeString(toInternetAddress(address)));
		return this;
	}

	public Address[] getTo() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("To");
		if (values == null) {
			return new InternetAddress[0];
		}
		InternetAddress[] a = InternetAddress.parse(values[0]);
		return (a != null) ? fixPersonal(a) : new InternetAddress[0];
	}

	public Message setTo(Address...addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.TO, addresses);
		getMimeHeaders().setValue("To", InternetAddress.toUnicodeString(toInternetAddress(addresses)));
		return this;
	}

	public Address[] getCc() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("Cc");
		if (values == null) {
			return new InternetAddress[0];
		}
		InternetAddress[] a = InternetAddress.parse(values[0]);
		return (a != null) ? fixPersonal(a) : new InternetAddress[0];
	}

	public Message setCc(Address...addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.CC, addresses);
		getMimeHeaders().setValue("Cc", InternetAddress.toUnicodeString(toInternetAddress(addresses)));
		return this;
	}

	public Address[] getBcc() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("Bcc");
		if (values == null) {
			return new InternetAddress[0];
		}
		InternetAddress[] a = InternetAddress.parse(values[0]);
		return (a != null) ? fixPersonal(a) : new InternetAddress[0];
	}

	public Message setBcc(Address...addresses) throws MessagingException {
		fMessage.setRecipients(RecipientType.BCC, addresses);
		getMimeHeaders().setValue("Bcc", InternetAddress.toUnicodeString(toInternetAddress(addresses)));
		return this;
	}

	public Address[] getReplyTo() throws MessagingException {
		String[] values = getMimeHeaders().getDecoded("Reply-To");
		if (values == null) {
			return new InternetAddress[0];
		}
		InternetAddress[] a = InternetAddress.parse(values[0]);
		return (a != null) ? fixPersonal(a) : new InternetAddress[0];
	}

	public Message setReplyTo(Address...addresses) throws MessagingException {
		fMessage.setReplyTo(addresses);
		getMimeHeaders().setValue("Reply-To", InternetAddress.toUnicodeString(toInternetAddress(addresses)));
		return this;
	}

	public String getSubject() throws MessagingException {
		return getMimeHeaders().getDecoded("Subject")[0];
	}

	public Message setSubject(String value) throws MessagingException {
		fMessage.setSubject(value, getCharset());
		getMimeHeaders().setValue("Subject", value);
		return this;
	}

	public String[] getInReplyTo() throws MessagingException {
		return getMimeHeaders().getDecoded("In-Reply-To");
	}

	public Message setInReplyTo(String[] values) throws MessagingException {
		fMessage.removeHeader("In-Reply-To");
		for (String value : values) {
			fMessage.addHeader("In-Reply-To", value);
		}
		getMimeHeaders().setValue("In-Reply-To", String.join(" ", values));
		return this;
	}

	public String[] getReferences() throws MessagingException {
		return getMimeHeaders().getDecoded("References");
	}

	public Message setReferences(String[] values) throws MessagingException {
		fMessage.removeHeader("References");
		for (String value : values) {
			fMessage.addHeader("References", value);
		}
		getMimeHeaders().setValue("References", String.join(" ", values));
		return this;
	}

	public String[] getIdentifiers() throws MessagingException {
		List<String> l = new ArrayList<>();
		for (String[] sa : new String[][] { new String[] { getMessageID() }, getInReplyTo(), getReferences() }) {
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

	public Message setPriority(String priority) throws MessagingException {
		fMessage.setHeader("Priority", priority);
		return this;
	}

	public String getContent(String mimeType) throws MessagingException, IOException {
		return fContents.get(mimeType);
	}

	public Message setContent(String value, String mimeType) throws MessagingException, IOException {
		List<Part> contentParts = new ArrayList<>();
		List<Part> attachmentParts = new ArrayList<>();
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				attachmentParts.add(p);
				continue;
			}

			ContentType type;
			try {
				type = new ContentType(p.getContentType());
			} catch (Throwable ignore) {
				continue;
			}

			if (type.match(mimeType)) {
				continue;
			}

			contentParts.add(p);
		}

		if (value != null && !value.isEmpty()) {
			MimeBodyPart p = new MimeBodyPart();
			ContentType type = new ContentType(mimeType);
			type.setParameter("charset", getCharset());
			setText(p, value, type);
			contentParts.add(p);
			contentParts.sort(new ContentPartComparator());
		}

		replaceContent(contentParts, attachmentParts);

		return this;
	}

	public Message addAttachment(InputStream stream, String filename, String mimeType) throws MessagingException, IOException {
		try (stream) {
			List<Part> contentParts = new ArrayList<>();
			List<Part> attachmentParts = new ArrayList<>();
			for (Part p : fParts) {
				if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
					if (!filename.equalsIgnoreCase(new AttachmentImpl(p).getFilename())) {
						attachmentParts.add(p);
					}
					continue;
				}

				contentParts.add(p);
			}

			InputStreamDataSource ds = new InputStreamDataSource(stream, filename, mimeType);
			fCloser.add(ds);

			MimeBodyPart p = new MimeBodyPart();
			p.setDataHandler(new DataHandler(ds));
			p.setDisposition(Part.ATTACHMENT);
			p.setFileName(MimeUtility.encodeWord(filename, getCharset(), "B"));
			p.setHeader("Content-Type", mimeType);
			p.setHeader("Content-Transfer-Encoding", "base64");
			attachmentParts.add(p);
			attachmentParts.sort(new AttachmentPartComparator());

			replaceContent(contentParts, attachmentParts);

			return this;
		}
	}

	public Message removeAttachment(String filename) throws MessagingException, IOException {
		List<Part> contentParts = new ArrayList<>();
		List<Part> attachmentParts = new ArrayList<>();
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				if (!filename.equalsIgnoreCase(new AttachmentImpl(p).getFilename())) {
					attachmentParts.add(p);
				}
				continue;
			}

			contentParts.add(p);
		}

		replaceContent(contentParts, attachmentParts);

		return this;
	}

	public Attachment[] getAttachments() throws MessagingException {
		List<Attachment> l = new ArrayList<>();
		for (Part p : fParts) {
			if (!Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				continue;
			}

			l.add(new AttachmentImpl(p));
		}
		return l.toArray(new Attachment[l.size()]);
	}

	public boolean hasContent(String mimeType) throws MessagingException {
		return fContents.containsKey(mimeType);
	}

	public boolean hasAttachments() throws MessagingException {
		for (Part p : fParts) {
			if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
				return true;
			}
		}
		return false;
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

	public MimeMessage toMimeMessage() {
		return fMessage;
	}

	public void writeTo(OutputStream out) throws MessagingException, IOException {
		getMimeHeaders().writeTo(out);
		out.write("\r\n".toString().getBytes(getCharset()));
		if (fMessage.getContent() instanceof Multipart) {
			Multipart mp = (Multipart) fMessage.getContent();
			mp.writeTo(out);
		} else {
			Object value = fMessage.getContent();
			if (value != null) {
				if (!(value instanceof String)) {
					throw new MessagingException("The content value of the specified type cannot be encoded: " + value.getClass().getName());
				}
				String encoding = null;
				try {
					encoding = getMimeHeaders().getDecoded("Content-Transfer-Encoding")[0];
				} catch (Throwable ignore) {}
				if (encoding != null && encoding.equalsIgnoreCase("base64")) {
					value = new String(Base64.getEncoder().encode(value.toString().getBytes(getCharset())), "ISO-8859-1");
				}
				out.write(value.toString().getBytes(getCharset()));
			}
		}
		out.flush();
	}

	@SuppressWarnings("resource")
	public InputStream toInputStream() throws MessagingException, IOException {
		return new MimeCache().getInputStream();
	}

	public String getCharset() throws MessagingException {
		return Charsets.from(fMessage);
	}

	public interface Attachment {
		ContentType getContentType() throws MessagingException;

		String getMimeType() throws MessagingException;

		String getCharset() throws MessagingException;

		String getFilename() throws MessagingException;

		InputStream getInputStream() throws MessagingException, IOException;

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
			return decodeText(fPart.getFileName());
		}

		@Override
		public InputStream getInputStream() throws MessagingException, IOException {
			return new BufferedInputStream(fPart.getInputStream());
		}

		@Override
		public void writeTo(OutputStream out) throws MessagingException, IOException {
			try (InputStream in = getInputStream()) {
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

	private class ContentPartComparator implements Comparator<Part> {
		@Override
		public int compare(Part p1, Part p2) {
			int c1 = getCategory(p1);
			int c2 = getCategory(p2);
			if (c1 < c2) {
				return -1;
			}
			if (c1 > c2) {
				return 1;
			}
			return 0;
		}

		private int getCategory(Part p) {
			ContentType type;
			try {
				type = new ContentType(p.getContentType());
			} catch (Throwable ignore) {
				return 9;
			}

			if ("html".equals(type.getSubType())) {
				return 0;
			}

			if ("plain".equals(type.getSubType())) {
				return 1;
			}

			return 9;
		}
	}

	private class AttachmentPartComparator implements Comparator<Part> {
		@Override
		public int compare(Part p1, Part p2) {
			Attachment a1 = new AttachmentImpl(p1);
			Attachment a2 = new AttachmentImpl(p2);
			try {
				return a1.getFilename().compareToIgnoreCase(a2.getFilename());
			} catch (Throwable ignore) {}
			return 0;
		}
	}

	private class MimeHeaders {
		private final Map<String, List<String>> fHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		public MimeHeaders() throws MessagingException {
			Path path = null;
			try {
				path = Files.createTempFile("mime-", null);
				path.toFile().deleteOnExit();

				try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
					fMessage.writeTo(out);
				}

				try (BufferedReader in = newBufferedReader(path)) {
					String line = in.readLine();
					for (;;) {
						if (line == null || line.trim().isEmpty()) {
							break;
						}

						int i = line.indexOf(":");
						String name = line.substring(0, i);
						String value = line.substring(i + 1);
						if (!value.isEmpty() && (value.charAt(0) == ' ' || value.charAt(0) == '\t')) {
							value = value.substring(1);
						}
						StringBuilder buf = new StringBuilder();
						buf.append(value);
						for (;;) {
							line = in.readLine();
							if (line == null || line.trim().isEmpty()) {
								break;
							}
							if (!(line.charAt(0) == ' ' || line.charAt(0) == '\t')) {
								break;
							}
							buf.append(line.substring(1));
						}
						addEncoded(name, buf.toString());
					}
				}
			} catch (IOException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			} finally {
				try {
					Files.deleteIfExists(path);
				} catch (Throwable ignore) {}
			}
		}

		private BufferedReader newBufferedReader(Path path) throws MessagingException, IOException {
			InputStream in = null;
			try {
				in = new BufferedInputStream(Files.newInputStream(path));
				in.mark(3);
				if (in.available() >= 3) {
					byte[] buffer = new byte[3];
					in.read(buffer);
					if (!(buffer[0] == (byte) 0xEF && buffer[1] == (byte) 0xBB && buffer[2] == (byte) 0xBF)) {
						in.reset();
					}
				}
			} catch (Throwable ex) {
				try {
					in.close();
				} catch (Throwable ignore) {}
				if (ex instanceof IOException) {
					throw (IOException) ex;
				}
				throw (IOException) new IOException(ex.getMessage()).initCause(ex);
			}
			return new BufferedReader(new InputStreamReader(in, Charset.forName(getCharset())));
		}

		public String[] getDecoded(String name) throws MessagingException {
			List<String> values = fHeaders.get(name);
			if (values == null) {
				return null;
			}

			List<String> l = new ArrayList<>();
			for (String e : values) {
				l.add(decodeText(e).trim());
			}
			return l.toArray(new String[l.size()]);
		}

		public void setEncoded(String name, String value) throws MessagingException {
			List<String> values = new ArrayList<>();
			values.add(value);
			fHeaders.put(name, values);
		}

		public void addEncoded(String name, String value) throws MessagingException {
			List<String> values = fHeaders.get(name);
			if (values == null) {
				values = new ArrayList<>();
			}
			values.add(value);
			fHeaders.put(name, values);
		}

		public void setValue(String name, String value) throws MessagingException {
			if (value == null) {
				removeValue(name);
				return;
			}

			setValue(name, new String[] { value });
		}

		public void setValue(String name, String[] values) throws MessagingException {
			if (values == null) {
				removeValue(name);
				return;
			}

			for (int i = 0; i < values.length; i++) {
				try {
					if (i == 0) {
						setEncoded(name, MimeUtility.encodeWord(values[i], getCharset(), "B"));
					} else {
						addEncoded(name, MimeUtility.encodeWord(values[i], getCharset(), "B"));
					}
				} catch (UnsupportedEncodingException ex) {
					throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
				}
			}
		}

		public void removeValue(String name) throws MessagingException {
			fHeaders.remove(name);
		}

		public void writeTo(OutputStream out) throws MessagingException, IOException {
			for (Map.Entry<String, List<String>> e : fHeaders.entrySet()) {
				for (String value : e.getValue()) {
					String line = e.getKey() + ": " + MimeUtility.encodeText(decodeText(value)) + "\r\n";
					out.write(line.getBytes(getCharset()));
				}
			}
			out.flush();
		}
	}

	private class MimeCache implements Closeable {
		private final Path fPath;

		private MimeCache() throws MessagingException {
			try {
				fPath = Files.createTempFile("mime-", null);
				fPath.toFile().deleteOnExit();

				try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(fPath))) {
					writeTo(out);
				}
			} catch (IOException ex) {
				throw (MessagingException) new MessagingException(ex.getMessage()).initCause(ex);
			}
		}

		public InputStream getInputStream() throws IOException {
			return new BufferedInputStream(Files.newInputStream(fPath)) {
				@Override
				public void close() throws IOException {
					super.close();
					MimeCache.this.close();
				}
			};
		}

		@Override
		public void close() throws IOException {
			try {
				Files.deleteIfExists(fPath);
			} catch (Throwable ignore) {}
		}
	}

}
