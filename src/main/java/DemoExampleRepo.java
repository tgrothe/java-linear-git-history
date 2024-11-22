import java.io.File;
import java.io.IOException;
import java.util.*;

public class DemoExampleRepo {
  public static void main(String[] args) throws IOException, InterruptedException {
    createExample(3, 2);
  }

  public static void createExample(int n, int m) throws IOException, InterruptedException {
    List<AbstractMap.SimpleEntry<String, String>> branches = new ArrayList<>();
    String base1 = null;
    for (int i = 0; i < n; i++) {
      String base2 = String.format("%02d", i + 1);
      branches.add(new AbstractMap.SimpleEntry<>(base1, base2));
      for (int j = 0; j < m; j++) {
        String base3 = String.format("%02d_%02d", i + 1, j + 1);
        branches.add(new AbstractMap.SimpleEntry<>(base2, base3));
      }
      base1 = base2;
    }
    Collections.shuffle(branches);
    String repo = "example-repo";
    Main.exec(true, new File("."), "mkdir -p " + repo);
    File rf = new File(repo);
    Main.exec(true, rf, "touch .gitkeep");
    Main.exec(true, rf, "git init");
    Main.exec(true, rf, "git add -A");
    Main.exec(true, rf, "git commit -m 'Init master'");
    boolean changed = true;
    while (changed) {
      changed = false;
      for (AbstractMap.SimpleEntry<String, String> b : branches) {
        boolean exists =
            "0"
                .equals(
                    Main.exec(false, rf, "git show-ref --quiet refs/heads/" + b.getValue()).get(0));
        if (exists) {
          continue;
        }
        if (b.getKey() == null) {
          Main.exec(true, rf, "git checkout master");
        } else {
          if ("0"
              .equals(
                  Main.exec(false, rf, "git show-ref --quiet refs/heads/" + b.getKey()).get(0))) {
            Main.exec(true, rf, "git checkout " + b.getKey());
          } else {
            continue;
          }
        }
        Main.exec(true, rf, "git checkout -b " + b.getValue());
        Main.exec(true, rf, "git commit --allow-empty -m '" + b.getValue() + " a'");
        Main.exec(true, rf, "git commit --allow-empty -m '" + b.getValue() + " b'");
        changed = true;
      }
    }
    List<String> graph =
        Main.exec(true, rf, "git log --graph --pretty=oneline --abbrev-commit --all");
    for (String l : graph) {
      System.out.println(l);
    }
  }
}
