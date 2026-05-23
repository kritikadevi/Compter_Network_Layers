package Simulator;

import java.util.*;

// --- LAYER 5: APPLICATION LAYER ---
public class Layer5Application {
    public String layerProtocolType;
    public String dataPayload;
    public int srcPort, dstPort;

    private static int nextPort = 49152;
    private static final Map<String, Integer> servicePorts = new LinkedHashMap<>();

    static {
        servicePorts.put("HTTP", 80);
        servicePorts.put("FTP", 21);
        servicePorts.put("DNS", 53);
        servicePorts.put("SSH", 22);
    }

    public Layer5Application(String type, String message, int srcPort, int dstPort) {
        this.layerProtocolType = type;
        this.dataPayload = message;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public static synchronized int requestDynamicPortAllocations() {
        return nextPort++;
    }

    public static Layer5Application constructHTTPTraffic(String message) {
        message = message.trim();
        return new Layer5Application("HTTP", "GET /" + message + " HTTP/1.1", requestDynamicPortAllocations(), servicePorts.get("HTTP"));
    }

    public static Layer5Application constructFTPTraffic(String command, String fileName) {
        command = command.trim();
        fileName = fileName.trim();
        return new Layer5Application("FTP", command + " " + fileName, requestDynamicPortAllocations(), servicePorts.get("FTP"));
    }

    public static Layer5Application constructDNSTraffic(String hostName) {
        hostName = hostName.trim();
        return new Layer5Application("DNS", "QUERY " + hostName, requestDynamicPortAllocations(), servicePorts.get("DNS"));
    }

    public static void printPortTable() {
        System.out.println("  [L5 - APPLICATION] Well-known ports: " + servicePorts + " | Next ephemeral port: " + nextPort);
    }
}

class EndDevice {
    public String name, macAddress, ipAddress, networkMask;
    public String gatewayIP = "";
    public Router gatewayRouter;
    public ARPCacheEngine hardwareArpCacheTable = new ARPCacheEngine();

    public List<EndDevice> directNodeConnections = new ArrayList<>();
    public List<Hub> connectedHubs = new ArrayList<>();
    public List<NetworkSwitch> connectedSwitches = new ArrayList<>();
    public List<Bridge> connectedBridges = new ArrayList<>();

    public EndDevice(String name, String mac, String ip, String mask) {
        this.name = name;
        this.macAddress = mac;
        this.ipAddress = ip;
        this.networkMask = mask;
    }

    public void linkDirectDevice(EndDevice peerNode) {
        if (!directNodeConnections.contains(peerNode)) directNodeConnections.add(peerNode);
    }

    public void setGateway(String ip, Router router) {
        gatewayIP = ip;
        gatewayRouter = router;
        hardwareArpCacheTable.mapAddress(ip, "GW-" + router.routerName);
    }

    public void printInfo() {
        String gatewayText = gatewayIP.isEmpty() ? "none" : gatewayIP;
        System.out.println("  " + name + " | IP=" + ipAddress + " | MAC=" + macAddress + " | Gateway=" + gatewayText);
    }

    // Top-Down Encapsulation Engine
    public void injectTrafficIntoNetworkStack(Layer5Application appMsg, EndDevice receiver) {
        System.out.println("\n==================== TOP-DOWN ENCAPSULATION PROCESS STARTED [" + name + " --> " + receiver.name + "] ====================");
        System.out.println("  [L5 - APPLICATION] Protocol: " + appMsg.layerProtocolType + " | Message Content Staged: " + appMsg.dataPayload);
        Layer5Application.printPortTable();

        Layer4Transport.executeTcpHandshake(ipAddress, appMsg.srcPort, receiver.ipAddress, appMsg.dstPort);
        List<Layer4Transport> segments = Layer4Transport.processSlidingWindow(appMsg, ipAddress, receiver.ipAddress);

        hardwareArpCacheTable.mapAddress(receiver.ipAddress, receiver.macAddress);
        String nextHopIP = gatewayRouter == null || sameSubnet(receiver) ? receiver.ipAddress : gatewayIP;
        String nextHopMAC = hardwareArpCacheTable.getMacMapping(nextHopIP);

        for (Layer4Transport segment : segments) {
            Layer3Network packet = new Layer3Network(ipAddress, receiver.ipAddress, segment);
            if (gatewayRouter != null && !sameSubnet(receiver)) {
                packet = gatewayRouter.forwardL3Packet(packet);
                if (packet == null) return;
            }

            boolean sent = false;
            int tries = 0;
            while (!sent) {
                tries++;
                System.out.println("  [L2 - DATA LINK]   [Slotted ALOHA] Checking time slot, Attempt #" + tries);

                if (Math.random() > 0.20) {
                    Layer2DataLink frame = new Layer2DataLink(macAddress, nextHopMAC, tries, packet);

                    for (Hub hub : connectedHubs) hub.floodHubSignal(frame, this, receiver);
                    for (NetworkSwitch netSwitch : connectedSwitches) netSwitch.switchForward(frame, this);
                    for (Bridge bridge : connectedBridges) bridge.forward(frame, this);

                    Layer1Physical.transmitPhysicalBits(frame.toString(), name, receiver.name);

                    if (frame.parityBit == 1) {
                        System.out.println("  [L2 - DATA LINK]     !! Parity error detected. Damaged frame dropped and retransmitted.");
                    } else {
                        System.out.println("  [L2 - DATA LINK]     -> Frame Verification Success: Parity validation clear.");
                        sent = true;
                        deliverPacket(receiver, packet, nextHopMAC);
                    }
                } else {
                    System.out.println("  [L2 - DATA LINK]     !! Slotted ALOHA collision detected. Backoff started.");
                }
            }
        }
    }

    private void deliverPacket(EndDevice receiver, Layer3Network packet, String nextHopMAC) {
        if (!directNodeConnections.isEmpty()) {
            for (EndDevice peer : directNodeConnections) {
                if (peer.macAddress.equals(nextHopMAC) || peer.ipAddress.equals(receiver.ipAddress)) {
                    peer.receiveAndDecodeNetworkStack(packet);
                    return;
                }
            }
        }
        receiver.receiveAndDecodeNetworkStack(packet);
    }

    // Bottom-Up Decapsulation Engine
    public void receiveAndDecodeNetworkStack(Layer3Network inboundPacket) {
        System.out.println("\n==================== BOTTOM-UP DECAPSULATION PROCESS STARTED [" + name + "] ====================");
        System.out.println("  [L1 - PHYSICAL]    Digital signal sampled and reassembled into frame data.");
        System.out.println("  [L2 - DATA LINK]   MAC matched. Parity verified. Layer 2 wrapper removed.");
        System.out.println("  [L3 - NETWORK]     Target IP matches local interface. IP header removed: " + inboundPacket);

        Layer4Transport segment = inboundPacket.encapsulatedSegment;
        System.out.println("  [L4 - TRANSPORT]   Segment accepted. Demuxing stream to socket Port: " + segment.dstPort);

        System.out.println("==================== DATA PACKET UNWRAPPED AND RECEIVED BY APPLICATION LAYER ====================");
        System.out.println("  [L5 - APPLICATION] Process target resolved! Content piece extracted: " + segment.analyticalDataChunk);
    }

    private boolean sameSubnet(EndDevice other) {
        String[] a = ipAddress.split("\\.");
        String[] b = other.ipAddress.split("\\.");
        return a[0].equals(b[0]) && a[1].equals(b[1]) && a[2].equals(b[2]);
    }
}
