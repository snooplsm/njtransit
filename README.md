# njtransit

An android interface for the [nj transit](http://www.njtransit.com/) system.

## requirements

[maven](http://maven.apache.org/download.html)

## build

To generate .classpath, .project run `mvn eclipse:eclipse` (see [plugin](http://maven.apache.org/plugins/maven-eclipse-plugin/eclipse-mojo.html)) from the scheduler directory.

To upgrade the rail database extract the zip to the proper provider and run `mvn schedule-maker:make [-P profile name]`

To prepare for a release or to change package structure run `mvn mp:touch -DpackageName=com.njtransit`
After doing this your eclipse files will become outdated and if using an ide everything will become outdated.  Refresh the project, clean it, and refresh again.  

To release run `mvn install -Dkeystore=keylocation -Dstorepass=keystorepass -Dkeypass=keypass`


## known issues

can not package large assets with application, need to split them up into partitions.  we currently use 50KB partitions

njtransit license agreement means we need to download their gtfs updates within 3days.  license does not stipulate that we need to upgrade user app within 3 days.  However, schedules that yield bad information will upset clients.  

## install

This app comes bundled with an sqlite lite store of njtransit train scheduling data. 


To work with the database free form, we recommend using the [lita](http://www.dehats.com/drupal/?q=node/58) sqlite client.

To generate the database, run the script `./scripts/gendb.sh` which will generate a `scheduler.sqlite` file

2010 Ryan Gravener (@notryangravener) & Doug Tangren (@softprops)