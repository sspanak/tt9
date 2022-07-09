#!/bin/bash

total_tags=`git tag | wc -l`

if [[ $1 == "short" ]];
then
	echo "1.$total_tags"
	exit 0
fi

total_commits=`git rev-list --count HEAD`
last_commit_hash=`git log -n 1 --pretty=format:"%h"`
last_commit_date=`git log -n 1 --date=iso --pretty=format:"%ad"`

json='{
	"build": "'"$total_commits"'",
	"last_commit_date": "'"$last_commit_date"'",
	"last_commit_hash": "'"$last_commit_hash"'",
	"version": "1.'"$total_tags"'"
}'

echo $json
