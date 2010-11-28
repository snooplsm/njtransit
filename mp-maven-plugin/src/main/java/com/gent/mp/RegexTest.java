package com.gent.mp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rMatch = "(com\\.scheduler\\.R)";
		Pattern p = Pattern.compile(rMatch);
		String importStatement = "import com.scheduler.R;";
		Matcher m = p.matcher(importStatement);
		System.out.println(m.replaceAll("com.scheduler.njtransit.R"));		
		System.out.println(importStatement.matches(rMatch));
		System.out.println(rMatch.matches(importStatement));
	}

}