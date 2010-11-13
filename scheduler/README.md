# njtransit

An android interface for the [nj transit](http://www.njtransit.com/) system.

## install

This app comes bundled with an sqlite lite store of njtransit train scheduling data. 

To work with the database free form, we recommend using the [lita](http://www.dehats.com/drupal/?q=node/58) sqlite client.

To generate the database, run the script `./scripts/gendb.sh` which will generate a `scheduler.sqlite` file

2010 Ryan Gravener (@notryangravener) & Doug Tangren (@softprops)