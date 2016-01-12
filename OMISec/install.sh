#!/bin/bash
export SCRIPT_DIR=$(cd "$(dirname "$0")"; pwd)
cd src/main
printf "compiling classes..."
javac -d . -cp ".:/java:../../lib/*" java/com/aaltoasia/db/*.java
javac -d . -cp ".:/java:../../lib/*" java/com/aaltoasia/*.java
echo "done"
cd ../..
echo "enter apache tomcat installation path (absolute): "
read tomcat_path
cd $tomcat_path
echo "copying libraries and source files..."
LIBS_DIR="$SCRIPT_DIR/lib/*"
cp $LIBS_DIR "lib/"
WEB_DIR="$SCRIPT_DIR/webclient/*"
cp -r $WEB_DIR "webapps/ROOT/"
CONFIG_DIR="$SCRIPT_DIR/tomcat_config_file/*"
cp $CONFIG_DIR "webapps/ROOT/WEB-INF/"
CLASSES_DIR="$SCRIPT_DIR/src/main/com"
cp -r $CLASSES_DIR "webapps/ROOT/WEB-INF/classes"
echo "done"
