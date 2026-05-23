# Physical Layer Network Simulator (Layer 1)

A Java-based simulation tool designed to emulate the fundamental operations of the Physical Layer of the OSI model. This project focuses on signal propagation, broadcasting through Hubs, and the structural arrangement of devices in different network topologies.


# Features

1. Hardware Emulation :
     - End Device (PC): Represents network terminals (Computers/Laptops) with unique MAC addresses. They are capable of initiating transmissions and verifying if an incoming frame is addressed to them.

     - Hub (Layer 1): A "dumb" networking device that operates purely on the physical layer. It facilitates Broadcasting—any message received on one port is automatically forwarded to all other connected ports.

2. Topology Support

The simulator allows for the creation and testing of four primary network architectures:

  - Star Topology: All devices connect to a central Hub.

  - Ring Topology: Each device connects to exactly two other devices, forming a continuous circular pathway.

  - Bus Topology: Devices are connected in a linear sequence.

  - Mesh Topology: A fully connected network where every device has a dedicated point-to-point connection to every other device.



# Working Principle

1. Signal Broadcasting (Hub Logic): 
 Unlike a Switch, a Hub does not look at MAC addresses. In this simulator:
  - An End Device sends a Message to the Hub.

  - The Hub receives the message and iterates through its connections list.

  - The Hub forwards the message to every device except the original sender to prevent loops.

2. Frame Filtering (End Device Logic):
  Since the Hub broadcasts to everyone, every PC receives the data.

  - The PC compares its own mac with the receiverMAC in the header.

  - If it matches, the message is accepted and printed.

  - If it doesn't match, the message is ignored (or forwarded in the case of Ring/Bus topologies).


# Test Case  Scenarios

Case 1: Point-to-Point (Direct Connection)
  - Setup: Two End Devices (A and B) connected directly.

  - Setup: Two End Devices (A and B) connected directly.

Case 2: Star Topology (Hub-Based)
   - Setup: Five End Devices connected to a central Hub.

   - Validation: * Sender sends data to the Hub.
      - Hub triggers a Broadcast event.
      - Only the device with the matching MAC address logs the received text; others remain silent.



# How to Run

1. Compile the source: javac Main.java

2. Run the Application: java Main

3. Interaction:
     - Choose Mode 1 for a simple Peer-to-Peer test.

    - Choose Mode 2 to build a network topology.

    - Enter the number of devices and choose the desired structure (Star, Ring, Bus, or Mesh).

    - Input the sender and receiver MACs to witness the signal propagation.



# Logic & Architecture

  - Message Class: Simulates the physical frame containing senderMAC, receiverMAC, and the payload text.

  - NodeDevice Base: An abstract representation of any network node, managing a List<NodeDevice> for physical connections.

  - Polymorphism: The receive() method is overridden to differentiate between the "Broadcasting" behavior of a Hub and the "Filtering" behavior of a PC.