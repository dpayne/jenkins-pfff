package com.applovin.jenkins;

import org.junit.Test;

import java.io.File;

/**
 * User: dpayne
 * Date: 2/1/13
 * Time: 2:31 PM
 */
public class SCheckTest
{
    @Test
    public void test() {
        ReportBuilder reportBuilder = new ReportBuilder(new File("/tmp/pfff-jenkins/"), new File("/usr/local/adserver"),
          "", "1", "AdServer", 13, new File("/Users/dpayne/Workspace/jenkins-pfff/src/test/resources/scheck_log"),
          "Unused Param variable,Unused LocalExn variable",
          "Tools,DebugTools,Config", System.out);
        reportBuilder.generateReports();
        System.out.println("Status: " + reportBuilder.getBuildStatus());
    }
}
