package http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class HttpClient {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        boolean continueConnection = true;//controlar se a conexão com o servidor deve ser mantida ou encerrada.

        while (continueConnection) {//permite que o cliente interaja com o servidor até encerrar a conexao
            System.out.println("Solicitação HTTP: GET, POST, PUT, DELETE, para fechar a conexão: QUIT");
            String method = reader.readLine().toUpperCase();//converte a entrada do cliente para letras maiusculas

            switch (method) {//le a entrada do cliente e envia o metodo com os dados inseridos que processam cada solicitacao
                case "GET":
                    sendRequest("GET");
                    break;
                case "POST":
                    System.out.println("Insira os dados para que quer adicionar(POST):");
                    String postData = reader.readLine();
                    sendRequest("POST", postData);
                    break;
                case "PUT":
                    System.out.println("Insira o índice que quer atualizar e o conteúdo que quer atualizar(PUT):");
                    String putData = reader.readLine();
                    sendRequest("PUT", putData);
                    break;
                case "DELETE":
                    System.out.println("Insira o índice que quer deletar(DELETE):");
                    int deleteIndex = Integer.parseInt(reader.readLine());
                    sendDeleteRequest(deleteIndex);
                    break;

                case "QUIT":
                    System.out.println("Conexão Encerrada");
                    continueConnection = false;
                    break;
                default:
                    System.out.println("Método inválido.");
            }
        }

        reader.close();//fecha o leitor quando a conexao e encerrada
    }

    private static void sendRequest(String method) throws IOException {
        URL url = new URL("http://localhost:8080/" + method.toLowerCase());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        int responseCode = connection.getResponseCode();
        System.out.println(method + " Código de resposta: " + responseCode);

        Map<String, List<String>> headers = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String value : headerValues) {
                System.out.println(headerName + ": " + value);
            }
        }

        if (method.equals("GET")) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        }

        connection.disconnect();
    }


    private static void sendRequest(String method, String data) throws IOException {
        URL url = new URL("http://localhost:8080/" + method.toLowerCase());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        
        // Define os cabeçalhos para solicitações POST, PUT e DELETE
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes("utf-8"));
        }

        int responseCode = connection.getResponseCode();
        System.out.println(method + " Código de resposta: " + responseCode);

        Map<String, List<String>> headers = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String value : headerValues) {
                System.out.println(headerName + ": " + value);
            }
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder responseBody = new StringBuilder();
            while ((line = in.readLine()) != null) {
                responseBody.append(line);
            }
            System.out.println("Response Body: " + responseBody.toString());
        }

        connection.disconnect();
    }


    private static void sendDeleteRequest(int index) throws IOException {
        String data = String.valueOf(index);
        URL url = new URL("http://localhost:8080/delete");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes("utf-8"));
        }

        int responseCode = connection.getResponseCode();
        System.out.println("DELETE Código de resposta: " + responseCode);

        Map<String, List<String>> headers = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String value : headerValues) {
                System.out.println(headerName + ": " + value);
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();

        connection.disconnect();
    }

}