#!/data/data/com.termux/files/usr/bin/bash
cd ~/../usr
find . -type l | while read -r symlink; do
    target=$(readlink "$symlink")
    echo "$target←$symlink" >> ~/SYMLINKS.txt
done
#rm -rf `find ~/.. -type l`