package com.applovin.jenkins;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * User: dpayne Date: 2/1/13 Time: 1:08 PM
 */
public class ReportBuilder
{
    private boolean             buildStatus = true;
    private final File          reportBuildDirectory;
    private final String        pluginUrlPath;
    private final String        buildNumber;
    private final String        buildProjectName;
    private final int           failedThreshold;
    private final File          scheckLogFile;
    private final String        ignoreErrors;
    private final String        excludes;
    private final PrintStream   logger;
    private static final String LOG_TAG     = "[PFFF] ";
    private final File          workspace;
    private List<SCheckError>   errors;

    public ReportBuilder(File reportBuildDirectory, File workspace, String pluginUrlPath, String buildNumber,
            String buildProjectName, int failedThreshold, File scheckLogFile,
            String ignoreErrors, String excludes, PrintStream logger)
    {
        this.reportBuildDirectory = reportBuildDirectory;
        this.pluginUrlPath = pluginUrlPath;
        this.buildNumber = buildNumber;
        this.buildProjectName = buildProjectName;
        this.failedThreshold = failedThreshold;
        this.scheckLogFile = scheckLogFile;
        this.ignoreErrors = ignoreErrors;
        this.excludes = excludes;
        this.logger = logger;
        this.workspace = workspace;
    }

    public void generateReports()
    {
        SCheckParser sCheckParser = new SCheckParser( ignoreErrors, excludes, workspace, logger );
        errors = sCheckParser.parseSCheckLog( scheckLogFile );
        copyResources( reportBuildDirectory );
        generateSCheckHTMLReport( errors );
        if ( errors.size() >= failedThreshold )
        {
            buildStatus = false;
        }
    }

    private void generateSCheckHTMLReport(List<SCheckError> errors)
    {
        VelocityEngine ve = new VelocityEngine();
        try
        {
            ve.init( getProperties() );
            Template projectOverview = ve.getTemplate( "templates/scheckOverview.vm" );
            VelocityContext context = new VelocityContext();
            context.put( "build_project_name", buildProjectName );
            context.put( "build_number", buildNumber );
            context.put( "scheck_errors", errors );
            context.put( "total_errors", errors.size() );
            context.put( "jenkins_base", pluginUrlPath );
            generateReport( "scheck-overview.html", projectOverview, context );
        }
        catch (Exception e)
        {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void copyResources(File reportDirectory)
    {
        try
        {
            Util util = new Util( logger );
            logger.println( LOG_TAG + "Copying resources from \"" + ReportBuilder.class.getResource( "/bootstrap" ).toURI() + "\"to " + reportDirectory.getAbsolutePath() );
            util.copyResourcesRecursively( ReportBuilder.class.getResource( "/bootstrap" ).toURI().toURL(), reportDirectory );
        }
        catch (IOException e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
        catch (URISyntaxException e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
        catch (Exception e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
    }

    private void copyResource(String relativePath, File reportDirectory)
    {
        try
        {
            Util util = new Util( logger );
            logger.println( LOG_TAG + "Copying resources from \"" + ReportBuilder.class.getResource( relativePath ).toURI() + "\"to " + reportDirectory.getAbsolutePath() );
            util.copyResourcesRecursively( ReportBuilder.class.getResource( relativePath ).toURI().toURL(), reportDirectory );
        }
        catch (IOException e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
        catch (URISyntaxException e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
        catch (Exception e)
        {
            logger.println( LOG_TAG + "Exception in copyResource: " );
            e.printStackTrace( logger );
        }
    }

    public boolean getBuildStatus()
    {
        return buildStatus;
    }

    private String getPluginUrlPath(String path)
    {
        return path.isEmpty() ? "/" : path;
    }

    private void generateReport(String fileName, Template featureResult, VelocityContext context) throws Exception
    {
        Writer writer = new FileWriter( new File( reportBuildDirectory, fileName ) );
        featureResult.merge( context, writer );
        writer.flush();
        writer.close();
    }

    private Properties getProperties()
    {
        Properties props = new Properties();
        props.setProperty( "resource.loader", "class" );
        props.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        props.setProperty( "runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem" );
        return props;
    }

    public List<SCheckError> getErrors()
    {
        return errors;
    }
}
