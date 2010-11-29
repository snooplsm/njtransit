package com.gent.mp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public interface ContentValuesProvider {
	List<List<Object>> getContentValues(Map<String,Integer> headerToPos, CSVReader reader) throws IOException;
	String getInsertString();
}