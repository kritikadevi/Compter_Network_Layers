import java.util.*;

// 🔹 Frame
class Frame {
    String source, destination, data;

    Frame(String s, String d, String data) {
        this.source = s;
        this.destination = d;
        this.data = data; // ✅ FIXED
    }
}

// 🔹 End Device
class EndDevice {
    String name;

    EndDevice(String name) {
        this.name = name;
    }

    void receive(Frame f) {
        System.out.println(name + " received: " + f.data);
    }
}

// 🔹 HUB
class Hub {
    List<EndDevice> devices = new ArrayList<>();

    void connect(EndDevice d) {
        devices.add(d);
    }

    void broadcast(Frame f, EndDevice sender) {
        System.out.println("Hub Broadcasting...");
        for (EndDevice d : devices) {
            if (d != sender) {
                d.receive(f);
            }
        }
    }
}

// 🔹 SWITCH (MAC Learning)
class Switch {
    Map<String, EndDevice> macTable = new HashMap<>();
    List<EndDevice> devices = new ArrayList<>();

    void connect(EndDevice d) {
        devices.add(d);
    }

    void forward(Frame f, EndDevice sender) {

        // Learn MAC
        macTable.put(f.source, sender);
        System.out.println("Switch learned MAC: " + f.source);

        if (macTable.containsKey(f.destination)) {
            System.out.println("Forwarding to " + f.destination);
            macTable.get(f.destination).receive(f);
        } else {
            System.out.println("Unknown destination → Broadcasting");
            for (EndDevice d : devices) {
                if (d != sender) {
                    d.receive(f);
                }
            }
        }
    }
}

// 🔹 Node (Slotted ALOHA)
class Node {
    int id;
    Queue<String> queue;

    Node(int id, int frames) {
        this.id = id;
        queue = new LinkedList<>();
        for (int i = 1; i <= frames; i++) {
            queue.add("F" + i);
        }
    }

    boolean sendRequest() {
        return !queue.isEmpty() && Math.random() < 0.5;
    }

    String send() {
        return queue.poll();
    }
}

public class Main {

    static Scanner sc = new Scanner(System.in);

    // 🔹 Stop-and-Wait
    static void stopAndWait() {
        Random rand = new Random();
        boolean ack = false;

        while (!ack) {
            System.out.println("Sending Frame");

            if (rand.nextBoolean()) {
                System.out.println("ACK received");
                ack = true;
            } else {
                System.out.println("ACK lost → Resending...");
            }
        }
    }

    // 🔹 Go-Back-N
    static void goBackN() {
        int total = 5, windowSize = 3;
        Queue<Integer> window = new LinkedList<>();

        int start = 0, next = 0;

        while (start < total) {

            while (next < start + windowSize && next < total) {
                System.out.println("Send: " + next);
                window.add(next);
                next++;
            }

            int ack = (start == 2) ? -1 : start;

            if (ack == -1) {
                System.out.println("Error! Resend from: " + start);
                next = start;
                window.clear();
            } else {
                System.out.println("ACK: " + ack);

                while (!window.isEmpty() && window.peek() <= ack) {
                    window.poll();
                    start++;
                }
            }

            System.out.println("------------");
        }
    }

    // 🔹 MAIN
    public static void main(String[] args) {

        System.out.println("===== SELECT FUNCTION =====");
        System.out.println("1. Slotted ALOHA");
        System.out.println("2. Stop-and-Wait ARQ");
        System.out.println("3. Go-Back-N");
        System.out.println("4. Switch Network");
        System.out.println("5. Hub + Switch Network");

        System.out.print("Enter choice: ");
        int choice = sc.nextInt();

        switch (choice) {

            case 1:
                System.out.print("Enter number of nodes: ");
                int nodes = sc.nextInt();

                ArrayList<Node> nodeList = new ArrayList<>();
                for (int i = 0; i < nodes; i++) {
                    nodeList.add(new Node(i, 2));
                }

                for (int t = 1; t <= 5; t++) {
                    ArrayList<Node> active = new ArrayList<>();

                    for (Node n : nodeList) {
                        if (n.sendRequest()) active.add(n);
                    }

                    if (active.size() == 0) {
                        System.out.println("Idle");
                    } else if (active.size() == 1) {
                        System.out.println("Node " + active.get(0).id + " sent " + active.get(0).send());
                    } else {
                        System.out.print("Collision: ");
                        for (Node n : active) System.out.print(n.id + " ");
                        System.out.println();
                    }
                }
                break;

            case 2:
                stopAndWait();
                break;

            case 3:
                goBackN();
                break;

            case 4:
                System.out.print("Enter number of devices: ");
                int n = sc.nextInt();

                EndDevice[] devices = new EndDevice[n];

                for (int i = 0; i < n; i++) {
                    System.out.print("Enter name for device " + (i + 1) + ": ");
                    String name = sc.next();
                    devices[i] = new EndDevice(name);
                }

                Switch sw = new Switch();
                for (EndDevice d : devices) sw.connect(d);

                System.out.print("Enter number of transmissions: ");
                int t = sc.nextInt();

                for (int i = 0; i < t; i++) {
                    System.out.print("Sender (1-" + n + "): ");
                    int s = sc.nextInt() - 1;

                    System.out.print("Receiver (1-" + n + "): ");
                    int r = sc.nextInt() - 1;

                    sw.forward(new Frame(devices[s].name, devices[r].name, "Message"), devices[s]);
                }

                System.out.println("Broadcast Domains = 1");
                System.out.println("Collision Domains = " + n);
                break;

            case 5:
                System.out.print("Enter number of hubs: ");
                int h = sc.nextInt();

                System.out.print("Enter devices per hub: ");
                int d = sc.nextInt();

                Hub[] hubs = new Hub[h];
                EndDevice[] allDevices = new EndDevice[h * d];

                for (int i = 0; i < h; i++) {
                    hubs[i] = new Hub();
                }

                int index = 0;
                for (int i = 0; i < h; i++) {
                    for (int j = 0; j < d; j++) {
                        System.out.print("Enter name for device " + (index + 1) + ": ");
                        String name = sc.next();

                        allDevices[index] = new EndDevice(name);
                        hubs[i].connect(allDevices[index]);
                        index++;
                    }
                }

                Switch sw2 = new Switch();
                for (EndDevice dev : allDevices) sw2.connect(dev);

                System.out.print("Enter number of transmissions: ");
                int tr = sc.nextInt();

                for (int i = 0; i < tr; i++) {
                    System.out.print("Sender (1-" + (h * d) + "): ");
                    int s = sc.nextInt() - 1;

                    System.out.print("Receiver (1-" + (h * d) + "): ");
                    int r = sc.nextInt() - 1;

                    sw2.forward(new Frame(allDevices[s].name, allDevices[r].name, "Message"), allDevices[s]);
                }

                System.out.println("Broadcast Domains = 1");
                System.out.println("Collision Domains = " + h);
                break;

            default:
                System.out.println("Invalid choice");
        }
    }
}