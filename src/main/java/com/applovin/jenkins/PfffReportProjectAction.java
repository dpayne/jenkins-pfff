package com.applovin.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.HealthReport;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.IOException;

public class PfffReportProjectAction extends PfffReportBaseAction implements ProminentProjectAction
{
    public final AbstractProject<?,?> project;

    public PfffReportProjectAction(AbstractProject project)
    {
        this.project = project;
    }

    @Override
    protected File dir()
    {
        if ( this.project instanceof AbstractProject )
        {
            AbstractProject abstractProject = (AbstractProject) this.project;

            Run run = abstractProject.getLastCompletedBuild();
            if ( run != null )
            {
                File javadocDir = getBuildArchiveDir( run );

                if ( javadocDir.exists() )
                {
                    return javadocDir;
                }
            }
        }

        return getProjectArchiveDir( this.project );
    }

    private File getProjectArchiveDir(AbstractItem project)
    {
        return new File( project.getRootDir(), "pfff-html-reports" );
    }

    /**
     * Gets the directory where the HTML report is stored for the given build.
     */
    private File getBuildArchiveDir(Run run)
    {
        return new File( run.getRootDir(), "pfff-html-reports" );
    }


    @Override
    protected String getTitle()
    {
        return this.project.getDisplayName() + " html2";
    }

    public HealthReport getBuildHealth()
    {
        return new HealthReport( getReport().getErrors().size(), " number of errors" );
    }



    /**
     * Gets the most recent {@link PfffReportBaseAction} object.
     */
    public PfffReportBaseAction getLastResult() {
        for( AbstractBuild<?,?> b = project.getLastBuild(); b!=null; b=b.getPreviousBuild()) {
            if(b.getResult()== Result.FAILURE)
                continue;
            PfffReportBaseAction r = b.getAction(PfffReportBaseAction.class);
            if(r!=null)
                return r;
        }
        return null;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp)
    {
        if (getLastResult() != null)
            getLastResult().doGraph(req,rsp);
    }
}