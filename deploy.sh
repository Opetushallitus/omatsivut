#!/usr/bin/env bash

usage()
{
cat << EOF
usage: $0 options

This script deploys the war to environment.

OPTIONS:
   -h      Show this message
   -s      Server address (required)
   -u      Server username (required)
   -k      SSH private key file (required)
   -d      Remote deploy dir (required)
   -c      Remote command to execute on server
EOF
}

SERVER=
USER=
KEYFILE=
DEPLOYDIR=
COMMAND=
while getopts “hs:u:k:d:c:” OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         s)
             SERVER=$OPTARG
             ;;
         u)
             USER=$OPTARG
             ;;
         k)
             KEYFILE=$OPTARG
             ;;
         d)
             DEPLOYDIR=$OPTARG
             ;;
         c)
             COMMAND=$OPTARG
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

if [[ -z $SERVER ]] || [[ -z $USER ]] || [[ -z $KEYFILE ]] || [[ -z $DEPLOYDIR ]] || [[ -z $COMMAND ]]
then
     usage
     exit 1
fi
find target -name *.war -exec cp -v {} omatsivut.war \;
echo scp -i $KEYFILE omatsivut.war $USER@$SERVER:$DEPLOYDIR
echo $COMMAND