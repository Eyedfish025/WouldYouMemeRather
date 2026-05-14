import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Scanner;

public class Game {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("🎮 Qual meme você prefere?\n");

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/pergunta"))
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            String body = response.body();

            String titulo1 = extrairValor(body, "titulo", 0);
            String url1    = extrairValor(body, "url", 0);
            String titulo2 = extrairValor(body, "titulo", 1);
            String url2    = extrairValor(body, "url", 1);

            System.out.println("\n👉 Qual você prefere?\n");
            System.out.println("1 - " + titulo1);
            System.out.println("   🔗 " + url1);
            System.out.println("\n2 - " + titulo2);
            System.out.println("   🔗 " + url2);
            System.out.print("\nEscolha (1/2) ou 'q' pra sair: ");

            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("q")) {
                System.out.println("\n👋 Valeu por jogar!");
                break;
            } else if (answer.equals("1") || answer.equals("2")) {
                System.out.println("🔥 Boa escolha!");
            } else {
                System.out.println("❌ Escolha inválida!");
            }
        }

        scanner.close();
    }

    // extrai o Nth valor de uma chave no JSON
    static String extrairValor(String json, String chave, int ocorrencia) {
        String busca = "\"" + chave + "\":\"";
        int index = 0;
        int pos = 0;
        while (index <= ocorrencia) {
            pos = json.indexOf(busca, pos);
            if (pos == -1) return "";
            pos += busca.length();
            index++;
        }
        int fim = json.indexOf("\"", pos);
        return json.substring(pos, fim);
    }
}