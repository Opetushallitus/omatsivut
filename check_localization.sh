#!/bin/bash -e

log()
{
  git --no-pager log --pretty=format:'%h %ad %s (%an)' --date=short  origin/localization-reviewed..origin/master -- src/main/resources/translations
}
diff()
{
  git --no-pager diff  origin/localization-reviewed origin/master src/main/resources/translations
}

git fetch origin
if [ ! -z "`log`" ]; then
  echo "COMMITS FOUND AFTER REVIEW:"
  log
  echo
  diff
  echo
  echo "You can mark these as reviewed with the following command:"
  echo "    git push origin HEAD:localization-reviewed"
  exit 1
else
  echo "no changes after localization review"
fi
