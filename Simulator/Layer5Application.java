package Simulator;

import java.util.*;

// --- LAYER 5: APPLICATION LAYER ---
public class Layer5Application {
    public String layerProtocolType; 
    public String dataPayload;
    public int srcPort, dstPort;

    public Layer5Application(String type, String message, int sPort, int dPort) {
        this.layerProtocolType = type; this.dataPayload = message;
        this.srcPort = sPort; this.dstPort = dPort;
    }

    private static int baselinePortPool = 49152;
    public static synchronized int requestDynamicPortAllocations() { return baselinePortPool++; }

    public static Layer5Application constructHTTPTraffic(String message) {
        return new Layer5Application("HTTP", "GET /" + message + " HTTP/1.1", requestDynamicPortAllocations(), 80);
    }
}

class EndDevice {
    public String name, macAddress, ipAddress, networkMask;
    public ARPCacheEngine hardwareArpCacheTable = new ARPCacheEngine();
    
    public List<EndDevice> directNodeConnections = new ArrayList<>();
    public List<Hub> connectedHubs = new ArrayList<>();
    public List<NetworkSwitch> connectedSwitches = new ArrayList<>();

    public EndDevice(String name, String mac, String ip, String mask) {
        this.name = name; this.macAddress = mac; this.ipAddress = ip; this.networkMask = mask;
    }

    public void linkDirectDevice(EndDevice peerNode) {
        directNodeConnections.add(peerNode);
    }

    // Top-Down Encapsulation Engine
    public void injectTrafficIntoNetworkStack(Layer5Application applicationMessage, EndDevice ultimateReceiverHost) {
        System.out.println("\n==================== TOP-DOWN ENCAPSULATION PROCESS STARTED [" + this.name + " ──> " + ultimateReceiverHost.name + "] ====================");
        System.out.println("  [L5 - APPLICATION] Protocol: " + applicationMessage.layerProtocolType + " | Message Content Staged: " + applicationMessage.dataPayload);

        Layer4Transport.executeTcpHandshake(this.ipAddress, applicationMessage.srcPort, ultimateReceiverHost.ipAddress, applicationMessage.dstPort);
        List<Layer4Transport> readySegmentPool = Layer4Transport.processSlidingWindow(applicationMessage, this.ipAddress, ultimateReceiverHost.ipAddress);

        String nextHopHardwareAddressMAC = hardwareArpCacheTable.getMacMapping(ultimateReceiverHost.ipAddress);

        for (Layer4Transport segment : readySegmentPool) {
            Layer3Network generatedIPPacket = new Layer3Network(this.ipAddress, ultimateReceiverHost.ipAddress, segment);

            // Layer 2 Media Access Control Channel Protocol execution: Slotted ALOHA
            boolean channelAcquisitionPass = false;
            int transmissionAttemptsCounter = 0;
            while (!channelAcquisitionPass) {
                transmissionAttemptsCounter++;
                System.out.println("  [L2 - DATA LINK]   [Slotted ALOHA Channel Sync] Synchronizing time slot... Checking carrier medium, Attempt #" + transmissionAttemptsCounter);

                if (Math.random() > 0.20) { // 80% clear slot verification check success probability
                    Layer2DataLink structuredL2Frame = new Layer2DataLink(this.macAddress, nextHopHardwareAddressMAC, transmissionAttemptsCounter, generatedIPPacket);

                    for (Hub hubComponent : connectedHubs) hubComponent.floodHubSignal(structuredL2Frame.toString(), this);
                    for (NetworkSwitch switchComponent : connectedSwitches) switchComponent.switchForward(structuredL2Frame, this);

                    Layer1Physical.transmitPhysicalBits(structuredL2Frame.toString(), this.name, ultimateReceiverHost.name);

                    // Parity checking Error Control system
                    if (structuredL2Frame.parityBit == 1) {
                        System.out.println("  [L2 - DATA LINK]     !! Framing Parity Error matching failure. Dropping damaged frame on line.");
                    } else {
                        System.out.println("  [L2 - DATA LINK]     -> Frame Verification Success: Parity validation clear.");
                        channelAcquisitionPass = true;
                        
                        // Pass data to neighbor list nodes or directly to target
                        if (!directNodeConnections.isEmpty()) {
                            for (EndDevice peer : directNodeConnections) {
                                if (peer.macAddress.equals(nextHopHardwareAddressMAC) || peer.ipAddress.equals(ultimateReceiverHost.ipAddress)) {
                                    peer.receiveAndDecodeNetworkStack(generatedIPPacket);
                                    break;
                                }
                            }
                        } else {
                            ultimateReceiverHost.receiveAndDecodeNetworkStack(generatedIPPacket);
                        }
                    }
                } else {
                    System.out.println("  [L2 - DATA LINK]     !! Slotted ALOHA Channel Collision detected. Executing backoff cooldown window delay.");
                }
            }
        }
    }

    // Bottom-Up Decapsulation Engine
    public void receiveAndDecodeNetworkStack(Layer3Network inboundPacket) {
        System.out.println("\n==================== BOTTOM-UP DECAPSULATION PROCESS STARTED [" + this.name + "] ====================");
        System.out.println("  [L1 - PHYSICAL]    Digital voltages successfully sampled and reassembled into frame string array.");
        System.out.println("  [L2 - DATA LINK]   MAC Address matched. Parity verified. Stripping Layer 2 link wrappers.");
        System.out.println("  [L3 - NETWORK]     Target IP matches local interface. Strip Layer 3 IP Headers: " + inboundPacket);
        
        Layer4Transport segment = inboundPacket.encapsulatedSegment;
        System.out.println("  [L4 - TRANSPORT]   Segment validation sequence complete. Demuxing stream targeting socket Port: " + segment.dstPort);
        
        System.out.println("==================== DATA PACKET UNWRAPPED AND RECEIVED BY APPLICATION LAYER ====================");
        System.out.println("  [L5 - APPLICATION] Process target resolved! Content piece extracted: " + segment.analyticalDataChunk);
    }
}