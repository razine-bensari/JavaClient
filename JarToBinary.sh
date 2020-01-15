#!/bin/sh

#The purpose of this file is to convert the jar file built by maven into a binary
# so we can use the httpc commands without going through the complicated and long java -cp *** commands


#The following commands turns the jar into an executable
echo "Removing potential broken binary"
rm -r httpc
echo "Creating binary of httpc from jar file"
echo "#! /usr/local/openjdk-8 java -jar" > httpc
cat ./target/httpc-1.jar >> httpc
chmod +x httpc
echo "Binary Created!"