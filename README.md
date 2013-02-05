jenkins-pfff
============

# Publish pretty php debugging reports using [pfff](https://github.com/facebook/pfff)


This is a jenkins plugin for [pfff](https://github.com/facebook/pfff). Currently the plugin builds a html report
from the output of the "scheck" program. This plugin does not run the "scheck" program, it uses a file containing the
output of the scheck program generated in the build process.

A compiled version of the plugin is avaliable at [pfff-reports](https://github.com/dpayne/jenkins-pfff/raw/master/pfff-reports.hpi)

## Building from source:
Build with "mvn clean package"

The generated plugin should now be available at "target/pfff-reports.hpi"

## Installing
Upload the plugin to you jenkins server under Jenkins->Manage Jenkins->Manage Plugins. Go to the Advanced tab, then
use the "Upload Plugin" section to upload the plugin.

Next configure your build to run the plugin.

Click "Configure" under your build, then "Add post-build action" then add "Publish pfff results as a report".

![post action build config]
(https://github.com/dpayne/jenkins-pfff/raw/master/readme/PostActionBuildConfig.png)

There are some basic configuration options the most important being the "SCheck log file Path". This is the relative
path to a file containing the output of "scheck". It is also possible to ignore certain warnings/errors or entire
folders so that they won't be included in the generated report.





After running your build again a report will be generated containing all the errors and warning outputted by scheck.

![Scheck report]
(https://github.com/dpayne/jenkins-pfff/raw/master/readme/ScheckReport.png)



The build page contains a short summary of how many errors were detected by scheck

![Build page]
(https://github.com/dpayne/jenkins-pfff/raw/master/readme/BuildPage.png)




Going back to your project page, there should be a small graph containing a chart graphing the result of each build.

![Project page]
(https://github.com/dpayne/jenkins-pfff/raw/master/readme/ProjectPage.png)
