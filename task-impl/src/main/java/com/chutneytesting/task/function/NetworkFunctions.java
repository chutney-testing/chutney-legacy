package com.chutneytesting.task.function;

import static com.chutneytesting.tools.SocketUtils.findAvailableTcpPort;
import static com.chutneytesting.tools.SocketUtils.findAvailableTcpPorts;
import static com.chutneytesting.tools.SocketUtils.findAvailableUdpPort;
import static com.chutneytesting.tools.SocketUtils.findAvailableUdpPorts;
import static java.util.Collections.list;

import com.chutneytesting.task.spi.SpelFunction;
import com.chutneytesting.tools.SocketUtils;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.ThrowingPredicate;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Random;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NetworkFunctions {

    @SpelFunction
    public static int tcpPort() {
        return findAvailableTcpPort();
    }

    @SpelFunction
    public static SortedSet<Integer> tcpPorts(int num) {
        return findAvailableTcpPorts(num);
    }

    @SpelFunction
    public static int tcpPortMin(int minPort) {
        return findAvailableTcpPort(minPort);
    }

    @SpelFunction
    public static int tcpPortMinMax(int minPort, int maxPort) {
        return findAvailableTcpPort(minPort, maxPort);
    }

    @SpelFunction
    public static SortedSet<Integer> tcpPortsMinMax(int num, int minPort, int maxPort) {
        return findAvailableTcpPorts(num, minPort, maxPort);
    }

    @SpelFunction
    public static int tcpPortRandomRange(int range) {
        Integer[] httpPortRange = getRandomPortRange(range);
        return findAvailableTcpPort(httpPortRange[0], httpPortRange[1]);
    }

    @SpelFunction
    public static SortedSet<Integer> tcpPortsRandomRange(int num, int range) {
        Integer[] httpPortRange = getRandomPortRange(range);
        return findAvailableTcpPorts(num, httpPortRange[0], httpPortRange[1]);
    }

    @SpelFunction
    public static int udpPort() {
        return findAvailableUdpPort();
    }

    @SpelFunction
    public static SortedSet<Integer> udpPorts(int num) {
        return findAvailableUdpPorts(num);
    }

    @SpelFunction
    public static int udpPortMin(int minPort) {
        return findAvailableUdpPort(minPort);
    }

    @SpelFunction
    public static int udpPortMinMax(int minPort, int maxPort) {
        return findAvailableUdpPort(minPort, maxPort);
    }

    @SpelFunction
    public static SortedSet<Integer> udpPortsMinMax(int num, int minPort, int maxPort) {
        return findAvailableUdpPorts(num, minPort, maxPort);
    }

    @SpelFunction
    public static int udpPortRandomRange(int range) {
        Integer[] httpPortRange = getRandomPortRange(range);
        return findAvailableUdpPort(httpPortRange[0], httpPortRange[1]);
    }

    @SpelFunction
    public static SortedSet<Integer> udpPortsRandomRange(int num, int range) {
        Integer[] httpPortRange = getRandomPortRange(range);
        return findAvailableUdpPorts(num, httpPortRange[0], httpPortRange[1]);
    }

    @SpelFunction
    public static String randomNetworkMask() {
        StringBuilder networkMaskBuilder = new StringBuilder();
        networkMaskBuilder.append(System.nanoTime() * new Random().nextLong());
        // reverse current timestamp to get the 9 last numbers
        networkMaskBuilder.reverse();
        networkMaskBuilder.delete(9, networkMaskBuilder.length());
        // split into xxx.xxx.xxx
        networkMaskBuilder.insert(6, '.').insert(3, '.');

        // reinit networkMaskBuilder to get fields into range [0,255]
        StringTokenizer stringTokenizer = new StringTokenizer(networkMaskBuilder.toString(), ".");
        networkMaskBuilder = new StringBuilder();
        while (stringTokenizer.hasMoreTokens()) {
            networkMaskBuilder.append(Integer.valueOf(stringTokenizer.nextToken()) % 255);
            networkMaskBuilder.append(".");
        }

        // delete the last "."
        networkMaskBuilder.deleteCharAt(networkMaskBuilder.length() - 1);
        return networkMaskBuilder.toString();
    }

    @SpelFunction
    public static String hostIpMatching(String regex) throws Exception {
        return list(NetworkInterface.getNetworkInterfaces())
            .stream()
            .filter(ThrowingPredicate.toUnchecked(NetworkInterface::isUp))
            .map(ThrowingFunction.toUnchecked(NetworkInterface::getInetAddresses))
            .flatMap(addresses -> list(addresses).stream())
            .flatMap(address -> Stream.of(address.getCanonicalHostName(), address.getHostAddress()))
            .distinct()
            .filter(ip -> matches(regex, ip))
            .findFirst()
            .orElse(InetAddress.getLocalHost().getHostAddress());
    }

    @SpelFunction
    public static String hostIpReaching(String remoteHost) throws Exception {
        // Note : remotePort is not important : No real connection is required here, we only need
        // to resolve routing table
        final int remotePort = 8888;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(remoteHost), remotePort);
            return socket.getLocalAddress().getHostAddress();
        }
    }

    private static Boolean matches(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[]{rangeMin, rangeMin + range};
    }
}
