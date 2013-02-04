package com.applovin.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerProxy;

import java.io.File;
import java.lang.ref.WeakReference;

public final class PfffReportBuildAction extends PfffObject<PfffReportBuildAction> implements HealthReportingAction,
  StaplerProxy
{
    final AbstractBuild<?, ?> build;

    private PfffReport report;

    private PfffConfig pfffConfig;

    public PfffReportBuildAction(AbstractBuild<?, ?> build, PfffConfig pfffConfig)
    {
        super();
        this.build = build;
        this.pfffConfig = pfffConfig;
    }

    protected String getTitle()
    {
        return this.build.getDisplayName() + " html3";
    }

    protected File dir()
    {
        return new File( build.getRootDir(), "pfff-html-reports" );
    }

    @Override
    public void setReport(PfffReport report) {
        this.report = report;
    }

    public String getNumberOfErrors() {
        PfffReport tempReport = null;
        if (report != null)
        {
            tempReport = report;
        }
        if (tempReport == null || tempReport.getSCheckErrors() == null) {
            return "0";
        }
        else
        {
            return String.valueOf(tempReport.getSCheckErrors().size());
        }
    }

    /**
     * Get the coverage {@link hudson.model.HealthReport}.
     *
     * @return The health report or <code>null</code> if health reporting is disabled.
     * @since 1.7
     */
    public HealthReport getBuildHealth() {
        if (pfffConfig == null) {
            // no thresholds => no report
            return null;
        }
        pfffConfig.fix();

        HealthReport healthReport = null;
        return healthReport;
    }

    /**
     * Obtains the detailed {@link PfffReport} instance.
     */
    public synchronized PfffReport getResult() {

        if(report!=null) {
           return report;
        }

        // Generate the report
        PfffReport r = new PfffReport(this);

        report = r;
        return r;
    }

    public PfffReportBuildAction getPreviousResult() {
        return getPreviousResult(build);
    }

    /**
     * Gets the previous {@link PfffReportBuildAction} of the given build.
     */
    /*package*/ static PfffReportBuildAction getPreviousResult(AbstractBuild<?,?> start) {
        AbstractBuild<?,?> b = start;
        while(true) {
            b = b.getPreviousBuild();
            if(b==null)
                return null;
            if(b.getResult()== Result.FAILURE)
                continue;
            PfffReportBuildAction r = b.getAction(PfffReportBuildAction.class);
            if(r!=null)
                return r;
        }
    }

    public PfffReport getReport() {
        if (report == null) {
            report = new PfffReport(this);
        }

        return report;
    }

    public Object getTarget() {
        return getResult();
    }

    public String getIconFileName() {
        return "/plugin/pfff-reports/pfff.png";
    }

    public String getDisplayName() {
        return "Pfff Reports";
    }

    public String getUrlName() {
        return "pfff";
    }
}
