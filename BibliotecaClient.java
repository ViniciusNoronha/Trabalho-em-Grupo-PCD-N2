

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BibliotecaClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12124;

    public static void main(String[] args) {
        try (Socket skt = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
             PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            String cmd;
            while (true) {
                System.out.println("Digite um comando (LIST, BORROW, RETURN, ADD, QUIT):");
                cmd = sc.nextLine();

                if (cmd.equalsIgnoreCase("QUIT")) {
                    break;
                }

                if (cmd.startsWith("BORROW") || cmd.startsWith("RETURN")) {
                    System.out.println("Digite o título do livro:");
                    String titulo = sc.nextLine();
                    out.println(cmd + " " + titulo);
                } else if (cmd.startsWith("ADD")) {
                    System.out.println("Digite os dados do livro em formato JSON:");
                    String data = sc.nextLine();
                    out.println(cmd + " " + data);
                } else {
                    out.println(cmd);
                }

                String resp;
                while ((resp = in.readLine()) != null) {
                    System.out.println(resp);
                    if (!in.ready()) break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
