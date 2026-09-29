package io.github.ElementalEscape;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/** Red TCP simple para 2 PCs. Un PC es SERVIDOR, otro CLIENTE. */
public class GestorRed {
    public enum Modo { LOCAL, SERVIDOR, CLIENTE }
    public static final int PUERTO = 54555;

    public Modo modo = Modo.LOCAL;
    public volatile boolean conectado = false;
    public volatile String estado = "local";

    // Estado del otro jugador
    public volatile float otroX = 100, otroY = 100, otroVelX = 0, otroVelY = 0, otroAnim = 0;
    public volatile int otroDir = 1;
    public volatile boolean otroVivo = true;
    public volatile int otroGemas = 0;
    public volatile int otroNivel = 0;
    public volatile boolean otroGano = false;

    private ServerSocket servidor;
    private Socket conexion;
    private PrintWriter salida;
    private BufferedReader entrada;
    private volatile boolean activo = false;
    private Thread hilo;

    public String miIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            StringBuilder sb = new StringBuilder();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> direcciones = ni.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress d = direcciones.nextElement();
                    if (d instanceof Inet4Address && !d.isLoopbackAddress())
                        sb.append(d.getHostAddress()).append(" ");
                }
            }
            if (sb.length() == 0) return InetAddress.getLocalHost().getHostAddress();
            return sb.toString().trim();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    public void iniciarServidor() {
        modo = Modo.SERVIDOR; activo = true; estado = "esperando cliente en puerto " + PUERTO + "...";
        hilo = new Thread(() -> {
            try {
                servidor = new ServerSocket(PUERTO);
                estado = "SERVIDOR esperando en " + miIp() + ":" + PUERTO;
                conexion = servidor.accept();
                prepararFlujos();
                conectado = true; estado = "conectado con " + conexion.getRemoteSocketAddress();
                bucleLectura();
            } catch (Exception e) { estado = "error servidor: " + e.getMessage(); }
        });
        hilo.setDaemon(true); hilo.start();
    }

    public void iniciarCliente(String ip) {
        modo = Modo.CLIENTE; activo = true; estado = "conectando a " + ip + "...";
        hilo = new Thread(() -> {
            try {
                conexion = new Socket();
                conexion.connect(new InetSocketAddress(ip.trim(), PUERTO), 5000);
                prepararFlujos();
                conectado = true; estado = "conectado al servidor " + ip;
                bucleLectura();
            } catch (Exception e) { estado = "no se pudo conectar: " + e.getMessage() + " (revisa IP/firewall)"; }
        });
        hilo.setDaemon(true); hilo.start();
    }

    private void prepararFlujos() throws IOException {
        salida = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conexion.getOutputStream())), true);
        entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
    }

    private void bucleLectura() {
        try {
            String linea;
            while (activo && (linea = entrada.readLine()) != null) {
                interpretar(linea);
            }
        } catch (Exception e) { estado = "desconectado: " + e.getMessage(); }
        conectado = false;
    }

    private void interpretar(String s) {
        try {
            String[] p = s.trim().split(" ");
            if (p.length < 10) return;
            otroX = Float.parseFloat(p[0]); otroY = Float.parseFloat(p[1]);
            otroVelX = Float.parseFloat(p[2]); otroVelY = Float.parseFloat(p[3]);
            otroDir = Integer.parseInt(p[4]); otroAnim = Float.parseFloat(p[5]);
            otroVivo = p[6].equals("1"); otroGemas = Integer.parseInt(p[7]);
            otroNivel = Integer.parseInt(p[8]); otroGano = p[9].equals("1");
        } catch (Exception ignored) {}
    }

    /** Llamar ~20 veces/seg desde render. */
    public synchronized void enviar(float x, float y, float velX, float velY, int dir, float anim, boolean vivo, int gemas, int nivel, boolean gano) {
        if (salida == null || !conectado) return;
        try {
            salida.println(x + " " + y + " " + velX + " " + velY + " " + dir + " " + anim + " " + (vivo ? 1 : 0) + " " + gemas + " " + nivel + " " + (gano ? 1 : 0));
        } catch (Exception ignored) {}
    }

    public void cerrar() {
        activo = false;
        try { if (conexion != null) conexion.close(); } catch (Exception ignored) {}
        try { if (servidor != null) servidor.close(); } catch (Exception ignored) {}
    }
}
