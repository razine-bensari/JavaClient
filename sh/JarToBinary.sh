#!/bin/sh

#The purpose of this file is to convert the jar file built by maven into a binary
# so we can use the httpc and httpfs commands without going through the complicated and long java -cp *** commands

echo "Creating binary of httpc and httpfs from jar files"
cat ./httpc.sh ./../httpc/target/httpc-1-jar-with-dependencies.jar > httpc && chmod +x httpc
cat ./httpfs.sh ./../httpfs/target/httpfs-1-jar-with-dependencies.jar > httpfs && chmod +x httpfs
echo "Binaries Created!"
echo "Moving Binaries to /bin folder..."
mv ./httpc /usr/local/bin
mv ./httpfs /usr/local/bin