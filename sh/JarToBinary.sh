#!/bin/sh

#The purpose of this file is to convert the jar file built by maven into a binary
# so we can use the httpc commands without going through the complicated and long java -cp *** commands

echo "Removing potential broken binary"
rm -r ./../bin/httpc
echo "Creating binary of httpc from jar file"
cat ./httpc.sh ./../target/httpc-1-jar-with-dependencies.jar > httpc && chmod +x httpc
echo "Binary Created!"
echo "Moving Binary to /bin folder..."
mv ./httpc /usr/local/bin