import java.util.*;

public class VLSM {

    static class Subnet implements Comparable<Subnet> {
        String name;
        int neededSize;

        public Subnet(String name, int neededSize) {
            this.name = name;
            this.neededSize = neededSize;
        }

        @Override
        public int compareTo(Subnet o) {
            return Integer.compare(o.neededSize, this.neededSize); // Descending
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Java VLSM Calculator ===");
        
        System.out.print("Enter Major Network IP (e.g., 192.168.1.0): ");
        String ipStr = scanner.nextLine();
        
        System.out.print("Enter CIDR Prefix (e.g., 24): ");
        int cidr = scanner.nextInt();
        
        System.out.print("How many subnets do you need? ");
        int numSubnets = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        List<Subnet> subnets = new ArrayList<>();
        for (int i = 0; i < numSubnets; i++) {
            System.out.print("Name for subnet " + (i + 1) + ": ");
            String name = scanner.nextLine();
            System.out.print("Required hosts for " + name + ": ");
            int size = scanner.nextInt();
            scanner.nextLine();
            subnets.add(new Subnet(name, size));
        }
        
        Collections.sort(subnets);
        
        long currentIp = ipToLong(ipStr);
        long majorMask = cidr == 0 ? 0 : (~((1L << (32 - cidr)) - 1) & 0xFFFFFFFFL);
        currentIp = currentIp & majorMask;
        
        long maxIp = currentIp + (1L << (32 - cidr));
        
        System.out.println("\n--- VLSM Results ---");
        System.out.printf("%-15s %-10s %-10s %-18s %-18s %-35s %-18s\n", 
            "Name", "Needed", "Allocated", "Network", "Mask", "Usable Range", "Broadcast");
            
        for (Subnet s : subnets) {
            int neededHosts = s.neededSize + 2;
            int allocatedHosts = 1;
            int newCidr = 32;
            
            while (allocatedHosts < neededHosts) {
                allocatedHosts *= 2;
                newCidr--;
            }
            
            if (currentIp + allocatedHosts > maxIp) {
                System.out.println("Error: Not enough space in major network for subnet " + s.name);
                break;
            }
            
            long networkAddr = currentIp;
            long broadcastAddr = currentIp + allocatedHosts - 1;
            long firstUsable = currentIp + 1;
            long lastUsable = broadcastAddr - 1;
            long subnetMask = newCidr == 0 ? 0 : (~((1L << (32 - newCidr)) - 1) & 0xFFFFFFFFL);
            
            System.out.printf("%-15s %-10d %-10d %-18s %-18s %-35s %-18s\n",
                s.name, s.neededSize, allocatedHosts - 2,
                longToIp(networkAddr) + "/" + newCidr,
                longToIp(subnetMask),
                longToIp(firstUsable) + " - " + longToIp(lastUsable),
                longToIp(broadcastAddr)
            );
            
            currentIp += allocatedHosts;
        }
        scanner.close();
    }

    private static long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= Long.parseLong(octets[i]) << (24 - (8 * i));
        }
        return result & 0xFFFFFFFFL;
    }

    private static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }
}
