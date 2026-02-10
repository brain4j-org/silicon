package org.silicon;

import org.silicon.api.Silicon;
import org.silicon.api.device.ComputeArena;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.device.ComputeDevice;

import java.util.Scanner;

public class MemoryTest {
    public static void main(String[] args) throws InterruptedException {
        ComputeDevice device = Silicon.createDevice();
        ComputeContext context = device.createContext();
        Scanner scanner = new Scanner(System.in);
        
        try (ComputeArena arena = context.createArena()) {
            arena.allocateBytes(1024 * 1024 * 1024 * 8L); // 8 GB buffer
            
            System.out.println("Waiting...");
            scanner.nextLine();
        }
        
        System.out.println("Released buffer!");
        scanner.nextLine();
    }
}
