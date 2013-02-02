package com.applovin.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: dpayne Date: 2/2/13 Time: 2:17 AM
 */
public class PfffReport implements Serializable
{
    private static final Logger LOG    = Logger.getLogger( PfffReport.class.getName() );

    private AbstractBuild<?, ?> build;
    private PfffConfig          config;
    public List<SCheckError>    errors = new ArrayList<SCheckError>();

    public PfffReport(AbstractBuild<?, ?> build, List<SCheckError> errors)
    {
        this.build = build;
        this.errors = errors;
    }

    /**
     * Set the build.
     * 
     * @param build
     *            the current build.
     */
    public void setBuild(AbstractBuild<?, ?> build)
    {
        this.build = build;
    }

    /**
     * Get the build.
     * 
     * @return the build.
     */
    public AbstractBuild<?, ?> getBuild()
    {
        return build;
    }

    /**
     * Set the config.
     * 
     * @param config
     *            the config.
     */
    public void setConfig(PfffConfig config)
    {
        this.config = config;
    }

    /**
     * Get the config.
     * 
     * @return the config.
     */
    public PfffConfig getConfig()
    {
        return config;
    }

    /**
     * Get the overall health for the build.
     * 
     * @return the health report, null if there are no counts.
     */
    public HealthReport getBuildHealth()
    {
        // todo: use thresholds to vary message
        HealthReport ret = new HealthReport( errors.size(), "/plugin/pfff-reports/pfff.png", " this is a message" );
        return ret;
    }

    /**
     * Graph this report. Note that for some reason, yet unknown, hudson seems to pick an in memory ViolationsReport object and not the report for the build. (Reason may be related to the fact that serialized builds may not be the same as in-memory builds). Need to find the correct build from the
     * URI.
     * 
     * @param req
     *            the request paramters
     * @param rsp
     *            the response.
     * @throws IOException
     *             if there is an error writing the graph.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp)
            throws IOException
    {
        AbstractBuild<?, ?> tBuild = build;
        int buildNumber = HelpHudson.findBuildNumber( req );
        if ( buildNumber != 0 )
        {
            tBuild = (AbstractBuild<?, ?>) build.getParent().getBuildByNumber( buildNumber );
            if ( tBuild == null )
            {
                tBuild = build;
            }
        }

        PfffReportBaseAction r = tBuild.getAction( PfffReportBaseAction.class );
        if ( r == null )
        {
            return;
        }
        r.doGraph( req, rsp );
    }

    /**
     * Get the icon for a type.
     * 
     * @param t
     *            the type
     * @return the icon name.
     */
    public String getIcon(String t)
    {
        HealthReport h = getBuildHealth();
        if ( h == null )
        {
            return null;
        }
        return h.getIconUrl();
    }

    /**
     * Get the previous ViolationsReport
     * 
     * @return the previous report if present, null otherwise.
     */
    public PfffReport previous()
    {
        return findPfffReport( build.getPreviousBuild() );
    }

    /**
     * Get the unstable status for this report.
     * 
     * @return true if one of the violations equals or exceed the unstable threshold for that violations type.
     */
    private boolean isUnstable()
    {
        int count = errors.size();
        if ( count >= config.getUnstableLimit() )
        {
            return true;
        }
        return false;
    }

    /**
     * Get the failed status for this report.
     * 
     * @return true if one of the violations equals or exceed the failed threshold of that violations type.
     */
    private boolean isFailed()
    {
        int count = errors.size();
        if ( count >= config.getErrorLimit() )
        {
            return true;
        }
        return false;
    }

    /**
     * Set the unstable/failed status of a build based on this violations report.
     */
    public void setBuildResult()
    {
        if ( isFailed() )
        {
            build.setResult( Result.FAILURE );
            return;
        }
        if ( isUnstable() )
        {
            build.setResult( Result.UNSTABLE );
        }
    }

    private static final long serialVersionUID = 1L;

    public static PfffReport findPfffReport(
            AbstractBuild<?, ?> b)
    {
        for (; b != null; b = b.getPreviousBuild())
        {
            if ( b.getResult().isWorseOrEqualTo( Result.FAILURE ) )
            {
                continue;
            }
            PfffReportBaseAction action = b.getAction( PfffReportBaseAction.class );
            if ( action == null || action.getReport() == null )
            {
                continue;
            }
            PfffReport ret = action.getReport();
            ret.setBuild( b );
            return ret;
        }
        return null;
    }

    public static PfffReportIterator iteration(
            AbstractBuild<?, ?> build)
    {
        return new PfffReportIterator( build );
    }

    public List<SCheckError> getErrors()
    {
        return errors;
    }

    public static class PfffReportIterator
            implements Iterator<PfffReport>, Iterable<PfffReport>
    {
        private AbstractBuild<?, ?> curr;

        public PfffReportIterator(AbstractBuild<?, ?> curr)
        {
            this.curr = curr;
        }

        public Iterator<PfffReport> iterator()
        {
            return this;
        }

        public boolean hasNext()
        {
            return findPfffReport( curr ) != null;
        }

        public PfffReport next()
        {
            PfffReport ret = findPfffReport( curr );
            if ( ret != null )
            {
                curr = ret.getBuild().getPreviousBuild();
            }
            return ret;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
