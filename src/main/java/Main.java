import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {
    final static String CRLF = "\r\n";

    private static void handler(Socket clientSocket, String directory) throws IOException {
        //System.out.println("accepted new connection");
        try(InputStream requestInputStream = clientSocket.getInputStream()) {
            Request request = parseRequest(requestInputStream);

            BufferedWriter response = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            if(request.path.equals("/")) {
                response.write("HTTP/1.1 200 OK" + CRLF + CRLF);
            }
            else if(request.path.startsWith("/echo/")) {
                String msg = request.path.substring(6);
                response.write("HTTP/1.1 200 OK" + CRLF +
                               "Content-Type: text/plain" + CRLF + 
                               "Content-Length: " + msg.length() + CRLF + CRLF + msg);
            }
            else if(request.path.equals("/user-agent")) {
                String userAgent = request.headers.get("User-Agent");
                response.write("HTTP/1.1 200 OK" + CRLF +
                               "Content-Type: text/plain" + CRLF + 
                               "Content-Length: " + userAgent.length() + CRLF + CRLF + userAgent);
            }
            else if(request.path.startsWith("/files/")) {
                String fileName = request.path.substring(7);
                File file = new File(new File(directory), fileName);
                switch(request.method) {
                    case "GET" -> {
                        if(!file.exists() || file.isDirectory()) {
                            response.write("HTTP/1.1 404 Not Found" + CRLF +
                                           "Content-Type: text/plain" + CRLF + 
                                           "Content-Length: 0" + CRLF + CRLF);
                        }
                        else {
                            response.write("HTTP/1.1 200 OK" + CRLF +
                                           "Content-Type: application/octet-stream" + CRLF + 
                                           "Content-Length: " + file.length() + CRLF + CRLF);
                            FileReader fileReader = new FileReader(file);
                            StringBuilder content = new StringBuilder();
                            while(fileReader.ready())
                                content.append((char)fileReader.read());
                            response.write(content.toString());
                            fileReader.close();
                        }
                    }
                    case "POST" -> {
                        file.createNewFile();
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(request.body);
                        fileWriter.close();
                        response.write("HTTP/1.1 201 Created" + CRLF +  CRLF);
                    }
                    default -> System.out.println("Uncovered HTTP method: " + request.method);
                }  
            }
            else{
                response.write("HTTP/1.1 404 Not Found" + CRLF + CRLF);
            }
            response.flush();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static Request parseRequest(InputStream requestInputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(requestInputStream);
        BufferedReader requestReader = new BufferedReader(inputStreamReader);

        if(!requestReader.ready())
            return new Request();

        String[] startLine = requestReader.readLine().split(" ");

        Map<String, String> headers = new HashMap<>();
        String header = requestReader.readLine();
        while(header != null && !header.isEmpty()){
            String[] splitedHeader = header.split(": ");
            headers.put(splitedHeader[0], splitedHeader[1]);
            header = requestReader.readLine();
        }

        StringBuilder body = new StringBuilder();
        while(requestReader.ready())
            body.append((char)requestReader.read());

        return Request.Builder.newInstance().setStartLine(startLine).setHeaders(headers).setBody(body.toString()).build();
    }

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        // System.out.println("Logs from your program will appear here!");
    
        try(ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = serverSocket.accept();
                String directory = (args.length > 1 && args[0].equals("--directory")) ? args[1] : "./";
                new Thread(() -> {
                    try {
                        handler(clientSocket, directory);
                    } catch (IOException e) {
                        System.out.println("IOException(Handler): " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("IOException(Server socket): " + e.getMessage());
        }
    }
}
