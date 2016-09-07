#!/bin/bash -e
echo "Removing fi.vm.sade directories and local directories from Ivy2 and Maven repositories."
rm -rf ~/.ivy2/local ~/.ivy2/cache/fi.vm.sade*
rm -rf ~/.m2/repository/fi/vm/sade