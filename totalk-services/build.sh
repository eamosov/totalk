#!/bin/sh

rm cache.properties ; ant -f build-thrift.xml  -lib ~/.m2/repository/org/codehaus/groovy/groovy-all/2.3.0/groovy-all-2.3.0.jar
