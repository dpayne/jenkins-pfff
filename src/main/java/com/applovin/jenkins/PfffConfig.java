package com.applovin.jenkins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dpayne Date: 2/2/13 Time: 2:36 AM
 */
public class PfffConfig implements Cloneable, Serializable
{
    private static final int LIMIT_DEFAULT = 100;

    /** The number of violations per type per file to display */
    private int              limit         = LIMIT_DEFAULT;

    /** The (optional) source path pattern. */
    private String           sourcePathPattern;

    /** The (optional) project base used for "faux" projects. */
    private String           fauxProjectPath;

    /** The encoding to use for reading source files */
    private String           encoding      = "default";
    private int              unstableLimit;
    private int              errorLimit;
    private String SCheckLogFilePath;
    private String pluginUrlPath;
    private String ignoredErrors;
    private int failedThreshold;
    private String excludes;

    /**
     * The constructor fot eh violations config. This creates a config with default values.
     */
    public PfffConfig()
    {
    }

    /**
     * Clone the config object.
     * 
     * @return a copy of this object.
     */
    public PfffConfig clone()
    {
        PfffConfig ret = new PfffConfig();
        // FIXME: the following should use super.clone()
        ret.limit = limit;
        ret.sourcePathPattern = sourcePathPattern;
        ret.fauxProjectPath = fauxProjectPath;
        ret.encoding = encoding;
        return ret;
    }

    /**
     * Get the limit.
     * 
     * @return the limit of violations per type to display per file.
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Set the limit.
     * 
     * @param limit
     *            the value to use.
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * Get the source path pattern. This is used if one has a class but no source file. This is true for the current maven find bugs plugin. It may be true for other violation detectors as well.
     * 
     * @return the source path pattern.
     */
    public String getSourcePathPattern()
    {
        return sourcePathPattern;
    }

    /**
     * Set the source path patter.
     * 
     * @param sourcePathPattern
     *            the value to use.
     */
    public void setSourcePathPattern(String sourcePathPattern)
    {
        this.sourcePathPattern = sourcePathPattern;
    }

    /**
     * Get the faux project path. This is used for projects that are not actually in the hudson job directory area.
     * 
     * @return the faux project path - may be blank.
     */
    public String getFauxProjectPath()
    {
        return fauxProjectPath;
    }

    /**
     * Set the faux project path.
     * 
     * @param fauxProjectPath
     *            the value to use.
     */
    public void setFauxProjectPath(String fauxProjectPath)
    {
        this.fauxProjectPath = fauxProjectPath;
    }

    /** FIXME: use a java api to get supported encodings */
    private final static String[]     ENCODING_STRINGS = new String[] {
                                                       "default",
                                                       "Cp1252",
                                                       // "MS-932", -- check again
            "ISO8859_1",
            "UTF-8",
            "UTF-16",
                                                       };

    private final static List<String> ENCODINGS        = new ArrayList<String>();
    static
    {
        for (String m : ENCODING_STRINGS)
        {
            ENCODINGS.add( m );
        }
    }

    public List<String> getEncodings()
    {
        return ENCODINGS;
    }

    /**
     * Get the encoding.
     * 
     * @return the encoding of source files.
     */
    public String getEncoding()
    {
        if ( encoding == null )
        {
            this.encoding = "default";
        }
        return encoding;
    }

    /**
     * Set the encoding.
     * 
     * @param encoding
     *            the value to use.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * After configuration change, reset the numbers if they are out of range.
     */
    public void fix()
    {
        if ( limit <= 0 )
        {
            limit = LIMIT_DEFAULT;
        }
    }

    private static final long serialVersionUID = 1L;

    public int getUnstableLimit()
    {
        return unstableLimit;
    }

    public int getErrorLimit()
    {
        return errorLimit;
    }

    public void setSCheckLogFilePath(String SCheckLogFilePath)
    {
        this.SCheckLogFilePath = SCheckLogFilePath;
    }

    public void setPluginUrlPath(String pluginUrlPath)
    {
        this.pluginUrlPath = pluginUrlPath;
    }

    public String getPluginUrlPath()
    {
        return pluginUrlPath;
    }

    public void setIgnoredErrors(String ignoredErrors)
    {
        this.ignoredErrors = ignoredErrors;
    }

    public void setFailedThreshold(int failedThreshold)
    {
        this.failedThreshold = failedThreshold;
    }

    public void setExcludes(String excludes)
    {
        this.excludes = excludes;
    }
}
