import java.util.ArrayList;
import java.util.List;

/**
 * Enum que representa os estados do AFD.
 * q0 -> quantidade par de 'a' lidas (estado inicial e final)
 * q1 -> quantidade ímpar de 'a' lidas
 */
enum Estado {
    Q0(true),   // estado final
    Q1(false);  // estado não final

    private final boolean estadoFinal;

    Estado(boolean estadoFinal) {
        this.estadoFinal = estadoFinal;
    }

    public boolean isFinal() {
        return estadoFinal;
    }
}

/**
 * Representa o resultado do processamento de uma cadeia pelo AFD.
 * Uso de record para imutabilidade e clareza.
 */
record ResultadoProcessamento(
        String cadeia,
        boolean aceita,
        int quantidadeDeAs,
        List<Estado> caminho,
        String erro // null se não houve erro
) {
    public boolean temErro() {
        return erro != null;
    }
}

/**
 * Autômato Finito Determinístico (AFD) que reconhece cadeias
 * com número PAR de símbolos 'a' sobre o alfabeto {a, b}.
 *
 * Expressão regular equivalente: b*(ab*a)*b*
 */
class AFD {

    private static final Estado ESTADO_INICIAL = Estado.Q0;

    /**
     * Função de transição δ(estado, símbolo) -> novo estado.
     * Lança exceção se o símbolo não pertence ao alfabeto {a, b}.
     */
    private Estado transicao(Estado atual, char simbolo) {
        return switch (simbolo) {
            case 'a' -> (atual == Estado.Q0) ? Estado.Q1 : Estado.Q0;
            case 'b' -> atual; // 'b' nunca muda o estado
            default -> throw new IllegalArgumentException(
                    "Símbolo inválido: '" + simbolo + "' não pertence ao alfabeto {a, b}");
        };
    }

    /**
     * Processa uma cadeia de entrada e retorna um ResultadoProcessamento
     * contendo aceitação, contagem de 'a's e o caminho de estados percorrido.
     */
    public ResultadoProcessamento processar(String cadeia) {
        Estado atual = ESTADO_INICIAL;
        List<Estado> caminho = new ArrayList<>();
        caminho.add(atual);
        int quantidadeDeAs = 0;

        for (char simbolo : cadeia.toCharArray()) {
            try {
                atual = transicao(atual, simbolo);
            } catch (IllegalArgumentException e) {
                return new ResultadoProcessamento(cadeia, false, quantidadeDeAs, caminho, e.getMessage());
            }
            caminho.add(atual);
            if (simbolo == 'a') {
                quantidadeDeAs++;
            }
        }

        boolean aceita = atual.isFinal();
        return new ResultadoProcessamento(cadeia, aceita, quantidadeDeAs, caminho, null);
    }

    /**
     * Processa uma lista de cadeias de teste, retornando os resultados na mesma ordem.
     */
    public List<ResultadoProcessamento> processarLista(List<String> cadeias) {
        List<ResultadoProcessamento> resultados = new ArrayList<>();
        for (String cadeia : cadeias) {
            resultados.add(processar(cadeia));
        }
        return resultados;
    }
}

/**
 * Classe responsável apenas pela formatação da saída no console (ANSI colors).
 */
class Formatador {

    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String AMARELO = "\u001B[33m";
    private static final String CIANO = "\u001B[36m";
    private static final String NEGRITO = "\u001B[1m";

    public static void imprimirCabecalho() {
        System.out.println(NEGRITO + CIANO +
                "=========================================================" + RESET);
        System.out.println(NEGRITO + CIANO +
                "   AFD - Cadeias com número PAR de símbolos 'a'          " + RESET);
        System.out.println(NEGRITO + CIANO +
                "   Regex equivalente: b*(ab*a)*b*                        " + RESET);
        System.out.println(NEGRITO + CIANO +
                "=========================================================" + RESET);
    }

    public static void imprimirResultado(ResultadoProcessamento resultado) {
        String cadeiaExibida = resultado.cadeia().isEmpty() ? "ε (vazia)" : resultado.cadeia();

        System.out.println("---------------------------------------------------------");
        System.out.println(NEGRITO + "Cadeia testada : " + RESET + cadeiaExibida);

        if (resultado.temErro()) {
            System.out.println(VERMELHO + NEGRITO + "Resultado      : ERRO" + RESET);
            System.out.println(AMARELO + "Motivo         : " + resultado.erro() + RESET);
            return;
        }

        String status = resultado.aceita()
                ? VERDE + NEGRITO + "ACEITA" + RESET
                : VERMELHO + NEGRITO + "REJEITADA" + RESET;

        System.out.println("Resultado      : " + status);
        System.out.println("Qtde de 'a's   : " + resultado.quantidadeDeAs()
                + " (" + (resultado.quantidadeDeAs() % 2 == 0 ? "par" : "ímpar") + ")");
        System.out.println("Caminho        : " + formatarCaminho(resultado.caminho()));
    }

    private static String formatarCaminho(List<Estado> caminho) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < caminho.size(); i++) {
            sb.append(caminho.get(i).name());
            if (i < caminho.size() - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }

    public static void imprimirRodape(int aceitas, int rejeitadas, int erros) {
        System.out.println("=========================================================");
        System.out.println(NEGRITO + "Resumo final:" + RESET);
        System.out.println(VERDE + "  Aceitas    : " + aceitas + RESET);
        System.out.println(VERMELHO + "  Rejeitadas : " + rejeitadas + RESET);
        System.out.println(AMARELO + "  Erros      : " + erros + RESET);
        System.out.println("=========================================================");
    }
}

/**
 * Classe principal: define as cadeias de teste e executa o AFD.
 */
public class Main {
    public static void main(String[] args) {
        AFD afd = new AFD();

        List<String> cadeiasDeTeste = List.of(
                "",        // válida - 0 a's
                "bb",      // válida - 0 a's
                "aa",      // válida - 2 a's
                "abab",    // válida - 2 a's
                "aabbaa",  // válida - 4 a's
                "a",       // inválida - 1 a
                "ab",      // inválida - 1 a
                "aaa",     // inválida - 3 a's
                "baaab",   // inválida - 3 a's
                "bbbab",   // inválida - 1 a
                "aabc"     // erro - símbolo 'c' fora do alfabeto
        );

        Formatador.imprimirCabecalho();

        List<ResultadoProcessamento> resultados = afd.processarLista(cadeiasDeTeste);

        int aceitas = 0, rejeitadas = 0, erros = 0;
        for (ResultadoProcessamento resultado : resultados) {
            Formatador.imprimirResultado(resultado);
            if (resultado.temErro()) {
                erros++;
            } else if (resultado.aceita()) {
                aceitas++;
            } else {
                rejeitadas++;
            }
        }

        Formatador.imprimirRodape(aceitas, rejeitadas, erros);
    }
}