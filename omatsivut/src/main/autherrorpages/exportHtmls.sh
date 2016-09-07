#!/usr/bin/env bash

usage()
{
cat << EOF
usage: $0 options

This script export error page htmls from omatsivut github repository by svn.

OPTIONS:
   -h      Show this message
   -b      branch name (if not given export master)
   -d      output dir (required) WARNING: if exists is overwriten
EOF
}

BRANCH=
DEPLOYDIR=
while getopts “hb:d:” OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         b)
             BRANCH=$OPTARG
             ;;
         d)
             DEPLOYDIR=$OPTARG
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

if [[ -z $DEPLOYDIR ]]
then
     usage
     exit 1
fi
if [[ -n $BRANCH ]]
then
     SVN="svn export --force https://github.com/Opetushallitus/omatsivut/branches/$BRANCH/src/main/autherrorpages/htmls $DEPLOYDIR"
else
     SVN="svn export --force https://github.com/Opetushallitus/omatsivut/trunk/src/main/autherrorpages/htmls $DEPLOYDIR"
fi
echo Executing $SVN
$SVN
