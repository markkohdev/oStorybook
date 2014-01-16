#!/bin/sh

echo "Java VM version:"
java -version

echo "starting oStorybook ..."
cd ~/opt/oStorybook
java -Dfile.encoding=UTF-8 -splash:splash.png -XX:MaxPermSize=256m -Xmx400m -jar Storybook.jar $*
echo "done."
