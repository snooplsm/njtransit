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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
public class MyMojo
    extends AbstractMojo
{
	
	private static DateFormat VERSION_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat VERSION_CODE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	
    /**
     * Location of the file.
     * @parameter
     * @required
     */
    private File androidManifestTarget;
    
    /**
     * Location of the file.
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

    public void execute()
        throws MojoExecutionException
    {
    	File androidManifest = androidManifestFile;
    	
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		Document doc = null;
        try {
			doc = docBuilder.parse (androidManifest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}	
		
		doc.getDocumentElement ().normalize ();
        System.out.println ("Root element of the doc is " + 
             doc.getDocumentElement().getNodeName());
        System.out.println("packageName "+doc.getDocumentElement().getAttribute("package"));
        setPackageName(doc.getDocumentElement());
        System.out.println("packageName "+doc.getDocumentElement().getAttribute("package"));
        setVersion(doc.getDocumentElement());
        setDebuggable(doc.getDocumentElement());
        System.out.println("versionName "+doc.getDocumentElement().getAttribute("android:versionName"));
        System.out.println("versionCode "+doc.getDocumentElement().getAttribute("android:versionCode"));
    	
        File f = outputDirectory;

        if ( !f.exists() )
        {
            f.mkdirs();
        }

        File touch = new File( f, "touch.txt" );

        FileWriter w = null;
        try
        {
            w = new FileWriter( touch );

            w.write( "touch.txt" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + touch, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
    
    private void setPackageName(Element element) {
    	_packageName = element.getAttribute("package");
    	element.setAttribute("package", packageName);
    }
    
    private void setDebuggable(Element element) {
    	((Element)element.getElementsByTagName("application")).setAttribute("android:debuggable", Boolean.FALSE.toString());
    }
    
    private void setVersion(Element element) {
    	Calendar cal = Calendar.getInstance();
    	Date date = cal.getTime();
    	element.setAttribute("android:versionName", VERSION_NAME_FORMAT.format(date));
    	element.setAttribute("android:versionCode", VERSION_CODE_FORMAT.format(date));
    }
}
