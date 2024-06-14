import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BibliotecaClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String command;
            while (true) {
                System.out.println("Digite um comando (LIST, RENT:<titulo>, RETURN:<titulo>, ADD:<livro>): ");
                command = scanner.nextLine();

                out.println(command);

                if (command.startsWith("LIST")) {
                    String response = in.readLine();
                    System.out.println(response);
                } else {
                    String response = in.readLine();
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
