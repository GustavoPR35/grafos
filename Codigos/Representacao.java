import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Representacao {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.print("Escreva o nome do arquivo a ser aberto: ");

            String arquivo = sc.nextLine();
            File file = new File(arquivo);
            Scanner sc_arq = new Scanner(file);

            String header = sc_arq.nextLine();
            // Regex sugerido pelo ChatGPT
            String[] header_parts = header.split("\\s+");
            int n_vertices = Integer.parseInt(header_parts[0]);
            int m_arestas = Integer.parseInt(header_parts[1]);

            /* FORWARD STAR */
            int[] origemForwStar = new int[m_arestas + 1];
            int[] destinoForwStar = new int[m_arestas + 1];
            int[] pointers = new int[n_vertices + 2];
            int[] contSucessores = new int[n_vertices + 1];

            /* REVERSE STAR */
            int[] origemRevStar = new int[m_arestas + 1];
            int[] destinoRevStar = new int[m_arestas + 1];
            int[] rev_pointers = new int[n_vertices + 2];
            int[] contPredecessores = new int[n_vertices + 1];

            for (int i = 1; i <= m_arestas; i++) {
                String line = sc_arq.nextLine();
                String[] line_parts = line.trim().split("\\s+");
                int v_origem = Integer.parseInt(line_parts[0]);
                int v_destino = Integer.parseInt(line_parts[1]);

                /* FORWARD STAR */
                origemForwStar[i] = v_origem;
                destinoForwStar[i] = v_destino;
                /* CONTAGEM DE SUCESSORES */
                contSucessores[origemForwStar[i]]++;

                /* REVERSE STAR */
                origemRevStar[i] = v_origem;
                destinoRevStar[i] = v_destino;
                /* CONTAGEM DE PREDECESSORES */
                contPredecessores[destinoRevStar[i]]++;
            }
            sc_arq.close();

            /* POINTERS FORWARD STAR */
            pointers[1] = 1;
            for (int i = 2; i <= n_vertices + 1; i++) {
                pointers[i] = pointers[i - 1] + contSucessores[i - 1];
            }
            origemForwStar = null;
            /* FIM CONSTRUÇÃO FORWARD STAR */

            quickSort(destinoRevStar, 1, destinoRevStar.length - 1, origemRevStar);

            /* REV_POINTERS REVERSE STAR */
            rev_pointers[1] = 1;
            rev_pointers[rev_pointers.length - 1] = m_arestas + 1;
            for (int i = 2; i <= n_vertices; i++) {
                rev_pointers[i] = rev_pointers[i - 1] + contPredecessores[i - 1];
            }

            destinoRevStar = null;
            /* FIM CONSTRUÇÃO REVERSE STAR */

            int v_choice;
            while (true) {
                System.out.print("Escolha um vértice entre 1 e " + n_vertices + " (insira -1 para sair): ");
                v_choice = Integer.parseInt(sc.nextLine());
                if (v_choice == -1) {
                    break;
                }

                if (v_choice < 1 || v_choice > n_vertices) {
                    System.out.println("Vértice escolhido deve ser entre 1 e " + n_vertices + "");
                    continue;
                }

                int[] sucessores = sucessores(destinoForwStar, pointers, v_choice, false);
                int[] predecessores = predecessores(origemRevStar, rev_pointers, v_choice);
                int rangeForwStar = pointers[v_choice + 1] - pointers[v_choice];
                int rangeRevStar = rev_pointers[v_choice + 1] - rev_pointers[v_choice];

                System.out.println();
                System.out.println("Grau de saída (" + v_choice + "): " + rangeForwStar);
                System.out.println("Grau de entrada (" + v_choice + "): " + rangeRevStar);
                System.out.println("Sucessores (" + v_choice + "): ");
                System.out.print("{ ");
                for (int i = 0; i < sucessores.length; i++) {
                    System.out.print(sucessores[i]);
                    System.out.print(i == sucessores.length - 1 ? "" : ", ");
                }
                System.out.println(" }");
                System.out.println("Predecessores (" + v_choice + "): ");
                System.out.print("{ ");
                for (int i = 0; i < predecessores.length; i++) {
                    System.out.print(predecessores[i]);
                    System.out.print(i == predecessores.length - 1 ? "" : ", ");
                }
                System.out.println(" }");
            }

        } catch (Exception e) {
            System.out.println("Erro");
            e.printStackTrace();
        }
        sc.close();
    }

    static public int[] sucessores(int destinoForwStar[], int pointers[], int v_choice, boolean sort) {
        int rangeForwStar = pointers[v_choice + 1] - pointers[v_choice];
        int[] sucessores = new int[rangeForwStar];
        for (int i = 0; i < rangeForwStar; i++) {
            sucessores[i] = destinoForwStar[pointers[v_choice] + i];
        }
        if (sort) {
            Arrays.sort(sucessores);
        }
        return sucessores;
    }

    static public int[] predecessores(int origemRevStar[], int rev_pointers[], int v_choice) {
        int rangeRevStar = rev_pointers[v_choice + 1] - rev_pointers[v_choice];
        int[] predecessores = new int[rangeRevStar];
        for (int i = 0; i < rangeRevStar; i++) {
            predecessores[i] = origemRevStar[rev_pointers[v_choice] + i];
        }
        return predecessores;
    }

    /*
     * Algoritmo de ordenação QuickSort encontrado em:
     * https://www.baeldung.com/java-quicksort
     * Alterações foram feitas para ordenar arrays de destino e origem
     */
    static public void quickSort(int arr[], int begin, int end, int extra[]) {
        if (begin < end) {
            int partitionIndex = partition(arr, begin, end, extra);

            quickSort(arr, begin, partitionIndex - 1, extra);
            quickSort(arr, partitionIndex + 1, end, extra);
        }
    }

    static private int partition(int arr[], int begin, int end, int extra[]) {
        int pivot = arr[end];
        int i = (begin - 1);

        for (int j = begin; j < end; j++) {
            if (arr[j] <= pivot) {
                i++;

                int swapTemp = arr[i];
                arr[i] = arr[j];
                arr[j] = swapTemp;

                int swapTempEx = extra[i];
                extra[i] = extra[j];
                extra[j] = swapTempEx;
            }
        }

        int swapTemp = arr[i + 1];
        arr[i + 1] = arr[end];
        arr[end] = swapTemp;

        int swapTempEx = extra[i + 1];
        extra[i + 1] = extra[end];
        extra[end] = swapTempEx;

        return i + 1;
    }
}
