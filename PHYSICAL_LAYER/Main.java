
import java.util.*;


//DATA-FRAME 
//NODE(A)-NODE(B):Data send
//data should have the source and destination in its header
//To simulate that
//we will define a class 
//MAC as we are in Data Link Layer

class Message {

    //TAKING MAC AS INT WE HAVE MAKE IT MORE SIMPLE
    //We Take it as a String 
    //M1,M2,M3....Mn //
    String senderMAC;  
    String receiverMAC;

    //only string text 
    String text;

    //Initalize the variables
    Message(String s, String r, String t) {
        senderMAC = s;
        receiverMAC = r;
        text = t;
    }
}



//NODE (END-DEVICES)
//now we need to stimulate the node how it will be like
//node : the end device -it can be computer ,laptop
//to keep it simple 
//each node should have its NAME +MAC

class NodeDevice {

    //Name: PC1,PC2,PC3,PC4......PCn //
    String name;
    //MAC: M1,M2,M3,M4,M5........Mn//
    String mac;

    
    //every node is storing its own list of connections
    List<NodeDevice> connections = new ArrayList<>();


    //Initalize variables
    NodeDevice(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    
    //pc1 send msg to pc1=[pc2]
    void connect(NodeDevice d) {
        connections.add(d);
    }


// print our connections 
    void showConnections() {
        System.out.print(name + " -> ");
        //loop-over node devices
        for (NodeDevice d : connections) {
            System.out.print(d.name + " ");
        }
        System.out.println();
    }



    
    //send krne ke liye method 
    //pc1=[pc2,pc3,pc4]
    void send(Message m, NodeDevice sender) {  //("hello",pc1)
        for (NodeDevice d : connections) {  //d : who is receiving nd this 
            //d = Hub   for star

            //receiver should not be equal to the receiver 
            if (d != sender) {          //pc same
                d.receive(m, this); 


                //hub.receive(m, pc);  
                //during start hub h toh uskae andr store hub h 
                //so call hub function 
                //d actually Hub object hai 
            }
        }
    }


    //later over-ridden
    void receive(Message m, NodeDevice sender) {
        // overridden in child
        //as to receive on edn device there should be some limitations

    }
}





//end to end device which has properties of the node-device
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





//HUB
 class Hub extends NodeDevice {

    //initalize 
    Hub(String name) {
        super(name, "HUB");
    }

    @Override
    //hub receiver is broadcasting
    void receive(Message m, NodeDevice sender) {
        System.out.println(name + " is broadcasting");

        for (NodeDevice d : connections) {
            if (d != sender) {
                d.receive(m, this);
            }
        }
    }
}




//MAIN CLASS 

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

                
                //Direct end devices 
                //create two end devices of type NodeDevices
                EndDevice A = new EndDevice("A", "M1");
                EndDevice B = new EndDevice("B", "M2");
                

                A.connect(B); //A=[B]
                B.connect(A);//B=[A]

                System.out.println("\nTopology:");


                //show topology connections
                // Topology:
                // A -> B 
                // B -> A
                A.showConnections();
                B.showConnections();



                sc.nextLine();


                System.out.print("\nEnter message: ");
                String text = sc.nextLine();
                Message msg = new Message("M1", "M2", text);

                System.out.println("\nA sending...");

                //ony A sending
                A.send(msg, null);

                break;
            }

            // -------- MODE 2: TOPOLOGIES --------
            case 2: {    
                //TOPOLOGY-SETUP
                 

                //user - input 
                System.out.print("\nEnter number of devices: ");
                int n = sc.nextInt();

                //array of size the no of end devices
                //type Enddevice ka array hoga 
                //adjacency list 
                EndDevice[] pcs = new EndDevice[n];
                for (int i = 0; i < n; i++) {

                  //[["pc1",M1],["PC2,M2"].....["PCn,Mn"]]
                    pcs[i] = new EndDevice("PC" + (i + 1), "M" + (i + 1));
                }


                //print krayi h list apni 
                int x=1;
                for(NodeDevice in:pcs)
                {
                    
                    System.out.println(x+"th computer "+"."+in.name+","+in.mac);
                    x++;

                    
                  }


                

                System.out.println("\nChoose Topology:");
                System.out.println("1. Star");
                System.out.println("2. Ring");
                System.out.println("3. Bus");
                System.out.println("4. Mesh");

                int choice = sc.nextInt();

                //hub object
                Hub hub = new Hub("Hub");

                switch (choice) {

                    case 1:
                        System.out.println("STAR TOPOLOGY IS APPLIED ........");
                        //node he wo stores unka mac and all in pcs array
                        for (int i = 0; i < n; i++) {
                            pcs[i].connect(hub);
                            hub.connect(pcs[i]);
                        }
                        break;


                        //PC1.connections = [Hub]
                       // PC2.connections = [Hub]  
                       //PC ke andar hub ka object store hai



                    case 2: // RING
                        System.out.println(" RING TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n; i++) {
                            //circular 
                            pcs[i].connect(pcs[(i + 1) % n]);
                            pcs[(i + 1) % n].connect(pcs[i]);
                        }
                        break;

                    case 3: // BUS
                        System.out.println(" BUS TOPOLOGY IS APPLIED ........");
                        for (int i = 0; i < n - 1; i++) {
                            //adjacent nodes are the device
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
                //loop on pcs arr to show connections

                //pc kis kis se connected he
                for (EndDevice pc : pcs) {
                    pc.showConnections();
                }
                //hub kiskis se connected he
                if (choice == 1) {
                    hub.showConnections();
                }



                // message input
                System.out.println("\nDevices: M1 to M" + n);

                System.out.print("Enter sender MAC: ");
                String sender = sc.next();

                System.out.print("Enter receiver MAC: ");
                String receiver = sc.next();


                 //conditional check
                //if sender == receiver 
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

                     //checking mac jo diya sender ka uska corresponding pc name kya h 
                    if (pc.mac.equals(sender)) {

                       
                        System.out.println("\n" + pc.name + " sending...");
                        pc.send(msg, null); //this 

                        //pc.send(msg, null);
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
