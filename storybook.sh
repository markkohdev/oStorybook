#!/bin/sh

echo "starting Storybook ..."
java -Dfile.encoding=UTF-8 -XX:MaxPermSize=256m -Xmx300m -jar Sb5.jar $*
echo "done."
