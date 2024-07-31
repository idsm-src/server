#!/bin/bash

if [[ "${BASH_SOURCE}" = */* ]]; then
  cd -- "${BASH_SOURCE%/*}/.."
fi

LOCKFILE=.sachem-chebi-update.lock

if [ -e "$LOCKFILE" ]; then
    echo Lock file exits
    exit 2
fi

touch "$LOCKFILE"

LOG="${HOME}/idsm/log/sachem/chebi-$(date '+%Y-%m-%dT%R').log"
STATUS=0

java -classpath 'classes:lib/*' cz.iocb.load.sachem.ChEBICompoundUpdater >> "$LOG" 2>&1 || { STATUS=1; echo 'ChEBI update error'; }

rm "$LOCKFILE"
exit $STATUS
