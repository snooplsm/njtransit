package com.jayway.maven.plugins.android;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the zipaplign goal. Implements parsing parameters from pom or command line arguments and sets
 * useful defaults as well.
 *
 * @author Manfred Moser <manfred@simpligility.com>
 */
public abstract class AbstractZipalignMojo extends AbstractAndroidMojo {

    /**
     * The zipalign command configuration to use. As soon as a zipalign goal is invoked the command will be executed
     * unless the skip parameter is set. By default the input file is the apk produced by the build in target. The
     * outputApk will use the postfix -aligned.apk. The following shows a default full configuration of the zipalign
     * goal as an example for changes.
     * <pre>
     * &lt;zipalign&gt;
     *     &lt;skip&gt;false&lt;/skip&gt;
     *     &lt;verbose&gt;true&lt;/verbose&gt;
     *     &lt;inputApk&gt;${project.build.directory}/${project.artifactId}.apk&lt;/inputApk&gt;
     *     &lt;outputApk&gt;${project.build.directory}/${project.artifactId}-aligned.apk&lt;/outputApk&gt;
     * &lt;/zipalign&gt;
     * </pre>
     *
     * @parameter
     */
    private Zipalign zipalign;

    /**
     * @parameter expression="${android.zipalign.skip}"
     * @readonly
     * @see Zipalign#skip
     */
    private Boolean zipalignSkip;

    /**
     * @parameter expression="${android.zipalign.verbose}"
     * @readonly
     * @see Zipalign#verbose
     */
    private Boolean zipalignVerbose;

    /**
     * @parameter expression="${android.zipalign.inputApk}"
     * @readonly
     * @see Zipalign#inputApk
     */
    private String zipalignInputApk;

    /**
     * @parameter expression="${android.zipalign.outputApk}"
     * @readonly
     * @see Zipalign#outputApk
     */
    private String zipalignOutputApk;

    private Boolean parsedSkip;
    private Boolean parsedVerbose;
    private String parsedInputApk;
    private String parsedOutputApk;

    /**
     * the apk file to be zipaligned.
     */
    private File apkFile;
    /**
     * the output apk file for the zipalign process.
     */
    private File alignedApkFile;

    private static final List<String> SUPPORTED_PACKAGING_TYPES = new ArrayList<String>();

    static {
        SUPPORTED_PACKAGING_TYPES.add("apk");
    }

    /**
     * actually do the zipalign
     *
     * @throws MojoExecutionException
     */
    protected void zipalign() throws MojoExecutionException {

        // If we're not on a supported packaging with just skip (Issue 87)
        // http://code.google.com/p/maven-android-plugin/issues/detail?id=87
        if (! SUPPORTED_PACKAGING_TYPES.contains(project.getPackaging())) {
            getLog().info("Skipping zipalign on " + project.getPackaging());
            return;
        }

        parseParameters();
        if (parsedSkip) {
            getLog().info("Skipping zipalign");
        } else {
            CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
            executor.setLogger(this.getLog());

            String command = getAndroidSdk().getZipalignPath();

            List<String> parameters = new ArrayList<String>();
            if (parsedVerbose) {
                parameters.add("-v");
            }
            parameters.add("-f"); // force overwriting existing output file
            parameters.add("4"); // byte alignment has to be 4!
            parameters.add(parsedInputApk);
            parameters.add(parsedOutputApk);

            try {
                getLog().info("Running command: " + command);
                getLog().info("with parameters: " + parameters);
                executor.executeCommand(command, parameters);

                // Attach the resulting artifact (Issue 88)
                // http://code.google.com/p/maven-android-plugin/issues/detail?id=88
                File aligned = new File(parsedOutputApk);
                if (aligned.exists()) {
                    projectHelper.attachArtifact(project, "apk", "aligned", aligned);
                    getLog().info("Attach " + aligned.getAbsolutePath() + " to the project");
                } else {
                    getLog().error("Cannot attach " + aligned.getAbsolutePath() + " to the project" +
                            " - The file does not exist");
                }
            } catch (ExecutionException e) {
                throw new MojoExecutionException("", e);
            }
        }
    }

    private void parseParameters() {
        getLog().debug("Parsing parameters");
        // <zipalign> exist in pom file
        if (zipalign != null) {
            // <zipalign><skip> exists in pom file
            if (zipalign.isSkip() != null) {
                parsedSkip = zipalign.isSkip();
            } else {
                parsedSkip = determineSkip();
            }

            // <zipalign><verbose> exists in pom file
            if (zipalign.isVerbose() != null) {
                parsedVerbose = zipalign.isVerbose();
            } else {
                parsedVerbose = determineVerbose();
            }

            // <zipalign><inputApk> exists in pom file
            if (zipalign.getInputApk() != null) {
                parsedInputApk = zipalign.getInputApk();
            } else {
                parsedInputApk = determineInputApk();
            }


            // <zipalign><outputApk> exists in pom file
            if (zipalign.getOutputApk() != null) {
                parsedOutputApk = zipalign.getOutputApk();
            } else {
                parsedOutputApk = determineOutputApk();
            }

        }
        // command line options
        else {
            parsedSkip = determineSkip();
            parsedVerbose = determineVerbose();
            parsedInputApk = determineInputApk();
            parsedOutputApk = determineOutputApk();
        }

        getLog().debug("skip:" + parsedSkip);
        getLog().debug("verbose:" + parsedVerbose);
        getLog().debug("inputApk:" + parsedInputApk);
        getLog().debug("outputApk:" + parsedOutputApk);
    }

    /**
     * Get skip value for zipalign from command line option.
     *
     * @return if available return command line value otherwise return default false.
     */
    private Boolean determineSkip() {
        Boolean enabled;
        if (zipalignSkip != null) {
            enabled = zipalignSkip;
        } else {
            getLog().debug("Using default for zipalign.skip=false");
            enabled = Boolean.FALSE;
        }
        return enabled;
    }

    /**
     * Get verbose value for zipalign from command line option.
     *
     * @return if available return command line value otherwise return default false.
     */
    private Boolean determineVerbose() {
        Boolean enabled;
        if (zipalignVerbose != null) {
            enabled = zipalignVerbose;
        } else {
            getLog().debug("Using default for zipalign.verbose=false");
            enabled = Boolean.FALSE;
        }
        return enabled;
    }

    /**
     * Gets the apk file location from basedir/target/finalname.apk
     *
     * @return absolute path.
     */
    private String getApkLocation() {
        if (apkFile == null) apkFile = new File(project.getBuild().getDirectory(),
                project.getBuild().getFinalName() + ANDROID_PACKAGE_EXTENSTION);
        return apkFile.getAbsolutePath();
    }

    /**
     * Gets the apk file location from basedir/target/finalname-aligned.apk. "-aligned" is the inserted string for the
     * output file.
     *
     * @return absolute path.
     */
    private String getAlignedApkLocation() {
        if (alignedApkFile == null) alignedApkFile = new File(project.getBuild().getDirectory(),
                project.getBuild().getFinalName() + "-aligned" + ANDROID_PACKAGE_EXTENSTION);
        return alignedApkFile.getAbsolutePath();
    }

    /**
     * Get inputApk value for zipalign from command line option.
     *
     * @return if available return command line value otherwise return default.
     */
    private String determineInputApk() {
        String inputApk;
        if (zipalignInputApk != null) {
            inputApk = zipalignInputApk;
        } else {
            String inputPath = getApkLocation();
            getLog().debug("Using default for zipalign.inputApk: " + inputPath);
            inputApk = inputPath;
        }
        return inputApk;
    }

    /**
     * Get outputApk value for zipalign from command line option.
     *
     * @return if available return command line value otherwise return default.
     */
    private String determineOutputApk() {
        String outputApk;
        if (zipalignOutputApk != null) {
            outputApk = zipalignOutputApk;
        } else {
            String outputPath = getAlignedApkLocation();
            getLog().debug("Using default for zipalign.outputApk: " + outputPath);
            outputApk = outputPath;
        }
        return outputApk;
    }

}
