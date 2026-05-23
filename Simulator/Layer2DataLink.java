package Simulator;

import java.util.*;

// --- LAYER 2: DATA LINK LAYER ---
public class Layer2DataLink {
    public String srcMAC, dstMAC;
    public int seqNo;
    public int parityBit; // Error Control: 0 = Clean, 1 = Corrupted
    public Layer3Network encapsulatedPacket;

    public Layer2DataLink(String srcMAC, String dstMAC, int seqNo, Layer3Network packet) {
        this.srcMAC = srcMAC;
        this.dstMAC = dstMAC;
        this.seqNo = seqNo;
        this.encapsulatedPacket = packet;
        this.parityBit = (Math.random() < 0.10) ? 1 : 0; // 10% automated framing error drop risk
    }

    @Override
    public String toString() {
        return "[Frame: " + srcMAC + " -> " + dstMAC + " | Seq=" + seqNo + " | Parity=" + parityBit + "]";
    }
}

class NetworkSwitch {
    public String switchName;
    public Map<String, EndDevice> macTable = new LinkedHashMap<>();
    public List<EndDevice> ports = new ArrayList<>();

    public NetworkSwitch(String name) { this.switchName = name; }

    public void attachDevice(EndDevice device) {
        ports.add(device);
        device.connectedSwitches.add(this);
    }

    public void switchForward(Layer2DataLink frame, EndDevice senderNode) {
        if (!macTable.containsKey(frame.srcMAC)) {
            macTable.put(frame.srcMAC, senderNode);
            System.out.println("  [L2 - SWITCH / " + switchName + "] Learning: MAC " + frame.srcMAC + " mapped to port [" + senderNode.name + "]");
        }

        EndDevice lookupPort = macTable.get(frame.dstMAC);
        if (lookupPort != null) {
            System.out.println("  [L2 - SWITCH / " + switchName + "] Decision: UNICAST frame directly to " + lookupPort.name);
        } else {
            System.out.println("  [L2 - SWITCH / " + switchName + "] Unknown MAC: FLOODING frame out to all alternate ports.");
        }
    }
}