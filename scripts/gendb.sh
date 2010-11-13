#!/bin/sh

# reconstructs sqlite database from file fragments

if [ ! -d "scheduler" ]; then
  echo "run script from project root"
  exit 1
fi

DB="db.sqlite"

if [ -f $DB ]; then
  rm $DB
fi

find scheduler/assets/database/database* | xargs cat > $DB

exit 0