package com.applovin.jenkins;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dpayne Date: 2/2/13 Time: 2:17 AM
 */
public class PfffReport extends PfffObject<PfffReport>
{
    private final PfffReportBuildAction action;

    private String                      name;
    private List<SCheckError>           sCheckErrors;

    public PfffReport(PfffReportBuildAction action)
    {
        super();
        this.action = action;
        setName( "Pfff Reports" );
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        return name;
    }

    public PfffReport getPreviousResult()
    {
        PfffReportBuildAction prev = action.getPreviousResult();
        if ( prev != null )
            return prev.getResult();
        else
            return null;
    }

    @Override
    public AbstractBuild<?, ?> getBuild()
    {
        return action.build;
    }

    public List<SCheckError> getSCheckErrors()
    {
        if ( sCheckErrors != null )
        {
            return sCheckErrors;
        }
        return null;
    }

    public void setSCheckErrors(List<SCheckError> sCheckErrors)
    {
        this.sCheckErrors = new ArrayList<SCheckError>( sCheckErrors );
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport( this.action, new FilePath( this.action.dir() ), this.action.getTitle(), "graph.gif", false );
        dbs.setIndexFileName( "scheck-overview.html" );
        dbs.generateResponse( req, rsp, this );
    }
}
