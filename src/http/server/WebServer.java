///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serveur HTTP simple pouvant correctement recevoir 5 type de requetes differentes :
 * - GET, pour recuperer le contenu d'un fichier sur le serveur
 * - DELETE, pour supprimer un fichier ou un dossier vide
 * - PUT, pour ecrire par dessus un fichier (ou le creer)
 * - POST, pour ajouter du contenu a la fin d'un fichier (ou le creer)
 * - HEAD, pour recuperer le header renvoye par une methode GET equivalente.
 *
 * De plus, ce serveur gere de nombreuses cas nominaux et non nominaux differents, dont notamment les codes 200, 400,
 * 403, 404 et 500 (et plusieurs autres).
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class WebServer {

    /**
     * Le chemin de base pour acceder aux ressources du serveur.
     */
    private static final String BASE_PATH = "resources";

    // #-- Constructing generic response

    /**
     * Construit une reponse HTTP a partir de parametres generiques. Cette methode est principalement destinee a etre
     * utilisee par des methodes specialisantes au dessus.
     * @param responseCode La premiere ligne de la reponse (code, description, version HTTP)
     * @param content Le contenu de la reponse
     * @param restOfHeader D'autres lignes a ajouter au header
     * @return La reponse construite a partir des parametres donnes.
     */
    private Response constructGenericResponse(String responseCode, byte[] content, String... restOfHeader) {
        StringBuilder header = new StringBuilder(responseCode);
        for (String line : restOfHeader) {
            header.append("\n").append(line);
        }
        header.append("\n").append("Server: Bot");
        return new Response(header.toString(), content);
    }

    /**
     * Construit une reponse HTTP contenant du HTML a partir de deux parametres : le statut de la reponse et son
     * contenu.
     * @param responseType La premiere ligne du header de la reponse.
     * @param content Le contenu de la reponse.
     * @return La reponse construite avec le bon header et le contenu passe en parametre.
     * @see #constructGenericResponse(String, byte[], String[])
     */
    private Response constructGenericHTMLResponse(String responseType, String content) {
        return constructGenericResponse(responseType, content.getBytes(), "Content-Type: text/html");
    }

    // - Good (2XX)

    /**
     * Construit une reponse HTTP 200 (OK) avec le type MIME et le contenu passe en parametre, indiquant que tout s'est
     * bien passe.
     * @param contentType Le type MIME du contenu.
     * @param content Le contenu de la reponse.
     * @return La reponse construite.
     */
    private Response constructOKResponse(String contentType, byte[] content) {
        return constructGenericResponse(
                "HTTP/1.0 200 OK",
                content,
                "Content-Type: " + contentType
        );
    }

    /**
     * Construit une reponse HTTP 201 (CREATED), indiquant que la ressource au chemin passe en parametre a ete cree.
     * @param resource Le chemin vers la ressource creee.
     * @return La reponse construite.
     */
    private Response constructCreatedResponse(String resource) {
        return constructGenericResponse(
                "HTTP/1.0 201 CREATED",
                new byte[] {},
                "Content-Location: " + resource
        );
    }

    /**
     * Construit une reponse HTTP 204 (NO CONTENT), indiquant que tout s'est bien passe sans necessite d'informations
     * supplementaires ou de corps de reponse.
     * @return La reponse construite.
     */
    private Response constructOKNoContentResponse() {
        return constructGenericResponse(
                "HTTP/1.0 204 NO CONTENT",
                new byte[] {}
        );
    }

    // - External errors (4XX)

    /**
     * Construit une reponse HTTP 400 (BAD REQUEST), indiquant une requete mal formee ou a la demande impossible.
     * @return La reponse construite.
     */
    private Response constructBadRequestResponse() {
        return constructGenericHTMLResponse(
                "HTTP/1.0 400 BAD REQUEST",
                "<h1 style='color: red;'>Erreur 400 : requete mal formee.</h1>\n"
        );
    }

    /**
     * Construit une reponse HTTP 403 (FORBIDDEN), indiquant que l'acces a la ressource passee en parametre est
     * impossible et/ou interdit.
     * @param resource Le chemin vers la ressource impossible d'acces.
     * @return La reponse construite.
     */
    private Response constructForbiddenResponse(String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 403 FORBIDDEN",
                "<h1 style='color: darkred'>Erreur 403 : '" + resource + "' n'est pas accessible</h1>\n"
        );
    }

    /**
     * Construit une reponse HTTP 404 (NOT FOUND), indiquant que la ressource passee en parametre n'existe pas pour
     * le serveur.
     * @param resource Le chemin vers la ressource inexistante.
     * @return La reponse construite.
     */
    private Response constructNotFoundResponse(String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 404 NOT FOUND",
                "<h1 style='color: darkred'>Erreur 404 : '" + resource + "' est introuvable</h1>\n"
        );
    }

    /**
     * Construit une reponse HTTP 406 (NOT ACCEPTABLE), indiquant qu'une methode n'est pas applicable a une ressource.
     * @param method La methode dont l'application est impossible.
     * @param resource La ressource sur laquelle la methode est inapplicable.
     * @return La reponse construite.
     */
    private Response constructNotAcceptableResponse(String method, String resource) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 406 NOT ACCEPTABLE",
                "<h1 style='color: darkred'>Erreur 406 : " + method + " inutilisable sur '" + resource + "'</h1>\n"
        );
    }

    // - Internal errors (5XX)

    /**
     * Construit une reponse HTTP 500 (INTERNAL SERVER ERROR), indiquant qu'une erreur interne au serveur est arrive.
     * @param message Le message d'erreur a afficher.
     * @return La reponse construite.
     */
    private Response constructInternalErrorResponse(String message) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 500 INTERNAL SERVER ERROR",
                "<h1 style='color: darkred'>Erreur 500 : '" + message + "'</h1>\n"
        );
    }

    /**
     * Construit une reponse HTTP 501, indiquant que la methode HTTP demandee n'est pas implementee sur ce serveur.
     * @param method La methode non-implementee.
     * @return La reponse construite.
     */
    private Response constructNotImplementedResponse(String method) {
        return constructGenericHTMLResponse(
                "HTTP/1.0 501 NOT IMPLEMENTED",
                "<h1 style='color: darkred'>Erreur 501 : Service '" + method + "' non implemente</h1>\n"
        );
    }

    // #-- Handling different HTTP methods

    /**
     * Implementation de la methode HTTP get, allant chercher le contenu d'un fichier pour le renvoyer.
     * @param in Le contenu complet de la requete.
     * @return La reponse a renvoyer au client.
     * @throws IOException En cas ou d'erreur de lecture du contenu de la requete, ou d'erreur de manipulation du
     * fichier.
     */
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

    /**
     * Implementation de la methode HTTP POST. Note implementation est simpliste, car elle permet simplement d'ajouter a
     * la fin d'un fichier le contenu passe en corps de requete (ou de creer le fichier avec ce contenu s'il n'existe
     * pas deja).
     * @param in Le contenu complet de la requete.
     * @return La reponse a renvoyer au client.
     * @throws IOException En cas ou d'erreur de lecture du contenu de la requete, ou d'erreur de manipulation du
     * fichier.
     */
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

    /**
     * Implementation de la methode HTTP HEAD, renvoyant la meme en-tete qu'une methode GET appelee sur la meme
     * ressource, sans le contenu d'une telle reponse.
     * @param in Le contenu complet de la requete.
     * @return La reponse a renvoyer au client.
     * @throws IOException En cas ou d'erreur de lecture du contenu de la requete, ou d'erreur de manipulation du
     * fichier.
     */
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

    /**
     * Implementation de la methode HTTP PUT, permettant de remplacer le contenu d'un fichier avec le contenu de la
     * requete (ou de creer le fichier s'il n'existe pas).
     * @param in Le contenu complet de la requete.
     * @return La reponse a renvoyer au client.
     * @throws IOException En cas ou d'erreur de lecture du contenu de la requete, ou d'erreur de manipulation du
     * fichier.
     */
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

    /**
     * Implementation de la methode HTTP DELETE, permettant de supprimer une ressource (si elle peut etre supprimee).
     * @param in Le contenu complet de la requete.
     * @return La reponse a renvoyer au client.
     * @throws IOException En cas ou d'erreur de lecture du contenu de la requete, ou d'erreur de manipulation du
     * fichier.
     */
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

    /**
     * Renvoie une reponse BAD REQUEST entierement construite.
     * @return Une reponse BAD REQUEST.
     */
    private Response badRequest() {
        return constructBadRequestResponse();
    }

    /**
     * Renvoie une reponse NOT IMPLEMENTED indiquant que la methode HTTP passee en parametre n'est pas implementee.
     * @param method La methode HTTP non implementee.
     * @return Une reponse NOT IMPLEMENTED.
     */
    private Response notImplemented(String method) {
        return constructNotImplementedResponse(method);
    }

    // #-- The server itself

    /**
     * Lance le serveur HTTP pour qu'il ecoute les connexions de client sur le port passe en parametre.
     * @param port Le port sur lequel ecouter les connexions.
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
     * Fonction principale, lancee au lancement du programme. Elle lance un serveur des son appel.
     * @param args Les parametres ne sont pas utilises.
     */
    public static void main(String[] args) {
        WebServer ws = new WebServer();
        ws.start(80);
    }
}
