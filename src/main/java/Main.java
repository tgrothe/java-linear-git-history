import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.print("Enter full path to repo: ");
    String path = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    System.out.print("Enter main branch name: ");
    String main = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    linearize1(new File(path), main);
  }

  private static void linearize1(File repo, String main) throws IOException, InterruptedException {
    List<String> branches = exec(repo, "git", "branch");
    branches.remove(0); // status int
    branches =
        branches.stream()
            .map(
                n -> {
                  if (n.startsWith("* ")) {
                    return n.substring(2);
                  }
                  return n;
                })
            .collect(Collectors.toCollection(ArrayList::new));
    branches.sort(Comparator.naturalOrder());
    //    branches.sort(Comparator.reverseOrder());
    //    Collections.shuffle(branches);
    HashMap<Map.Entry<String, String>, Boolean> ancestorMap = new HashMap<>();
    for (int i = 0; i < branches.size(); i++) {
      String b1 = branches.get(i);
      for (int j = 0; j < branches.size(); j++) {
        if (i != j) {
          String b2 = branches.get(j);
          boolean b = "0".equals(exec(repo, "git", "merge-base", "--is-ancestor", b1, b2).get(0));
          ancestorMap.put(new AbstractMap.SimpleEntry<>(b1, b2), b);
        }
      }
    }
    System.out.println("ancestorMap = " + ancestorMap);
    TreeMap<String, List<String>> descendants = new TreeMap<>();
    for (int i = 0; i < branches.size(); i++) {
      String b1 = branches.get(i);
      for (int j = 0; j < branches.size(); j++) {
        if (i == j) {
          continue;
        }
        String b2 = branches.get(j);
        if (ancestorMap.get(new AbstractMap.SimpleEntry<>(b1, b2))) {
          System.out.printf("%s is ancestor of %s.%n", b1, b2);
          boolean isDirectAncestor = true;
          for (int k = 0; k < branches.size(); k++) {
            if (k == i || k == j) {
              continue;
            }
            String b3 = branches.get(k);
            if (ancestorMap.get(new AbstractMap.SimpleEntry<>(b3, b2))
                && !ancestorMap.get(new AbstractMap.SimpleEntry<>(b3, b1))) {
              isDirectAncestor = false;
              break;
            }
          }
          if (isDirectAncestor) {
            System.out.printf("%s is direct ancestor of %s.%n", b1, b2);
            if (!descendants.containsKey(b1)) {
              descendants.put(b1, new ArrayList<>());
            }
            descendants.get(b1).add(b2);
          }
        }
      }
    }
    System.out.println("descendants = " + descendants);
    // linearize2(repo, main, descendants);
  }

  private static boolean areEqual(List<String> a, List<String> b) {
    if (a.size() != b.size()) return false;
    for (int i = 0; i < a.size(); i++) {
      if (!a.get(i).equals(b.get(i))) return false;
    }
    return true;
  }

  private static void linearize2(File repo, String b1, TreeMap<String, List<String>> descendants)
      throws IOException, InterruptedException {
    if (!descendants.containsKey(b1)) {
      return;
    }

    System.out.printf("Rebase %s.%n", b1);
    exec(repo, "git", "checkout", b1);
    List<String> rebase1 = exec(repo, "git", "rebase", b1);
    System.out.println("rebase = " + rebase1);
    if (!"0".equals(rebase1.get(0))) {
      System.exit(0);
    }

    for (String b2 : descendants.get(b1)) {
      System.out.printf("Rebase %s and %s.%n", b1, b2);
      List<String> rebase2 = exec(repo, "git", "rebase", b1, b2);
      System.out.println("rebase = " + rebase2);
      if (!"0".equals(rebase2.get(0))) {
        System.exit(0);
      }
    }
    for (String b2 : descendants.get(b1)) {
      linearize2(repo, b2, descendants);
    }
  }

  private static List<String> exec(File wd, String... cmd)
      throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.directory(wd);
    builder.redirectErrorStream(true);
    Process process = builder.start();

    Scanner s = new Scanner(process.getInputStream(), Charset.defaultCharset());
    List<String> lines = new LinkedList<>();
    while (s.hasNextLine()) {
      lines.add(s.nextLine().trim());
    }
    s.close();

    int result = process.waitFor();
    lines.add(0, String.valueOf(result));

    return lines;
  }
}
