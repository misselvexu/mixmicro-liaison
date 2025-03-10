/* Copyright 2009, 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.util;



import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.predic8.beautifier.HtmlBeautifierFormatter;
import com.predic8.beautifier.PlainBeautifierFormatter;
import com.predic8.beautifier.XMLBeautifier;
import com.predic8.beautifier.XMLBeautifierFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Character.toUpperCase;
import static java.lang.Integer.*;
import static javax.xml.stream.XMLInputFactory.*;
import static org.apache.commons.lang3.StringUtils.join;


public class TextUtil {
	private static final Logger log = LoggerFactory.getLogger(TextUtil.class.getName());

	private static final XMLInputFactory xmlInputFactory = newInstance();

	private static final char[] source;
	private static final String[] replace;

	static {
		source = new char[] {    '*',    '?',  '.',    '\\',      '(' ,    ')',    '+',      '|',    '^',     '$',    '%',       '@'    };
		replace = new String[] { ".*",   ".",  "\\.",  "\\\\",   "\\(",   "\\)",   "\\+",   "\\|",  "\\^",   "\\$",    "\\%",   "\\@"   };
	}


	public static String formatXML(Reader reader) {
		return formatXML(reader, false);
	}

	public static String formatXML(Reader reader, boolean asHTML) {
		StringWriter out = new StringWriter();

		try {
			XMLBeautifierFormatter formatter = asHTML ? new HtmlBeautifierFormatter(out, 0) : new PlainBeautifierFormatter(out, 0);
			XMLBeautifier beautifier = new XMLBeautifier(formatter);
			beautifier.parse(reader);
		}
		catch (Exception e){
			log.error("", e);
		} finally {
			try {
				out.close();
				reader.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return out.toString();
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String globToRegExp(String glob) {
		StringBuilder buf = new StringBuilder();
		buf.append("^");
		for(int i = 0; i < glob.length(); i ++) {
			appendReplacement(glob.charAt(i), buf);
		}
		buf.append("$");
		return buf.toString();
	}

	private static void appendReplacement(char c, StringBuilder buf) {
		for (int j = 0; j < source.length; j++) {
			if (c == source[j]) {
				buf.append(replace[j]);
				return;
			}
		}
		buf.append(c);
	}

	public static String toEnglishList(String conjuction, String... args) {
		ArrayList<String> l = new ArrayList<>();
		for (String arg : args)
			if (arg != null && arg.length() > 0)
				l.add(arg);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < l.size(); i++) {
			sb.append(l.get(i));
			if (i == l.size() - 2) {
				sb.append(" ");
				sb.append(conjuction);
				sb.append(" ");
			}
			if (i < l.size() - 2)
				sb.append(", ");
		}
		return sb.toString();
	}

	public static Object capitalize(String english) {
		if (english.length() == 0)
			return "";
		return toUpperCase(english.charAt(0)) + english.substring(1);
	}



	static {
		xmlInputFactory.setProperty(IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	}

	/**
	 * Checks whether s is a valid (well-formed and balanced) XML snippet.
	 * Note that attributes escaped by single quotes are accepted (which is illegal by spec).
	 */
	public static boolean isValidXMLSnippet(String s) {
		try {
			XMLEventReader parser;
			synchronized (xmlInputFactory) {
				parser = xmlInputFactory.createXMLEventReader(new StringReader("<a>" + s + "</a>"));
			}
			XMLEvent event = null;
			while (parser.hasNext()) {
				event = (XMLEvent) parser.next();
			}
			return event != null && event.isEndDocument();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public static String linkURL(String url) {
		if (url.startsWith("http://") || url.startsWith("https://")) {
			url = StringEscapeUtils.escapeHtml4(url);
			return "<a href=\"" + url + "\">" + url + "</a>";
		}
		return StringEscapeUtils.escapeHtml4(url);
	}

	public static Object removeFinalChar(String s) {
		StringBuilder sb = new StringBuilder(s);
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public static String removeCommonLeadingIndentation(String src) {
		// TODO: only handles tabs at the moment
		String[] lines = src.split("\n");
		int indent = MAX_VALUE;
		for (String line : lines) {
			if (StringUtils.strip(line).length() == 0)
				continue;
			int i = 0;
			while (i < line.length() && line.charAt(i) == '\t')
				i++;
			indent = Math.min(indent, i);
		}
		if (indent == 0 || indent == MAX_VALUE)
			return src;
		for (int i = 0; i < lines.length; i++)
			lines[i] = lines[i].length() > indent ? lines[i].substring(indent) : "";

		return join(lines, '\n');
	}

	/**
	 * Counts from 1 cause this is needed for getting lines from Javascript source code.
	 *
	 * @param s Multiline string
	 * @param lineNumber number of line to return. Counts from 1
	 * @return line
	 */
	public static String getLineFromMultilineString(String s,int lineNumber) {
		return s.split("\n")[lineNumber-1];
	}

}