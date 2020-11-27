///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serveur HTTP simple pouvant correctement recevoir 5 type de requêtes différentes :
 * - GET, pour récupérer le contenu d'un fichier sur le serveur
 * - DELETE, pour supprimer un fichier ou un dossier vide
 * - PUT, pour écrire par dessus un fichier (ou le créer)
 * - POST, pour ajouter du contenu à la fin d'un fichier (ou le créer)
 * - HEAD, pour récupérer le header renvoyé par une méthode GET équivalente.
 *
 * De plus, ce serveur gère de nombreuses cas nominaux et non nominaux différents, dont notamment les codes 200, 400,
 * 403, 404 et 500 (et plusieurs autres).
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class WebServer {

    /**
     * Le chemin de base pour accéder aux ressources du serveur.
     */
    private static final String BASE_PATH = "resources";

    // #-- Constructing generic response

    /**
     * Construit une réponse HTTP à partir de paramètres génériques. Cette méthode est principalement destinée à être
     * utilisée par des méthodes spécialisantes au dessus.
     * @param responseCode La première ligne de la réponse (code, description, version HTTP)
     * @param content Le contenu de la réponse
     * @param restOfHeader D'autres lignes à ajouter au header
     * @return La réponse construite à partir des paramètres donnés.
     */
    private Response constructGenericResponse(String responseCode, byte[] content, String... restOfHeader) {
        StringBuilder header = new StringBuilder(responseCode);
        for (String line : restOfHeader) {
            header.append("\n").append(line);
        }
        header.append("\n").append("Server: Bot");
        return new Response(header.toString(), content);
    }

    private Response constructGenericHTMLResponse(String responseCode, String content) {
        return constructGenericResponse(responseCode, content.getBytes(), "Content-Type: text/html");
    }

    // - Good (2XX)

    // 200
    private Response constructOKResponse(String contentType, byte[] content) {
        return constructGenericResponse(
                "HTTP/1.0 200 OK",
                content,
                "Content-Type: " + contentType
        );
    }

    // 201
    private Response constructCreatedResponse(String resource) {
        return constructGenericResponse(
                "HTTP/1.0 201 CREATED",
                new byte[] {},
                "Content-Location: " + resource
        );
    }

    // 204
    private Response constructOKNoContentResponse() {
        return constructGenericResponse(
                "HTTP/1.0 204 NO CONTENT",
                new byte[] {}
        );
    }

    // - External errors (4XX)

    // 400
    private Response constructBadRequestResponse() {
        return constructGenericHTMLResponse(
                "HTTP/1.0 400 BAD REQUEST",
                "<h1 style='color: red;'>Erreur 400 : requete mal formee.</h1>\n"
        );
    }

    // 403
    private Response constructForbiddenResponse(String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 403 FORBIDDEN",
                "<h1 style='color: darkred'>Erreur 403 : '" + resource + "' n'est pas accessible</h1>\n"
        );
    }

    // 404
    private Response constructNotFoundResponse(String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 404 NOT FOUND",
                "<h1 style='color: darkred'>Erreur 404 : '" + resource + "' est introuvable</h1>\n"
        );
    }

    // 406
    private Response constructNotAcceptableResponse(String method, String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 406 NOT ACCEPTABLE",
                "<h1 style='color: darkred'>Erreur 406 : " + method + " inutilisable sur '" + resource + "'</h1>\n"
        );
    }

    // - Internal errors (5XX)

    // 500
    private Response constructInternalErrorResponse(String message) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 500 INTERNAL SERVER ERROR",
                "<h1 style='color: darkred'>Erreur 500 : '" + message + "'</h1>\n"
        );
    }

    // 501
    private Response constructNotImplementedResponse(String method) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 501 NOT IMPLEMENTED",
                "<h1 style='color: darkred'>Erreur 501 : Service '" + method + "' non implemente</h1>\n"
        );
    }

    // #-- Handling different HTTP methods

    private Response get(BufferedReader in) throws IOException {
        // Extract important headers info
        String[] requestArgs = in.readLine().split(" ");
        if (requestArgs.length != 3) {
            return constructBadRequestResponse();
        }

        // Skip rest of header
        String resource = requestArgs[1];
        Path fullPath = Path.of(BASE_PATH + "/" + resource);

        // Check error cases
        if (!Files.exists(fullPath)) {
            return constructNotFoundResponse(resource);
        } else if (Files.isDirectory(fullPath)) {
            return constructNotAcceptableResponse("GET", resource);
        } else if (!Files.isReadable(fullPath)) {
            return constructForbiddenResponse(resource);
        }

        // Read the file and create a response with its content
        byte[] content = Files.readAllBytes(fullPath);
        return constructOKResponse(Files.probeContentType(fullPath), content);
    }

    private Response post(BufferedReader in) throws IOException {
        // Extract important headers info
        String[] requestArgs = in.readLine().split(" ");
        if (requestArgs.length != 3) {
            return constructBadRequestResponse();
        }

        // Skip rest of header
        for (String line = in.readLine(); line != null && !line.equals(""); line = in.readLine());

        // Get resource path
        String resource = requestArgs[1];
        Path fullPath = Path.of(BASE_PATH + "/" + resource);
        boolean existedBefore = Files.exists(fullPath);

        // Append the content of the request to the file (creates it if it doesn't exist)
        try {
            FileWriter fileWriter = new FileWriter(fullPath.toString(), true);
            if (existedBefore) {
                fileWriter.write('\n');
            }
            while (in.ready()) {
                int c = in.read();
                if (c == -1)
                    break;
                fileWriter.write(c);
            }
            fileWriter.flush();
        } catch (IOException e) {
            if (existedBefore) {
                if (!Files.isReadable(fullPath) || !Files.isWritable(fullPath))
                    return constructForbiddenResponse(resource);
                if (Files.isDirectory(fullPath))
                    return constructNotAcceptableResponse("PUT", resource);
            }
            throw e;
        }

        // Return nominal responses
        if (existedBefore) {
            return constructOKNoContentResponse();
        } else {
            return constructCreatedResponse(resource);
        }
    }

    private Response head(BufferedReader in) throws IOException {
        // Extract important headers info
        String[] requestArgs = in.readLine().split(" ");
        if (requestArgs.length != 3) {
            return constructBadRequestResponse();
        }

        // Skip rest of header
        String resource = requestArgs[1];
        Path fullPath = Path.of(BASE_PATH + "/" + resource);

        // Check error cases
        Response response;
        if (!Files.exists(fullPath)) {
            response = constructNotFoundResponse(resource);
        } else if (Files.isDirectory(fullPath)) {
            response = constructNotAcceptableResponse("GET", resource);
        } else if (!Files.isReadable(fullPath)) {
            response = constructForbiddenResponse(resource);
        } else {
            response = constructOKResponse(Files.probeContentType(fullPath), new byte[] {});
        }

        // Return nominal response WITHOUT CONTENT (it's the purpose of the HEAD method)
        response.setContent(new byte[] {});
        return response;
    }

    private Response put(BufferedReader in) throws IOException {
        // Extract important headers info
        String[] requestArgs = in.readLine().split(" ");
        if (requestArgs.length != 3) {
            return constructBadRequestResponse();
        }

        // Skip rest of header
        for (String line = in.readLine(); line != null && !line.equals(""); line = in.readLine());

        // Get resource path
        String resource = requestArgs[1];
        Path fullPath = Path.of(BASE_PATH + "/" + resource);
        boolean existedBefore = Files.exists(fullPath);

        // Write the content of the request in the file (overwriting it if it already exists)
        try {
            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(fullPath.toString()));
            while (in.ready()) {
                int c = in.read();
                if (c == -1)
                    break;
                fileOut.write(c);
            }
            fileOut.flush();
        } catch (IOException e) {
            if (existedBefore) {
                if (!Files.isReadable(fullPath) || !Files.isWritable(fullPath))
                    return constructForbiddenResponse(resource);
                if (Files.isDirectory(fullPath))
                    return constructNotAcceptableResponse("PUT", resource);
            }
            throw e;
        }

        // Return nominal responses
        if (existedBefore) {
            return constructOKNoContentResponse();
        } else {
            return constructCreatedResponse(resource);
        }
    }

    private Response delete(BufferedReader in) throws IOException {
        // Extract important headers info
        String[] requestArgs = in.readLine().split(" ");
        if (requestArgs.length != 3) {
            return constructBadRequestResponse();
        }

        // Get resource path
        String resource = requestArgs[1];
        Path fullPath = Path.of(BASE_PATH + "/" + resource);

        // Check error cases
        if (!Files.exists(fullPath)) {
            return constructNotFoundResponse(resource);
        } else if (!Files.isReadable(fullPath)) {
            return constructForbiddenResponse(resource);
        }

        // Delete the file
        try {
            Files.delete(fullPath);
        } catch (DirectoryNotEmptyException e) {
            return constructNotAcceptableResponse("DELETE", resource);
        }

        // Return nominal response
        return constructOKNoContentResponse();
    }

    private Response badRequest() {
        return constructBadRequestResponse();
    }

    private Response notImplemented(String method) {
        return constructNotImplementedResponse(method);
    }

    // #-- The server itself

    /**
     * Lance le serveur HTTP pour qu'il écoute les connexions de client sur le port passé en paramêtre.
     * @param port Le port sur lequel écouter les connexions.
     */
    protected void start(int port) {
        ServerSocket s;

        System.out.println("Webserver starting up on port " + port);
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (;;) {
            // wait for a connection
            try (Socket remote = s.accept()){

                // remote is now the connected socket
                System.out.println("Connection.");
                BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));

                // Analyze the header to use the proper HTTP service
                in.mark(512);
                String str = in.readLine();
                in.reset();

                // Get the response to send
                Response response;
                try {
                    if (str != null && !str.equals("")) {
                        String method = str.split(" ")[0];
                        switch (method) {
                            case "GET" -> response = get(in);
                            case "POST" -> response = post(in);
                            case "HEAD" -> response = head(in);
                            case "PUT" -> response = put(in);
                            case "DELETE" -> response = delete(in);
                            case "CONNECT", "OPTIONS", "TRACE", "PATCH" -> response = notImplemented(method);
                            default -> response = badRequest();
                        }
                    } else {
                        response = badRequest();
                    }
                } catch (Exception e) {
                    response = constructInternalErrorResponse(e.getMessage());
                }

                // Send the response
                response.printOnStream(remote.getOutputStream());
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement d'un requete : ");
                e.printStackTrace();
            }
        }
    }

    // #-- Main method

    /**
     * Fonction principale, lancée au lancement du programme. Elle lance un serveur dès son appel.
     * @param args Les paramètres ne sont pas utilisés.
     */
    public static void main(String[] args) {
        WebServer ws = new WebServer();
        ws.start(80);
    }
}
