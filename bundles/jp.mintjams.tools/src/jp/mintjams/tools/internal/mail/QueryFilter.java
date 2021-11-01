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

package jp.mintjams.tools.internal.mail;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.IntegerComparisonTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.SubjectTerm;

public class QueryFilter {

	private static final Map<String, Integer> FILTER_TYPES = new LinkedHashMap<>();
	static {
		FILTER_TYPES.put(">=", ComparisonTerm.GE);
		FILTER_TYPES.put(">", ComparisonTerm.GT);
		FILTER_TYPES.put("<=", ComparisonTerm.LE);
		FILTER_TYPES.put("<", ComparisonTerm.LT);
		FILTER_TYPES.put("!=", ComparisonTerm.NE);
		FILTER_TYPES.put("=", ComparisonTerm.EQ);
	}

	private static final Map<String, RecipientType> RECIPIENT_TYPES = new HashMap<>();
	static {
		RECIPIENT_TYPES.put("To", RecipientType.TO);
		RECIPIENT_TYPES.put("Cc", RecipientType.CC);
		RECIPIENT_TYPES.put("Bcc", RecipientType.BCC);
	}

	public SearchTerm parse(String filter) {
		Objects.requireNonNull(filter);
		filter = filter.trim();
		if (!filter.startsWith("(") || !filter.endsWith(")")) {
			throw new IllegalArgumentException("Invalid filter: " + filter);
		}

		if (filter.startsWith("(&") || filter.startsWith("(|")) {
			String expr = filter.substring(2, filter.length() - 1);
			String[] filters = splitFilter(expr);
			List<SearchTerm> l = new ArrayList<>();
			for (String f : filters) {
				l.add(new QueryFilter().parse(f));
			}
			SearchTerm[] terms = l.toArray(new SearchTerm[l.size()]);
			if (filter.startsWith("(&")) {
				return new AndTerm(terms);
			}
			return new OrTerm(terms);
		}

		if (filter.startsWith("(!")) {
			filter = filter.substring(2, filter.length() - 1);
			return new NotTerm(new QueryFilter().parse(filter));
		}

		String expr = filter.substring(1, filter.length() - 1);
		int typeIndex = -1;
		String type = null;
		for (String s : FILTER_TYPES.keySet().toArray(new String[FILTER_TYPES.size()])) {
			for (int fromIndex = 0;;) {
				int i = expr.indexOf(s, fromIndex);
				if (i == -1) {
					break;
				}
				if (i > 0 && expr.charAt(i - 1) == '\\') {
					fromIndex = i + 1;
					continue;
				}
				typeIndex = i;
				type = s;
				break;
			}
			if (typeIndex != -1) {
				break;
			}
		}
		if (typeIndex == -1) {
			throw new IllegalArgumentException("Invalid filter: " + filter);
		}

		String name = expr.substring(0, typeIndex).trim();
		String value = expr.substring(typeIndex + type.length()).trim();

		if ("MessageNumber".equals(name)) {
			return new MessageNumberFilterTerm(FILTER_TYPES.get(type), new BigDecimal(value).intValue());
		}

		if ("MessageId".equals(name)) {
			if (!"=".equals(type)) {
				throw new IllegalArgumentException("Invalid filter: " + filter);
			}
			return new MessageIDTerm(value);
		}

		if ("ReceivedDate".equals(name)) {
			return new ReceivedDateTerm(FILTER_TYPES.get(type), java.util.Date.from(ZonedDateTime.parse(value).toInstant()));
		}

		if ("SentDate".equals(name)) {
			return new SentDateTerm(FILTER_TYPES.get(type), java.util.Date.from(ZonedDateTime.parse(value).toInstant()));
		}

		if ("SizeDate".equals(name)) {
			return new SizeTerm(FILTER_TYPES.get(type), new BigDecimal(value).intValue());
		}

		if ("From".equals(name)) {
			if (!"=".equals(type)) {
				throw new IllegalArgumentException("Invalid filter: " + filter);
			}
			return new FromStringTerm(value);
		}

		if ("To".equals(name) || "Cc".equals(name) || "Bcc".equals(name)) {
			if (!"=".equals(type)) {
				throw new IllegalArgumentException("Invalid filter: " + filter);
			}
			return new RecipientStringTerm(RECIPIENT_TYPES.get(name), value);
		}

		if ("Subject".equals(name)) {
			if (!"=".equals(type)) {
				throw new IllegalArgumentException("Invalid filter: " + filter);
			}
			return new SubjectTerm(value);
		}

		if ("Body".equals(name)) {
			if (!"=".equals(type)) {
				throw new IllegalArgumentException("Invalid filter: " + filter);
			}
			return new BodyTerm(value);
		}

		throw new IllegalArgumentException("Invalid filter: " + filter);
	}

	private String[] splitFilter(String filterList) {
		List<String> l = new ArrayList<>();
		StringBuilder filter = new StringBuilder();
		filterList = filterList.trim();
		int nest = -1;
		while (!filterList.isEmpty()) {
			char c0 = filterList.charAt(0);
			if (c0 == '(') {
				nest++;
				filter.append(c0);
				filterList = filterList.substring(1);
				continue;
			}
			if (c0 == ')') {
				nest--;
				filter.append(c0);
				filterList = filterList.substring(1);
				l.add(filter.toString().trim());
				filter = new StringBuilder();
				continue;
			}

			if (nest == -1) {
				filterList = filterList.substring(1);
				continue;
			}

			if (c0 == '\\') {
				char c1 = filterList.charAt(1);
				filter.append(c0).append(c1);
				filterList = filterList.substring(2);
				continue;
			}

			filter.append(c0);
			filterList = filterList.substring(1);
		}
		return l.toArray(new String[l.size()]);
	}

	private static class MessageNumberFilterTerm extends IntegerComparisonTerm {
		private static final long serialVersionUID = 1L;

		private MessageNumberFilterTerm(int comparison, int number) {
			super(comparison, number);
		}

		@Override
		public boolean match(Message msg) {
			if (comparison == ComparisonTerm.EQ) {
				return (msg.getMessageNumber() == number);
			}
			if (comparison == ComparisonTerm.GE) {
				return (msg.getMessageNumber() >= number);
			}
			if (comparison == ComparisonTerm.GT) {
				return (msg.getMessageNumber() > number);
			}
			if (comparison == ComparisonTerm.LE) {
				return (msg.getMessageNumber() <= number);
			}
			if (comparison == ComparisonTerm.LT) {
				return (msg.getMessageNumber() < number);
			}
			if (comparison == ComparisonTerm.NE) {
				return (msg.getMessageNumber() != number);
			}
			return false;
		}
	}
}
