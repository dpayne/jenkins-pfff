package com.applovin.jenkins;

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
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
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: dpayne
 * Date: 2/3/13
 * Time: 1:53 PM
 */
@ExportedBean
public abstract class PfffObject<SELF extends PfffObject<SELF>>
{
    private static final long serialVersionUID = 1L;
    private static final Logger LOG    = Logger.getLogger( PfffReport.class.getName() );

    private AbstractBuild<?, ?> build;
    private PfffConfig          config;
    public List<SCheckError> errors = new ArrayList<SCheckError>();
    private PfffReportBuildAction action;
    private PfffReport previousResult;
    private PfffReport report;

    public void setBuildAction(PfffReportBuildAction buildAction)
    {
        this.action = buildAction;
        this.build = buildAction.getBuild();
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
        HealthReport ret = new HealthReport( errors.size(), "/plugin/pfff-reports/pfff.png", " this is a message" );
        return ret;
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
        return findPfffReport(build.getPreviousBuild());
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

    public void setReport(PfffReport report)
    {
        this.report = report;
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

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if(ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath()+"/images/headless.png");
            return;
        }

        AbstractBuild<?,?> build = getBuild();
        Calendar t = build.getTimestamp();

        String w = hudson.Util.fixEmptyAndTrim(req.getParameter("width"));
        String h = hudson.Util.fixEmptyAndTrim(req.getParameter("height"));
        int width = (w != null) ? Integer.valueOf(w) : 500;
        int height = (h != null) ? Integer.valueOf(h) : 200;

        new GraphImpl(this, t, width, height) {

            protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet(PfffObject obj)
            {
                DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

                for (PfffObject a = obj; a != null; a = a.previous())
                {
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.getBuild());
                    dsb.add(a.errors.size(), "Number of scheck errors", label);
                }

                return dsb;
            }
        }.doPng(req, rsp);
    }

    public Api getApi() {
        return new Api(this);
    }

    private abstract class GraphImpl extends Graph
    {

        private PfffObject<SELF> obj;

        public GraphImpl(PfffObject<SELF> obj, Calendar timestamp, int defaultW, int defaultH) {
            super(timestamp, defaultW, defaultH);
            this.obj = obj;
        }

        protected abstract DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet(PfffObject<SELF> obj);

        protected JFreeChart createGraph() {
            final CategoryDataset dataset = createDataSet(obj).build();
            final JFreeChart chart = ChartFactory.createLineChart(
              null, // chart title
              null, // unused
              "%", // range axis label
              dataset, // data
              PlotOrientation.VERTICAL, // orientation
              true, // include legend
              true, // tooltips
              false // urls
            );

            // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

            final LegendTitle legend = chart.getLegend();
            legend.setPosition(RectangleEdge.RIGHT);

            chart.setBackgroundPaint(Color.white);

            final CategoryPlot plot = chart.getCategoryPlot();

            // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlinePaint(null);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.black);

            CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
            plot.setDomainAxis(domainAxis);
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            domainAxis.setLowerMargin(0.0);
            domainAxis.setUpperMargin(0.0);
            domainAxis.setCategoryMargin(0.0);

            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setUpperBound(100);
            rangeAxis.setLowerBound(0);

            final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setBaseStroke(new BasicStroke(4.0f));
            ColorPalette.apply(renderer);

            // crop extra space around the graph
            plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

            return chart;
        }
    }

    public PfffReportBuildAction getAction()
    {
        return action;
    }

    public void setAction(PfffReportBuildAction action)
    {
        this.action = action;
    }

    public void setPreviousResult(PfffReport previousResult)
    {
        this.previousResult = previousResult;
    }
}
