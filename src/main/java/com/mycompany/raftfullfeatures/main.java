package com.mycompany.raftfullfeatures;

/*
This is a test main created for experimentation purposes only. 
It is not intended for production use and may not fully represent the final implementation.
*/

public class main {
    public static void main(String[] args) throws InterruptedException {
        // Create Servers.
        RAFTFullFeatures s1 = new RAFTFullFeatures(1);
        RAFTFullFeatures s2 = new RAFTFullFeatures(2);
        RAFTFullFeatures s3 = new RAFTFullFeatures(3);

        // Add Servers to the Cluster.
        RAFTFullFeatures.addServer(s1);
        RAFTFullFeatures.addServer(s2);
        RAFTFullFeatures.addServer(s3);

        // Start Servers.
        s1.start();
        s2.start();
        s3.start();

        // Allow some time for the simulation to run.
        Thread.sleep(3000);

        // Simulate failure of a Server.
        s3.simulateFailure();
        
        // Allow time for recovery and election.
        Thread.sleep(10000);

        // Add a new server to the cluster.
        RAFTFullFeatures s4 = new RAFTFullFeatures(4);
        RAFTFullFeatures.addServer(s4);
        s4.start();

        // Allow some time for the new server to sync.
        Thread.sleep(3000);

        // Optionally remove another server from the cluster.
        // RAFTFullFeatures.removeServer(s2);

        // Allow some time to observe the result.
        Thread.sleep(15000);

        // Shutdown Servers.
        s1.shutdown();
        s2.shutdown();
        s4.shutdown();
    }
}

