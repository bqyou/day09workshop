package httpserverclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.LinkedList;

public class HttpClient {

    private Socket sc;
    private String method;
    private String file;
    private File actualFile;

    public HttpClient(Socket sc) {
        this.sc = sc;
    }

    public void request() {
        try {
            BufferedReader request = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            String firstLine = request.readLine();
            String[] requestArray = firstLine.split(" ");
            this.method = requestArray[0];
            if (requestArray[1].equals("/")) {
                this.file = "index.html";
            } else {
                this.file = requestArray[1].replaceAll("/", "");
            }

        } catch (IOException ex) {
            System.out.println("IO exception");
        }
    }

    public void response(LinkedList<String> directories) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpWriter writer = new HttpWriter(sc.getOutputStream());
            if (!method.equals("GET")) {
                getResponse(405, sb);
                writer.writeString(sb.toString());
                writer.flush();
                sc.close();
            }
            if (fileNotExist(directories)) {
                getResponse(404, sb);
                writer.writeString(sb.toString());
                writer.flush();
                sc.close();
            } else {
                getResponse(200, sb);
                byte[] content = readFile(actualFile);
                writer.writeString(sb.toString());
                writer.writeBytes(content);
                writer.flush();
                sc.close();
            }
        } catch (Exception ex) {
            System.out.println("IO Exception");
        } finally {
            System.exit(0);
        }
    }

    public void getResponse(Integer code, StringBuilder sb) {
        switch (code) {
            case 200:
                sb.append("HTTP/1.1 200 OK\r\n");
                sb.append("\r\n");
                break;
            case 404:
                sb.append("HTTP/1.1 404 NOTFOUND\r\n");
                sb.append("\r\n");
                sb.append("%s not found\r\n".formatted(file));
                break;
            case 405:
                sb.append("HTTP/1.1 405 METHOD NOT ALLOWED\r\n");
                sb.append("\r\n");
                sb.append("%s not supported\r\n".formatted(method));
                break;
            default:
                break;
        }
    }

    public byte[] readFile(File file) throws Exception {
        FileInputStream is = new FileInputStream(file);
        byte[] arr = new byte[512];
        is.read(arr);
        is.close();
        return arr;
    }

    public Boolean fileNotExist(LinkedList<String> directories) {
        for (String s : directories) {
            String pathname = "%s/%s".formatted(s, file);
            File f = Paths.get(pathname).toFile();
            if (f.isFile() && f.exists()) {
                this.actualFile = f;
                return false;
            }
        }
        return true;
    }

}
