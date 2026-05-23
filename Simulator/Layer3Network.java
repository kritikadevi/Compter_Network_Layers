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