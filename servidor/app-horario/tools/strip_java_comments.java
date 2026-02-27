import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class strip_java_comments {

  enum State {
    CODE,
    LINE_COMMENT,
    BLOCK_COMMENT,
    STRING,
    CHAR
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: java strip_java_comments <rootDir>");
      System.exit(2);
    }

    Path root = Paths.get(args[0]).toAbsolutePath().normalize();
    if (!Files.isDirectory(root)) {
      System.err.println("Not a directory: " + root);
      System.exit(2);
    }

    Files.walkFileTree(
        root,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(".java")) {
              stripFile(file);
            }
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private static void stripFile(Path file) throws IOException {
    String input = Files.readString(file, StandardCharsets.UTF_8);
    String output = stripCommentsPreserveStrings(input);

    // Avoid rewriting unchanged files.
    if (!output.equals(input)) {
      Files.writeString(file, output, StandardCharsets.UTF_8);
    }
  }

  static String stripCommentsPreserveStrings(String s) {
    StringBuilder out = new StringBuilder(s.length());

    State state = State.CODE;
    boolean escaped = false;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      char next = (i + 1 < s.length()) ? s.charAt(i + 1) : '\0';

      switch (state) {
        case CODE -> {
          if (c == '/' && next == '/') {
            state = State.LINE_COMMENT;
            i++; // consume next
          } else if (c == '/' && next == '*') {
            state = State.BLOCK_COMMENT;
            i++; // consume next
          } else if (c == '"') {
            state = State.STRING;
            escaped = false;
            out.append(c);
          } else if (c == '\'') {
            state = State.CHAR;
            escaped = false;
            out.append(c);
          } else {
            out.append(c);
          }
        }
        case LINE_COMMENT -> {
          // Consume until end of line; keep the newline.
          if (c == '\n') {
            out.append(c);
            state = State.CODE;
          } else if (c == '\r') {
            out.append(c);
            // Handle CRLF: if next is \n, it will be processed next iteration.
            state = State.CODE;
          } else {
            // skip
          }
        }
        case BLOCK_COMMENT -> {
          if (c == '*' && next == '/') {
            state = State.CODE;
            i++; // consume '/'
          } else {
            // Preserve newlines to keep line numbers reasonably stable.
            if (c == '\n' || c == '\r') {
              out.append(c);
            }
          }
        }
        case STRING -> {
          out.append(c);
          if (escaped) {
            escaped = false;
          } else if (c == '\\') {
            escaped = true;
          } else if (c == '"') {
            state = State.CODE;
          }
        }
        case CHAR -> {
          out.append(c);
          if (escaped) {
            escaped = false;
          } else if (c == '\\') {
            escaped = true;
          } else if (c == '\'') {
            state = State.CODE;
          }
        }
      }
    }

    return out.toString();
  }
}
