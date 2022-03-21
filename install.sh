#!/bin/bash
sudo -s
wget https://project-downloads.drogon.net/wiringpi-latest.deb
dpkg -i wiringpi-latest.deb
apt-get install aptitude
apt-get install mc
apt-get install openjdk-8-jdk
apt-get install maven
apt-get install realvnc-vnc-server
apt-get install maria-db
apg-get install phpmyadmin
git clone https://github.com/JozefDropco/heating.git
mvn clean package -DskipTests=true
#zapnut VNC v konfiguracii a one wire
# do rc.local dopisat javu jar java -jar -Dhtml='/home/pi/heating/resources' target/heating-jar-with-dependencies.jar
# do /home/pi/autostart pridat odkaz na chrome
#
