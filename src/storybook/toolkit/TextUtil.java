/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package storybook.toolkit;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import storybook.toolkit.html.HtmlUtil;

/**
 * @author martin
 *
 */
public class TextUtil {
	// private final static String NON_THIN = "[^iIl1\\.,']";

	public static int countWords(String text) {
		int count = 0;
		StringTokenizer stk = new StringTokenizer(text, " ");
		while (stk.hasMoreTokens()) {
			@SuppressWarnings("unused")
			String token = stk.nextToken();
			count++;
		}
		return count;
	}

	public static String[] getTextLines(String str) {
		return str.split("\\r?\\n");
	}

	public static String trimText(String str) {
		StringBuffer buf = new StringBuffer();
		String[] lines = getTextLines(str);
		for (String line : lines) {
			buf.append(line.trim() + "\n");
		}
		return buf.toString();
	}

	public static String truncateText(String text) {
		return truncateText(text, 200);
	}

	public static String truncateText(String text, int length) {
		return truncateString(HtmlUtil.htmlToText(text), length);
	}

	public static String truncateString(String str, int length) {
		if (str == null) {
			return "";
		}
		if (str.length() > length) {
			return StringUtils.left(str, length) + " ...";
		}
		return str;
	}

	// private static int textWidth(String str) {
	// return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	// }

	public static String ellipsize(String text, int max) {
		return StringUtils.abbreviate(text, max);

		// old
		// if (textWidth(text) <= max)
		// return text;
		//
		// // Start by chopping off at the word before max
		// // This is an over-approximation due to thin-characters...
		// int end = text.lastIndexOf(' ', max - 3);
		//
		// // Just one long word. Chop it off.
		// if (end == -1)
		// return text.substring(0, max-3) + "...";
		//
		// // Step forward as long as textWidth allows.
		// int newEnd = end;
		// do {
		// end = newEnd;
		// newEnd = text.indexOf(' ', end + 1);
		//
		// // No more spaces.
		// if (newEnd == -1)
		// newEnd = text.length();
		//
		// } while (textWidth(text.substring(0, newEnd) + "...") < max);
		//
		// return text.substring(0, end) + "...";
	}
}
