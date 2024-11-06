import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.print("Enter full path to repo: ");
    String path = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    System.out.print("Enter main branch name: ");
    String main = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    linearize1(new File(path), main);
  }

  private static void linearize1(File repo, String main) throws IOException, InterruptedException {
    exec(true, repo, "git", "fetch", "--all");

    List<String> branches =
        exec(
            true,
            repo,
            "git branch --format '%(refname:short) %(upstream)' | awk '{if (!$2) print $1;}'");
    branches.remove(0); // status int
    if (!branches.contains(main)) {
      System.out.printf("Warning: %s is no local branch and has remote.%n", main);
      branches.add(main);
    }
    branches.sort(Comparator.naturalOrder());
    //    branches.sort(Comparator.reverseOrder());
    //    Collections.shuffle(branches);
    HashMap<Map.Entry<String, String>, Boolean> ancestorMap = new HashMap<>();
    for (int i = 0; i < branches.size(); i++) {
      String b1 = branches.get(i);
      for (int j = i + 1; j < branches.size(); j++) {
        String b2 = branches.get(j);
        System.out.printf("Check %s and %s.%n", b1, b2);
        boolean a =
            "0".equals(exec(false, repo, "git", "merge-base", "--is-ancestor", b1, b2).get(0));
        if (a) {
          ancestorMap.put(new AbstractMap.SimpleEntry<>(b1, b2), true);
          ancestorMap.put(new AbstractMap.SimpleEntry<>(b2, b1), false);
        } else {
          boolean b =
              "0".equals(exec(false, repo, "git", "merge-base", "--is-ancestor", b2, b1).get(0));
          ancestorMap.put(new AbstractMap.SimpleEntry<>(b1, b2), false);
          ancestorMap.put(new AbstractMap.SimpleEntry<>(b2, b1), b);
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
    removeCycles(descendants);
    System.out.println("descendants:");
    for (Map.Entry<String, List<String>> a : descendants.entrySet()) {
      System.out.println(a);
    }
    linearize2(repo, main, descendants, true);
    System.out.print("Would you like to continue? Then enter yes: ");
    if ("yes".equals(new Scanner(System.in, Charset.defaultCharset()).nextLine())) {
      linearize2(repo, main, descendants, false);
    }
    System.out.println("Program end.");
  }

  private static void removeCycles(TreeMap<String, List<String>> descendants) {
    List<String> toRemove = new ArrayList<>();
    ArrayList<Map.Entry<String, List<String>>> entries = new ArrayList<>(descendants.entrySet());
    for (int i = 0; i < entries.size(); i++) {
      Map.Entry<String, List<String>> a = entries.get(i);
      for (int j = i + 1; j < entries.size(); j++) {
        Map.Entry<String, List<String>> b = entries.get(j);
        if (areEqual(a.getValue(), b.getValue())) {
          System.out.printf("%s and %s are equal.%n", a, b);
          toRemove.add(a.getKey());
        }
      }
    }
    for (String key : toRemove) {
      descendants.remove(key);
    }
  }

  private static boolean areEqual(List<String> a, List<String> b) {
    if (a.size() != b.size()) {
      return false;
    }
    for (int i = 0; i < a.size(); i++) {
      if (!a.get(i).equals(b.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static void linearize2(
      File repo, String b1, TreeMap<String, List<String>> descendants, boolean dryRun)
      throws IOException, InterruptedException {
    if (!descendants.containsKey(b1)) {
      return;
    }
    for (String b2 : descendants.get(b1)) {
      System.out.printf("Checkout %s and amend last commit to now.%n", b2);
      if (!dryRun) {
        exec(true, repo, "git", "checkout", b2);
        List<String> amend =
            exec(true, repo, "git", "commit", "--amend", "--date=now", "--no-edit");
        System.out.println("amend = " + amend);
      }

      System.out.printf("Rebase %s and %s.%n", b1, b2);
      if (!dryRun) {
        exec(true, repo, "git", "checkout", b2);
        List<String> rebase = exec(true, repo, "git", "rebase", b1, b2);
        System.out.println("rebase = " + rebase);
      }
    }
    for (String b2 : descendants.get(b1)) {
      linearize2(repo, b2, descendants, dryRun);
    }
  }

  private static final boolean shouldCmdWrappedToBash = true;

  private static List<String> exec(boolean exitIfNoSuccess, File wd, String... cmd)
      throws IOException, InterruptedException {
    String[] newCmd;
    if (shouldCmdWrappedToBash) {
      newCmd = new String[] {"bash", "-c", String.join(" ", cmd)};
    } else {
      newCmd = cmd;
    }
    ProcessBuilder builder = new ProcessBuilder(newCmd);
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

    if (exitIfNoSuccess && result != 0) {
      System.out.println(Arrays.toString(newCmd));
      for (String l : lines) {
        System.out.println(l);
      }
      System.out.println("Aborted.");
      System.exit(0);
    }

    return lines;
  }
}
