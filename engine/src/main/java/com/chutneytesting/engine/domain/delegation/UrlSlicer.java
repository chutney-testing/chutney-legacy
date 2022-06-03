package com.chutneytesting.engine.domain.delegation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: look at https://github.com/jnr/jnr-netdb to replace ?
public class UrlSlicer {

    private static final String URL_REGEX = "^(?<protocol>.*)://(?<host>[^:/]*)(?<port>:\\d{1,5})?(?<path>/.*)?$";
    private static final String JDBC_ORACLE_REGEX = "(?i)^jdbc:oracle:thin:@.*(\\(HOST=(?<host>.*?)\\)|\\(PORT=(?<port>.*?)\\)(.*?)){2}.*$";

    private static final Map<String, Integer> portByProtocols = new HashMap<>();

    static {
        portByProtocols.put("http", 80);
        portByProtocols.put("https", 443);
        portByProtocols.put("ssh", 22);
        portByProtocols.put("amqp", 5672);
        portByProtocols.put("amqps", 5671);
    }

    private static final Pattern[] patterns = {
        Pattern.compile(URL_REGEX),
        Pattern.compile(JDBC_ORACLE_REGEX)
    };

    public final String host;
    public final int port;

    public UrlSlicer(String url) {
        Matcher urlMatcher = findMatcher(url).orElseThrow(() -> new IllegalArgumentException("Given URL does not match any known pattern: " + url));
        host = urlMatcher.group("host");

        port = Optional.ofNullable(urlMatcher.group("port"))
            .map(s->s.startsWith(":")?s.substring(1):s) // Remove colon if present
            .map(Integer::valueOf) // Map to int if present
            .orElseGet(()->portByProtocols.get(urlMatcher.group("protocol"))); // default to protocol type.
    }

    private static Optional<Matcher> findMatcher(String url) {
        return Arrays.stream(patterns)
            .map(pattern -> pattern.matcher(url))
            .filter(Matcher::find)
            .findFirst();
    }
}
