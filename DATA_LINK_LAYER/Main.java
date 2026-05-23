
import java.util.*;

// Data-frame bnaya 
//message should have sender and receiver source and destination address
// senderMac | parityBit| text | seqno | receiverMac
class Message {

    String senderMAC;
    String receiverMAC;
    //messgae 
    String text;

    //receiver ko pta chale ki packets in order me aye hein ki nahi 
    int seqNo;
    //Error detection
    int parityBit;

    boolean isACK;

    Message(String s, String r, String t, int seq, boolean isACK) {
        this.senderMAC = s;
        this.receiverMAC = r;
        this.text = t;
        this.seqNo = seq;
        this.isACK = isACK;
        this.parityBit = calculateEvenParity(t);       //constructor me hi call kra 
    }

    //error  detection 
//    int calculateEvenParity(String t) {
//     int count = 0;
//     for (char c : t.toCharArray()) {
//         count += Integer.bitCount(c);
//     }

//     int parity = (count % 2 == 0) ? 0 : 1;

//     //  random error 
//     if (Math.random() < 0.3) {
//         parity = 1;
//     }

//     return parity;
// }



int calculateEvenParity(String t) {
    return (Math.random() < 0.3) ? 1 : 0;
}


}



// --- 2. NODE DEVICE 
//EVERY COMPUTER KE CORRESPONDING 
//uska name,mac,
//list-which stores the connection ---adj list 
class NodeDevice {

    String name;
    String mac;
    List<NodeDevice> connections = new ArrayList<>();

    NodeDevice(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    //pc1=[pc2,pc3]
    //list attached to a particular node 
    void connect(NodeDevice d) {
        connections.add(d);
    }

    void showConnections() {
        System.out.print(name + " -> ");
        for (NodeDevice d : connections) {
            System.out.print(d.name + " ");
        }
        System.out.println();
    }

    void send(Message m, NodeDevice sender) {
        for (NodeDevice d : connections) {

            if (d != sender) {
                d.receive(m, this);
            }
        }
    }

    void receive(Message m, NodeDevice sender) {

        //OVER-RIDE LATER
    }
}

// --- 3. SWITCH (Layer 2 - Address Learning) ---
class Switch extends NodeDevice {

    //address learning using hashmap
    //key: Mac 
    //value: PC1,PC2......N 
    //   switch
    //  1/    2\ 
    //  pc1    pc2
    Map<String, NodeDevice> macTable = new HashMap<>();

    Switch(String name) {
        super(name, "SWITCH_L2");
    }

    @Override
    void receive(Message m, NodeDevice sender) {

        //macTable = HashMap<MAC, Device>
        macTable.putIfAbsent(m.senderMAC, sender); // Learning

        // PC1 → Switch ko message bheja
        //MAC = M1
        //Switch store karega:
        //M1 → PC1
        //receiver mac user will give 

        if (macTable.containsKey(m.receiverMAC)) {

            //checks map is this receiver device is present with me
            System.out.println("[Switch " + name + "] Unicasting Seq " + (m.isACK ? "ACK " : "Seq ") + m.seqNo + " to " + m.receiverMAC);

            //UNICAST
            macTable.get(m.receiverMAC).receive(m, this);

            //     MAC Table:
            //    M1 → PC1
            //    M2 → PC2
            //  PC1 sends → PC2
            //   Switch: "M2 pata hai → direct PC2 ko bhejo"
            // [Switch S1] Unicasting Seq 1 to M2
        } else {
            //Receiver Unknown → BROADCAST

            System.out.println("[Switch " + name + "] Unknown MAC (" + m.receiverMAC + "). Broadcasting...");

            // with iterating over all the d =[]
            for (NodeDevice d : connections) {
                if (d != sender) {

                    //of the type object
                    // direct loop krke it will send
                    d.receive(m, this);

                }

            }
        }
    }
}

// --- 4. HUB (Layer 1 - Broadcast) ---
class Hub extends NodeDevice {

    Hub(String name) {
        super(name, "HUB");
    }

    @Override

    void receive(Message m, NodeDevice sender) {

        for (NodeDevice d : connections) {

            if (d != sender) {
                d.receive(m, this);

            }
        }
    }
}

// --- 5. END DEVICE (Clean Version) ---
class EndDevice extends NodeDevice {

    boolean ackFlag = false;

    int m_bits = 3;

    // (0-2^m -1)
    int windowSize = (int) Math.pow(2, m_bits) - 1;

    EndDevice(String name, String mac) {
        super(name, mac);
    }


 
    public void startGBN_only(String destMac, String data, NodeDevice nextHop) {

        String[] packets = data.split(" ");

        /*
        f_unack : firts unacked frame'=
        nextSeq: which is the next frame to send

         */
        int f_unack = 0;
        int nextSeq = 0;

        System.out.println("\n--- GooBACK-N ARQ START ---");

        while (f_unack < packets.length) {

             ackFlag = false;

            //[f_unack  ........  f_unack + windowSize - 1]
            while (nextSeq < f_unack + windowSize && nextSeq < packets.length) {

                //aloha implementation
                waitForSlot();

                System.out.println("Send Frame " + nextSeq);

                nextHop.receive(new Message(mac, destMac, packets[nextSeq], nextSeq, false), this);
                nextSeq++;
            }


            if (ackFlag)
             {
                System.out.println("ACK " + f_unack);
                f_unack++;
            } 
            else
                 {
                System.out.println("Timeout → Resending from " + f_unack);
                nextSeq = f_unack; //  resend
            }

        }

        System.out.println("--- GooBackN END ---");
    }


    public void sendAloha(String destMac, String data, NodeDevice nextHop) {

        String[] packets = data.split(" ");

        System.out.println("\n--- ALOHA START ---");


        for (int i = 0; i < packets.length; i++) {
                                 
            waitForSlot();

            System.out.println("Send Packet " + i);

            //collission stimulation we used random value (0-1)
            //70% chance success
            // 30% chance collision

            if (Math.random() > 0.3) {

                // if value is greater than 0.3 then we will send the packet to the receiver
                nextHop.receive(new Message(mac, destMac, packets[i], i, false), this);
                System.out.println("Success");
            } 
            else
                 {
                // we will not send 
                System.out.println("Collision -> Retry");

                //i-- so that again go to the same packet and send again
                i--; // resend
            }
        }

        System.out.println("---S ALOHA END ---");
    }

    private void waitForSlot() {

        System.out.println("------- ");
        System.out.println("Waiting for Slot...");
        System.out.println(" ------");

        try {  //200ms  try to resend again it 
            Thread.sleep(200);
        } catch (Exception e) {

        }
    }

    @Override
    void receive(Message m, NodeDevice sender) {

        if (m.isACK) 
        {
            ackFlag = true;
            return;
        }

        // Agar message isi PC ke liye hai
        if (this.mac.equals(m.receiverMAC)) {

            System.out.println("[" + name + "] Received Seq " + m.seqNo + " | Parity: " + m.parityBit);

            if (m.parityBit == 1)

                 {

                System.out.println("[" + name + "] Error detected! Packet dropped ");
                return; //  ACK nahi bhejna
            }

              //ack
            if (!m.text.equals("ACK_REPLY")) 
                {
                System.out.println("[" + name + "] Sending ACK to help Switch learn my MAC...");

                // Wapas reply bhejo: (Source=M2, Dest=M1)
                //when message sedn then is ack true

                sender.receive(new Message(this.mac, m.senderMAC, "ACK_REPLY", m.seqNo, true), this);
            }
           
        }
    }

}

// 👉 Slotted ALOHA = “kab send karna hai” control karta hai
// 👉 Go-Back-N = “kaunse frames resend karne hai” control karta hai

// --- 6. MAIN CLASS ---
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("1. Test Case 1: Switch with n devices");
        System.out.println("2. Test Case 2: 2 Hub Topologies + Switch");
        int testCase = sc.nextInt();

        switch (testCase) {
            case 1: {

                System.out.print("Enter number of devices (n): ");
                int n = sc.nextInt();

                Switch sw = new Switch("SW1");

                //list of n devices
                EndDevice[] pcs = new EndDevice[n];

                for (int i = 0; i < n; i++) {
                    pcs[i] = new EndDevice("PC" + (i + 1), "M" + (i + 1));
                    pcs[i].connect(sw);
                    sw.connect(pcs[i]);
                }

                System.out.println("\n--- Connected Devices ---");

                int x = 1;
                for (int i = 0; i < n; i++) {

                    System.out.println(x + "." + pcs[i].name + " , " + pcs[i].mac);
                    x++;
                }

                System.out.print("\nSender MAC: ");
                String s = sc.next();

                System.out.print("Receiver MAC: ");
                String r = sc.next();

                sc.nextLine();
                System.out.print("Message: ");
                String msg = sc.nextLine();

                //checking sender hamara end device me he bhi ki nahi 
                for (EndDevice pc : pcs) {

                    if (pc.mac.equals(s)) {

                        pc.startGBN_only(r, msg, sw);
                        break;
                    }
                }
                System.out.println("\nCollision Domains: " + n + " | Broadcast Domains: 1");
                break;
            }

            case 2: {

                System.out.print("Enter devices per Hub (n): ");
                int n = sc.nextInt();

                //centre switch 
                Switch mainSw = new Switch("CenterSwitch");

                //hub 1 
                Hub h1 = new Hub("HubA");

                //hub 2
                Hub h2 = new Hub("HubB");

                // switch connect to hub 1
                // hub 2 to switch and vice versa
                mainSw.connect(h1);
                h1.connect(mainSw);
                mainSw.connect(h2);
                h2.connect(mainSw);

                EndDevice[] allPcs = new EndDevice[n * 2];

                for (int i = 0; i < n; i++) {

                    //Group A → A_PC1, A_PC2, ..., A_PCn (connected to h1)
                    //Group B → B_PC1, B_PC2, ..., B_PCn (connected to h2)
                    // Total PCs = 2 * n
                    allPcs[i] = new EndDevice("A_PC" + (i + 1), "M" + (i + 1));
                    allPcs[i].connect(h1);
                    h1.connect(allPcs[i]);

                    allPcs[i + n] = new EndDevice("B_PC" + (i + 1), "M" + (i + n + 1));
                    allPcs[i + n].connect(h2);
                    h2.connect(allPcs[i + n]);
                }

                // print 
                System.out.println("\n--- Network Topology ---");
                System.out.println("[HubA Connections]:");

                for (int i = 0; i < n; i++) {
                    System.out.println("  - " + allPcs[i].name + " (MAC: " + allPcs[i].mac + ")");
                }

                //
                System.out.println("[HubB Connections]:");
                for (int i = 0; i < n; i++) {
                    System.out.println("  - " + allPcs[i + n].name + " (MAC: " + allPcs[i + n].mac + ")");
                }

                System.out.println("------------------------");

                System.out.print("\nSender MAC: ");
                String s = sc.next();

                System.out.print("Receiver MAC: ");
                String r = sc.next();

                sc.nextLine();
                System.out.print("Message: ");
                String msg = sc.nextLine();

                for (EndDevice pc : allPcs) {

                    if (pc.mac.equals(s)) {

                        //pc is connected to a hub 
                        //pc -hub 
                        NodeDevice nextHop = pc.connections.get(0);

                        //then decide next
                        pc.startGBN_only(r, msg, nextHop);
                        break;
                    }
                }

                System.out.println("\nCollision Domains: 2 | Broadcast Domains: 1");
                break;
            }
        }
        sc.close();
    }
}
