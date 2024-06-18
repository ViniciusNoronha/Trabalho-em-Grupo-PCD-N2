

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final List<Livro> livros;

    public ClientHandler(Socket socket, List<Livro> livros) {
        this.socket = socket;
        this.livros = livros;
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
        synchronized (livros) {
            for (Livro livro : livros) {
                if (livro.getTitulo().equalsIgnoreCase(titulo) && livro.getExemplares() > 0) {
                    livro.setExemplares(livro.getExemplares() - 1);
                    BibliotecaServer.salvarLivros();
                    out.println("Livro alugado com sucesso!");
                    return;
                }
            }
            out.println("Livro não disponível para aluguel.");
        }
    }

    private void processReturn(String titulo, PrintWriter out) {
        synchronized (livros) {
            for (Livro livro : livros) {
                if (livro.getTitulo().equalsIgnoreCase(titulo)) {
                    livro.setExemplares(livro.getExemplares() + 1);
                    BibliotecaServer.salvarLivros();
                    out.println("Livro devolvido com sucesso!");
                    return;
                }
            }
            out.println("Livro não encontrado para devolução.");
        }
    }

    private void processAdd(String data, PrintWriter out) {
        synchronized (livros) {
            Livro novoLivro = new Gson().fromJson(data, Livro.class);
            livros.add(novoLivro);
            BibliotecaServer.salvarLivros();
            out.println("Livro adicionado com sucesso!");
        }
    }
}
