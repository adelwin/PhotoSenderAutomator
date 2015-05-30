#!/bin/sh
#############################################
#
#
#
#
#############################################

function log {
        LEVEL=${1}
        shift
        MESSAGE=${*}
        DATETIME=`date +%Y-%m-%d-%H:%M:%S`
        printf "[%7s][%s] - %s\n" " ${LEVEL}" "${DATETIME}" "${MESSAGE}"
}

log "INFO" "Starting Function"
log "INFO" "building classpath"

export LOCAL_CLASSPATH=.
export LOCAL_CLASSPATH=${LOCAL_CLASSPATH}":"./cfg

log "INFO" "iterating jar files in lib folder"

for FILES in ./lib/*.jar
do
        export LOCAL_CLASSPATH=${LOCAL_CLASSPATH}":"${FILES}
done

log "INFO" $LOCAL_CLASSPATH

log "INFO" "running tools"
java -cp ${LOCAL_CLASSPATH} com.si.diamond.tools.photoSender.App
