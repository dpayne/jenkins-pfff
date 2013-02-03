package com.applovin.jenkins;

import hudson.model.AbstractBuild;

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
}
