package com.applovin.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;

import java.io.File;

public class PfffReportBuildAction extends PfffReportBaseAction
{
    private final AbstractBuild<?, ?> build;

    public PfffReportBuildAction(AbstractBuild<?, ?> build)
    {
        this.build = build;
    }

    @Override
    protected String getTitle()
    {
        return this.build.getDisplayName() + " html3";
    }

    @Override
    protected File dir()
    {
        return new File( build.getRootDir(), "pfff-html-reports" );
    }

    public HealthReport getBuildHealth()
    {
        return new HealthReport( getReport().getErrors().size(), " number of errors" );
    }
}
