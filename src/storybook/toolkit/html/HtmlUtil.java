package storybook.toolkit.html;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

import org.jsoup.Jsoup;
import storybook.toolkit.TextUtil;
import storybook.toolkit.swing.ColorUtil;

public class HtmlUtil {

	public static String getCleanHtml(String html) {
		StringBuffer buf = new StringBuffer();
		appendCleanHtml(buf, html);
		return buf.toString();
	}

	public static void appendCleanHtml(StringBuffer buf, String html) {
		// doesn't work for plain text
		/*
		Document doc = Jsoup.parseBodyFragment(text);
		Element body = doc.body();
//		Elements content = body.getElementsContainingText("");
		Elements content = body.getAllElements();
		for(Element el:content){
			buf.append(el.toString());
			buf.append("\n");
		}
		*/

		// remove new lines
		html = html.replace("\n", "");
		// replace empty div tags with paragraphs: "<div>\s*</div>"
		html = html.replaceAll("<div>\\s*</div>", "<p></p>");

		// body>(.*)</body
		Pattern p = Pattern.compile("body>(.*)</body");
		Matcher m = p.matcher(html);
		if (m.find() == true) {
			html = m.group(1);
		}
		html = html.trim();

		// not in any tag at all, so build one
		boolean addDiv = false;
		if (!html.startsWith("<")) {
			addDiv = true;
			buf.append("<div>");
		}
		buf.append(html);
		if (addDiv) {
			buf.append("</div>");
		}
	}

	// doesn't work
//	public static String getContent(String htmlText) {
//		try {
//			StringReader reader = new StringReader(htmlText);
//			HTMLEditorKit htmlKit = new HTMLEditorKit();
//			HTMLDocument htmlDoc = (HTMLDocument) htmlKit
//					.createDefaultDocument();
//			HTMLEditorKit.Parser parser = new ParserDelegator();
//			HTMLEditorKit.ParserCallback callback = htmlDoc.getReader(0);
//			parser.parse(reader, callback, true);
//			return getContent(htmlDoc);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return "";
//	}

	public static String getContent(HTMLDocument doc) {
		try {
			StringWriter writer = new StringWriter();
			HTMLWriter htmlWriter = new HtmlBodyWriter(writer, doc);
			htmlWriter.write();
			return writer.toString();
		} catch (IOException | BadLocationException e) {
		}
		return "";
	}

	public static String findHref(String html) {
		Pattern pattern = Pattern
				.compile("(((file|http|https)://)|(www\\.))+(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(/[a-zA-Z0-9\\&amp;%_\\./-~-]*)?");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String wrapIntoTable(String html) {
		return wrapIntoTable(html, 200);
	}

	public static String wrapIntoTable(String html, int width) {
		StringBuffer ret = new StringBuffer();
		ret.append("<html>");
		ret.append("<table width='" + width + "'><tr><td>");
		ret.append(html);
		ret.append("<td></tr><table>");
		return ret.toString();
	}

	public static boolean equalsHtml(String html1, String html2) {
		String s1 = Jsoup.parse(html1).text();
		String s2 = Jsoup.parse(html2).text();
		return (s1 == s2 || (s1 != null && s1.equals(s2)));
	}

	public static String htmlToText(String html) {
		return htmlToText(html, false);
	}

	public static String htmlToText(String html, boolean preserveNewLines) {
		html = getCleanHtml(html);
		if (!preserveNewLines) {
			return Jsoup.parse(html).text();
		}
		html = html.replaceAll("<br>", "__BR__");
		html = html.replaceAll("</br>", "__BR_END__");
		html = html.replaceAll("<p>", "__P__");
		html = html.replaceAll("</p>", "__P_END__");
		html = Jsoup.parse(html).text();
		html = html.replaceAll("__BR__", "\n");
		html = html.replaceAll("__BR_END__", "");
		html = html.replaceAll("__P__", "\n");
		html = html.replaceAll("__P_END__", "\n");
		html = html.replaceAll("<br>", "\n");
		return TextUtil.trimText(html);
	}

	public static String textToHTML(String text) {
		if (text == null) {
			return null;
		}
		int length = text.length();
		boolean prevSlashR = false;
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
			case '\r':
				if (prevSlashR) {
					out.append("<br>");
				}
				prevSlashR = true;
				break;
			case '\n':
				prevSlashR = false;
				out.append("<br>");
				break;
			case '"':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&quot;");
				break;
			case '<':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&lt;");
				break;
			case '>':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&gt;");
				break;
			case '&':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&amp;");
				break;
			default:
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append(ch);
				break;
			}
		}
		return out.toString();
	}

	public static String getRow2Cols(StringBuffer text1, String text2) {
		return getRow2Cols(text1.toString(), text2.toString());
	}

	public static String getRow2Cols(StringBuffer text1, StringBuffer text2) {
		return getRow2Cols(text1.toString(), text2.toString());
	}

	public static String getRow2Cols(String text1, StringBuffer text2) {
		return getRow2Cols(text1.toString(), text2.toString());
	}

	public static String getRow2Cols(String text1, String text2) {
		return "<tr><td>" + text1 + "</td><td>" + text2 + "</td></tr>";
	}

	public static String getTitle(String title) {
		StringBuffer buf = new StringBuffer();
		buf.append("<div style='padding-top:2px;padding-bottom:2px;");
		buf.append("padding-left:4px;padding-right:4px;");
		buf.append("margin-bottom:2px;");
		buf.append("'><b>");
		buf.append(title);
		buf.append("</b></div>");
		return buf.toString();
	}

	public static String getColorSpan(Color clr) {
		String htmlClr = (clr == null ? "white" : ColorUtil.getHexName(clr));
		StringBuffer buf = new StringBuffer();
		buf.append("<span style='");
		if (clr != null) {
			buf.append("background-color:");
			buf.append(htmlClr);
			buf.append(";");
		}
		buf.append("'>");
		buf.append("&nbsp; &nbsp; &nbsp; &nbsp; </div>");
		return buf.toString();
	}

	public static String getColoredTitle(Color clr, String title) {
		String htmlClr = (clr == null ? "white" : ColorUtil.getHexName(clr));
		StringBuffer buf = new StringBuffer();
		buf.append("<div style='padding-top:2px;padding-bottom:2px;");
		buf.append("padding-left:4px;padding-right:4px;");
		buf.append("margin-bottom:2px;");
		if (clr != null) {
			buf.append("background-color:");
			buf.append(htmlClr);
			buf.append(";");
		}
		if (clr != null && ColorUtil.isDark(clr)) {
			buf.append("color:white;");
		}
		buf.append("'><b>");
		buf.append(title);
		buf.append("</b></div>");
		return buf.toString();
	}

	public static String getHeadWithCSS() {
		return getHeadWithCSS(10);
	}

	public static String getHeadWithCSS(int fontSize) {
		StringBuffer buf = new StringBuffer();
		buf.append("<head>");
		buf.append("<style type='text/css'><!--\n");
		// body
		buf.append("body {");
		buf.append("font-family:Arial,sans-serif;");
		buf.append("font-size:" + fontSize + "px;");
		buf.append("padding-left:2px;");
		buf.append("padding-right:2px;");
		buf.append("}\n");
		// h1
		buf.append("h1 {");
		buf.append("font-family:Arial,sans-serif;");
		buf.append("font-size:140%;");
		buf.append("text-align:center;");
		buf.append("margin-top:15px;");
		buf.append("margin-bottom:15px;");
		buf.append("}\n");
		// h2
		buf.append("h2 {");
		buf.append("font-family:Arial,sans-serif;");
		buf.append("font-size:120%;");
		buf.append("margin-top:15px;");
		buf.append("}\n");
		// div
		buf.append("p {");
		buf.append("margin-top:2px;");
		buf.append("}\n");
		// div
		buf.append("div {");
		buf.append("padding-left:5px;");
		buf.append("padding-right:5px;");
		buf.append("}\n");
		// unordered list
		buf.append("ul {");
		buf.append("margin-top:2px;");
		buf.append("margin-left:15px;");
		buf.append("margin-bottom:2px;");
		buf.append("}\n");
		// ordered list
		buf.append("ol {");
		buf.append("margin-top:2px;");
		buf.append("margin-left:15px;");
		buf.append("margin-bottom:2px;");
		buf.append("}\n");
		// table
		buf.append("table tr {");
		// buf.append("border-width:1px;border-color:#000000;border-style:solid;");
		buf.append("margin:0px;");
		buf.append("padding:0px;");
		buf.append("}\n");
		buf.append("td {");
		buf.append("margin-right:5px;");
		buf.append("padding:2px;");
		buf.append("}\n");
		buf.append("--></style>");
		buf.append("</head>\n");
		return buf.toString();
	}

	public static String getBold(String str) {
		return "<b>" + str + "</b>";
	}

	public static String getWarning(String str) {
		return "<font color='red'><b>" + str + "</b></font>";
	}

	public static String getHr() {
		return "<hr style='margin:10px' />";
	}

	public static void appendFormatedWarning(StringBuffer buf, String warning) {
		String str = warning.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
		if (str.isEmpty()) {
			return;
		}
		buf.append("<div style='color:red'>");
		buf.append(str);
		buf.append("</div>");
	}

	public static void appendFormatedDescr(StringBuffer buf, String descr,
			boolean shorten) {
		String str = descr.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
		if (str.isEmpty()) {
			return;
		}
		if (shorten) {
			buf.append("<div style='width:300px'>");
			buf.append(TextUtil.truncateString(str, 300));
		} else {
			buf.append("<div>");
			buf.append(str);
		}
		buf.append("</div>");
	}

	public static void appendFormatedNotes(StringBuffer buf, String notes,
			boolean shorten) {
		String str = notes.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
		if (str.isEmpty()) {
			return;
		}
		buf.append("<hr style='margin:5px'/>");
		if (shorten) {
			buf.append("<div style='width:300px'>");
			buf.append(TextUtil.truncateString(str, 300));
		} else {
			buf.append("<div>");
			buf.append(str);
		}
		buf.append("</div>");
	}

	public static void appendFormatedMetaInfos(StringBuffer buf,
			String metaInfos) {
		String str = metaInfos.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
		if (str.isEmpty()) {
			return;
		}
		buf.append("<hr style='margin:5px'/>");
		buf.append("<div>");
		buf.append(str);
		buf.append("</div>");
	}
}
