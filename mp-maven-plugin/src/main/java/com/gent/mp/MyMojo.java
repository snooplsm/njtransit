package com.gent.mp;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase process-sources
 */
public class MyMojo extends AbstractMojo {

	private static DateFormat VERSION_NAME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static DateFormat VERSION_CODE_FORMAT = new SimpleDateFormat(
			"yyyyMMddHH");

	/**
	 * Location of the file.
	 * 
	 * @parameter
	 * @required
	 */
	private File androidManifestTarget;

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @parameter expression="${project.basedir}/AndroidManifest.xml"
	 * @required
	 */
	private File androidManifestFile;

	/**
	 * @parameter
	 * @required
	 */
	private String packageName;

	private String _packageName;

	/**
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * @parameter expression="${project.basedir}/res"
	 * @required
	 */
	private File resourceDirectory;
	
	public void execute() throws MojoExecutionException {
		File androidManifest = androidManifestFile;

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		Document doc = null;
		try {
			doc = docBuilder.parse(androidManifest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		doc.getDocumentElement().normalize();
		System.out.println("Root element of the doc is "
				+ doc.getDocumentElement().getNodeName());
		System.out.println("packageName "
				+ doc.getDocumentElement().getAttribute("package"));
		setPackageName(doc.getDocumentElement());
		System.out.println("packageName "
				+ doc.getDocumentElement().getAttribute("package"));
		setVersion(doc.getDocumentElement());
		setDebuggable(doc.getDocumentElement());
		System.out.println("versionName "
				+ doc.getDocumentElement().getAttribute("android:versionName"));
		System.out.println("versionCode "
				+ doc.getDocumentElement().getAttribute("android:versionCode"));

		renamePackages();

		TransformerFactory tFactory =
		    TransformerFactory.newInstance();
		  Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			throw new RuntimeException(e1);
		}

		  DOMSource source = new DOMSource(doc);
		  StreamResult result = new StreamResult(androidManifest);
		  try {
			transformer.transform(source, result);
		} catch (TransformerException e1) {
			throw new RuntimeException(e1);
		} 

		
		File f = outputDirectory;

		if (!f.exists()) {
			f.mkdirs();
		}

		File touch = new File(f, "touch.txt");

		FileWriter w = null;
		try {
			w = new FileWriter(touch);

			w.write("touch.txt");
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating file " + touch, e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private void renamePackages() {
		FileFilter javaFilter = new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory() || arg0.getName().endsWith(".java") || arg0.getName().endsWith(".xml");
			}

		};

		String rMatch = _packageName + ".R";
		rMatch = "("+rMatch.replaceAll("\\.", "\\\\.")+")";
		String mMatch = "http://schemas.android.com/apk/res/"+_packageName;
		rMatch = "("+rMatch.replaceAll("\\.","\\\\.")+")";
		mMatch = "("+mMatch.replaceAll("\\.","\\\\.")+")";
		Pattern p = Pattern.compile(rMatch);
		Pattern m = Pattern.compile(mMatch);
		String mNew = "http://schemas.android.com/apk/res/"+packageName;
		String rNew = packageName + ".R";
		Stack<File> files = new Stack<File>();
		files.push(sourceDirectory);
		files.push(resourceDirectory);
		while (!files.isEmpty()) {
			File currentFolder = files.pop();
			for (File file : currentFolder.listFiles(javaFilter)) {
				if (file.isDirectory()) {
					files.push(file);
				} else {					
					FileInputStream fin = null;
					BufferedInputStream bis = null;
					try {
						fin = new FileInputStream(file);
						bis = new BufferedInputStream(fin);
						BufferedReader br = new BufferedReader(new InputStreamReader(bis));
						StringBuilder b = new StringBuilder();
						String line = null;
						boolean changed = false;
						while((line = br.readLine())!=null) {
							if(file.getName().endsWith(".java")) {
								Matcher matcher = p.matcher(line);							
								if(matcher.find()) {
									String newString = matcher.replaceAll(rNew);
									b.append(newString);
									changed=true;
								} else {
									b.append(line);
								}
							}
							if(file.getName().endsWith(".xml")) {
								Matcher matcher = m.matcher(line);							
								if(matcher.find()) {
									String newString = matcher.replaceAll(mNew);
									b.append(newString);
									changed=true;
								} else {
									b.append(line);
								}
							}
							b.append("\n");
						}
						if(changed) {
							FileOutputStream fos = null;
							BufferedOutputStream bos = null;
							try {
								fos = new FileOutputStream(file);
								bos = new BufferedOutputStream(fos);
								bos.write(b.toString().getBytes());
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
							finally {
								if(bos!=null) {
									bos.flush();
									bos.close();
								}
								if(fos!=null) {									
									fos.close();
								}
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						try {
							fin.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void setPackageName(Element element) {
		_packageName = element.getAttribute("package");
		element.setAttribute("package", packageName);
	}

	private void setDebuggable(Element element) {
		Object o = element.getElementsByTagName("application").item(0);
		((Element) o).setAttribute(
				"android:debuggable", Boolean.FALSE.toString());
	}

	private void setVersion(Element element) {
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		element.setAttribute("android:versionName", VERSION_NAME_FORMAT
				.format(date));
		element.setAttribute("android:versionCode", VERSION_CODE_FORMAT
				.format(date));
	}
}
