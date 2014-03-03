#/bin/bash
echo "To use: #bash ./docs/change_ip.sh [previous_ip]"

ip=`ifconfig | grep 192 | awk '{print $2}' | head -1`
files=`grep -r '192.168.' src res | awk '{print $1}' | sed 's/://g' | uniq`
for file in $files;
do
  echo "Replacing $1 with $ip in $file"
  perl -e "s/$1/$ip/g" -p -i $file
done
