
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;
//class represents a semaphore , a synchronization primitive
class Semaphore {
    //variable to store the current state of semaphore
    //number of available permits
    private int value;
    //constructor
    //initializes the semaphore with the specified initial number of permits
    //the initial value determines how many threads can acquire permits without being blocked
    public Semaphore(int initial)
    {
        value = initial;
    }


    // Wait
    //this method is used by thread to request a permit from the semaphore
    public synchronized void P() throws InterruptedException
    {
        //if no permits available
        //thread will wait
        while (value == 0) {
            wait();
        }
        //decrements the value
        value--;
    }

    // Signal
    //this method is used by thread to release a permit back to the semaphore
    public synchronized void V() {
        //increments the value
        value++;
        //notifies any waiting threads that a permit is now available
        notify();
    }

    //this method returns the current number of available permits
    public synchronized int availablePermits()
    {
        return value;
    }
}


//this class represents a network connection
class Connection
{
    //variable to store the state of connection
    //if device is occupied (T) if not (F)
    private boolean occupied;

    //constructor
    //when a new "connection" object is created, occupied = false
    public Connection()
    {
        occupied = false;
    }

    //method that returns the current state of connection
    //occupied = T ,not occupied = F
    public boolean isOccupied()
    {
        return occupied;
    }

    //this method is called when a device wants to make a connection
    //occupied = true
    public void occupy()
    {
        occupied = true;
    }

    //this method is called when a device disconnects and releases the connection
    //occupied = false
    public void release()
    {
        occupied = false;
    }
}
//class router the manages connections for multiple devices
class Router
{

    //variable that store max number of connections the router can handle
    private final int max_connections;

    //list that holds instances of the "connection"
    //each element in the list represents a connection managed by the router
    private final List<Connection> connections_list;

    //controls access to the connections,by limiting the number of devices that can occupy connections
    private final Semaphore semaphore;

    //constructor initializes the router with the specified maximum number of connections
    public Router(int maxi_connections)
    {
        max_connections = maxi_connections;
        //this creates a new array list that holds connection objects
        connections_list = new ArrayList<>(maxi_connections);

        //this loop adds a new connection object to the list
        for (int i = 0; i < maxi_connections; i++)
        {
            connections_list.add(new Connection());
        }

        // creates a Semaphore with the same maximum number of connections.
        semaphore = new Semaphore(maxi_connections);
    }

    //method that provides access  to the list of connections managed by the router.
    public List<Connection> getConnectionsList()
    {
        return connections_list;
    }

    //This method returns the number of available permits (connections)
    public synchronized int availablePermits()
    {
        return semaphore.availablePermits();
    }

    //This method simulates a device (d) performing online activity.
    public void performOnlineActivity(Device d)
    {
        //determines the index of the connection associated with the device d in the connections_list
        int connectionIndex = connections_list.indexOf(d.getConnection()) + 1;
        System.out.println("Connection " + connectionIndex + ": " + d.getDeviceName() + " is performing online activity.");

        //creates new random object
        //to generate a random duration for simulating online activity
        Random randomTime = new Random();

        //generates a random integer between 0 and 5000
        long duration = randomTime.nextInt(5000);

        try {
            //delay in the execution of the thread
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void occupyConnection(Device d) throws InterruptedException
    {
        //acquires a permit from the semaphore
        semaphore.P();

        //synchronization block
        //preventing multiple threads from interfering with each other when trying to occupy connections.
        synchronized (this)
        {

            //This loop iterates over the list of connections managed by the router.
            for (int i = 0; i < connections_list.size(); i++) {
                //retrieves a connection from the list at the current iteration.
                Connection connection_port = connections_list.get(i);

                //checks if the connection is not already occupied by another device
                if (!connection_port.isOccupied()) {
                    connection_port.occupy();
                    int connectionIndex = i + 1;
                    System.out.println("Connection " + connectionIndex + ": " + d.getDeviceName() + " Occupied");
                    System.out.println("Connection " + connectionIndex + ": " + d.getDeviceName() + " login");
                    d.setConnection(connection_port);
                    break;
                }
            }

        }
    }

    public void releaseConnection(Device d)
    {
        //synchronization block
        //preventing multiple threads from interfering with each other when trying to occupy connections.
        synchronized (this) {
            Connection connection_port = d.getConnection();
            int connectionIndex = connections_list.indexOf(connection_port) + 1;
            System.out.println("Connection " + connectionIndex + ": " + d.getDeviceName() + " logged out");

            //mark the connection as unoccupied.
            connection_port.release();

            //The device's connection is set to null to indicate that it is no longer associated with any connection.
            d.setConnection(null);

            //release a permit back to the semaphore
            semaphore.V();
        }
        // Notify waiting devices that a connection is available outside the synchronized block
        synchronized (this)
        {
            notify();
        }
    }
}

class Device extends Thread {

    private final String device_name; //name of device
    private final String device_type; //type of device
    private final PrintStream outputStream; //output object
    private Router device_router; //router object
    private Connection connection; //connection object
    //constructor
    //it Initializes the device with a name, type, and a reference to a router
    public Device(String name, String type, Router router,  PrintStream outputStream) {
        device_name = name;
        device_type = type;
        device_router = router;
        connection = null;
        this.outputStream = outputStream;
    }
    //Retrieve the name the device.
    public String getDeviceName() {
        return device_name;
    }

    //Retrieve the type of the device.
    public String getDeviceType() {
        return device_type;
    }

    //Retrieve the current connection of the device.
    public Connection getConnection() {
        return connection;
    }

    // set the current connection of the device.
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    //This method is called when a device wants to connect to the network
    public void connect() throws InterruptedException {
        synchronized (device_router) {
            //Checks if there are available permits (connections) in the router
            if (device_router.availablePermits() > 0)
            {
                System.out.println("(" + device_name + ")(" + device_type + ") arrived");
                //device occupies a connection
                device_router.occupyConnection(this);
            } else {
                System.out.println("(" + device_name + ")(" + device_type + ") arrived and waiting");
                device_router.wait(); // Wait for a connection to become available
                device_router.occupyConnection(this); // Try to occupy the connection again after waking up
            }
        }
    }

    //This method is called when a device wants to disconnect from the network.
    public void disconnect() {
        //release the current connection.
        device_router.releaseConnection(this);
    }

    //Overrides the run method from the Thread class.
    @Override
    public void run() {
        try {
            connect();
            device_router.performOnlineActivity(this);
            device_router.releaseConnection(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class Network {
    public static void main(String[] args) {
        int max_num_of_connections;
        int total_num_of_devices;
        //creates sccaner object to read user input from the console
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.println("What is the number of WI-FI Connections?");
        max_num_of_connections = scanner.nextInt();
        System.out.println("What is the number of devices Clients want to connect?");
        total_num_of_devices = scanner.nextInt();

        // Creates a Router object with the specified maximum number of connections.
        Router r = new Router(max_num_of_connections);

        //creates array list to store instances of device class
        List<Device> devices = new ArrayList<>();

        // For console output
        PrintStream consoleOut = System.out;

        for (int i = 0; i < total_num_of_devices; i++) {
            System.out.print("Enter name and type of device " + (i + 1) + ": ");
            String name = scanner.next();
            String type = scanner.next();
            devices.add(new Device(name, type, r, consoleOut));
        }

        try {
            // For file output
            PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt"));

            // Create an array of OutputStreams
            OutputStream[] outputStreams = {consoleOut, fileOut};

            // Redirect System.out to the file
            System.setOut(new PrintStream(new DualOutputStream(outputStreams)));

            //this loop is used to start the threads of each device
            for (int i = 0; i < devices.size(); i++) {
                devices.get(i).start();
            }
        } catch (Exception e) {

        }
        scanner.close();
    }
}

class DualOutputStream extends OutputStream {
    private OutputStream[] outputStreams;

    //this constructor takes a variable number of outputstream objects
    //stores them in an array
    public DualOutputStream(OutputStream[] outputStreams) {
        this.outputStreams = outputStreams;
    }

    // This method is responsible for writing a byte of data to the output streams.
    @Override
    public void write(int b) throws IOException {
        for (int i = 0; i < outputStreams.length; i++) {
            OutputStream os = outputStreams[i];
            os.write(b);
        }
    }
}
