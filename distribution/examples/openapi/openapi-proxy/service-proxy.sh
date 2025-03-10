#!/bin/bash
homeSet() {
 echo "MEMBRANE_HOME variable is now set"
 CLASSPATH="$MEMBRANE_HOME/conf"
 CLASSPATH="$CLASSPATH:$MEMBRANE_HOME/starter.jar"
 export CLASSPATH
 echo Membrane Router running...
 java  -classpath "$CLASSPATH" -Dlog4j.configurationFile=file:/Users/thomas/tmp/kurs-msa/membrane-service-proxy-5.0.0-SNAPSHOT/examples/openapi/openapi-proxy/log4j2.xml com.predic8.membrane.core.Starter -c   proxies.xml
 
}

terminate() {
	echo "Starting of Membrane Router failed."
	echo "Please execute this script from the MEMBRANE_HOME/examples/proxy directory"
	
}

homeNotSet() {
  echo "MEMBRANE_HOME variable is not set"

  if [ -f  "`pwd`/../../../starter.jar" ]
    then 
    	export MEMBRANE_HOME="`pwd`/../../.."
    	homeSet	
    else
    	terminate    
  fi 
}


if  [ "$MEMBRANE_HOME" ]  
	then homeSet
	else homeNotSet
fi

