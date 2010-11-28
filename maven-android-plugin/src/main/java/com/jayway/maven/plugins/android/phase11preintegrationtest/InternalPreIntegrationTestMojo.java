/*
 * Copyright (C) 2009 Jayway AB
 * Copyright (C) 2007-2008 JVending Masa
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
package com.jayway.maven.plugins.android.phase11preintegrationtest;

import com.jayway.maven.plugins.android.AbstractIntegrationtestMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Internal. Do not use.<br/>
 * Called automatically when the lifecycle reaches phase <code>pre-integration-test</code>. Figures out whether to
 * call goals in this phase; and if so, calls <code>android:deploy-dependencies</code> and <code>android:deploy</code>.
 *
 * @author hugo.josefson@jayway.com
 * @goal internal-pre-integration-test
 * @phase pre-integration-test
 */
public class InternalPreIntegrationTestMojo extends AbstractIntegrationtestMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isEnableIntegrationTest()) {
            deployDependencies();
            deployBuiltApk();
        }
    }

}