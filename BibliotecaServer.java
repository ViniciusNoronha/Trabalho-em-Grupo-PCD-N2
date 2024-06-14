import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaServer {
    private static List<Livro> livros = new ArrayList<>();
    private static final String FILENAME = "livros.json";

    public static void main(String[] args) {
        carregarLivros();
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor iniciado na porta 12345");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarLivros() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME), "UTF-8"))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            parseJson(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseJson(String jsonString) {
        livros.clear();
        jsonString = jsonString.trim().replace("{ \"livros\": [", "").replace("]}", "").trim();
        String[] livroStrings = jsonString.split("}, \\{");
        for (String livroString : livroStrings) {
            livroString = livroString.replace("{", "").replace("}", "").trim();
            String[] attributes = livroString.split(",");
            String titulo = "", autor = "", genero = "";
            int exemplares = 0;
            for (String attribute : attributes) {
                String[] keyValue = attribute.split(":");
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                switch (key) {
                    case "titulo":
                        titulo = value;
                        break;
                    case "autor":
                        autor = value;
                        break;
                    case "genero":
                        genero = value;
                        break;
                    case "exemplares":
                        exemplares = Integer.parseInt(value);
                        break;
                }
            }
            livros.add(new Livro(titulo, autor, genero, exemplares));
        }
    }

    private static void salvarLivros() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILENAME), "UTF-8"))) {
            writer.write("{ \"livros\": [");
            for (int i = 0; i < livros.size(); i++) {
                Livro livro = livros.get(i);
                writer.write("{ \"titulo\": \"" + livro.getTitulo() + "\", \"autor\": \"" + livro.getAutor() + "\", \"genero\": \"" + livro.getGenero() + "\", \"exemplares\": " + livro.getExemplares() + "}");
                if (i < livros.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write("] }");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(":", 2);
                    String command = parts[0];
                    String data = parts.length > 1 ? parts[1] : "";

                    switch (command) {
                        case "LIST":
                            out.println(listLivros());
                            break;
                        case "RENT":
                            handleRent(data, out);
                            break;
                        case "RETURN":
                            handleReturn(data, out);
                            break;
                        case "ADD":
                            handleAdd(data, out);
                            break;
                        default:
                            out.println("Comando inválido");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
