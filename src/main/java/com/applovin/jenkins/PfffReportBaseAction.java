package com.applovin.jenkins;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public abstract class PfffReportBaseAction
        implements Action
{
    private PfffReport report;

    protected abstract String getTitle();

    protected abstract File dir();


    public String getUrlName()
    {
        return "pfff-html-reports";
    }

    public String getDisplayName()
    {
        return "Pfff Reports";
    }

    public String getNumberOfErrors()
    {
        return String.valueOf( report.getErrors().size() );
    }

    public String getIconFileName()
    {
        return "/plugin/pfff-reports/pfff.png";
    }

    public PfffReport getReport()
    {
        return report;
    }

    public void setReport(PfffReport report)
    {
        this.report = report;
    }

    public boolean isFloatingBoxActive() {
        return true;
    }

    public boolean isGraphActive() {
        return true;
    }

    public String getGraphName() {
        return getDisplayName();
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport( this, new FilePath( this.dir() ), this.getTitle(), "graph.gif", false );
        dbs.setIndexFileName( "scheck-overview.html" );
        dbs.generateResponse( req, rsp, this );
    }

    public abstract void doGraph(StaplerRequest req, StaplerResponse rsp);
}
