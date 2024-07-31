#!/bin/bash

if [[ "${BASH_SOURCE}" = */* ]]; then
  cd -- "${BASH_SOURCE%/*}/.."
fi

LOCKFILE=.sachem-chembl-update.lock

if [ -e "$LOCKFILE" ]; then
    echo Lock file exits
    exit 2
fi

touch "$LOCKFILE"

LOG="${HOME}/idsm/log/sachem/chembl-$(date '+%Y-%m-%dT%R').log"
STATUS=0

java -classpath 'classes:lib/*' cz.iocb.load.sachem.ChEMBLCompoundUpdater >> "$LOG" 2>&1 || { STATUS=1; echo 'ChEMBL update error'; }

rm "$LOCKFILE"
exit $STATUS
