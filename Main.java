//FUNCIONES LISTAS:
    //1. Entrada del usuario (solicita el número del registro)
    //2. Lectura de la cabecera (LectorCabecera obtiene la frecuencia, los canales, las muestras y el archivo de señal.)
    //3. Generación gráfica

//FUNCIONES QUE FALTAN POR IMPLEMENTAR:
    //Detectar los picos R. (Entrega 1)
    //Calcular intervalos RR y frecuencia cardíaca. (Entrega 1)
    //Impresión del resultado del análisis (Entrega 1 y 2)
    //Clasificar el ritmo. (Entrega 2)
    //Comparar con las anotaciones de referencia. (Entrega 2)

//LIBRERIAS
//Excepción que se lanza cuando ocurre un error de entrada/salida (lectura o escritura de archivos).
import java.io.IOException;
//Métodos utilitarios para trabajar con archivos y directorios (leer, escribir, copiar, borrar).
import java.nio.file.Files;
//Representa rutas de archivos o directorios en el sistema.
import java.nio.file.Path;
//Interfaz para listas ordenadas (colecciones dinámicas de elementos).
import java.util.List;
//Clase para leer datos de entrada (por ejemplo, desde consola, archivos o cadenas).
import java.util.Scanner;

public class Main {
    //Es el directorio que contiene la carpeta con los registros ECG
    private static final Path DIRECTORIO_DATOS = Path.of("Muestras_Pruebas");
    public static void main(String[] args) {
        //Scanner permite recibir por consola el número de la referencia
        try (Scanner teclado = new Scanner(System.in)) {
            //Solicita el número del registro
            System.out.print("Ingrese el numero del registro (000-999): "); //Modificar después de tener definido los archivos que vamos a usar, mantener nombres de los archivos como REF_100, para poder acceder fácil.
            String numero = teclado.nextLine().trim();

            //Verifica que el número tenga exacrtamente 3 números
            if (!numero.matches("\\d{3}")) {
                System.out.println("Error: debe ingresar exactamente tres digitos.");
                return;
            }

            //Construir las rutas de la carpeta y de los archivos del registro
            Path carpeta = DIRECTORIO_DATOS.resolve("REF_" + numero);
            Path cabecera = carpeta.resolve(numero + ".hea");
            Path anotaciones = carpeta.resolve(numero + ".atr");
            Path auxiliar = carpeta.resolve(numero + ".xws");

            //Por si la referencia está mal
            if (!Files.isDirectory(carpeta)) {
                System.out.println("Error: no existe la carpeta " + carpeta);
                return;
            }

            //Por si no existe el archivo .hea (cabecera)
            if (!Files.isRegularFile(cabecera)) {
                System.out.println("Error: no existe el archivo " + cabecera);
                return;
            }

            //Leer los metadatos del registro.
            CabeceraECG datos = LectorCabecera.leer(cabecera);

            //Verificar el archivo de señal declarado en .hea.
            //sen es el archivo de la señal
            //Normaliza el texto Unicode, permite buscar y ordenar texto más fácil.
            Path sen = carpeta.resolve(datos.archivosen).normalize();
 
            //Verificar que la ruta pertenexca dentro de la carpeta y que el archivo de la señal exista
            if (!sen.startsWith(carpeta.normalize())|| !Files.isRegularFile(sen)) {
                throw new IOException("No se encuentra el archivo de señal: " + sen);
            }

            //Verificar las anotaciones de referencia .atr
            if (!Files.isRegularFile(anotaciones)) {
                throw new IOException("No se encuentra el archivo .atr: " + anotaciones);
            }

            //Archivo auxiliar .xws (si está disponible)
            List<String> aux = Files.isRegularFile(auxiliar) ? LectorAuxiliar.leer(auxiliar) : List.of();
 
            //Toda la salida pasa por el graficador
            GraficadorECG graficador = new GraficadorECG(datos, sen, anotaciones, aux);
            //Generar la gráfica y guardarla como imagen PNG
            graficador.mostrar(Path.of("salida", "REF_" + numero + ".png"));
 
        } 
        catch (IOException | IllegalArgumentException e) {
            //Mostrar los errores de lectura, acceso a archivos o interpretación de datos
            System.err.println("Error de lectura: " + e.getMessage());
        }
    }
}
