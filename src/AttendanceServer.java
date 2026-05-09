import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.text.SimpleDateFormat;

public class AttendanceServer {

    static final String FILE_NAME = "data/attendance.txt";
    static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        new File("data").mkdirs();
        new File(FILE_NAME).createNewFile();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticHandler());
        server.createContext("/api/attendance", new AttendanceHandler());
        server.createContext("/api/report", new ReportHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("✓ Server started at http://localhost:" + PORT);
        System.out.println("  Open your browser and go to: http://localhost:" + PORT);
    }

    // ── Static file handler ────────────────────────────────────────────────
    static class StaticHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("web" + path);
            if (file.exists() && !file.isDirectory()) {
                String ct = path.endsWith(".css") ? "text/css"
                          : path.endsWith(".js")  ? "application/javascript"
                          : "text/html";
                byte[] bytes = Files.readAllBytes(file.toPath());
                ex.getResponseHeaders().set("Content-Type", ct + "; charset=UTF-8");
                ex.sendResponseHeaders(200, bytes.length);
                ex.getResponseBody().write(bytes);
            } else {
                send(ex, 404, "text/plain", "Not Found");
            }
            ex.close();
        }
    }

    // ── Attendance CRUD handler ────────────────────────────────────────────
    static class AttendanceHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            String method = ex.getRequestMethod();

            if (method.equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); ex.close(); return; }

            if (method.equals("GET")) {
                String query = ex.getRequestURI().getQuery();
                String search = null;
                if (query != null && query.startsWith("search="))
                    search = query.substring(7).toLowerCase();

                List<Map<String,String>> records = readAll();
                if (search != null) {
                    final String s = search;
                    records.removeIf(r ->
                        !r.get("name").toLowerCase().contains(s) &&
                        !r.get("roll").toLowerCase().contains(s) &&
                        !r.get("subject").toLowerCase().contains(s));
                }
                send(ex, 200, "application/json", toJson(records));

            } else if (method.equals("POST")) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> data = parseJson(body);
                String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());

                FileWriter fw = new FileWriter(FILE_NAME, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(data.getOrDefault("roll","") + "|"
                       + data.getOrDefault("name","") + "|"
                       + data.getOrDefault("subject","") + "|"
                       + data.getOrDefault("status","Present") + "|"
                       + date);
                bw.newLine();
                bw.close();
                send(ex, 200, "application/json", "{\"success\":true,\"message\":\"Attendance saved\"}");

            } else if (method.equals("DELETE")) {
                String query = ex.getRequestURI().getQuery();
                String roll = (query != null && query.startsWith("roll=")) ? query.substring(5) : "";
                int deleted = deleteByRoll(roll);
                send(ex, 200, "application/json",
                    "{\"success\":true,\"deleted\":" + deleted + "}");

            } else {
                send(ex, 405, "text/plain", "Method Not Allowed");
            }
            ex.close();
        }
    }

    // ── Report handler ─────────────────────────────────────────────────────
    static class ReportHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204,-1); ex.close(); return; }

            List<Map<String,String>> records = readAll();
            int present = 0, absent = 0, late = 0;
            Map<String,Integer> bySubject = new LinkedHashMap<>();
            Map<String,int[]> byStudent = new LinkedHashMap<>();

            for (Map<String,String> r : records) {
                String s = r.get("status").toLowerCase();
                if (s.contains("present")) present++;
                else if (s.contains("absent")) absent++;
                else if (s.contains("late"))   late++;
                String subj = r.get("subject");
                bySubject.put(subj, bySubject.getOrDefault(subj, 0) + 1);

                String key = r.get("roll") + " - " + r.get("name");
                byStudent.computeIfAbsent(key, k -> new int[]{0,0,0});
                int[] cnt = byStudent.get(key);
                if (s.contains("present")) cnt[0]++;
                else if (s.contains("absent")) cnt[1]++;
                else cnt[2]++;
            }

            int total = present + absent + late;
            double pct = total > 0 ? present * 100.0 / total : 0;

            StringBuilder sb = new StringBuilder();
            sb.append("{\"total\":").append(total)
              .append(",\"present\":").append(present)
              .append(",\"absent\":").append(absent)
              .append(",\"late\":").append(late)
              .append(",\"percentage\":").append(String.format("%.1f", pct))
              .append(",\"bySubject\":{");
            boolean first = true;
            for (Map.Entry<String,Integer> e : bySubject.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
                first = false;
            }
            sb.append("},\"byStudent\":[");
            first = true;
            for (Map.Entry<String,int[]> e : byStudent.entrySet()) {
                if (!first) sb.append(",");
                int[] c = e.getValue();
                int stotal = c[0]+c[1]+c[2];
                double spct = stotal > 0 ? c[0]*100.0/stotal : 0;
                sb.append("{\"name\":\"").append(e.getKey()).append("\"")
                  .append(",\"present\":").append(c[0])
                  .append(",\"absent\":").append(c[1])
                  .append(",\"late\":").append(c[2])
                  .append(",\"percentage\":").append(String.format("%.1f", spct))
                  .append("}");
                first = false;
            }
            sb.append("]}");
            send(ex, 200, "application/json", sb.toString());
            ex.close();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    static List<Map<String,String>> readAll() throws IOException {
        List<Map<String,String>> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                Map<String,String> m = new LinkedHashMap<>();
                m.put("id",      String.valueOf(id++));
                m.put("roll",    p.length > 0 ? p[0] : "");
                m.put("name",    p.length > 1 ? p[1] : "");
                m.put("subject", p.length > 2 ? p[2] : "");
                m.put("status",  p.length > 3 ? p[3] : "");
                m.put("date",    p.length > 4 ? p[4] : "");
                list.add(m);
            }
        }
        return list;
    }

    static int deleteByRoll(String roll) throws IOException {
        File input = new File(FILE_NAME);
        File temp  = new File("data/temp.txt");
        int deleted = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(input));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(roll + "|")) { bw.write(line); bw.newLine(); }
                else deleted++;
            }
        }
        input.delete();
        temp.renameTo(input);
        return deleted;
    }

    static String toJson(List<Map<String,String>> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String,String> m : list) {
            if (!first) sb.append(",");
            sb.append("{");
            boolean f2 = true;
            for (Map.Entry<String,String> e : m.entrySet()) {
                if (!f2) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":\"")
                  .append(e.getValue().replace("\"","\\\"")).append("\"");
                f2 = false;
            }
            sb.append("}");
            first = false;
        }
        return sb.append("]").toString();
    }

    static Map<String,String> parseJson(String json) {
        Map<String,String> map = new HashMap<>();
        json = json.replaceAll("[{}\"]", "");
        for (String pair : json.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    static void send(HttpExchange ex, int code, String ct, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", ct + "; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
    }

    static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
