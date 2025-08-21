#!/bin/bash
sudo -s
echo "Advanced settings -> Wayland na X11"
raspi-config
wget https://github.com/WiringPi/WiringPi/releases/download/3.16/wiringpi_3.16_armhf.deb
dpkg -i wiringpi_3.16_armhf.deb
apt-get install aptitude
apt-get install mc
apt-get install openjdk-8-jdk
apt-get install maven
apt-get install realvnc-vnc-server
apt-get install mariadb-server
apt-get install phpmyadmin
git clone https://github.com/JozefDropco/heating.git
mvn clean package -DskipTests=true
#zapnut VNC v konfiguracii a one wire
# do rc.local dopisat javu jar java -jar -Dhtml='/home/pi/heating/resources' target/heating-jar-with-dependencies.jar
# do /home/pi/autostart pridat odkaz na chrome
#
echo """# /etc/systemd/system/rc-local.service
[Unit]
 Description=/etc/rc.local Compatibility
 ConditionPathExists=/etc/rc.local

[Service]
 Type=forking
 ExecStart=/etc/rc.local start
 TimeoutSec=0
 StandardOutput=tty
 RemainAfterExit=yes
 SysVStartPriority=99

[Install]
 WantedBy=multi-user.target""" > /etc/systemd/system/rc-local.service

echo """
java  -Dhtml='/home/jodido/heating/resources' -jar /home/jodido/heating/target/heating-jar-with-dependencies.jar --heating --solar & > /var/log/heating 
exit 0
""" >/etc/rc.local
sudo chmod +x /etc/rc.local
sudo systemctl enable rc-local
