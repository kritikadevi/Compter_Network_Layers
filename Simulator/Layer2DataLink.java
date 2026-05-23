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

class Bridge {
    public String name;
    public List<EndDevice> sideA = new ArrayList<>();
    public List<EndDevice> sideB = new ArrayList<>();
    public Map<String, String> macSide = new LinkedHashMap<>();

    public Bridge(String name) {
        this.name = name;
    }

    public void attachA(EndDevice device) {
        sideA.add(device);
        device.connectedBridges.add(this);
    }

    public void attachB(EndDevice device) {
        sideB.add(device);
        device.connectedBridges.add(this);
    }

    public void forward(Layer2DataLink frame, EndDevice sender) {
        String inSide = sideA.contains(sender) ? "A" : "B";
        macSide.put(frame.srcMAC, inSide);
        String outSide = macSide.get(frame.dstMAC);

        System.out.println("  [L2 - BRIDGE / " + name + "] Learned " + frame.srcMAC + " on segment " + inSide);
        if (outSide == null) {
            System.out.println("  [L2 - BRIDGE / " + name + "] Unknown destination, flooding to opposite segment.");
        } else if (outSide.equals(inSide)) {
            System.out.println("  [L2 - BRIDGE / " + name + "] Same segment traffic, filtering frame.");
        } else {
            System.out.println("  [L2 - BRIDGE / " + name + "] Forwarding frame from segment " + inSide + " to " + outSide);
        }
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
        learnDevice(senderNode);

        EndDevice lookupPort = macTable.get(frame.dstMAC);
        if (lookupPort != null) {
            System.out.println("  [L2 - SWITCH / " + switchName + "] Decision: UNICAST frame directly to " + lookupPort.name);
        } else {
            System.out.println("  [L2 - SWITCH / " + switchName + "] Unknown MAC: FLOODING frame out to all alternate ports.");
        }
    }

    public void learnDevice(EndDevice device) {
        EndDevice oldPort = macTable.put(device.macAddress, device);
        if (oldPort == null) {
            System.out.println("  [L2 - SWITCH / " + switchName + "] Learning: MAC " + device.macAddress + " mapped to port [" + device.name + "]");
        }
    }

    public void printMacTable() {
        System.out.println("  [L2 - SWITCH / " + switchName + "] MAC Address Table");
        for (Map.Entry<String, EndDevice> entry : macTable.entrySet()) {
            System.out.println("    " + entry.getKey() + " -> " + entry.getValue().name);
        }
    }
}
