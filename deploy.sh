#!/usr/bin/env bash

usage()
{
cat << EOF
usage: $0 options

This script deploys the war to environment.

OPTIONS:
   -h      Show this message
   -s      Server address
   -u      Server username
   -k      SSH private key file
   -d      Remote deploy dir
   -c      Remote command to execute on server
   -v      Verbose
EOF
}

SERVER=
USER=
KEYFILE=
DEPLOYDIR=
COMMAND=
VERBOSE=
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
         v)
             VERBOSE=1
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
ls -al
echo scp -i $KEYFILE omatsivut.war $USER@$SERVER:$DEPLOYDIR