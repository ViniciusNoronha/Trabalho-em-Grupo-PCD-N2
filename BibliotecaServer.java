import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaServer {
    private static List<Livro> livros = new ArrayList<>();
    private static final String FILENAME = "C:\\Users\\guide\\OneDrive\\Documentos\\Servidor\\livros.json";

    public static void main(String[] args) {
        carregarLivros();
        try (ServerSocket serverSocket = new ServerSocket(12341)) {
            System.out.println("Servidor iniciado na porta 12341");

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
            System.out.println("Livros carregados: " + livros);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseJson(String jsonString) {
        livros.clear();
        jsonString = jsonString.trim().substring(11, jsonString.length() - 2); // Remove o início e fim do JSON
        String[] livroStrings = jsonString.split("},\\s*\\{");

        for (String livroString : livroStrings) {
            livroString = livroString.replace("{", "").replace("}", "").trim();
            String[] attributes = livroString.split("\",\\s*\"");
            String titulo = "", autor = "", genero = "";
            int exemplares = 0;

            for (String attribute : attributes) {
                String[] keyValue = attribute.split("\":\\s*\"");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();

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

        private String listLivros() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < livros.size(); i++) {
                Livro livro = livros.get(i);
                sb.append("{ \"titulo\": \"").append(livro.getTitulo())
                        .append("\", \"autor\": \"").append(livro.getAutor())
                        .append("\", \"genero\": \"").append(livro.getGenero())
                        .append("\", \"exemplares\": ").append(livro.getExemplares()).append("}");
                if (i < livros.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        private void handleRent(String data, PrintWriter out) {
            Livro livro = findLivro(data);
            if (livro != null && livro.getExemplares() > 0) {
                System.out.println("Alugando livro: " + livro);
                livro.setExemplares(livro.getExemplares() - 1);
                salvarLivros();
                out.println("Aluguel realizado com sucesso");
            } else {
                System.out.println("Livro indisponível ou não encontrado: " + data);
                out.println("Livro indisponível");
            }
        }

        private void handleReturn(String data, PrintWriter out) {
            Livro livro = findLivro(data);
            if (livro != null) {
                livro.setExemplares(livro.getExemplares() + 1);
                salvarLivros();
                out.println("Devolução realizada com sucesso");
            } else {
                out.println("Livro não encontrado");
            }
        }

        private void handleAdd(String data, PrintWriter out) {
            Livro novoLivro = parseLivro(data);
            livros.add(novoLivro);
            salvarLivros();
            out.println("Livro adicionado com sucesso");
        }

        private Livro findLivro(String titulo) {
            return livros.stream().filter(l -> l.getTitulo().equalsIgnoreCase(titulo)).findFirst().orElse(null);
        }

        private Livro parseLivro(String data) {
            data = data.replace("{", "").replace("}", "").trim();
            String[] attributes = data.split(",\\s*\"");
            String titulo = "", autor = "", genero = "";
            int exemplares = 0;

            for (String attribute : attributes) {
                String[] keyValue = attribute.split("\":\\s*\"");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();

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
            }
            return new Livro(titulo, autor, genero, exemplares);
        }
    }
}
