package com.applovin.jenkins;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.HealthReportingAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public abstract class PfffReportBaseAction
        implements Action, HealthReportingAction
{

    private static PfffReport report;

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

    public String getSCheckDescription() {
        return "SCheck: " + report.getErrors().size() + " warnings from one analysis.";
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
        PfffReportBaseAction.report = report;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport( this, new FilePath( this.dir() ), this.getTitle(), "graph.gif", false );
        dbs.setIndexFileName( "scheck-overview.html" );
        dbs.generateResponse( req, rsp, this );
    }
}
