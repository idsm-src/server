#!/bin/bash

if [[ "${BASH_SOURCE}" = */* ]]; then
  cd -- "${BASH_SOURCE%/*}/.."
fi

LOCKFILE=.sachem-pubchem-update.lock

if [ -e "$LOCKFILE" ]; then
    echo Lock file exits
    exit 2
fi

touch "$LOCKFILE"

LOG="${HOME}/idsm/log/sachem/pubchem-$(date '+%Y-%m-%dT%R').log"
STATUS=0

java -classpath 'classes:lib/*' cz.iocb.load.sachem.PubChemCompoundUpdater >> "$LOG" 2>&1 || { STATUS=1; echo 'PubChem update error'; }

rm "$LOCKFILE"
exit $STATUS
