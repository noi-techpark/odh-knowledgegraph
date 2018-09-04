package semanticdw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QFileParser {
	private Pattern sectionPattern = Pattern.compile("\\s*\\[QueryItem=\"([^]]*)\"\\]\\s*");
	private List<String> sections = new ArrayList<String>();
	private List<String> codes = new ArrayList<String>();

	public QFileParser(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			String section = "";
			String code = "";
			while (null != (line = br.readLine())) {
				Matcher m = sectionPattern.matcher(line);

				if (m.matches()) {
					if (section != null && code != "") {
						codes.add(code);
						code = "";
					}
					section = m.group(1).trim();
					sections.add(section);
				} else {
					code += line + "\n";
				}
			}
			codes.add(code);
		}
	}

	public static String replaceLongIRIs(String text, String code) {
		text = text.replace("<", "").replace(">", "");

		for (Entry<String, String> prefix : getPrefixes(code).entrySet()) {
			text = text.replace(prefix.getValue(), prefix.getKey());
			System.out.println("Replace: " + prefix.toString());
		}
		return text;
	}

	public static String replaceLiteralDatatype(String l) {
		Pattern r = Pattern.compile("\"(.*)\"\\^\\^.*");
		Matcher m = r.matcher(l);
		if (m.find()) {
			l = m.group(1);
		}
		return l;
	}

	public List<String> getCodes() {
		return codes;
	}

	public List<String> getSections() {
		return sections;
	}

	private static Map<String, String> getPrefixes(String code) {

		Map<String, String> prefixes = new HashMap<String, String>();
		for (String line : code.split("\n")) {

			// Remove comment part
			line = line.trim();

			if (line.startsWith("PREFIX")) {
				String[] parts = line.split("\\s+");
				String key = parts[1].trim();
				String val = parts[2].replace("<", "").replace(">", "").trim();
				prefixes.put(key, val);
			}
		}
		return prefixes;
	}

	public static void main(String[] args) {
		try {
			QFileParser qfp = new QFileParser("res/tourism/tourism_v2.q");
			for (String v : qfp.getSections()) {
				System.out.println(v);
			}
			for (String v : qfp.getCodes()) {
				System.out.println(v);

				Map<String, String> prefixes = getPrefixes(v);
				System.out.println(prefixes.toString());
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
