
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaServer {
    private static final int PORT = 12129;
    private static List<Livro> livros;

    public static void main(String[] args) {
        carregarLivros();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, livros)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarLivros() {
        try (Reader reader = new FileReader("src/livros.json")) {
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            Type listType = new TypeToken<ArrayList<Livro>>() {}.getType();
            livros = new Gson().fromJson(jsonObject.get("livros"), listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void salvarLivros() {
        try (Writer writer = new FileWriter("src/livros.json")) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("livros", new Gson().toJsonTree(livros));
            new Gson().toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
