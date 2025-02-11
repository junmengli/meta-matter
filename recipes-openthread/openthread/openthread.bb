PN = "openthread"
SUMMARY = "OTBR on i.MX boards"
DESCRIPTION = "OTBR applications"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=543b6fe90ec5901a683320a36390c65f"

SRC_URI = "gitsm://github.com/openthread/openthread.git;branch=main;protocol=https"

SRCREV = "2ce3d3bf0218566484be2e9943b95c755cefebe3"

S = "${WORKDIR}/git"
#FILES_${PN} += "lib/systemd"
#FILES_${PN} += "usr/share"

DEPENDS += " avahi boost "
RDEPENDS_${PN} += " libavahi-client "

TARGET_CFLAGS += " -Wno-error "
def get_rcp_bus(d):
    for arg in (d.getVar('MACHINE') or '').split():
        if arg == "imx93evk":
            return '-DOT_POSIX_CONFIG_RCP_BUS=SPI'
    for arg in (d.getVar('OT_RCP_BUS') or '').split():
        if arg == "SPI":
            return '-DOT_POSIX_CONFIG_RCP_BUS=SPI'
        else:
            return '-DOT_POSIX_CONFIG_RCP_BUS=UART'
    return ''

inherit cmake
EXTRA_OECMAKE = "-GNinja -DCMAKE_EXPORT_COMPILE_COMMANDS=ON -DOT_COMPILE_WARNING_AS_ERROR=OFF -DOT_PLATFORM=posix -DOT_SLAAC=ON -DOT_BORDER_AGENT=ON -DOT_BORDER_ROUTER=ON -DOT_COAP=ON -DOT_COAP_BLOCK=ON -DOT_COAP_OBSERVE=ON -DOT_COAPS=ON -DOT_COMMISSIONER=ON -DOT_CHANNEL_MANAGER=ON -DOT_CHANNEL_MONITOR=ON -DOT_CHILD_SUPERVISION=ON -DOT_DATASET_UPDATER=ON -DOT_DHCP6_CLIENT=ON -DOT_DHCP6_SERVER=ON -DOT_DIAGNOSTIC=ON -DOT_DNS_CLIENT=ON -DOT_ECDSA=ON -DOT_IP6_FRAGM=ON -DOT_JAM_DETECTION=ON -DOT_JOINER=ON -DOT_LEGACY=ON -DOT_MAC_FILTER=ON -DOT_MTD_NETDIAG=ON -DOT_NEIGHBOR_DISCOVERY_AGENT=ON -DOT_PING_SENDER=ON -DOT_REFERENCE_DEVICE=ON -DOT_SERVICE=ON -DOT_SNTP_CLIENT=ON -DOT_SRP_CLIENT=ON -DOT_COVERAGE=OFF -DOT_LOG_LEVEL_DYNAMIC=ON -DOT_RCP_RESTORATION_MAX_COUNT=2 -DOT_LOG_OUTPUT=PLATFORM_DEFINED -DOT_POSIX_MAX_POWER_TABLE=ON -DOT_DAEMON=ON -DOT_THREAD_VERSION=1.3 -DCMAKE_BUILD_TYPE=Release"
EXTRA_OECMAKE += "${@get_rcp_bus(d)}"

do_install() {
	install -d -m 755 ${D}${bindir}
    install ${WORKDIR}/build/src/posix/ot-daemon ${D}${bindir}
    install ${WORKDIR}/build/src/posix/ot-ctl ${D}${bindir}/ot-client-ctl
}
