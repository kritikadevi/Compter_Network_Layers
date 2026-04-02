# Data Link Layer Simulator
A Java-based network simulator that implements core Layer 2 (Data Link Layer) functionalities. This project simulates hardware devices, MAC address learning, error detection, flow control, and multiple access protocols.


# Features

# 1) Network Device Emulation

1. Switch (Layer 2): Implements dynamic Address Learning using a MAC table (HashMap). It intelligently switches between Unicast (once the MAC is learned) and Broadcast (for unknown destinations).

2. Hub (Layer 1): Performs physical layer broadcasting, forwarding incoming signals to all connected ports except the source.

3. End Device (PC): Represents network hosts capable of generating frames, verifying parity, and handling ACKs.


# 2)Protocol Implementations

1. Flow Control:  Implemented Go-Back-N (GBN) ARQ, a sliding window protocol that ensures reliable delivery and sequential processing.

2. Access Control:  Implemented Slotted ALOHA logic to simulate slot-based transmissions and collision handling.

3. Error Control: Utilizes Even Parity Check. The simulator includes a localized error-injection mechanism (30 % probability) to demonstrate frame dropping and subsequent retransmission. used 500ms time for wait.



# Technical Architecture
The simulator is built using an Object-Oriented approach:

1. Message Class: Represents the Data Link Frame containing SenderMAC, ReceiverMAC, Text, SeqNo, and ParityBit.

2. NodeDevice (pc): Defines the fundamental send() and receive() behaviors for all network entities.

3. Polymorphism: Different receive logic is implemented for Switches (learning/unicasting) vs. Hubs (flooding).



# Test Case Scenarios

1. Case 1: Switch-Based Star TopologyConfiguration:

 - 1 Switch connected to $N$ End Devices.Behavior: The switch starts with an empty MAC table. 

 - The first frame results in a broadcast; once the receiver replies, the switch "learns" the port, and subsequent communication is unicast.

 - Network Stats: * Collision Domains: N (Each switch port is a separate collision domain).Broadcast Domains: 1.


 2. Case 2: Multi-Segment HybridTopology
 
 - Configuration: Two separate Hub-based star topologies connected via a central Layer 2 Switch.
 - Behavior: Demonstrates how a Switch isolates collision domains while allowing inter-segment communication.
 
 - Network Stats:
    Collision Domains: 2 (One for each Hub segment).

    Broadcast Domains: 1.



# How to Run

1. Compile the code:   javac Main.java
                     

2. Execute the simulator: java Main

3. Input Guide:
  - Select the Test Case (1 or 2).
  - Provide the number of devices.
  - Enter the Sender/Receiver MACs (e.g., M1, M2) and the message string.
                   

