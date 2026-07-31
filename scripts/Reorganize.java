import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mirrors the key order of a hand-curated en_us.json onto every other locale.
 * <p>
 * - en_us.json is the master: its order and its blank-line grouping are the
 *   single source of truth. It is never modified.
 * - Every other locale is rewritten to match that exact order and grouping,
 *   so all files stay comparable line by line.
 * - Missing keys are filled in: if a locale lacks a key that en_us has, the
 *   English line is copied verbatim into its correct position (a placeholder
 *   the translator can later replace).
 * - Stale keys (present in a locale but not in en_us) are kept, grouped at the
 *   end of the file so you can spot and remove them.
 * <p>
 * Usage (no compilation needed, Java 11+):
 *     java Reorganize.java assets/ae2/lang
 *     java Reorganize.java es_es.json es_mx.json (en_us.json must be alongside or listed)
 */
public class Reorganize {

    /** Default path used when the program is launched with no arguments. */
    static final String DEFAULT_PATH = "assets/ae2/lang";

    // ----------------------------------------------------------- minimal JSON

    /** Parses a flat {"key":"value", ...} object, preserving insertion order. */
    static Map<String, String> parse(String src) {
        Map<String, String> out = new LinkedHashMap<>();
        int i = skipWs(src, 0);
        if (i >= src.length() || src.charAt(i) != '{')
            throw new IllegalArgumentException("Expected '{' at the start");
        i = skipWs(src, i + 1);
        if (i < src.length() && src.charAt(i) == '}') return out;

        while (i < src.length()) {
            i = skipWs(src, i);
            if (src.charAt(i) != '"')
                throw new IllegalArgumentException("Expected a key at position " + i);
            StringBuilder sb = new StringBuilder();
            i = readString(src, i, sb);
            String key = sb.toString();

            i = skipWs(src, i);
            if (src.charAt(i) != ':')
                throw new IllegalArgumentException("Expected ':' at position " + i);
            i = skipWs(src, i + 1);

            if (src.charAt(i) != '"')
                throw new IllegalArgumentException(
                        "Only string values are supported (key: " + key + ")");
            sb = new StringBuilder();
            i = readString(src, i, sb);
            out.put(key, sb.toString());

            i = skipWs(src, i);
            char c = src.charAt(i);
            if (c == ',')      { i++; }
            else if (c == '}') { break; }
            else throw new IllegalArgumentException("Unexpected character '" + c + "'");
        }
        return out;
    }

    /**
     * Parses the master file into blank-line-separated groups of keys.
     * Two or more consecutive newlines between entries start a new group.
     */
    static List<List<String>> parseGroups(String src) {
        List<List<String>> groups = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int i = skipWs(src, 0);
        if (i >= src.length() || src.charAt(i) != '{')
            throw new IllegalArgumentException("Expected '{' at the start");
        i++;

        while (true) {
            int before = i;
            i = skipWs(src, i);
            boolean blankLine = countNewlines(src, before, i) >= 2;

            if (i >= src.length() || src.charAt(i) == '}') break;
            if (src.charAt(i) == ',') { i++; continue; }

            if (blankLine && !current.isEmpty()) {
                groups.add(current);
                current = new ArrayList<>();
            }

            StringBuilder sb = new StringBuilder();
            i = readString(src, i, sb);
            current.add(sb.toString());

            i = skipWs(src, i);
            i++;                                   // ':'
            i = skipWs(src, i);
            i = readString(src, i, new StringBuilder());
        }
        if (!current.isEmpty()) groups.add(current);
        return groups;
    }

    static int countNewlines(String s, int from, int to) {
        int n = 0;
        for (int i = from; i < to; i++) if (s.charAt(i) == '\n') n++;
        return n;
    }

    static int skipWs(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    /** Reads a JSON string starting at the quote s[i]; returns the next position. */
    static int readString(String s, int i, StringBuilder out) {
        i++; // opening quote
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"') return i + 1;
            if (c == '\\') {
                char e = s.charAt(++i);
                switch (e) {
                    case '"'  -> out.append('"');
                    case '\\' -> out.append('\\');
                    case '/'  -> out.append('/');
                    case 'b'  -> out.append('\b');
                    case 'f'  -> out.append('\f');
                    case 'n'  -> out.append('\n');
                    case 'r'  -> out.append('\r');
                    case 't'  -> out.append('\t');
                    case 'u'  -> {
                        out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> throw new IllegalArgumentException("Unknown escape: \\" + e);
                }
            } else {
                out.append(c);
            }
            i++;
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    /** Escapes only what's required; accents and symbols stay as-is (UTF-8). */
    static String esc(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    /**
     * Writes {@code data} following the group order of {@code masterGroups}.
     * A trailing group of {@code staleKeys} (if any) is appended after a blank line.
     */
    static void write(Path path, List<List<String>> masterGroups,
                      List<String> staleKeys, Map<String, String> data)
            throws IOException {
        // last key that will actually be written (for comma placement)
        String last = null;
        for (List<String> g : masterGroups)
            for (String k : g) if (data.containsKey(k)) last = k;
        if (!staleKeys.isEmpty()) last = staleKeys.getLast();

        StringBuilder sb = new StringBuilder("{\n");
        boolean firstGroup = true;

        for (List<String> group : masterGroups) {
            List<String> present = group.stream()
                    .filter(data::containsKey).toList();
            if (present.isEmpty()) continue;
            if (!firstGroup) sb.append('\n');      // blank line between groups
            firstGroup = false;
            for (String k : present) {
                sb.append("  ").append(esc(k)).append(": ").append(esc(data.get(k)));
                if (!k.equals(last)) sb.append(',');
                sb.append('\n');
            }
        }

        if (!staleKeys.isEmpty()) {
            if (!firstGroup) sb.append('\n');
            for (String k : staleKeys) {
                sb.append("  ").append(esc(k)).append(": ").append(esc(data.get(k)));
                if (!k.equals(last)) sb.append(',');
                sb.append('\n');
            }
        }

        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------- main

    public static void main(String[] args) throws Exception {
        // No arguments (e.g., when running from the IDE) -> use the default path.
        String[] targets = args.length > 0 ? args : new String[]{DEFAULT_PATH};
        if (args.length == 0)
            System.out.println("No arguments given, using: " + DEFAULT_PATH + "\n");

        // accepts folders or individual files
        List<Path> paths = new ArrayList<>();
        for (String a : targets) {
            Path p = Path.of(a);
            if (Files.isDirectory(p)) {
                try (var st = Files.list(p)) {
                    st.filter(f -> f.toString().endsWith(".json"))
                            .sorted().forEach(paths::add);
                }
            } else {
                paths.add(p);
            }
        }

        Path enPath = paths.stream()
                .filter(p -> p.getFileName().toString().equals("en_us.json"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "en_us.json is required as the reference key order."));

        String enSrc = Files.readString(enPath, StandardCharsets.UTF_8);
        Map<String, String> en = parse(enSrc);
        List<List<String>> masterGroups = parseGroups(enSrc);

        int keyCount = masterGroups.stream().mapToInt(List::size).sum();
        System.out.printf("Master en_us.json: %d groups, %d keys (left untouched)%n%n",
                masterGroups.size(), keyCount);

        paths.remove(enPath);                        // never touch the master

        for (Path p : paths) {
            Map<String, String> data =
                    parse(Files.readString(p, StandardCharsets.UTF_8));

            int had = data.size();
            List<String> filled = new ArrayList<>();
            for (String k : en.keySet()) {
                if (!data.containsKey(k)) {
                    data.put(k, en.get(k));          // copy English line verbatim
                    filled.add(k);
                }
            }

            List<String> stale = data.keySet().stream()
                    .filter(k -> !en.containsKey(k))
                    .collect(Collectors.toList());

            write(p, masterGroups, stale, data);

            String note = "";
            if (!filled.isEmpty()) note += "  +" + filled.size() + " filled from en_us";
            if (!stale.isEmpty())  note += "  !" + stale.size() + " stale (kept at end)";
            System.out.printf("  OK %-12s %d -> %d keys%s%n",
                    p.getFileName(), had, data.size(), note);
        }
    }
}