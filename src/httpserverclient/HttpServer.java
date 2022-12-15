package httpserverclient;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class HttpServer implements Runnable {

    private Integer port;
    private List<String> rootDirectories = new LinkedList<String>();
    private Socket sc;

    public HttpServer(String[] arg) {
        Integer argSize = arg.length;
        switch (argSize) {
            case 0:
                this.port = 3000;
                this.rootDirectories.add("./static");
                break;
            case 2:
                switch (arg[0]) {
                    case "--port":
                        try {
                            this.port = Integer.parseInt(arg[1]);
                            this.rootDirectories.add("./static");
                        } catch (NumberFormatException ex) {
                            System.out.println("Numbers only after --port");
                        }
                        break;
                    case "--docroot":
                        String[] directoryArray = arg[1].split(":");
                        for (Integer i = 0; i < directoryArray.length; i++) {
                            this.rootDirectories.add(directoryArray[i]);
                        }
                        this.port = 3000;
                        break;
                    default:
                        System.out.println("Wrong command input");
                        break;
                }
                break;
            case 4:
                if ((arg[0].equals("--port")) && (arg[2].equals("--docroot"))) {
                    try {
                        this.port = Integer.parseInt(arg[1]);
                    } catch (NumberFormatException ex) {
                        System.out.println("Numbers only after --port");
                    }
                    String[] directoryArray = arg[3].split(":");
                    for (Integer i = 0; i < directoryArray.length; i++) {
                        this.rootDirectories.add(directoryArray[i]);
                    }
                } else {
                    System.out.println("Wrong command input");
                }
                break;
            default:
                System.out.println("Wrong command input");
                break;
        }
    }

    public Integer getPort() {
        return port;
    }

    public List<String> getRootDirectories() {
        return rootDirectories;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            directoryCheck();
            System.out.printf("Waiting for connection to port %d \n", port);
            this.sc = server.accept();
            HttpClient client = new HttpClient(sc);
            client.request();
            client.response((LinkedList<String>) rootDirectories);
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }

    public void directoryCheck() {
        for (String s : rootDirectories) {
            File f = new File(s);
            if (!f.exists()) {
                System.out.printf("%s does not exist.", s);
                System.exit(1);
            }
            if (!f.isDirectory()) {
                System.out.printf("%s is not a directory.", s);
                System.exit(1);
            }
            if (!f.canRead()) {
                System.out.printf("%s can't be read.", s);
                System.exit(1);
            }
        }
    }

}
