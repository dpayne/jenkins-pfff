package com.applovin.jenkins;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util
{

    private static final String LOG_TAG     = "[PFFF] ";
    private PrintStream         log         = System.out;
    private static int          BUFFER_SIZE = 1 << 14;              // 16384 bytes
    public static byte[]        buffer      = new byte[BUFFER_SIZE];

    public Util()
    {
    }

    public Util(PrintStream log)
    {
        this.log = log;
    }

    public static String readFileAsString(String filePath) throws java.io.IOException
    {
        byte[] buffer = new byte[(int) new File( filePath ).length()];
        BufferedInputStream f = null;
        try
        {
            f = new BufferedInputStream( new FileInputStream( filePath ) );
            f.read( buffer );
        }
        finally
        {
            if ( f != null ) try
            {
                f.close();
            }
            catch (IOException ignored)
            {
            }
        }
        return new String( buffer );
    }

    public static String U2U(String s)
    {
        final Pattern p = Pattern.compile( "\\\\u\\s*([0-9(A-F|a-f)]{4})", Pattern.MULTILINE );
        String res = s;
        Matcher m = p.matcher( res );
        while (m.find())
        {
            res = res.replaceAll( "\\" + m.group( 0 ),
                    Character.toString( (char) Integer.parseInt( m.group( 1 ), 16 ) ) );
        }
        return res;
    }

    public void copyResourcesRecursively(URL originUrl, File destination) throws Exception
    {
        URLConnection urlConnection = originUrl.openConnection();
        if ( urlConnection instanceof JarURLConnection )
        {
            copyJarResourcesRecursively( destination, (JarURLConnection) urlConnection );
        }
        else if ( urlConnection instanceof FileURLConnection )
        {
            FileUtils.copyDirectory( new File( originUrl.getPath() ), destination );
        }
        else
        {
            throw new Exception( "URLConnection[" + urlConnection.getClass().getSimpleName() +
                    "] is not a recognized/implemented connection type." );
        }
    }

    public void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection) throws IOException
    {
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        JarEntry entry;
        while (jarEntries.hasMoreElements())
        {
            entry = jarEntries.nextElement();
            if ( entry.getName().startsWith( jarConnection.getEntryName() ) )
            {
                String fileName = StringUtils.removeStart( entry.getName(), jarConnection.getEntryName() );
                if ( !entry.isDirectory() )
                {
                    InputStream entryInputStream = null;
                    try
                    {
                        entryInputStream = jarFile.getInputStream( entry );
                        copyInputStreamToFile( entryInputStream, new File( destination, fileName ) );
                    }
                    finally
                    {
                        if ( entryInputStream != null )
                        {
                            entryInputStream.close();
                        }
                    }
                }
                else
                {
                }
            }
        }
    }

    public void copyInputStreamToFile(InputStream in, File destFile)
    {
        FileOutputStream out;
        try
        {
            if ( !destFile.getParentFile().exists() )
                destFile.getParentFile().mkdirs();
            if ( !destFile.exists() )
                destFile.createNewFile();
            out = new FileOutputStream( destFile );
        }
        catch (FileNotFoundException e)
        {
            log.println( LOG_TAG + "Exception in copyInputStreamToFile" );
            e.printStackTrace( log );
            return;
        }
        catch (IOException e)
        {
            log.println( LOG_TAG + "Exception in copyInputStreamToFile" );
            e.printStackTrace( log );
            return;
        }
        int len;
        try
        {
            while ((len = in.read( buffer )) != -1)
            {
                out.write( buffer, 0, len );
            }
        }
        catch (IOException e)
        {
            log.println( LOG_TAG + "Exception in copyInputStreamToFile" );
            e.printStackTrace( log );
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                log.println( LOG_TAG + "Exception in copyInputStreamToFile" );
                e.printStackTrace( log );
            }
        }
    }
}
