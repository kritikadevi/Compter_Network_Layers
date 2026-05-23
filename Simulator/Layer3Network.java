package Simulator;

import java.util.*;

// --- LAYER 3: NETWORK LAYER ---
public class Layer3Network {
    public String srcIP, dstIP;
    public int ttl = 64;
    public Layer4Transport encapsulatedSegment;

    public Layer3Network(String srcIP, String dstIP, Layer4Transport segment) {
        this.srcIP = srcIP;
        this.dstIP = dstIP;
        this.encapsulatedSegment = segment;
    }

    @Override
    public String toString() {
        return "[IP Packet: Src=" + srcIP + ", Dst=" + dstIP + ", TTL=" + ttl + "]";
    }
}

class ARPCacheEngine {
    private final Map<String, String> translationMap = new HashMap<>();

    public synchronized void mapAddress(String ip, String mac) { translationMap.put(ip, mac); }
    public synchronized String getMacMapping(String ip) {
        if (translationMap.containsKey(ip)) return translationMap.get(ip);
        String dynamicMAC = "MAC-" + Math.abs(ip.hashCode() % 10000);
        translationMap.put(ip, dynamicMAC);
        return dynamicMAC;
    }
}

class MapRouteEntry {
    public String networkID;
    public int maskPrefix;
    public String hopGatewayIP;
    public String localInterface;
    public int cost;

    public MapRouteEntry(String networkID, int maskPrefix, String hopGatewayIP, String localInterface) {
        this(networkID, maskPrefix, hopGatewayIP, localInterface, 1);
    }

    public MapRouteEntry(String networkID, int maskPrefix, String hopGatewayIP, String localInterface, int cost) {
        this.networkID = networkID;
        this.maskPrefix = maskPrefix;
        this.hopGatewayIP = hopGatewayIP;
        this.localInterface = localInterface;
        this.cost = cost;
    }
}

class Router {
    public String routerName;
    public Map<String, String> interfacesMap = new LinkedHashMap<>();
    public List<MapRouteEntry> subnetRoutingTable = new ArrayList<>();
    public List<Router> dynamicRipAdjacency = new ArrayList<>();
    public ARPCacheEngine arpCache = new ARPCacheEngine();

    public Router(String name) {
        this.routerName = name;
    }

    public void buildInterface(String iface, String ip) {
        interfacesMap.put(iface, ip);
        System.out.println("  [L3 - ROUTER / " + routerName + "] Interface " + iface + " configured with IP " + ip);
    }

    public void insertStaticRoute(String network, int prefix, String nextHop, String iface) {
        subnetRoutingTable.add(new MapRouteEntry(network, prefix, nextHop, iface));
        System.out.println("  [L3 - ROUTER / " + routerName + "] Static route " + network + "/" + prefix + " via " + nextHop + " dev " + iface);
    }

    public void addRipNeighbor(Router neighbor) {
        dynamicRipAdjacency.add(neighbor);
    }

    public void distributeRipMessages() {
        System.out.println("  [L3 - RIP / " + routerName + "] Sharing distance-vector routes with neighbors.");
        for (Router neighbor : dynamicRipAdjacency) {
            for (MapRouteEntry route : subnetRoutingTable) {
                if (!neighbor.hasRoute(route.networkID, route.maskPrefix)) {
                    String nextHop = interfacesMap.values().stream().findFirst().orElse("direct");
                    neighbor.subnetRoutingTable.add(new MapRouteEntry(route.networkID, route.maskPrefix, nextHop, "rip", route.cost + 1));
                    System.out.println("  [L3 - RIP] " + neighbor.routerName + " learned " + route.networkID + "/" + route.maskPrefix + " from " + routerName);
                }
            }
        }
    }

    public void runOspfShortestPath() {
        subnetRoutingTable.sort(Comparator.comparingInt(route -> route.cost));
        System.out.println("  [L3 - OSPF / " + routerName + "] Shortest-path calculation complete. Lower cost routes preferred.");
    }

    public Layer3Network forwardL3Packet(Layer3Network packet) {
        System.out.println("  [L3 - ROUTER / " + routerName + "] Routing packet: " + packet);
        packet.ttl--;
        if (packet.ttl <= 0) {
            System.out.println("  [L3 - ROUTER / " + routerName + "] TTL expired. Packet dropped.");
            return null;
        }

        MapRouteEntry bestRoute = longestPrefixMatch(packet.dstIP);
        if (bestRoute == null) {
            System.out.println("  [L3 - ROUTER / " + routerName + "] No route found for " + packet.dstIP + ". Packet dropped.");
            return null;
        }

        String nextHopMac = arpCache.getMacMapping(bestRoute.hopGatewayIP.equals("direct") ? packet.dstIP : bestRoute.hopGatewayIP);
        System.out.println("  [L3 - ROUTER / " + routerName + "] Longest prefix match: " + bestRoute.networkID + "/" + bestRoute.maskPrefix);
        System.out.println("  [L3 - ROUTER / " + routerName + "] Next hop " + bestRoute.hopGatewayIP + " on " + bestRoute.localInterface + " resolved by ARP to " + nextHopMac);
        return packet;
    }

    public void printRoutingTable() {
        System.out.println("  [L3 - ROUTER / " + routerName + "] Routing Table");
        System.out.printf("    %-16s %-7s %-16s %-8s %-5s%n", "Network", "Prefix", "Next Hop", "Iface", "Cost");
        for (MapRouteEntry route : subnetRoutingTable) {
            System.out.printf("    %-16s /%-6d %-16s %-8s %-5d%n", route.networkID, route.maskPrefix, route.hopGatewayIP, route.localInterface, route.cost);
        }
    }

    private boolean hasRoute(String network, int prefix) {
        for (MapRouteEntry route : subnetRoutingTable) {
            if (route.networkID.equals(network) && route.maskPrefix == prefix) return true;
        }
        return false;
    }

    private MapRouteEntry longestPrefixMatch(String ip) {
        MapRouteEntry bestRoute = null;
        for (MapRouteEntry route : subnetRoutingTable) {
            if (matches(ip, route.networkID, route.maskPrefix)
                    && (bestRoute == null || route.maskPrefix > bestRoute.maskPrefix)) {
                bestRoute = route;
            }
        }
        return bestRoute;
    }

    private boolean matches(String ip, String network, int prefix) {
        long mask = prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
        return (ipToLong(ip) & mask) == (ipToLong(network) & mask);
    }

    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long value = 0;
        for (String part : parts) value = (value << 8) | Integer.parseInt(part);
        return value;
    }
}
