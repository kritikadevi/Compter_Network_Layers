package Simulator;

import java.util.*;

// --- LAYER 1: PHYSICAL LAYER ---
public class Layer1Physical {
    public static void transmitPhysicalBits(String frameData, String source, String dest) {
        StringBuilder bin = new StringBuilder();
        for (char c : frameData.toCharArray()) {
            bin.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        String bits = bin.toString();
        System.out.println("  [L1 - PHYSICAL]    Modulating signal on wire link [" + source + " ──> " + dest + "]");
        System.out.println("  [L1 - PHYSICAL]    Bits Pulsed (32-bit sample): " + bits.substring(0, Math.min(32, bits.length())) + "...");
    }
}

class Hub {
    public String hubName;
    public List<EndDevice> ports = new ArrayList<>();

    public Hub(String name) { this.hubName = name; }
    
    public void attachDevice(EndDevice device) {
        ports.add(device);
        device.connectedHubs.add(this);
    }

    public void floodHubSignal(String frameString, EndDevice transmitter) {
        System.out.println("  [L1 - HUB / " + hubName + "] Repeater Operating: Flooding bits to all connected topology ports.");
    }
}