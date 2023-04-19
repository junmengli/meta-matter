# Running Matter Binaries on i.MX MPU platforms

This document describes how to use the Matter binaries on the i.MX MPU platforms. It shows how to run Matter binaries with OTBR on the i.MX MPU platform and how to commission with the K32W DK6 lighting application, then it shows how to run i.MX Matter binaries for ble-wifi and onnetwork commissioning.

 [**Hardware requirement**](#hardware-requirement)

 [**Software requirement**](#software-requirement)

 [**Running Matter binaries with OTBR on the i.MX MPU platform**](#matter-binaries-with-otbr)

 [**Running other Matter binaries on the i.MX MPU platform**](#other-matter-binaries)

<a name="hardware-requirement"></a>

## Hardware requirement

- i.MX93 EVK + IW612(WiFi-BT-Thread tri-radio single-chip module)  → role: Matter controller or Matter end device

- i.MX8M Mini EVK + 88W8987(WiFi-BT combo module)  → role: Matter controller or Matter end device

- i.MX6ULL EVK + 88W8987(WiFi-BT combo module)  → role: Matter controller or Matter end device

   For more information on the details of the i.MX MPU Matter platforms, please refer to [NXP MPU Matter platform](https://www.nxp.com/design/development-boards/i-mx-evaluation-and-development-boards/mpu-linux-hosted-matter-development-platform:MPU-LINUX-MATTER-DEV-PLATFORM).

- K32W RCP → role: OpenThread RCP module
    
   A K32W061 dongle is required to run the [OpenThread Radio Co-Processor](https://openthread.io/platforms/co-processor) firmware.

- K32W DK6 board → role: Matter lighting-app device

    More information about the details of the K32W DK6 can be found on the [NXP official website](https://www.nxp.com/products/wireless/multiprotocol-mcus/end-node-matter-with-thread-development-platform:END-NODE-MATTER-THREAD).

- Linux host computer

- Wi-Fi access point supporting IPv6 and IPv6 DHCP service

<a name="software-requirement"></a>

## Software requirement

- You can refer to the [Yocto images and binaries build guide](../../README.md) to build Yocto images and binaries for i.MX Matter devices. 
- You can refer to [K32W lighting example application guide](https://github.com/project-chip/connectedhomeip/tree/master/examples/lighting-app/nxp/k32w/k32w0/README.md) to build the K32W DK6 firmware and lighting-app firmware and set up the K32W lighting example application.

<a name="matter-binaries-with-otbr"></a>

## Running Matter binaries with OTBR on the i.MX MPU platform

For devices that support the Thread protocol, this guide uses the NXP K32W running lighting application as an example. The i.MX MPU platform can perform Matter networking with end devices using OTBR. Matter with OTBR on i.MX devices network topology diagram as shown below.

![imx93-otbr](../images/imx93-otbr.png)

Figure Matter with OTBR network topology diagram for i.MX93 EVK

![imx8mm_imx6ull-otbr.png](../images/imx8mm_imx6ull-otbr.png)

Figure Matter with OTBR network topology diagram for i.MX8M Mini EVK or i.MX6ULL EVK 

The commissioning process consists of the following main stages:

- Setup OTBR on i.MX MPU platform
- Configure OpenThread Network
- Factory reset lighting application on K32W DK6
- Commissioning lighting-app on i.MX controller
- Control lighting application on i.MX controller

### Setup OTBR on i.MX MPU platform

To set up OTBR on the i.MX MPU platform, you need to do three steps:

step1. Sync with the current time

    $ date -s "2023-03-23 23:23"

step2. Save the Wi-Fi SSID and password to a file

    $ wpa_passphrase ${SSID} ${PASSWORD} > wifiap.conf

Step 3. Connecting to the Wi-Fi AP, Enabling BT, and Setting Up OTBR on i.MX MPU platform

#### For i.MX93 EVK + IW612 platform

        、、、
        ifconfig eth0 down
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        wpa_supplicant -d -B -i mlan0 -c ./wifiap.conf
        sleep 5
        udhcpc -i mlan0 
        hciattach /dev/ttyLP4 any 115200 flow
        sleep 1
        hciconfig hci0 up
        hcitool -i hci0 cmd 0x3f 0x0009 0xc0 0xc6 0x2d 0x00
        killall hciattach
        hciattach /dev/ttyLP4 any -s 3000000 3000000 flow
        hciconfig hci0 up
        sleep 1
        echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
        echo 1 > /proc/sys/net/ipv4/ip_forward
        echo 2 > /proc/sys/net/ipv6/conf/all/accept_ra
        ln -sf /usr/sbin/xtables-nft-multi /usr/sbin/ip6tables
        ipset create -exist otbr-ingress-deny-src hash:net family inet6
        ipset create -exist otbr-ingress-deny-src-swap hash:net family inet6
        ipset create -exist otbr-ingress-allow-dst hash:net family inet6
        ipset create -exist otbr-ingress-allow-dst-swap hash:net family inet6
        sleep 1
        gpioset gpiochip6 0=1
        otbr-agent -I wpan0 -B mlan0 'spinel+spi:///dev/spidev0.0?gpio-reset-device=/dev/$ gpiochip6&gpio-int-device=/dev/gpiochip4&gpio-int-line=10&gpio-reset-line=1&
        spi-mode=0&spi-speed=1000000&spi-reset-delay=0' &
        sleep 2
        iptables -A FORWARD -i mlan0 -o wpan0 -j ACCEPT
        iptables -A FORWARD -i wpan0 -o mlan0 -j ACCEPT
        otbr-web &
        、、、

#### For i.MX8M Mini EVK + 88W8987 + K32W platform

        、、、
        ifconfig eth0 down
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        wpa_supplicant -d -B -i mlan0 -c ./wifiap.conf
        sleep 5
        udhcpc -i mlan0
        /usr/libexec/bluetooth/bluetoothd &
        hciattach /dev/ttymxc0 any 115200 flow
        hciconfig hci0 up
        echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
        echo 1 > /proc/sys/net/ipv4/ip_forward
        echo 2 > /proc/sys/net/ipv6/conf/all/accept_ra
        ln -sf /usr/sbin/xtables-nft-multi /usr/sbin/ip6tables
        ipset create -exist otbr-ingress-deny-src hash:net family inet6
        ipset create -exist otbr-ingress-deny-src-swap hash:net family inet6
        ipset create -exist otbr-ingress-allow-dst hash:net family inet6
        ipset create -exist otbr-ingress-allow-dst-swap hash:net family inet6
        sleep 1
        otbr-agent -I wpan0 -B mlan0  'spinel+hdlc+uart:///dev/ttyUSB0?uart-baudrate=1000000' -v -d 5 &
        sleep 2
        iptables -A FORWARD -i mlan0 -o wpan0 -j ACCEPT
        iptables -A FORWARD -i wpan0 -o mlan0 -j ACCEPT
        otbr-web &
        、、、

#### For i.MX6ULL EVK + 88W8987 + K32W platform

For i.MX6ULL, it is mandatory to change fdt_file to setup WiFi and BT. You must enter uboot mode and run follow cmds, then reboot the board.

    => print fdt_file
    fdt_file=undefined
    => fatls mmc 1
        36171   imx6ull-14x14-evk-btwifi-sdio3_0.dtb
        36035   imx6ull-14x14-evk-btwifi.dtb
        35839   imx6ull-14x14-evk-emmc.dtb
        36249   imx6ull-14x14-evk-gpmi-weim.dtb
        35747   imx6ull-14x14-evk.dtb
        354120   tee.bin
        354184   uTee-6ullevk
        9656568   zImage

    8 file(s), 0 dir(s)

    => setenv fdt_file imx6ull-14x14-evk-btwifi-sdio3_0.dtb
    => saveenv
    Saving Environment to MMC... Writing to MMC(1)... OK

    #make sure fdt_file have been changed
    => print fdt_file
    fdt_file=imx6ull-14x14-evk-btwifi-sdio3_0.dtb
 
The i.MX6ULL OTBR setup commands are similar to the i.MX8M Mini commands, the only difference is hciattanch command.

    #for i.MX8M Mini
    $ hciattach /dev/ttymxc0 any 115200 flow
    #for i.MX6ULL
    $ hciattach /dev/ttymxc1 any 115200 flow

*Note: if "$ ifconfig wpan0" can find the wpan0 as shown bellow, the otbr-agent setup succesefully.*

    $ ifconfig wpan0
    wpan0: flags=4240<POINTOPOINT,NOARP,MULTICAST>  mtu 1280
    unspec 00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00  txqueuelen 500  (UNSPEC)
    RX packets 0  bytes 0 (0.0 B)
    RX errors 0  dropped 2  overruns 0  frame 0
    TX packets 0  bytes 0 (0.0 B)
    TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0 

### Configure OpenThread Network

You can form the Openthread network manually by following steps:
    
    $ ot-ctl dataset init new
    $ ot-ctl dataset commit active
    $ ot-ctl ifconfig up
    $ ot-ctl thread start

Then, you should get thread network credentials information.

    $ ot-ctl dataset active -x
    # Then you will get a really long blob like "0e080000000000010000000300001035060004001fffe00208d625374d9c65c2a30708fd57eb72a74fa52505108a177ca3b66becf3bbe2149eb3d135c8030f4f70656e5468726561642d656338350102ec85041044ac05395e78940b72c1df1e6ad02a120c0402a0f7f8"

### Factory reset lighting application on K32W DK6 

Step1. Press the SW2 button on OM15802, after 6 seconds, the device will reset its persistent configuration and initiate a reboot.

Step2. Toggle reset.

Step3. Press the USERINTERFACE button to start the BLE Advertising, which is mandatory for device commissioning.

### Commissioning lighting-app on i.MX controller

Commissioning command like bellow.
    
    # ble-thread pairing
    $ chip-tool pairing ble-thread 8888  hex:0e080000000000010000000300001035060004001fffe00208d625374d9c65c2a30708fd57eb72a74fa52505108a177ca3b66becf3bbe2149eb3d135c8030f4f70656e5468726561642d656338350102ec85041044ac05395e78940b72c1df1e6ad02a120c0402a0f7f8 20202021 3840

The parameter after "ble-thread" is node-id (here it is 8888, it can be any positive integer starting from 1) that should be set uniquely for each Matter device that joined the network, the last but one parameter is pin code (here it is fixed to 20202021), the last parameter is discriminator (here it is fixed to 3840).

If there is a **“Device commissioning completed with success”** message in the controller log, it means that the Matter device has successfully joined the network.

### Control lighting application on i.MX controller

    # control the lighting on/off
    $ chip-tool onoff toggle 8888 1

    # read the lighting on-off status
    $ chip-tool onoff read on-off 8888 1

## Running other Matter binaries on i.MX MPU platform

For example applications running on the i.MX MPU platform, you can test the applications through two commissioning methods. In these two commissioning methods, it is necessary to use the i.MX platform running chip-tool as the controller device and another i.MX platform as the end device.

### Run example applications with ble-wifi commissioning method

The network topology diagram by using ble-wifi commissioning is shown below.

 <img src="../images/ble-wifi.png" width = "500" alt="图片名称" align=center />


![ble-wifi](../images/ble-wifi.png " =100x" )

Figure network topology diagram for i.MX93 EVK

For ble-wifi commissioning method. The controller device need setup BLE and connect to a Wi-Fi AP, the end device need setup BLE.

The commissioning process consists of the following main stages:

- Set up BLE and connecte to a wifi AP on controller device
- Load Wi-Fi/BT firmware and set up BLE on the end device
- Run the example application on the end device
- Commisioning and control the end devices on the controller device

#### Set up BLE and connecte to a wifi AP on controller device

step1. Sync with current time

    $ date -s "2023-03-23 23:23"

step2. Save Wi-Fi SSID and Password to a file.

    $ wpa_passphrase ${SSID} ${PASSWORD} > wifiap.conf

step3. setup BLE and connectd to a WiFi AP.

For i.MX93 EVK

        、、、
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        wpa_supplicant -d -B -i mlan0 -c ./wifiap.conf
        sleep 5
        udhcpc -i mlan0
        hciattach /dev/ttyLP4 any 115200 flow
        sleep 1
        hciconfig hci0 up
        hcitool -i hci0 cmd 0x3f 0x0009 0xc0 0xc6 0x2d 0x00
        killall hciattach
        hciattach /dev/ttyLP4 any -s 3000000 3000000 flow
        sleep 1
        hciconfig hci0 up
        、、、

For i.MX8M Mini EVK

        、、、
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        wpa_supplicant -d -B -i mlan0 -c ./wifiap.conf
        sleep 5
        udhcpc -i mlan0
        /usr/libexec/bluetooth/bluetoothd &
        hciattach /dev/ttymxc0 any 115200 flow
        sleep 1
        hciconfig hci0 up
        、、、

For the i.MX6ULL EVK, the setup commands are similar to the i.MX8M Mini commands. The only difference is the hciattanch command.

    # for i.MX8M Mini EVK
    $ hciattach /dev/ttymxc0 any 115200 flow
    # for i.MX6ULL EVK
    $ hciattach /dev/ttymxc1 any 115200 flow

#### Load Wi-Fi/BT firmware and set up BLE on the end device

step1. Sync with current time

    $ date -s "2023-03-23 23:23"

step2. Load Wi-Fi/BT firmware and set up BLE 

For i.MX93 EVK

        、、、
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        hciattach /dev/ttyLP4 any 115200 flow
        sleep 1
        hciconfig hci0 up
        hcitool -i hci0 cmd 0x3f 0x0009 0xc0 0xc6 0x2d 0x00
        killall hciattach
        hciattach /dev/ttyLP4 any -s 3000000 3000000 flow
        sleep 1
        hciconfig hci0 up
        resolvconf -d mlan0.udhcpc
        ifconfig mlan0 192.168.1.1
        、、、

For i.MX8M Mini EVK

        、、、
        modprobe moal mod_para=nxp/wifi_mod_para.conf
        sleep 5
        udhcpc -i mlan0
        /usr/libexec/bluetooth/bluetoothd &
        hciattach /dev/ttymxc0 any 115200 flow
        sleep 1
        hciconfig hci0 up
        resolvconf -d mlan0.udhcpc
        ifconfig mlan0 192.168.1.1
        、、、

For i.MX6ULL EVK, replace command "hciattach /dev/ttymxc0 any 115200 flow" with "hciattach /dev/ttymxc1 any 115200 flow" in i.MX8M Mini EVK.
 
#### Run the example application on the end device

After setting up the network on both side platforms, run the example application on the end device.

    # to run chip-lighting-app 
    $ chip-lighting-app --wifi --ble-device 0

    # to run chip-all-clusters-app
    $ chip-all-clusters-app --wifi --ble-device 0

    # to run nxp-thermostat-app
    $ nxp-thermostat-app --wifi --ble-device 0

    # to run nxp-thermostat-app
    $ chip-bridge-app --wifi --ble-device 0

#### Finally, commissioning  and control the end devices on the controller device 

    # commissioning with end devices
    $ chip-tool pairing ble-wifi 8888 {AP_SSID} {AP_password} 20202021 3840

    # control the lighting app / chip-all-clusters-app
    $ chip-tool onoff toggle 8888 1
    $ chip-tool onoff read on-off 8888 1

    $ chip-tool thermostat read local-temperature 8888 1
    
    # to test chip-bridge-app
    $ chip-tool actions read setup-url 8888 1
    $ chip-tool actions read endpoint-lists 8888 1
    $ chip-tool actions read action-list 8888 1
    $ chip-tool actions instant-action 0x1001 8888 1   #the room 1 LED1 LED2 will be ON on the bridge end.

Currently, applications with trusty are supported on the i.MX8M Mini EVK, such as chip-tool-trusty, chip-lighting-app-trusty, and nxp-thermostat-app-trusty. Before running these example applications, you need to execute the following commands to enable the secure storage service (only need to execute once after initial boot), and the the following commissioning  steps are consistent with the above.

    $ systemctl enable storageproxyd
    $ systemctl start storageproxyd

##  Run example applications with onnetwork commissioning method

To test onnetwork, two devices need to connecte to the same Wi-Fi AP or connect to the same local area network. Taking the case of connecting to the same Wi-Fi AP as an example, the network topology diagram is shown below.

![onnetwork](../images/onnetwork.png)

First, you must connect the i.MX device that is playing as controller device to a Wi-Fi AP using the following commands.

    wpa_passphrase ${SSID} ${PASSWORD} > wifiap.conf

    modprobe moal mod_para=nxp/wifi_mod_para.conf
    wpa_supplicant -d -B -i mlan0 -c ./wifiap.conf
    udhcpc -i mlan0

Then, run example applications on another i.MX device that plays as the end device.

    # to run chip-lighting-app 
    $ chip-lighting-app

    # to run chip-all-clusters-app
    $ chip-all-clusters-app

    # to run nxp-thermostat-app
    $ nxp-thermostat-app

    # to run nxp-thermostat-app
    $ chip-bridge-app

Final, commissioning and control the end device on the controller device. 

    # commissioning with end devices
    $ chip-tool pairing onnetwork 8888 20202021

    # controll command same as ble-wifi method

To run applications with trusty on the i.MX8M Mini EVK, 
