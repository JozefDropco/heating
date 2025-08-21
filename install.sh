#!/bin/bash
mkdir /home/jodido/.config/autostart
echo """[Desktop entry]
Type=Application
Name=Chrome
icon=chromium-browser
Exec=chromium-browser
""" > /home/jodido/.config/autostart/chrome.desktop

sudo -s
apt-get remove wayvnc
echo "Advanced settings -> Wayland na X11 a inteface options -> OneWire"
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
chmod +x /etc/rc.local
systemctl enable rc-local
/etc/vnc/vncservice start vncserver-x11-serviced

