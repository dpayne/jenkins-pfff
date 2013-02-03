package com.applovin.jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.slaves.SlaveComputer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PfffReportPublisher extends Recorder
{
    private static final String   LOG_TAG = "[PFFF] ";

    public final String           scheckLogFilePath;
    public final String           pluginUrlPath;
    public final String           ignoredErrors;
    public final int              failedThreshold;
    public final String           excludes;
    private List<SCheckError>     errors;

    private AbstractBuild<?, ?>   build;
    private PfffReport            report;
    private PfffReportBuildAction action;

    @DataBoundConstructor
    public PfffReportPublisher(String scheckLogFilePath, String failedThreshold, String excludes, String ignoredErrors,
            String pluginUrlPath)
    {
        this.ignoredErrors = ignoredErrors;
        this.failedThreshold = Integer.parseInt( failedThreshold );
        this.excludes = excludes;
        this.scheckLogFilePath = scheckLogFilePath;
        this.pluginUrlPath = pluginUrlPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException
    {
        this.build = build;

        listener.getLogger().println( LOG_TAG + "Compiling Pfff Html Reports ..." );

        File workspaceReportDirectory = new File( build.getWorkspace().toURI().getPath() );
        File targetBuildDirectory = new File( build.getRootDir(), "pfff-html-reports" );
        File scheckLogFile = new File( workspaceReportDirectory, scheckLogFilePath );

        String buildNumber = Integer.toString( build.getNumber() );
        String buildProjectName = build.getProject().getName();

        if ( !targetBuildDirectory.exists() )
        {
            targetBuildDirectory.mkdirs();
        }

        boolean buildResult = true;

        // if we are on a slave
        if ( Computer.currentComputer() instanceof SlaveComputer )
        {
            // if we are on a slave
            listener.getLogger().println( LOG_TAG + "detected this build is running on a slave " );
        }
        else
        {
            // if we are on the master
            listener.getLogger().println( LOG_TAG + "detected this build is running on the master " );

            if ( scheckLogFile.exists() )
            {
                listener.getLogger().println( LOG_TAG + "copying json to reports directory: " + targetBuildDirectory );
                listener.getLogger().println( LOG_TAG + "Copying " + workspaceReportDirectory.getPath() + "/" + scheckLogFilePath );
                FileUtils.copyFile( new File( workspaceReportDirectory.getPath() + "/" + scheckLogFilePath ), new File( targetBuildDirectory, scheckLogFilePath ) );
            }
            else
            {
                listener.getLogger().println( LOG_TAG + "there were no scheck results found in: " + workspaceReportDirectory );
            }
        }

        // generate the reports from the targetBuildDirectory
        listener.getLogger().println( LOG_TAG + "Generating HTML reports" );

        try
        {
            ReportBuilder reportBuilder = new ReportBuilder(
                    targetBuildDirectory,
                    workspaceReportDirectory,
                    pluginUrlPath,
                    buildNumber,
                    buildProjectName,
                    failedThreshold,
                    scheckLogFile,
                    ignoredErrors,
                    excludes,
                    listener.getLogger() );

            reportBuilder.generateReports();
            this.errors = reportBuilder.getErrors();
            report = new PfffReport( build, errors );
            buildResult = reportBuilder.getBuildStatus();
        }
        catch (Exception e)
        {
            listener.getLogger().println( LOG_TAG + "Error in Feature ReportGenerator: " );
            e.printStackTrace( listener.getLogger() );
        }

        this.action = new PfffReportBuildAction( build );
        action.setReport( report );
        build.addAction( action );
        return buildResult;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project)
    {
        PfffReportProjectAction action = new PfffReportProjectAction( project );
        action.setReport( report );
        return action;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher>
    {
        @Override
        public String getDisplayName()
        {
            return "Publish pfff results as a report";
        }

        // Performs on-the-fly validation on the file mask wildcard.
        public FormValidation doCheck(@AncestorInPath AbstractProject project,
                @QueryParameter String value) throws IOException, ServletException
        {
            FilePath ws = project.getSomeWorkspace();
            return ws != null ? ws.validateRelativeDirectory( value ) : FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType)
        {
            return true;
        }
    }

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }
}
