import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class HTTPServer {

    private static void handleRequest(Socket socket) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        String firstLine = in.readLine();
        String[] requestParts = firstLine.split(" ");
        String method = requestParts[0];
        String path = requestParts[1];
        

        String line;
        String content = "";
        Boolean isPlain = false;
        Boolean isBody = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.isEmpty()) {
                break;
            }
            if (isPlain == false) {
                if (line.contains("Content-Type: text/plain")) {
                    isPlain = true;
                }
            }
            if (isPlain == true && isBody == false) {
                if (line.equals("")) {
                    isBody = true;
                }
            }
            if (isPlain == true && isBody == true) {
                content = content + line;
                
            }
            
        }

    


    System.out.println(content.toString());
        

        System.out.println(content);
        System.out.println("Content done" + isBody);
        System.out.println(isPlain);

        if (method.equals("GET")) {
        System.out.println("get");

            handleGetRequest(out, path);
            System.out.println("get");
        // } else if (method.equals("POST")) {
        //     handlePostRequest(in, out, path);

        } else {
            sendErrorResponse(out, 501, "Not Implemented");
        }
        out.close();

    }

    private static void handleGetRequest(DataOutputStream out, String path) throws IOException {
        File file = new File("." + path).getCanonicalFile();
        if (!file.isFile()) {
            sendErrorResponse(out, 404, "Not Implemented");
            System.out.println("404");

        } else {
            System.out.println("getting");
            // Object exists and is a file: accept with response code 200.
            System.out.println("get processing");
            String type = handleMIMEType(path);
            String contentType = "Content-Type: " + type + "\n" + "\n";
            String contentLength = "Content-Length: " + (int) file.length() + "\n";
            String statusLine = "HTTP/1.1 200 OK\n";
            String header = statusLine + contentLength + contentType;
            System.out.println(header);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            out.write(header.getBytes());
            out.write(fileContent);
            System.out.println("done");
        }

    }

    // private static void handlePostRequest(BufferedReader in, DataOutputStream out, String path) throws IOException {
    //     File file = new File("." + path).getCanonicalFile();
    //     String line;
    //     String content;
    //     Boolean isPlain = false;
    //     Boolean isBody = false;
    //     while ((line = in.readLine()) != null) {
    //         if (isPlain == false) {
    //             if (line.contains("Content-Type: text/plain")) {
    //                 isPlain = true;
    //             }
    //         }
    //         if (isPlain == true && isBody == false) {
    //             if (line.equals("")) {
    //                 isBody = true;
    //             }
    //         }
    //         if (isPlain == true && isBody == true) {
    //             content = content + line;
    //         }
            
    //     }

    //     if (!file.isFile())

    //     {
    //         sendErrorResponse(out, 404, "Not Implemented");
    //     } else if (requestType.equals("text/plain")) {
    //         InputStream msg = httpExchange.getRequestBody();
    //         try {
    //             FileWriter myWriter = new FileWriter(file, true);
    //             myWriter.write(msg.toString());
    //             myWriter.close();
    //             System.out.println("Successfully wrote to the file.");
    //             String response = "201 (Created)\n";
    //             httpExchange.sendResponseHeaders(201, 0);
    //             OutputStream os = httpExchange.getResponseBody();
    //             os.write(response.getBytes());
    //             os.close();
    //         } catch (IOException e) {
    //             System.out.println("An error occurred.");
    //             e.printStackTrace();
    //             handle503(httpExchange);
    //         }
    //     } else {
    //         handle404(httpExchange);
    //     }
    // }

    // private static void handlePutRequest(BufferedReader in, DataOutputStream out,
    // String path) throws IOException {
    // URI uri = httpExchange.getRequestURI();
    // File file = new File("." + uri.getPath()).getCanonicalFile();
    // String requestType = handleMIMEType(httpExchange);

    // if (file.isFile()) {
    // try {
    // if (requestType.equals("image/png")) {
    // httpExchange.getResponseHeaders().set("Content-Type", requestType);
    // BufferedImage img = ImageIO.read(httpExchange.getRequestBody());
    // ImageIO.write(img, "PNG", file);
    // } else {
    // InputStream content = httpExchange.getRequestBody();
    // FileWriter myWriter = new FileWriter(file, false);
    // myWriter.write(content.toString());
    // myWriter.close();
    // }
    // System.out.println("Successfully overwrote to the file.");
    // String response = "201 (Created)\n";
    // httpExchange.sendResponseHeaders(201, 0);
    // OutputStream os = httpExchange.getResponseBody();
    // os.write(response.getBytes());
    // os.close();
    // } catch (IOException e) {
    // System.out.println("An error occurred.");
    // e.printStackTrace();
    // handle503(httpExchange);
    // }
    // } else if (file.createNewFile()) {
    // try {
    // if (requestType.equals("image/png")) {
    // httpExchange.getResponseHeaders().set("Content-Type", requestType);
    // BufferedImage img = ImageIO.read(httpExchange.getRequestBody());
    // ImageIO.write(img, "PNG", file);
    // } else {
    // InputStream content = httpExchange.getRequestBody();
    // FileWriter myWriter = new FileWriter(file, false);
    // myWriter.write(content.toString());
    // myWriter.close();
    // }
    // System.out.println("File created: " + file.getName());
    // String response = "201 (Created)\n";
    // httpExchange.sendResponseHeaders(201, 0);
    // OutputStream os = httpExchange.getResponseBody();
    // os.write(response.getBytes());
    // os.close();
    // } catch (IOException e) {
    // System.out.println("An error occurred.");
    // e.printStackTrace();
    // handle500(httpExchange);
    // }
    // } else {
    // handle404(httpExchange);
    // }
    // }

    // private static void handleDeleteRequest(DataOutputStream out, String path)
    // throws IOException {
    // URI uri = httpExchange.getRequestURI();
    // File file = new File("." + uri.getPath()).getCanonicalFile();

    // if (file.delete()) {
    // try {
    // System.out.println("File deleted: " + file.getName());
    // String response = "202 (Accepted)\n";
    // httpExchange.sendResponseHeaders(202, 0);
    // OutputStream os = httpExchange.getResponseBody();
    // os.write(response.getBytes());
    // os.close();
    // } catch (IOException e) {
    // System.out.println("An error occurred.");
    // e.printStackTrace();
    // String response = "500 (Internal Server Error)\n";
    // handle500(httpExchange);
    // }
    // } else {
    // handle404(httpExchange);
    // }
    // }

    // private static void handleOptionRequest(DataOutputStream out) throws
    // IOException {
    // URI uri = httpExchange.getRequestURI();
    // String requestType = handleMIMEType(httpExchange);
    // String actions = "GET" + "\n" + "OPTIONS" + "\n" + "HEAD" + "\n";
    // File file = new File("." + uri.getPath()).getCanonicalFile();

    // if (file.exists()) {
    // actions = actions + "PUT" + "\n" + "DELETE";
    // if (requestType.equals("text/plain")) {
    // actions = actions + "POST";
    // }
    // } else {
    // actions = actions + "PUT";
    // }

    // httpExchange.sendResponseHeaders(200, actions.length());
    // OutputStream os = httpExchange.getResponseBody();
    // os.write(actions.getBytes());
    // os.close();
    // }

    // private static void handleHeadRequest(DataOutputStream out, String path)
    // throws IOException {
    // String header = httpExchange.getResponseHeaders().toString();
    // httpExchange.sendResponseHeaders(200, header.length());
    // OutputStream os = httpExchange.getResponseBody();
    // os.write(header.getBytes());
    // os.close();
    // }

    private static String handleMIMEType(String path) {
        if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }

    // private void handle404(HttpExchange httpExchange) throws IOException {
    // File cat404 = new File("404.png");

    // httpExchange.getResponseHeaders().set("Content-Type", "image/png");
    // httpExchange.sendResponseHeaders(404, 0);

    // OutputStream os = httpExchange.getResponseBody();
    // FileInputStream fs = new FileInputStream(cat404);

    // final byte[] buffer = new byte[0x10000];
    // int count = 0;
    // while ((count = fs.read(buffer)) >= 0) {
    // os.write(buffer, 0, count);
    // }
    // fs.close();
    // os.close();
    // }

    // private void handle500(HttpExchange httpExchange) throws IOException {
    // File cat500 = new File("500.png");

    // httpExchange.getResponseHeaders().set("Content-Type", "image/png");
    // httpExchange.sendResponseHeaders(500, 0);

    // OutputStream os = httpExchange.getResponseBody();
    // FileInputStream fs = new FileInputStream(cat500);

    // final byte[] buffer = new byte[0x10000];
    // int count = 0;
    // while ((count = fs.read(buffer)) >= 0) {
    // os.write(buffer, 0, count);
    // }
    // fs.close();
    // os.close();
    // }

    // private void handle503(HttpExchange httpExchange) throws IOException {
    // File cat503 = new File("503.png");

    // httpExchange.getResponseHeaders().set("Content-Type", "image/png");
    // httpExchange.sendResponseHeaders(503, 0);

    // OutputStream os = httpExchange.getResponseBody();
    // FileInputStream fs = new FileInputStream(cat503);

    // final byte[] buffer = new byte[0x10000];
    // int count = 0;
    // while ((count = fs.read(buffer)) >= 0) {
    // os.write(buffer, 0, count);
    // }
    // fs.close();
    // os.close();
    // }

    private static void sendErrorResponse(DataOutputStream out, int statusCode, String statusMessage)
            throws IOException {
        out.writeBytes("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        out.writeBytes("Content-Length: 0\r\n");
        out.writeBytes("\r\n");
    }

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(8000);
        Socket clientSocket = null;
        while ((clientSocket = serverSocket.accept()) != null) {
            System.out.println("im so tired");
            handleRequest(clientSocket);
        }
        serverSocket.close();
    }
}
