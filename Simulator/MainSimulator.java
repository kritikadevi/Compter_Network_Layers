package Simulator;

import java.util.*;

public class MainSimulator {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("==============================================================");
        System.out.println("        COMPUTER NETWORKS 5-LAYER NETWORK SIMULATOR           ");
        System.out.println("==============================================================");

        System.out.print("Enter number of end devices: ");
        int deviceCount = Math.max(2, readInt(sc, 2));
        sc.nextLine();

        System.out.println("Topology options: direct, hub, switch, bridge, router");
        System.out.print("Enter topology needed: ");
        String topology = sc.nextLine().trim().toLowerCase();

        if (topology.equals("direct")) {
            runDirect(sc, deviceCount);
        } else if (topology.equals("hub")) {
            runHub(sc, deviceCount);
        } else if (topology.equals("switch")) {
            runSwitch(sc, deviceCount);
        } else if (topology.equals("bridge")) {
            runBridge(sc, deviceCount);
        } else if (topology.equals("router")) {
            runRouter(sc, deviceCount);
        } else {
            System.out.println("Unknown topology. Running switch topology by default.");
            runSwitch(sc, deviceCount);
        }

        sc.close();
    }

    private static void runDirect(Scanner sc, int count) {
        title("DIRECT TOPOLOGY");
        EndDevice[] pcs = makeDevices(count, "192.168.1.", "AA:AA:AA:00:00:");
        for (int i = 0; i < pcs.length - 1; i++) {
            pcs[i].linkDirectDevice(pcs[i + 1]);
            pcs[i + 1].linkDirectDevice(pcs[i]);
        }

        printDevices(pcs);
        System.out.println("Broadcast Domains: 1 | Collision Domains: " + Math.max(1, count - 1));
        sendFromUser(sc, pcs, pcs);
    }

    private static void runHub(Scanner sc, int count) {
        title("HUB STAR TOPOLOGY");
        EndDevice[] pcs = makeDevices(count, "192.168.2.", "BB:BB:BB:00:00:");
        Hub hub = new Hub("HUB-1");
        for (EndDevice pc : pcs) hub.attachDevice(pc);

        printDevices(pcs);
        System.out.println("Hub repeats bits to every connected port.");
        System.out.println("Broadcast Domains: 1 | Collision Domains: 1");
        sendFromUser(sc, pcs, pcs);
    }

    private static void runSwitch(Scanner sc, int count) {
        title("SWITCH TOPOLOGY");
        EndDevice[] pcs = makeDevices(count, "192.168.3.", "CC:CC:CC:00:00:");
        NetworkSwitch sw = new NetworkSwitch("SW-1");
        for (EndDevice pc : pcs) sw.attachDevice(pc);

        printDevices(pcs);
        System.out.println("Switch learns source MAC addresses and forwards known frames.");
        System.out.println("Broadcast Domains: 1 | Collision Domains: " + count);
        sendFromUser(sc, pcs, pcs);
        sw.printMacTable();
    }

    private static void runBridge(Scanner sc, int count) {
        title("BRIDGE TOPOLOGY");
        int leftCount = askLeftCount(sc, count);
        int rightCount = count - leftCount;

        EndDevice[] left = makeDevices(leftCount, "192.168.4.", "DD:AA:00:00:00:");
        EndDevice[] right = makeDevices(rightCount, "192.168.4.", "DD:BB:00:00:00:", 10 + leftCount);
        renameDevices(left, "L-PC");
        renameDevices(right, "R-PC");
        Bridge bridge = new Bridge("BR-1");

        for (EndDevice pc : left) bridge.attachA(pc);
        for (EndDevice pc : right) bridge.attachB(pc);

        EndDevice[] all = join(left, right);
        printDevices(all);
        System.out.println("Left side devices: " + leftCount + " | Right side devices: " + rightCount);
        System.out.println("Broadcast Domains: 1 | Collision Domains: 2");
        sendFromUser(sc, left, right);
    }

    private static void runRouter(Scanner sc, int count) {
        title("ROUTER TOPOLOGY");
        int leftCount = askLeftCount(sc, count);
        int rightCount = count - leftCount;

        System.out.print("How many routers between left and right LAN: ");
        int routerCount = Math.max(1, readInt(sc, 1));
        sc.nextLine();

        EndDevice[] left = makeDevices(leftCount, "192.168.10.", "A1:00:00:00:00:");
        EndDevice[] right = makeDevices(rightCount, "10.0.0.", "B1:00:00:00:00:");
        renameDevices(left, "L-PC");
        renameDevices(right, "R-PC");

        NetworkSwitch leftSwitch = new NetworkSwitch("SW-LEFT");
        NetworkSwitch rightSwitch = new NetworkSwitch("SW-RIGHT");
        for (EndDevice pc : left) leftSwitch.attachDevice(pc);
        for (EndDevice pc : right) rightSwitch.attachDevice(pc);

        Router[] routers = makeRouters(routerCount);
        configureRouters(routers);

        for (EndDevice pc : left) pc.setGateway("192.168.10.1", routers[0]);
        for (EndDevice pc : right) pc.setGateway("10.0.0.1", routers[routers.length - 1]);

        EndDevice[] all = join(left, right);
        printDevices(all);
        System.out.println("Left LAN devices: " + leftCount + " | Right LAN devices: " + rightCount + " | Routers: " + routerCount);
        System.out.println("Broadcast Domains: 2 | Collision Domains: " + count);
        for (Router router : routers) router.printRoutingTable();

        sendFromUser(sc, left, right);
        leftSwitch.printMacTable();
        rightSwitch.printMacTable();
    }

    private static Router[] makeRouters(int count) {
        Router[] routers = new Router[count];
        for (int i = 0; i < count; i++) {
            routers[i] = new Router("R" + (i + 1));
            routers[i].buildInterface("g0/0", i == 0 ? "192.168.10.1" : "172.16." + i + ".2");
            routers[i].buildInterface("g0/1", i == count - 1 ? "10.0.0.1" : "172.16." + (i + 1) + ".1");
        }
        return routers;
    }

    private static void configureRouters(Router[] routers) {
        for (int i = 0; i < routers.length; i++) {
            String nextHopRight = i == routers.length - 1 ? "direct" : "172.16." + (i + 1) + ".2";
            String nextHopLeft = i == 0 ? "direct" : "172.16." + i + ".1";

            routers[i].insertStaticRoute("192.168.10.0", 24, nextHopLeft, "g0/0");
            routers[i].insertStaticRoute("10.0.0.0", 24, nextHopRight, "g0/1");
            routers[i].insertStaticRoute("10.0.0.10", 32, nextHopRight, "g0/1");

            if (i > 0) routers[i].addRipNeighbor(routers[i - 1]);
            if (i < routers.length - 1) routers[i].addRipNeighbor(routers[i + 1]);
        }

        for (Router router : routers) router.distributeRipMessages();
        for (Router router : routers) router.runOspfShortestPath();
    }

    private static void sendFromUser(Scanner sc, EndDevice[] srcList, EndDevice[] dstList) {
        EndDevice[] all = join(srcList, dstList);
        System.out.print("Enter source device name/IP (blank = first device): ");
        String srcText = sc.nextLine().trim();
        System.out.print("Enter destination device name/IP (blank = last device): ");
        String dstText = sc.nextLine().trim();

        EndDevice src = srcText.isEmpty() ? srcList[0] : findDevice(all, srcText);
        EndDevice dst = dstText.isEmpty() ? dstList[dstList.length - 1] : findDevice(all, dstText);

        if (src == null || dst == null || src == dst) {
            System.out.println("Invalid source/destination. Using first source and last destination.");
            src = srcList[0];
            dst = dstList[dstList.length - 1];
        }

        System.out.print("Choose application service (http/ftp/dns): ");
        String service = sc.nextLine().trim().toLowerCase();
        System.out.print("Enter message/data to send: ");
        String text = sc.nextLine().trim();

        Layer5Application msg;
        if (service.equals("ftp")) {
            msg = Layer5Application.constructFTPTraffic("GET", text);
        } else if (service.equals("dns")) {
            msg = Layer5Application.constructDNSTraffic(text);
        } else {
            msg = Layer5Application.constructHTTPTraffic(text);
        }

        System.out.println("Sending automatically from " + src.name + " to " + dst.name + " through all layers.");
        src.injectTrafficIntoNetworkStack(msg, dst);
    }

    private static int askLeftCount(Scanner sc, int total) {
        System.out.print("How many devices on left side: ");
        int left = clamp(readInt(sc, total / 2), 1, total - 1);
        sc.nextLine();
        return left;
    }

    private static EndDevice[] makeDevices(int count, String ipPrefix, String macPrefix) {
        return makeDevices(count, ipPrefix, macPrefix, 10);
    }

    private static EndDevice[] makeDevices(int count, String ipPrefix, String macPrefix, int startIP) {
        EndDevice[] pcs = new EndDevice[count];
        for (int i = 0; i < count; i++) {
            String num = String.format("%02d", i + 1);
            pcs[i] = new EndDevice("PC" + (i + 1), macPrefix + num, ipPrefix + (startIP + i), "255.255.255.0");
        }
        return pcs;
    }

    private static EndDevice findDevice(EndDevice[] pcs, String text) {
        for (EndDevice pc : pcs) {
            if (pc.name.equalsIgnoreCase(text) || pc.ipAddress.equals(text)) return pc;
        }
        return null;
    }

    private static void renameDevices(EndDevice[] pcs, String prefix) {
        for (int i = 0; i < pcs.length; i++) {
            pcs[i].name = prefix + (i + 1);
        }
    }

    private static EndDevice[] join(EndDevice[] first, EndDevice[] second) {
        EndDevice[] all = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, all, first.length, second.length);
        return all;
    }

    private static void printDevices(EndDevice[] pcs) {
        System.out.println("Devices:");
        for (EndDevice pc : pcs) pc.printInfo();
    }

    private static void title(String text) {
        System.out.println("\n---------------- " + text + " ----------------");
    }

    private static int readInt(Scanner sc, int fallback) {
        if (!sc.hasNextInt()) return fallback;
        return sc.nextInt();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
