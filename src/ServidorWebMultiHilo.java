import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class ServidorWebMultiHilo
{
    public static void main(String argv[]) throws Exception
    {
        // Establece el número de puerto.
        int puerto = 6789;

            
        // Estableciendo el socket de escucha.
            ServerSocket listenSocket = new ServerSocket(puerto);

        // Procesando las solicitudes HTTP en un ciclo infinito.
        while (true) {
                // Escuhando las solicitudes de conexión TCP.
                Socket conexion = listenSocket.accept();

                // Construye un objeto para procesar el mensaje de solicitud HTTP.
                SolicitudHttp solicitud = new SolicitudHttp(conexion);

                // Crea un nuevo hilo para procesar la solicitud.
                Thread hilo = new Thread(solicitud);

                // Inicia el hilo.
                hilo.start();
        }
        


    }
}




final class SolicitudHttp implements Runnable
{
    	final static String CRLF = "\r\n";
    	Socket socket;

    	// Constructor
    	public SolicitudHttp(Socket socket) throws Exception 
    	{
            	this.socket = socket;
    	}

    	// Implementa el método run() de la interface Runnable.
    	public void run()
    	{
            try{
                proceseSolicitud();
            }catch(Exception e){
                System.out.println(e);
            }
    	}

    	private void proceseSolicitud() throws Exception
    	{
            // Referencia al stream de salida del socket.
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            // Referencia y filtros (InputStreamReader y BufferedReader)para el stream de entrada.
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Recoge la línea de solicitud HTTP del mensaje.
            String lineaDeSolicitud = br.readLine();
            

            // Muestra la línea de solicitud en la pantalla.
            System.out.println();
            System.out.println(lineaDeSolicitud);

            // recoge y muestra las líneas de header.
            String lineaDelHeader = null;
            while ((lineaDelHeader = br.readLine()).length() != 0) {
                    System.out.println(lineaDelHeader);
            }


        StringTokenizer partesLinea = new StringTokenizer(lineaDeSolicitud);
        partesLinea.nextToken();  // "salta" sobre el método, se supone que debe ser "GET"
        String nombreArchivo = partesLinea.nextToken();

        nombreArchivo = URLDecoder.decode(nombreArchivo, "UTF-8");

        nombreArchivo = "." + nombreArchivo;

        FileInputStream fis = null;
        boolean existeArchivo = true;
        try {
            fis = new FileInputStream(nombreArchivo);
        } catch (FileNotFoundException e) {
            existeArchivo = false;
        }

        String lineaDeEstado = null;
        String lineaDeTipoContenido = null;
        String cuerpoMensaje = null;
        if (existeArchivo) {
            lineaDeEstado = "HTTP/1.0 200 Document Follows" + CRLF;
            lineaDeTipoContenido = "Content-type: " + contentType(nombreArchivo) + CRLF;
        } else {
            lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
            lineaDeTipoContenido = "Content-type: text/html " + CRLF;
            cuerpoMensaje = "<HTML>" +
                    "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                    "<BODY><b>404</b> Not Found</BODY></HTML>";
        }

        os.writeBytes(lineaDeEstado);

        os.writeBytes(lineaDeTipoContenido);

        os.writeBytes(CRLF);

        if (existeArchivo) {
            enviarBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(cuerpoMensaje);
        }

            // Cierra los streams y el socket.
            os.close();
            br.close();
            socket.close();

    	}

    private static void enviarBytes(FileInputStream fis, OutputStream os) throws Exception {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }
    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nombreArchivo.endsWith(".png")) {
            return "image/png";
        }
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (nombreArchivo.endsWith(".css")) {
            return "text/css";
        }
        return "application/octet-stream";
    }
}
