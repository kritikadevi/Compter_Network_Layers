package Simulator;

import java.util.*;

// --- LAYER 4: TRANSPORT LAYER ---
public class Layer4Transport {
    public String srcIP, dstIP;
    public int srcPort, dstPort;
    public int sequenceNo;
    public boolean synFlag, ackFlag;
    public String analyticalDataChunk;

    public Layer4Transport(String sIP, int sPort, String dIP, int dPort, int seq, boolean syn, boolean ack, String payload) {
        this.srcIP = sIP; this.srcPort = sPort;
        this.dstIP = dIP; this.dstPort = dPort;
        this.sequenceNo = seq; this.synFlag = syn;
        this.ackFlag = ack; this.analyticalDataChunk = payload;
    }

    public static void executeTcpHandshake(String sIP, int sPort, String dIP, int dPort) {
        System.out.println("  [L4 - TRANSPORT]   --- TCP 3-Way Handshake Connection SETUP ---");
        System.out.println("  [L4 - TRANSPORT]     [SYN]      " + sIP + ":" + sPort + " --> " + dIP + ":" + dPort);
        System.out.println("  [L4 - TRANSPORT]     [SYN-ACK]  Target host acknowledges synchronization maps.");
        System.out.println("  [L4 - TRANSPORT]     [ACK]      Channel connection state changed to ESTABLISHED.");
    }

    public static List<Layer4Transport> processSlidingWindow(Layer5Application appLayerPacket, String srcIP, String dstIP) {
        String[] stringBlocks = appLayerPacket.dataPayload.split(" "); // Tokenize string words
        List<Layer4Transport> trackingBuffer = new ArrayList<>();

        for (int i = 0; i < stringBlocks.length; i++) {
            trackingBuffer.add(new Layer4Transport(srcIP, appLayerPacket.srcPort, dstIP, appLayerPacket.dstPort, i, i == 0, false, stringBlocks[i]));
        }

        System.out.println("  [L4 - TRANSPORT]   GBN Segmentation: Staged " + trackingBuffer.size() + " segments. Sliding Window Size = 4");

        int baseIdx = 0, nextTransmitSeq = 0, currentWindowSize = 4;
        while (baseIdx < trackingBuffer.size()) {
            while (nextTransmitSeq < baseIdx + currentWindowSize && nextTransmitSeq < trackingBuffer.size()) {
                System.out.println("  [L4 - TRANSPORT]     -> Window Frame Open: Transmitting Segment Seq Marker #" + nextTransmitSeq);
                nextTransmitSeq++;
            }
            if (Math.random() > 0.15) { // 15% network drop retry simulator
                System.out.println("  [L4 - TRANSPORT]     <- Verification ACK Received. Sliding window base index advanced to Seq=" + baseIdx);
                baseIdx++;
            } else {
                System.out.println("  [L4 - TRANSPORT]     !! [TIMEOUT] Segment Drop detected. GBN ARQ Rolling Back to Base Marker #" + baseIdx);
                nextTransmitSeq = baseIdx; 
            }
        }
        return trackingBuffer;
    }
}
