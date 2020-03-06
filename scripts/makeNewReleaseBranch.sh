#!/bin/sh

export PATH=/bin:/usr/bin:/usr/sbin:/sbin:/usr/local/bin

setup(){
HERE=`pwd`
EXECDIR=$( cd "$( dirname "$0" )" && pwd )
cd $EXECDIR
cd ../../../../
BASEDIR=`pwd`
n=`pwd | sed s#"/"#" "#g | wc -w`
num=`expr $n + 1`
PROJ=`pwd | cut -d"/" -f$num`
NEWVERSION=""
}

chkModuleVersion(){
cd $EXECDIR
[ ! -f ../module_version ] && echo "There is no ../module_version - aborting!" && exit 1
}

noDanglingCommitsAllowed(){
fail=0
git status | tail -1 | grep "nothing to commit" >/dev/null 2>&1 && fail=1
[ $fail -eq 0 ] && git status | grep "nothing added to commit but untracked" >/dev/null 2>&1 && fail=1
[ $fail -eq 0 ] && echo "You have dangling commits - aborting!" && exit 1
}

chkBranchIsMaster(){
notMaster=1
git branch | grep "*" | grep master >/dev/null 2>&1 || notMaster=0 
[ $notMaster -eq 0 ] && echo "You are not on master - we _only_ branch from master!" && exit 1
}

isEverythingPushed(){
git status | grep "Your branch is ahead" > /dev/null 2>&1 && echo "You at least one commit that is not pushed!" && exit 1
}

isTheBranchReallyNew(){
echo
getLatestVersionPlusOne
echo
git branch -a | grep rel-$NEWVERSION >/dev/null 2>&1 && echo "Branch rel-$NEWVERSION already exists!" && echo "Maybe try echo '$NEWVERSION' > ../module_version and then retry?" && exit 1
}

setNewVersion(){
echo $NEWVERSION > ../module_version
git commit -a -m"Creating new release branch : rel-$NEWVERSION"
git push
}

createCheckoutAndTrackTheNewBranch(){
git branch rel-$NEWVERSION
git checkout rel-$NEWVERSION
git push origin rel-$NEWVERSION
git checkout master
git branch -D rel-$NEWVERSION
git checkout -t origin/rel-$NEWVERSION
echo
echo "You are now on the release branch rel-$NEWVERSION"
echo
}

getLatestVersionPlusOne(){
currVersion=`cat ../module_version | cut -d"_" -f2 | cut -d"." -f2 | sed s#" "##g`
newVersion=`expr $currVersion + 1`
mainVersion=`cat ../module_version | cut -d"." -f1 | sed s#" "##g | cut -d"-" -f2 | sed s#" "##g`
prefix=`cat ../module_version | cut -d"-" -f -1 | sed s#" "##g`
newVersionCompl="$prefix-$mainVersion.$newVersion"
echo "New module version release branch is : $newVersionCompl"
NEWVERSION=$newVersionCompl
}

###
#
# MAIN
#
setup
chkModuleVersion
noDanglingCommitsAllowed
chkBranchIsMaster
isEverythingPushed
isTheBranchReallyNew
setNewVersion
createCheckoutAndTrackTheNewBranch
cd $HERE
exit 0
