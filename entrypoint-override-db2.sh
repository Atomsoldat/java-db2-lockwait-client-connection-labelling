#!/bin/sh
# This script is really hacky and we should figure out how to correctly use the default entrypoint
# normally, the entrypoint is  /var/db2_setup/lib/setup_db2_instance.sh
# this script rips that one off liberally
# the reason why we wrote our own script is because the default entrypoint seems to not notice that the DB is initialised

# source default vars and functions
source ${SETUPDIR?}/include/db2_constants
source ${SETUPDIR?}/include/db2_common_functions

# if we don't do this, the db2 upgrade part of the default entrypoint will fail
# seems to be a problem that others have come across too
# https://community.ibm.com/community/user/discussion/121-container-community-edition-docker-start-fails-dbi20187e
if [ -f ${SETUP_COMPLETE?} ]; then
  echo "Database has already been initialised, fixing permissions"
  chown root:db2iadm1 /database/config/db2inst1/sqllib/adm/fencedid
fi

# run default entrypoint
.  /var/db2_setup/lib/setup_db2_instance.sh



