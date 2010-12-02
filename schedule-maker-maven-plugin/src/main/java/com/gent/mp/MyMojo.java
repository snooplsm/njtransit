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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal make
 * 
 * @phase process-sources
 */
public class MyMojo
    extends AbstractMojo
{
    /**
     * Location of the gtfs zip or folder.
     * @parameter
     * @required
     */
    private File gtfsFile;
    
    /**
     * Where we will process files
     * @parameter expression="${project.build.directory}"
     */
    private File workDir;
    
    /**
     * Where to store split files
     * @parameter expression="${project.build.directory}/partitions"
     * @required
     */
    private File partitionsTarget;

    /**
     * Kilobytes for which we will split the sqlite file
     */
    private Integer splitKilobytes = 50;
    
    public void execute()
        throws MojoExecutionException
    {
    	try {
			DatabaseCreater.main(new String[]{"-workdir", workDir.getAbsolutePath(),"-split",splitKilobytes.toString(),"-gtfs",gtfsFile.getAbsolutePath(),"-partitions",partitionsTarget.getAbsolutePath()});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}
