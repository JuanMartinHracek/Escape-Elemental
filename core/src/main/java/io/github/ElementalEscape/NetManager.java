package io.github.ElementalEscape;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/** Red TCP simple para 2 PCs. Un PC es HOST (server), otro CLIENT. */
public class NetManager {
    public enum Modo { LOCAL, HOST, CLIENT }
    public static final int PUERTO = 54555;

    public Modo modo = Modo.LOCAL;
    public volatile boolean connected = false;
    public volatile String status = "local";

    public volatile float rx = 100, ry = 100, rvx = 0, rvy = 0, ranim = 0;
    public volatile int rdir = 1;
    public volatile boolean rvivo = true;
    public volatile int rgems = 0;
    public volatile int rlevel = 0;
    public volatile boolean rwon = false;

    private ServerSocket server;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = false;
    private Thread hilo;

    public String miIp() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            StringBuilder sb = new StringBuilder();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress())
                        sb.append(a.getHostAddress()).append(" ");
                }
            }
            if (sb.length() == 0) return InetAddress.getLocalHost().getHostAddress();
            return sb.toString().trim();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    public void startHost() {
        modo = Modo.HOST; running = true; status = "esperando cliente en puerto " + PUERTO + "...";
        hilo = new Thread(() -> {
            try {
                server = new ServerSocket(PUERTO);
                status = "HOST esperando en " + miIp() + ":" + PUERTO;
                socket = server.accept();
                setupStreams();
                connected = true; status = "conectado con " + socket.getRemoteSocketAddress();
                loopLectura();
            } catch (Exception e) { status = "error host: " + e.getMessage(); }
        });
        hilo.setDaemon(true); hilo.start();
    }

    public void startClient(String ip) {
        modo = Modo.CLIENT; running = true; status = "conectando a " + ip + "...";
        hilo = new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip.trim(), PUERTO), 5000);
                setupStreams();
                connected = true; status = "conectado al host " + ip;
                loopLectura();
            } catch (Exception e) { status = "no se pudo conectar: " + e.getMessage() + " (revisa IP/firewall)"; }
        });
        hilo.setDaemon(true); hilo.start();
    }

    private void setupStreams() throws IOException {
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void loopLectura() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                parse(line);
            }
        } catch (Exception e) { status = "desconectado: " + e.getMessage(); }
        connected = false;
    }

    private void parse(String s) {
        try {
            String[] p = s.trim().split(" ");
            if (p.length < 10) return;
            rx = Float.parseFloat(p[0]); ry = Float.parseFloat(p[1]);
            rvx = Float.parseFloat(p[2]); rvy = Float.parseFloat(p[3]);
            rdir = Integer.parseInt(p[4]); ranim = Float.parseFloat(p[5]);
            rvivo = p[6].equals("1"); rgems = Integer.parseInt(p[7]);
            rlevel = Integer.parseInt(p[8]); rwon = p[9].equals("1");
        } catch (Exception ignored) {}
    }

    public synchronized void send(float x, float y, float vx, float vy, int dir, float anim, boolean vivo, int gems, int level, boolean won) {
        if (out == null || !connected) return;
        try {
            out.println(x + " " + y + " " + vx + " " + vy + " " + dir + " " + anim + " " + (vivo ? 1 : 0) + " " + gems + " " + level + " " + (won ? 1 : 0));
        } catch (Exception ignored) {}
    }

    public void close() {
        running = false;
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        try { if (server != null) server.close(); } catch (Exception ignored) {}
    }
}
