package Simulator;

import java.util.Scanner;
import java.util.concurrent.*;

public class MainSimulator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================================");
        System.out.println("     COMPREHENSIVE CUSTOMIZABLE TOPOLOGY 5-LAYER SIMULATOR       ");
        System.out.println("=================================================================");

        // 1. Ask for number of devices
        System.out.print("\nEnter number of end-devices to provision dynamically (n): ");
        int n = scanner.nextInt();

        // Dynamically auto-create hosts based on user chosen count, assigning IPs and MACs automatically
        EndDevice[] customHostPcs = new EndDevice[n];
        for (int i = 0; i < n; i++) {
            customHostPcs[i] = new EndDevice("PC-" + (i + 1), "MAC-0" + (i + 1), "192.168.1." + (10 + i), "255.255.255.0");
        }

        // 2. Ask for Topology choice
        System.out.println("\nChoose Topology Type layout to implement over devices:");
        System.out.println("  1. Star Topology  (Managed via a Shared Layer-1 Hub component)");
        System.out.println("  2. Ring Topology  (Managed via point-to-point circular connections)");
        System.out.println("  3. Bus Topology   (Managed via sequential backbone adjacent node paths)");
        System.out.println("  4. Mesh Topology  (Fully interconnected redundant connection matrix)");
        System.out.print("Select Topology Choice Number (1-4): ");
        int topologyChoice = scanner.nextInt();

        Hub operatingHub = new Hub("Custom_User_Hub");

        // Dynamically link devices together based on topology mathematics rules
        switch (topologyChoice) {
            case 1 -> {
                System.out.println(">> Structuring Star Topology connections graph...");
                for (EndDevice device : customHostPcs) operatingHub.attachDevice(device);
            }
            case 2 -> {
                System.out.println(">> Structuring Ring Topology connections graph...");
                for (int i = 0; i < n; i++) {
                    customHostPcs[i].linkDirectDevice(customHostPcs[(i + 1) % n]);
                }
            }
            case 3 -> {
                System.out.println(">> Structuring Bus Topology connections graph...");
                for (int i = 0; i < n - 1; i++) {
                    customHostPcs[i].linkDirectDevice(customHostPcs[i + 1]);
                    customHostPcs[i + 1].linkDirectDevice(customHostPcs[i]);
                }
            }
            case 4 -> {
                System.out.println(">> Structuring Mesh Topology connections graph...");
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (i != j) customHostPcs[i].linkDirectDevice(customHostPcs[j]);
                    }
                }
            }
        }

        // 3. Print the dynamically created active network graph map report
        System.out.println("\n--- RUNTIME REPORT: ACTIVE CONNECTIONS GRAPH MAP ---");
        for (EndDevice host : customHostPcs) {
            System.out.print("  Device: " + host.name + " | IP: " + host.ipAddress + " | MAC: " + host.macAddress + " ──> Links: ");
            for (EndDevice connectionPeer : host.directNodeConnections) System.out.print(connectionPeer.name + " ");
            if (topologyChoice == 1) System.out.print("Custom_User_Hub");
            System.out.println();
        }
        System.out.println("----------------------------------------------------");

        // 4. Ask for Sender and Receiver IPs dynamically
        System.out.print("\nEnter Sender Host IP (e.g. 192.168.1.10): ");
        String senderIPInput = scanner.next().trim();
        System.out.print("Enter Receiver Host IP (e.g. 192.168.1.11): ");
        String receiverIPInput = scanner.next().trim();
        scanner.nextLine(); // Clear scanner buffer lines

        // 5. Connectivity verification phase: Validate if input IPs exist in our runtime map
        EndDevice senderHost = null;
        EndDevice receiverHost = null;

        for (EndDevice device : customHostPcs) {
            if (device.ipAddress.equals(senderIPInput)) senderHost = device;
            if (device.ipAddress.equals(receiverIPInput)) receiverHost = device;
        }

        // Safety Gate Check: Cancel data stream pipeline if endpoints don't match or exist
        if (senderHost == null || receiverHost == null) {
            System.out.println("\n!! [ROUTING ERROR] Cannot reach network destination. Incorrect IP address entered. Device does not exist.");
            scanner.close();
            return;
        }
        if (senderHost == receiverHost) {
            System.out.println("\n!! [VALIDATION FAILURE] Loopback path blocked. Sender and Receiver cannot be identical nodes.");
            scanner.close();
            return;
        }

        System.out.print("Enter textual payload data message to send: ");
        String messagePayloadInput = scanner.nextLine().trim();

        // 6. Multithreaded Execution Matrix (Runs transactions over the structural user custom network)
        ExecutorService simulationProcessingEnginePool = Executors.newFixedThreadPool(2);

        EndDevice finalSender = senderHost;
        EndDevice finalReceiver = receiverHost;

        // Task 1: Main payload traffic flow (Runs top-down through all 5 layers)
        Runnable userTxTask = () -> {
            Layer5Application httpPackage = Layer5Application.constructHTTPTraffic(messagePayloadInput);
            finalSender.injectTrafficIntoNetworkStack(httpPackage, finalReceiver);
        };

        // Task 2: Background Interleaved control signal noise (satisfies parallel constraints rules check)
        Runnable backgroundNoiseTask = () -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            // If topology size is large enough, pick alternative background hosts noise channel
            EndDevice bgSender = (customHostPcs.length > 2) ? customHostPcs[1] : customHostPcs[0];
            EndDevice bgReceiver = (customHostPcs.length > 2) ? customHostPcs[2] : customHostPcs[1];
            if (bgSender != finalSender && bgReceiver != finalReceiver) {
                Layer5Application bgTraffic = new Layer5Application("CONTROL", "BG_ALIVE_FRAME_PULSE", 5000, 5000);
                bgSender.injectTrafficIntoNetworkStack(bgTraffic, bgReceiver);
            }
        };

        simulationProcessingEnginePool.submit(userTxTask);
        simulationProcessingEnginePool.submit(backgroundNoiseTask);

        simulationProcessingEnginePool.shutdown();
        try {
            simulationProcessingEnginePool.awaitTermination(25, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            error.printStackTrace();
        }

        System.out.println("\n=================================================================");
        System.out.println("   SIMULATION ENGINE DISPATCH RUN CONCLUDED. EXECUTIONS CLOSED.  ");
        System.out.println("=================================================================");
        scanner.close();
    }
}