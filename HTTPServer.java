import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class HTTPServer {

    static class MyHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if ("GET".equals(httpExchange.getRequestMethod())) {
                handleGetRequest(httpExchange);
               
            } else if ("POST".equals(httpExchange.getRequestMethod())) {
                handlePostRequest(httpExchange);
            } else if ("PUT".equals(httpExchange.getRequestMethod())) {
                handlePutRequest(httpExchange);
            } else if ("DELETE".equals(httpExchange.getRequestMethod())) {
                handleDeleteRequest(httpExchange);
            } else if ("OPTIONS".equals(httpExchange.getRequestMethod())) {
                handleOptionRequest(httpExchange);
            } else if ("HEAD".equals(httpExchange.getRequestMethod())) {
                handleHeadRequest(httpExchange);
            } else {
                handle404(httpExchange);
            }
            
        }

        private void handleGetRequest(HttpExchange httpExchange) throws IOException {
            String requestType = null;
            URI uri = httpExchange.getRequestURI();
            File file = new File("." + uri.getPath()).getCanonicalFile();

            if (!file.isFile()) {
                handle404(httpExchange);
            } else {
                // Object exists and is a file: accept with response code 200.

                requestType = handleMIMEType(httpExchange);
                httpExchange.getResponseHeaders().set("Content-Type", requestType);
                httpExchange.sendResponseHeaders(200, 0);
                
                OutputStream os = httpExchange.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                System.out.println("pic process");
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
            }

        }

        private void handlePostRequest(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            File file = new File("." + uri.getPath()).getCanonicalFile();
            String requestType = handleMIMEType(httpExchange);

            if (!file.isFile()) {
                // Object does not exist or is not a file: reject with 404 error.
                handle404(httpExchange);
            } else if (requestType.equals("text/plain")) {
                InputStream msg = httpExchange.getRequestBody();
                try {
                    FileWriter myWriter = new FileWriter(file, true);
                    myWriter.write(msg.toString());
                    myWriter.close();
                    System.out.println("Successfully wrote to the file.");
                    String response = "201 (Created)\n";
                    httpExchange.sendResponseHeaders(201, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    handle503(httpExchange);
                }
            } else {
                handle404(httpExchange);
            }
        }

        private void handlePutRequest(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            File file = new File("." + uri.getPath()).getCanonicalFile();
            String requestType = handleMIMEType(httpExchange);

            if (file.isFile()) {
                try {
                    if (requestType.equals("image/png")) {
                        httpExchange.getResponseHeaders().set("Content-Type", requestType);
                        BufferedImage img = ImageIO.read(httpExchange.getRequestBody());
                        ImageIO.write(img, "PNG", file);
                    } else {
                        InputStream content = httpExchange.getRequestBody();
                        FileWriter myWriter = new FileWriter(file, false);
                        myWriter.write(content.toString());
                        myWriter.close();
                    }
                    System.out.println("Successfully overwrote to the file.");
                    String response = "201 (Created)\n";
                    httpExchange.sendResponseHeaders(201, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    handle503(httpExchange);
                }
            } else if (file.createNewFile()) {
                try {
                    if (requestType.equals("image/png")) {
                        httpExchange.getResponseHeaders().set("Content-Type", requestType);
                        BufferedImage img = ImageIO.read(httpExchange.getRequestBody());
                        ImageIO.write(img, "PNG", file);
                    } else {
                        InputStream content = httpExchange.getRequestBody();
                        FileWriter myWriter = new FileWriter(file, false);
                        myWriter.write(content.toString());
                        myWriter.close();
                    }
                    System.out.println("File created: " + file.getName());
                    String response = "201 (Created)\n";
                    httpExchange.sendResponseHeaders(201, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    handle500(httpExchange);
                }
            } else {
                handle404(httpExchange);
            }
        }

        private void handleDeleteRequest(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            File file = new File("." + uri.getPath()).getCanonicalFile();

            if (file.delete()) {
                try {
                    System.out.println("File deleted: " + file.getName());
                    String response = "202 (Accepted)\n";
                    httpExchange.sendResponseHeaders(202, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    String response = "500 (Internal Server Error)\n";
                    handle500(httpExchange);
                }
            } else {
                handle404(httpExchange);
            }
        }

        private void handleOptionRequest(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String requestType = handleMIMEType(httpExchange);
            String actions = "GET" + "\n" + "OPTIONS" + "\n" + "HEAD" + "\n";
            File file = new File("." + uri.getPath()).getCanonicalFile();

            if (file.exists()) {
                actions = actions + "PUT" + "\n" + "DELETE";
                if (requestType.equals("text/plain")) {
                        actions = actions + "POST";
                }
            } else {
                actions = actions + "PUT";
            }
            
            httpExchange.sendResponseHeaders(200, actions.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(actions.getBytes());
            os.close();
        }

        private void handleHeadRequest(HttpExchange httpExchange) throws IOException {
            String header = httpExchange.getResponseHeaders().toString();
            httpExchange.sendResponseHeaders(200, header.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(header.getBytes());
            os.close();
        }

        private String handleMIMEType(HttpExchange httpExchange) {
            if (httpExchange.getRequestURI().toString().endsWith(".png")) {
                return "image/png";
            }
            return "text/plain";
        }

        private void handle404(HttpExchange httpExchange) throws IOException{
            File cat404 = new File("404.png");

            httpExchange.getResponseHeaders().set("Content-Type", "image/png");
            httpExchange.sendResponseHeaders(404, 0);

            OutputStream os = httpExchange.getResponseBody();
            FileInputStream fs = new FileInputStream(cat404);

            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }

        private void handle500(HttpExchange httpExchange) throws IOException{
            File cat500 = new File("500.png");

            httpExchange.getResponseHeaders().set("Content-Type", "image/png");
            httpExchange.sendResponseHeaders(500, 0);

            OutputStream os = httpExchange.getResponseBody();
            FileInputStream fs = new FileInputStream(cat500);

            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }

        private void handle503(HttpExchange httpExchange) throws IOException{
            File cat503 = new File("503.png");

            httpExchange.getResponseHeaders().set("Content-Type", "image/png");
            httpExchange.sendResponseHeaders(503, 0);

            OutputStream os = httpExchange.getResponseBody();
            FileInputStream fs = new FileInputStream(cat503);

            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }

    }

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 8000), 0);
        server.createContext("/", new MyHttpHandler());
        server.setExecutor(threadPoolExecutor);
        server.start();
        
    }
}
