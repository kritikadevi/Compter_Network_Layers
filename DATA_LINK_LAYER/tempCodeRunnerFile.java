
    // // implementation
    // public void startCombined(String destMac, String data, NodeDevice nextHop) {

    //     String[] packets = data.split(" ");
    //     int base = 0, nextSeq = 0;

    //     System.out.println("\n--- COMBINED START ---");

    //     while (base < packets.length) {

    //         while (nextSeq < base + windowSize && nextSeq < packets.length) {

    //             waitForSlot();
    //             System.out.println("Send Frame " + nextSeq);

    //             if (Math.random() > 0.2) {
    //                 nextHop.receive(new Message(mac, destMac, packets[nextSeq], nextSeq), this);
    //                 System.out.println("ACK " + nextSeq);
    //                 base++;
    //                 nextSeq++;
    //             } else {
    //                 System.out.println("LOSS -> Resend from " + base);
    //                 nextSeq = base;
    //                 break;
    //             }
    //         }
    //     }

    //     System.out.println("--- COMBINED END ---");
    // }
