package com.applovin.jenkins;

import java.io.File;

/**
 * User: dpayne Date: 2/1/13 Time: 3:02 PM
 */
public class SCheckError
{
    private final String filePath;
    private final String lineNumber;
    private final String columnNumber;
    private final String error;
    private final File   workspace;
    private final String workspacePath;
    private String       relativePath;
    private final String viewFilename;
    private boolean      isExcluded = false;

    public SCheckError(File workspace, String filePath, String lineNumber, String columnNumber, String error)
    {
        this.workspace = workspace;
        this.workspacePath = workspace.getAbsolutePath();
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.error = error;

        if ( filePath.substring( 0, workspacePath.length() ).equals( workspacePath ) )
        {
            relativePath = filePath.substring( workspacePath.length() );
            if ( relativePath.charAt( 0 ) == '/' )
            {
                relativePath = relativePath.substring( 1 );
            }
        }
        else
        {
            relativePath = filePath;
        }

        this.viewFilename = relativePath + ":" + lineNumber + ":" + columnNumber;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getLineNumber()
    {
        return lineNumber;
    }

    public String getColumnNumber()
    {
        return columnNumber;
    }

    public String getError()
    {
        return error;
    }

    public String getViewFileName()
    {
        return viewFilename;
    }

    public boolean isExcluded()
    {
        return isExcluded;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setIsExcluded(boolean excluded)
    {
        isExcluded = excluded;
    }
}
