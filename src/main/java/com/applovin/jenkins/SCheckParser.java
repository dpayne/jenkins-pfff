package com.applovin.jenkins;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dpayne Date: 2/1/13 Time: 2:57 PM
 */
public class SCheckParser
{
    private static final String  CHECK_LINE_PATTERN_STR = "(.*?):([0-9]+):([0-9]+): CHECK: (.*)";
    private static final Pattern checkLinePattern       = Pattern.compile( CHECK_LINE_PATTERN_STR );
    private final PrintStream    logger;
    private static final String  LOG_TAG                = "[PFFF] ";
    private final String         ignoreErrors;
    private final String         excludes;
    private List<String>         excludedFiles          = new ArrayList<String>();
    private List<String>         ignoredErrors          = new ArrayList<String>();
    private File                 workspace;

    public SCheckParser(String ignoreErrors, String excludes, File workspace, PrintStream logger)
    {
        this.ignoreErrors = ignoreErrors;
        this.excludes = excludes;
        this.workspace = workspace;
        this.logger = logger;

        if ( excludes != null && !excludes.isEmpty() )
        {
            String[] filePaths = excludes.split( "," );
            for (String excludedFile : filePaths)
            {
                excludedFiles.add( excludedFile.trim() );
            }
        }

        if ( ignoreErrors != null && !ignoreErrors.isEmpty() )
        {
            String[] errors = ignoreErrors.split( "," );
            for (String error : errors)
            {
                ignoredErrors.add( error.trim() );
            }
        }
    }

    public List<SCheckError> parseSCheckLog(File scheckLogFile)
    {
        List<SCheckError> errors = new ArrayList<SCheckError>();
        try
        {
            List<String> lines = FileUtils.readLines( scheckLogFile );
            Matcher matcher;
            SCheckError error;

            for (String line : lines)
            {
                matcher = checkLinePattern.matcher( line );
                if ( matcher.find() )
                {
                    error = new SCheckError( workspace, matcher.group( 1 ), matcher.group( 2 ), matcher.group( 3 ), matcher.group( 4 ) );
                    for (String excludedPath : excludedFiles)
                    {
                        // check for excluded paths
                        if ( error.getRelativePath().substring( 0, excludedPath.length() ).equals( excludedPath ) )
                        {
                            error.setIsExcluded( true );
                        }
                    }

                    for (String ignoredError : ignoredErrors)
                    {
                        if ( ignoredError.substring( 0, ignoredError.length() ).equals( ignoredError ) )
                        {
                            error.setIsExcluded( true );
                        }
                    }

                    if ( !error.isExcluded() )
                    {
                        errors.add( error );
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.println( LOG_TAG + "error parsing scheck log file" );
            e.printStackTrace( logger ); // To change body of catch statement use File | Settings | File Templates.
        }

        return errors;
    }
}
