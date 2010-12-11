package com.gent.mp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rMatch = "(com\\.njtransit\\.rail\\.R)";
		Pattern p = Pattern.compile(rMatch);
		String importStatement = "import com.njtransit.rail.R;";
		Matcher m = p.matcher(importStatement);
		System.out.println(m.replaceAll("com.metro.north.R"));		
		System.out.println(importStatement.matches(rMatch));
		System.out.println(rMatch.matches(importStatement));
		
		String mMatch = "http://schemas\\.android.com/apk/res/(com\\.scheduler\\.njtransit\\.rail)";
		Pattern pm = Pattern.compile(mMatch);
		importStatement = "xmlns:app=\"http://schemas.android.com/apk/res/com.metro.north\"";
		Matcher mm = pm.matcher(importStatement);
		System.out.println(mm.replaceAll("com.metro.north"));
	}

}
