package http;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final String DATA_FILE = "D:\\Documentos\\Eclipse-Workspace\\HTTPServer\\src\\http\\arquivo.txt"; // Caminho do arquivo

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);  // Cria um servidor socket que escuta na porta 8080
        System.out.println("Server listening on port 8080...");

        ExecutorService executorService = Executors.newFixedThreadPool(10);  // Cria um pool de threads para gerenciar as solicitações

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();  // Aguarda e aceita uma conexão de cliente
                executorService.submit(() -> handleClient(clientSocket));  // Envia o cliente para um thread do pool para tratamento
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Função que trata uma solicitação de cliente
    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            String requestLine = in.readLine();  // Lê a primeira linha da solicitação HTTP
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];  // Extrai o método HTTP (GET, POST, PUT, DELETE)
            String path = requestParts[1];  // Extrai o caminho do recurso solicitado

            String response = "";  // Inicializa a resposta a ser enviada de volta ao cliente

            if (method.equals("GET")) {  // Se o método for GET
                response = handleGetRequest();  // Trata a solicitação GET
            } else if (method.equals("POST")) {  // Se o método for POST
                // Extrai o comprimento do conteúdo da solicitação
                int contentLength = 0;
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                }
                // Lê o corpo da solicitação POST
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                String requestBody = new String(buffer);
                handlePostRequest(requestBody);  // Trata a solicitação POST
				response = "HTTP/1.1 200 OK\r\n\r\n";  // Cria uma resposta de sucesso
                
            } else if (method.equals("PUT")) {  // Se o método for PUT
                // Extrai o comprimento do conteúdo da solicitação
                int contentLength = 0;
                for (String line = in.readLine(); line != null && !line.isEmpty(); line = in.readLine()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                }
                // Lê o corpo da solicitação PUT
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                String putData = new String(buffer);

                handlePutRequest(putData);  // Trata a solicitação PUT
				response = "HTTP/1.1 200 OK\r\n\r\n";  // Cria uma resposta de sucesso;
            } else if (method.equals("DELETE")) {  // Se o método for DELETE
                // Extrai o comprimento do conteúdo da solicitação
                int contentLength = 0;

                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                }
                // Lê o corpo da solicitação DELETE (que contém o índice a ser removido)
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                int index = Integer.parseInt(new String(buffer).trim());

                if (index < 0) {
                    response = createErrorResponse(400, "Bad Request", "Invalid index.");  // Cria uma resposta de erro de solicitação inválida
                } else {
                    try {
                        handleDeleteRequest(index);  // Trata a solicitação DELETE
                        response = "HTTP/1.1 200 OK\r\n\r\n";  // Cria uma resposta de sucesso
                    } catch (IndexOutOfBoundsException e) {
                        response = createErrorResponse(404, "Not Found", "Index not found.");  // Cria uma resposta de erro de recurso não encontrado
                    }
                }
            } else if (method.equals("QUIT")) {  // Se o método for QUIT
                response = "HTTP/1.1 200 OK\r\n\r\n";  // Cria uma resposta de sucesso
                response += "Conexão encerrada. Obrigado por usar o servidor!\r\n";  // Adiciona uma mensagem de encerramento
                out.write(response.getBytes());  // Envia a resposta de encerramento para o cliente
                out.close();
                in.close();
                clientSocket.close();  // Fecha a conexão
            } else {
                response = createErrorResponse(400, "Bad Request", "Invalid method.");  // Cria uma resposta de erro de método inválido
            }

            out.write(response.getBytes());  // Envia a resposta ao cliente
            out.close();
            in.close();
            clientSocket.close();  // Fecha a conexão com o cliente
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String createResponse(int statusCode, String contentType, String responseBody) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(getStatusText(statusCode)).append("\r\n");
        response.append("Content-Type: ").append(contentType).append("\r\n");
        response.append("Content-Length: ").append(responseBody.length()).append("\r\n");
        response.append("Server: MeuServidorHTTP/1.0\r\n");
        response.append("\r\n\n"); // Adicionar uma linha em branco para separar cabeçalho do corpo
        response.append(responseBody);
        return response.toString();
    }
    
    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }


    private static String handleGetRequest() throws IOException {
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        String responseBody = response.toString();
        String fullResponse = createResponse(200, "text/plain", responseBody);

        return fullResponse;
    }


    private static String handlePostRequest(String postData) {
        System.out.println("Received POST data:");
        System.out.println(postData);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(DATA_FILE, true)))) {
            writer.print("\n" + postData);
            System.out.println("Data written to file.");
        } catch (IOException e) {
            e.printStackTrace();
            return createResponse(500, "text/plain", "Error writing data to file.");
        }

        String responseBody = "Data written to file.";
        return createResponse(200, "text/plain", responseBody);
    }
// Função para tratar solicitações PUT
    private static String handlePutRequest(String putData) {
        try {
            String[] parts = putData.split(" ", 2);
            if (parts.length >= 2) {
                int index = Integer.parseInt(parts[0]);
                String newData = parts[1];
                updateEntry(index, newData);

                String responseBody = "Data updated successfully.";
                return createResponse(200, "text/plain", responseBody);
            } else {
                String responseBody = "Invalid PUT request data.";
                return createResponse(400, "text/plain", responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String responseBody = "Error processing PUT request.";
            return createResponse(500, "text/plain", responseBody);
        }
    }

//Função para atualizar uma entrada no arquivo
private static void updateEntry(int index, String newData) throws IOException {
 List<String> lines = new ArrayList<>();

 try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
     String line;
     while ((line = reader.readLine()) != null) {
         lines.add(line);  // Lê cada linha do arquivo e a armazena na lista
     }
 }

 if (index >= 0 && index < lines.size()) {  // Verifica se o índice é válido
     lines.set(index, newData);  // Atualiza os dados na posição especificada pelo índice

     try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
         for (String line : lines) {
             writer.println(line);  // Escreve cada linha atualizada de volta no arquivo
         }
     }

     System.out.println("Data updated successfully.");  // Exibe uma mensagem de sucesso
 } else {
     System.out.println("Invalid index.");  // Exibe uma mensagem de erro se o índice for inválido
 }
}
// Função para tratar solicitações DELETE
private static String handleDeleteRequest(int index) {
    try {
        if (index < 0) {
            return createResponse(400, "text/plain", "Invalid index.");
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (index >= 0 && index < lines.size()) {
            lines.remove(index);

            try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
                for (String line : lines) {
                    if (!line.isEmpty()) {
                        writer.println(line);
                    }
                }
            }

            String responseBody = "Line at index " + index + " removed.";
            return createResponse(200, "text/plain", responseBody);
        } else {
            return createResponse(404, "text/plain", "Index not found.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return createResponse(500, "text/plain", "Error processing DELETE request.");
    }
}



    // Função para criar uma resposta de erro formatada
    private static String createErrorResponse(int statusCode, String statusText, String message) {
        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n\r\n" + message + "\r\n";
    }
}