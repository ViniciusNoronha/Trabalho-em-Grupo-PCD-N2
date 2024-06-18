import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;


import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaServer {
    private static final int PORT = 12310;
    private static List<Livro> livros;

    public static void main(String[] args) {
        carregarLivros();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClienteHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarLivros() {
        try (Reader reader = new FileReader("src/livros.json")) {
            // Ajuste para ler o objeto raiz "livros"
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            Type listType = new TypeToken<ArrayList<Livro>>() {}.getType();
            livros = new Gson().fromJson(jsonObject.get("livros"), listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void salvarLivros() {
        try (Writer writer = new FileWriter("src/livros.json")) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("livros", new Gson().toJsonTree(livros));
            new Gson().toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private final Socket socket;

        ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(" ", 2);
                    String command = parts[0];
                    String data = parts.length > 1 ? parts[1] : null;

                    switch (command) {
                        case "LIST":
                            out.println(new Gson().toJson(livros));
                            break;
                        case "BORROW":
                            processBorrow(data, out);
                            break;
                        case "RETURN":
                            processReturn(data, out);
                            break;
                        case "ADD":
                            processAdd(data, out);
                            break;
                        default:
                            out.println("Comando desconhecido: " + command);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processBorrow(String titulo, PrintWriter out) {
            for (Livro livro : livros) {
                if (livro.getTitulo().equalsIgnoreCase(titulo) && livro.getExemplares() > 0) {
                    livro.setExemplares(livro.getExemplares() - 1);
                    salvarLivros();
                    out.println("Livro alugado com sucesso!");
                    return;
                }
            }
            out.println("Livro não disponível para aluguel.");
        }

        private void processReturn(String titulo, PrintWriter out) {
            for (Livro livro : livros) {
                if (livro.getTitulo().equalsIgnoreCase(titulo)) {
                    livro.setExemplares(livro.getExemplares() + 1);
                    salvarLivros();
                    out.println("Livro devolvido com sucesso!");
                    return;
                }
            }
            out.println("Livro não encontrado para devolução.");
        }

        private void processAdd(String data, PrintWriter out) {
            Livro novoLivro = new Gson().fromJson(data, Livro.class);
            livros.add(novoLivro);
            salvarLivros();
            out.println("Livro adicionado com sucesso!");
        }
    }
}
