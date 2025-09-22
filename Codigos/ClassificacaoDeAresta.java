import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

class ClassificacaoDeAresta {
    public static int tempo;
    public static int[] TD;
    public static int[] TT;
    public static int[] pai;
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

            for (int i = 1; i <= m_arestas; i++) {
                String line = sc_arq.nextLine();
                String[] line_parts = line.trim().split("\\s+");
                int v_origem = Integer.parseInt(line_parts[0]);
                int v_destino = Integer.parseInt(line_parts[1]);

                origemForwStar[i] = v_origem;
                destinoForwStar[i] = v_destino;
                /* CONTAGEM DE SUCESSORES */
                contSucessores[origemForwStar[i]]++;
            }
            sc_arq.close();

            /* POINTERS FORWARD STAR */
            // Forma de montagem de pointers melhorado usando ChatGPT
            pointers[1] = 1;
            for (int i = 2; i <= n_vertices + 1; i++) {
                pointers[i] = pointers[i - 1] + contSucessores[i - 1];
            }
            origemForwStar = null;
            /* FIM CONSTRUÇÃO FORWARD STAR */

            buscaProfundidadeInicial(destinoForwStar, pointers, n_vertices);

            // Printar todas as árvores
            System.out.println("Arestas de árvore encontradas:");
            for (int i = 1; i < pai.length; i++) {
                if (pai[i] != 0) {
                    System.out.println("(" + pai[i] + ", " + i + ")");
                }
            }
            System.out.println("Todas as arestas de árvore estão listadas acima.");

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

                classificarArestas(v_choice, destinoForwStar, pointers);
            }

        } catch (Exception e) {
            System.out.println("Erro");
            e.printStackTrace();
        }
        sc.close();
    }

    /*
     * Lógicas para classificação de arestas
     * (v, w)
     * Árvore: pai[w] = v
     * Retorno: TD[v] > TD[w] && TT[v] < TT[w]
     * Avanço: TD[v] < TD[w] && TT[v] > TT[w]
     * Cruzamento: TD[v] > TD[w] && TT[v] > TT[w]
     */
    static public void classificarArestas(int v, int destinoForwStar[], int pointers[]) {
        int[] sucessores = sucessores(destinoForwStar, pointers, v, false);

        for (int i = 0; i < sucessores.length; i++) {
            int w = sucessores[i];
            if (pai[w] == v) {
                System.out.println("(" + v + ", " + w + "): árvore");
            } else if (TD[v] > TD[w] && TT[v] < TT[w]) {
                System.out.println("(" + v + ", " + w + "): retorno");
            } else if (TD[v] > TD[w] && TT[v] > TT[w]) {
                System.out.println("(" + v + ", " + w + "): cruzamento");
            } else if (TD[v] < TD[w] && TT[v] > TT[w]) {
                System.out.println("(" + v + ", " + w + "): avanço");
            } else {
                System.out.println("Erro na classificação");
            }
        }
    }

    /*
     * Busca em profundidade
     * Baseado no pseudocódigo do slide 07-Busca em profundidade
     * Alterado para usar pilha ao invés de recursão com ajuda do ChatGPT, se não ia estourar o limite no grafo de 50000 vértices
     */
    static public void buscaProfundidadeInicial(int destinoForwStar[], int pointers[], int n_vertices) {
        tempo = 0;
        TD = new int[n_vertices + 1];
        TT = new int[n_vertices + 1];
        pai = new int[n_vertices + 1];

        for (int i = 1; i <= n_vertices; i++) {
            if (TD[i] == 0) {
                buscaProfundidade(i, destinoForwStar, pointers);
            }
        }
    }

    static private void buscaProfundidade(int inicio, int destinoForwStar[], int pointers[]) {
        Stack<Integer> pilhaVertices = new Stack<>();
        Stack<Integer> pilhaIndiceSucessores = new Stack<>();
        
        pilhaVertices.push(inicio);
        pilhaIndiceSucessores.push(0); // índice do próximo sucessor a visitar
        
        tempo = tempo + 1;
        TD[inicio] = tempo;
        
        while (!pilhaVertices.isEmpty()) {
            int v = pilhaVertices.peek();
            int indiceAtual = pilhaIndiceSucessores.pop();
            
            int[] sucessores = sucessores(destinoForwStar, pointers, v, true);
            
            // Se processou todos os sucessores, finaliza o vértice
            if (indiceAtual >= sucessores.length) {
                pilhaVertices.pop();
                tempo++;
                TT[v] = tempo;
                continue;
            }
            
            // Atualiza o índice para o próximo sucessor
            pilhaIndiceSucessores.push(indiceAtual + 1);
            
            int w = sucessores[indiceAtual];
            // Aresta de árvore
            if (TD[w] == 0) {
                pai[w] = v;
                tempo = tempo + 1;
                TD[w] = tempo;

                pilhaVertices.push(w);
                pilhaIndiceSucessores.push(0);
            }
        }
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