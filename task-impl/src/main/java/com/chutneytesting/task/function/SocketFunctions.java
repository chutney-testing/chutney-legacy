package com.chutneytesting.task.function;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;
import static org.springframework.util.SocketUtils.findAvailableTcpPorts;
import static org.springframework.util.SocketUtils.findAvailableUdpPort;
import static org.springframework.util.SocketUtils.findAvailableUdpPorts;

import com.chutneytesting.task.spi.SpelFunction;
import java.util.Random;
import java.util.SortedSet;
import org.springframework.util.SocketUtils;

public class SocketFunctions {

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

    private static Integer[] getRandomPortRange(int maxRangesNumber) {
        int range = (SocketUtils.PORT_RANGE_MAX - SocketUtils.PORT_RANGE_MIN) / maxRangesNumber;
        int rangeMin = SocketUtils.PORT_RANGE_MIN + (new Random().nextInt(maxRangesNumber) * range);
        return new Integer[]{rangeMin, rangeMin + range};
    }
}
