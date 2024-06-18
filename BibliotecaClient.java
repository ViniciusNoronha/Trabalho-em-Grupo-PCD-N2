import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BibliotecaClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12310;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String command;
            while (true) {
                System.out.println("Digite um comando (LIST, BORROW, RETURN, ADD, QUIT):");
                command = scanner.nextLine();

                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                }

                if (command.startsWith("BORROW") || command.startsWith("RETURN")) {
                    System.out.println("Digite o título do livro:");
                    String titulo = scanner.nextLine();
                    out.println(command + " " + titulo);
                } else if (command.startsWith("ADD")) {
                    System.out.println("Digite os dados do livro em formato JSON:");
                    String data = scanner.nextLine();
                    out.println(command + " " + data);
                } else {
                    out.println(command);
                }

                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (!in.ready()) break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
