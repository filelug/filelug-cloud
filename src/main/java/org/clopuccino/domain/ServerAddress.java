package org.clopuccino.domain;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>ServerAddress</code> describes the network addresses of this server.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ServerAddress {

    private Set<String> ipv4 = new HashSet<String>();

    private Set<String> ipv6 = new HashSet<String>();

    public ServerAddress() {}

    public ServerAddress(Set<String> ipv4, Set<String> ipv6) {
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
    }

    public void addIpv4(String addr) throws UnknownHostException {
        if (Inet4Address.class.isInstance(InetAddress.getByName(addr))) {
            ipv4.add(addr);
        } else {
            throw new IllegalArgumentException(addr + " is not an IPv4 address");
        }
    }

    public void addIpv6(String addr) throws UnknownHostException {
        if (Inet6Address.class.isInstance(InetAddress.getByName(addr))) {
            ipv6.add(addr);
        } else {
            throw new IllegalArgumentException(addr + " is not an IPv6 address");
        }
    }

    public void addAddress(String addr) throws UnknownHostException {
        if (Inet6Address.class.isInstance(InetAddress.getByName(addr))) {
            ipv4.add(addr);
        } else {
            addIpv4(addr);
        }
    }

    public Set<String> getIpv4() {
        return ipv4;
    }

    public void setIpv4(Set<String> ipv4) {
        this.ipv4 = ipv4;
    }

    public Set<String> getIpv6() {
        return ipv6;
    }

    public void setIpv6(Set<String> ipv6) {
        this.ipv6 = ipv6;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerAddress{");
        sb.append("ipv4=").append(Arrays.toString(ipv4.toArray()));
        sb.append(", ipv6=").append(Arrays.toString(ipv6.toArray()));
        sb.append('}');
        return sb.toString();
    }
}
