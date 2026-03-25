
import java.util.*;

class Message {

    String senderMAC;
    String receiverMAC;
    String text;

    Message(String s, String r, String t) {
        senderMAC = s;
        receiverMAC = r;
        text = t;
    }
}

class NodeDevice {

    String name;
    String mac;
    List<NodeDevice> connections = new ArrayList<>();

    NodeDevice(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

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
        // overridden in child
    }
}

class EndDevice extends NodeDevice {

    EndDevice(String name, String mac) {
        super(name, mac);
    }

    @Override
    void receive(Message m, NodeDevice sender) {

        if (this.mac.equals(m.receiverMAC)) {
            System.out.println(name + " received from " + m.senderMAC + ": " + m.text);
            return;
        }

        // forward (for ring, bus, mesh)
        send(m, sender);
    }
}

 class Hub extends NodeDevice {

    Hub(String name) {
        super(name, "HUB");
    }

    @Override
    void receive(Message m, NodeDevice sender) {
        System.out.println(name + " is broadcasting");

        for (NodeDevice d : connections) {
            if (d != sender) {
                d.receive(m, this);
            }
        }
    }
}

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Choose Mode:");
        System.out.println("1. Two Devices (Direct)");
        System.out.println("2. Topologies");

        int mode = sc.nextInt();

        switch (mode) {

            // -------- MODE 1: DIRECT --------
            case 1: {

                EndDevice A = new EndDevice("A", "M1");
                EndDevice B = new EndDevice("B", "M2");

                A.connect(B);
                B.connect(A);

                System.out.println("\nTopology:");
                A.showConnections();
                B.showConnections();

                sc.nextLine();
                System.out.print("\nEnter message: ");
                String text = sc.nextLine();

                Message msg = new Message("M1", "M2", text);

                System.out.println("\nA sending...");
                A.send(msg, null);

                break;
            }

            // -------- MODE 2: TOPOLOGIES --------
            case 2: {

                System.out.print("\nEnter number of devices: ");
                int n = sc.nextInt();

                EndDevice[] pcs = new EndDevice[n];
                for (int i = 0; i < n; i++) {
                    pcs[i] = new EndDevice("PC" + (i + 1), "M" + (i + 1));
                }

                System.out.println("\nChoose Topology:");
                System.out.println("1. Star");
                System.out.println("2. Ring");
                System.out.println("3. Bus");
                System.out.println("4. Mesh");

                int choice = sc.nextInt();
                Hub hub = new Hub("Hub");

                switch (choice) {

                    case 1:
                        System.out.println("STAR TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n; i++) {
                            pcs[i].connect(hub);
                            hub.connect(pcs[i]);
                        }
                        break;

                    case 2: // RING
                        System.out.println(" RING TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n; i++) {
                            pcs[i].connect(pcs[(i + 1) % n]);
                            pcs[(i + 1) % n].connect(pcs[i]);
                        }
                        break;

                    case 3: // BUS
                        System.out.println(" BUS TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n - 1; i++) {
                            pcs[i].connect(pcs[i + 1]);
                            pcs[i + 1].connect(pcs[i]);
                        }
                        break;

                    case 4: // MESH
                        System.out.println("MESH TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n; i++) {
                            for (int j = i + 1; j < n; j++) {
                                pcs[i].connect(pcs[j]);
                                pcs[j].connect(pcs[i]);
                            }
                        }
                        break;

                    default:
                        System.out.println("Invalid topology choice");
                        return;
                }

                // show topology
                System.out.println("\nTopology:");
                for (EndDevice pc : pcs) {
                    pc.showConnections();
                }
                if (choice == 1) {
                    hub.showConnections();
                }

                // message input
                System.out.println("\nDevices: M1 to M" + n);

                System.out.print("Enter sender MAC: ");
                String sender = sc.next();

                System.out.print("Enter receiver MAC: ");
                String receiver = sc.next();


                //CH
                if (sender.equals(receiver)) {
                    System.out.println("Invalid: Sender and Receiver cannot be same!");
                    return;
                }

                sc.nextLine();
                System.out.print("Enter message: ");
                String text = sc.nextLine();

                Message msg = new Message(sender, receiver, text);

                // send
                for (EndDevice pc : pcs) {
                    if (pc.mac.equals(sender)) {
                        System.out.println("\n" + pc.name + " sending...");
                        pc.send(msg, null);
                        break;
                    }
                }

                break;
            }

            default:
                System.out.println("Invalid mode");
        }

        sc.close();
    }
}
