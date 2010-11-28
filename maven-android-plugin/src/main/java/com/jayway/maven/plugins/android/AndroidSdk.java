/*
 * Copyright (C) 2009 Jayway AB
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
package com.jayway.maven.plugins.android;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents an Android SDK.
 *
 * @author hugo.josefson@jayway.com
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class AndroidSdk {

    /**
     * property file in each platform folder with details about platform.
     */
    private static final String SOURCE_PROPERTIES_FILENAME = "source.properties";
    /**
     * property name for platform version in sdk source.properties file.
     */
    private static final String PLATFORM_VERSION_PROPERTY = "Platform.Version";
    /**
     * property name for api level version in sdk source.properties file.
     */
    private static final String API_LEVEL_PROPERTY = "AndroidVersion.ApiLevel";

    /**
     * folder name for the sdk sub folder that contains the different platform versions.
     */
    private static final String PLATFORMS_FOLDER_NAME = "platforms";

    private static final String PARAMETER_MESSAGE = "Please provide a proper Android SDK directory path as configuration parameter <sdk><path>...</path></sdk> in the plugin <configuration/>. As an alternative, you may add the parameter to commandline: -Dandroid.sdk.path=... or set environment variable " + AbstractAndroidMojo.ENV_ANDROID_HOME + ".";

    private static final class Platform {
        final String name;
        final String apiLevel;
        final String path;

        public Platform(String name, String apiLevel, String path) {
            super();
            this.name = name;
            this.apiLevel = apiLevel;
            this.path = path;
        }
    }

    private final File sdkPath;
    private final Platform platform;


    private Set<Platform> availablePlatforms;


    public AndroidSdk(File sdkPath, String platformOrApiLevel) {
        this.sdkPath = sdkPath;
        findAvailablePlatforms();

        if (platformOrApiLevel == null) {
            platform = null;
            // letting this through to preserve compatibility for now
        } else {
            platform = findPlatformByNameOrApiLevel(platformOrApiLevel);
            if (platform == null)
                throw new InvalidSdkException("Invalid SDK: Platform/API level " + platformOrApiLevel + " not available.");
        }
    }

    private Platform findPlatformByNameOrApiLevel(String platformOrApiLevel) {
        for (Platform p : availablePlatforms) {
            if (p.name.equals(platformOrApiLevel) || p.apiLevel.equals(platformOrApiLevel)) {
                return p;
            }
        }
        return null;
    }

    public enum Layout {LAYOUT_1_1, LAYOUT_1_5}

    ;

    public Layout getLayout() {

        assertPathIsDirectory(sdkPath);

        final File platforms = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        if (platforms.exists() && platforms.isDirectory()) {
            return Layout.LAYOUT_1_5;
        }

        final File androidJar = new File(sdkPath, "android.jar");
        if (androidJar.exists() && androidJar.isFile()) {
            return Layout.LAYOUT_1_1;
        }

        throw new InvalidSdkException("Android SDK could not be identified from path \"" + sdkPath + "\". " + PARAMETER_MESSAGE);
    }

    private void assertPathIsDirectory(final File path) {
        if (path == null) {
            throw new InvalidSdkException(PARAMETER_MESSAGE);
        }
        if (!path.isDirectory()) {
            throw new InvalidSdkException("Path \"" + path + "\" is not a directory. " + PARAMETER_MESSAGE);
        }
    }

    private static final Set<String> commonToolsIn11And15 = new HashSet<String>() {
        {
            add("adb");
            add("android");
            add("apkbuilder");
            add("ddms");
            add("dmtracedump");
            add("draw9patch");
            add("emulator");
            add("hierarchyviewer");
            add("hprof-conv");
            add("mksdcard");
            add("sqlite3");
            add("traceview");
            add("zipalign");
        }
    };

    /**
     * Returns the complete path for a tool, based on this SDK.
     * TODO: Implementation should try to find the tool in the different directories, instead of relying on a manually maintained list of where they are. (i.e. remove commonToolsIn11And15, and make lookup automatic based on which tools can actually be found where.)
     *
     * @param tool which tool, for example <code>adb</code>.
     * @return the complete path as a <code>String</code>, including the tool's filename.
     */
    public String getPathForTool(String tool) {
        if (getLayout() == Layout.LAYOUT_1_1) {
            return sdkPath + "/tools/" + tool;
        }

        if (getLayout() == Layout.LAYOUT_1_5) {
            if (commonToolsIn11And15.contains(tool)) {
                return sdkPath + "/tools/" + tool;
            } else {

                return getPlatform() + "/tools/" + tool;
            }
        }

        throw new InvalidSdkException("Unsupported layout \"" + getLayout() + "\"! " + PARAMETER_MESSAGE);
    }

    /**
     * Get the emulator path.
     *
     * @return
     */
    public String getEmulatorPath() {
        return getPathForTool("emulator");
    }

    /**
     * Get the android debug tool path (adb).
     *
     * @return
     */
    public String getAdbPath() {
        return getPathForTool("adb");
    }

    /**
     * Get the android debug tool path (adb).
     *
     * @return
     */
    public String getZipalignPath() {
        return getPathForTool("zipalign");
    }

    /**
     * Returns the complete path for <code>framework.aidl</code>, based on this SDK.
     *
     * @return the complete path as a <code>String</code>, including the filename.
     */
    public String getPathForFrameworkAidl() {
        if (getLayout() == Layout.LAYOUT_1_1) {
            return sdkPath + "/tools/lib/framework.aidl";
        }

        if (getLayout() == Layout.LAYOUT_1_5) {
            return getPlatform() + "/framework.aidl";
        }

        throw new InvalidSdkException("Unsupported layout \"" + getLayout() + "\"! " + PARAMETER_MESSAGE);
    }

    /**
     * Resolves the android.jar from this SDK.
     *
     * @return a <code>File</code> pointing to the android.jar file.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          if the file can not be resolved.
     */
    public File getAndroidJar() throws MojoExecutionException {
        if (getLayout() == Layout.LAYOUT_1_1) {
            return new File(sdkPath + "/android.jar");
        }

        if (getLayout() == Layout.LAYOUT_1_5) {
            return new File(getPlatform() + "/android.jar");
        }

        throw new MojoExecutionException("Invalid Layout \"" + getLayout() + "\"! " + PARAMETER_MESSAGE);
    }

    public File getPlatform() {
        assertPathIsDirectory(sdkPath);

        if (getLayout() == Layout.LAYOUT_1_1) {
            return sdkPath;
        }

        final File platformsDirectory = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        assertPathIsDirectory(platformsDirectory);

        if (platform == null) {
            final File[] platformDirectories = platformsDirectory.listFiles();
            Arrays.sort(platformDirectories);
            return platformDirectories[platformDirectories.length - 1];
        } else {
            final File platformDirectory = new File(platform.path);
            assertPathIsDirectory(platformDirectory);
            return platformDirectory;
        }
    }

    /**
     * Initialize the maps matching platform and api levels form the source properties files.
     *
     * @throws InvalidSdkException
     */
    private void findAvailablePlatforms() throws InvalidSdkException {
        availablePlatforms = new HashSet<Platform>();

        ArrayList<File> platformDirectories = getPlatformDirectories();
        for (File pDir : platformDirectories) {
            File propFile = new File(pDir, SOURCE_PROPERTIES_FILENAME);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(propFile));
            } catch (IOException e) {
                throw new InvalidSdkException("Error reading " + propFile.getAbsoluteFile());
            }
            if (properties.containsKey(PLATFORM_VERSION_PROPERTY) && properties.containsKey(API_LEVEL_PROPERTY)) {
                String platform = properties.getProperty(PLATFORM_VERSION_PROPERTY);
                String apiLevel = properties.getProperty(API_LEVEL_PROPERTY);
                availablePlatforms.add(new Platform(platform, apiLevel, pDir.getAbsolutePath()));
            }
        }
    }

    /**
     * Gets the source properties files from all locally installed platforms.
     *
     * @return
     */
    private ArrayList<File> getPlatformDirectories() {
        ArrayList<File> sourcePropertyFiles = new ArrayList<File>();
        final File platformsDirectory = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        assertPathIsDirectory(platformsDirectory);
        final File[] platformDirectories = platformsDirectory.listFiles();
        for (File file : platformDirectories) {
            // only looking in android- folder so only works on reasonably new sdk revisions..
            if (file.isDirectory() && file.getName().startsWith("android-"))
                sourcePropertyFiles.add(file);
        }
        return sourcePropertyFiles;
    }

}