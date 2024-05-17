# synchronization-OS
implementation of synchronization OS  java code
Network Connection Management System
This Java program simulates a network connection management system where devices connect to a router, perform online activities, and disconnect from the network. The system includes classes for Semaphore, Connection, Router, Device, and Network.

How to Use
Compile: Compile the Java files using javac command.

Run: Run the Network class and follow the prompts to input the number of Wi-Fi connections and the number of devices.

Classes
Semaphore: Represents a synchronization primitive to control access to connections.

Connection: Represents a network connection. Tracks the state of the connection (occupied or not).

Router: Manages connections for multiple devices. Controls access to connections using Semaphore.

Device: Represents a network device. Connects to the router, performs online activities, and disconnects.

Network: Main class to run the network simulation. Asks for user input, creates devices, redirects output to a file, and starts device threads.

DualOutputStream: Redirects output to multiple streams (console and file).

Classes Overview
Semaphore: Controls access to shared resources (connections) using synchronized methods.
Connection: Tracks the state of network connections (occupied or not) and provides methods to occupy and release connections.
Router: Manages connections and controls access using Semaphores. Tracks the maximum number of connections.
Device: Represents network devices. Connects to the router, performs online activities, and disconnects. Each device runs as a separate thread.
Network: Main class to run the simulation. Asks for user input, creates devices, redirects output to a file, and starts device threads.
DualOutputStream: Redirects output to multiple streams (console and file).
Additional Notes
Ensure to compile and run the Java files in a suitable development environment.
The program simulates devices connecting to a router, performing online activities, and disconnecting, with output directed to both console and a file named "output.txt".
