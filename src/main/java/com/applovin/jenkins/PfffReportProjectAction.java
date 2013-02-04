package com.applovin.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Api;
import hudson.model.HealthReport;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PfffReportProjectAction extends PfffReportBaseAction implements ProminentProjectAction
{
    public final AbstractProject<?, ?> project;

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

    public String getResultsGraph()
    {
        List<Integer> results = getResultsArray();

        return results.toString();
    }

    public List<Integer> getResultsArray()
    {
        PfffReportBuildAction lastResult;
        List<Integer> results = new ArrayList<Integer>();
        PfffReport pfffReport;

        for (lastResult = getLastResult(); lastResult != null; lastResult = lastResult.getPreviousResult())
        {
            pfffReport = lastResult.getReport();
            if ( pfffReport != null && pfffReport.getSCheckErrors() != null )
            {
                results.add( pfffReport.getSCheckErrors().size() );
            }
        }

        return results;
    }

    /**
     * Gets the most recent {@link PfffReportBaseAction} object.
     */
    public PfffReportBuildAction getLastResult()
    {
        for (AbstractBuild<?, ?> b = project.getLastBuild(); b != null; b = b.getPreviousBuild())
        {
            PfffReportBuildAction r = b.getAction( PfffReportBuildAction.class );
            if ( r != null )
                return r;
        }
        return null;
    }

    public String getUrlName()
    {
        return "pfff";
    }

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
        if ( ChartUtil.awtProblemCause != null )
        {
            // not available. send out error message
            rsp.sendRedirect2( req.getContextPath() + "/images/headless.png" );
            return;
        }

        PfffReportBuildAction lastResult = getLastResult();
        if (lastResult == null) {
            //no graph data yet
            return;
        }
        Calendar t = Calendar.getInstance();

        String w = hudson.Util.fixEmptyAndTrim( req.getParameter( "width" ) );
        String h = hudson.Util.fixEmptyAndTrim( req.getParameter( "height" ) );
        int width = (w != null) ? Integer.valueOf( w ) : 500;
        int height = (h != null) ? Integer.valueOf( h ) : 200;

        new GraphImpl( lastResult, t, width, height ) {

            protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet(PfffReportBuildAction result)
            {
                DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
                PfffReportBuildAction lastResult;
                PfffReport pfffReport;
                for (lastResult = result; lastResult != null; lastResult = lastResult.getPreviousResult())
                {
                    pfffReport = lastResult.getReport();
                    if ( pfffReport != null && pfffReport.getSCheckErrors() != null )
                    {
                        ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( pfffReport.getBuild() );
                        dsb.add( pfffReport.getSCheckErrors().size(), "scheck", label );
                        if ( pfffReport.getSCheckErrors().size() > super.maxColumnValue )
                        {
                            super.maxColumnValue = pfffReport.getSCheckErrors().size();
                        }
                    }
                }

                return dsb;
            }
        }.doPng( req, rsp );
    }

    public Api getApi()
    {
        return new Api( this );
    }

    private abstract class GraphImpl extends Graph
    {

        private PfffReportBuildAction obj;
        protected int                 maxColumnValue = 0;

        public GraphImpl(PfffReportBuildAction obj, Calendar timestamp, int defaultW, int defaultH)
        {
            super( timestamp, defaultW, defaultH );
            this.obj = obj;
        }

        protected abstract DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet(PfffReportBuildAction obj);

        protected JFreeChart createGraph()
        {
            final CategoryDataset dataset = createDataSet( obj ).build();
            final JFreeChart chart = ChartFactory.createAreaChart(
                    null, // chart title
                    null, // unused
                    "Number of Errors", // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // orientation
                    false, // include legend
                    true, // tooltips
                    false // urls
                    );

            // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

            chart.setBackgroundPaint( Color.WHITE );

            final CategoryPlot plot = chart.getCategoryPlot();

            // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
            plot.setBackgroundPaint( Color.WHITE );
            plot.setOutlinePaint( null );
            plot.setRangeGridlinesVisible( true );
            plot.setRangeGridlinePaint( Color.black );

            CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
            plot.setDomainAxis( domainAxis );
            domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
            domainAxis.setLowerMargin( 0.0 );
            domainAxis.setUpperMargin( 0.0 );
            domainAxis.setCategoryMargin( 0.0 );

            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
            rangeAxis.setUpperBound( maxColumnValue + 1 );
            rangeAxis.setLowerBound( 0 );

            final AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
            renderer.setBaseStroke( new BasicStroke( 4.0f ) );
            // ColorPalette.apply(renderer);

            // crop extra space around the graph
            plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

            return chart;
        }
    }

}