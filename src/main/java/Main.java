import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class Main {
  private static boolean amended;

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.print("Enter full path to repo: ");
    String path = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    System.out.print("Enter main branch name: ");
    String main = new Scanner(System.in, Charset.defaultCharset()).nextLine();
    linearize1(new File(path), main);
    System.out.println(
        "Should all branches found also be amended? This can sometimes be useful. Then enter yes:");
    amended = "yes".equals(new Scanner(System.in, Charset.defaultCharset()).nextLine());
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

    for (int i = 0; i < branches.size(); i++) {
      System.out.printf("%02d: %s%n", i + 1, branches.get(i));
    }
    System.out.println(
        "Speedup. Wich branches to exclude? Type for example 2-5 or 2 3 4 5. <Enter> for no excludes:");
    String[] excludes = new Scanner(System.in, Charset.defaultCharset()).nextLine().split(" ");
    List<Integer> excludeIndices = new ArrayList<>();
    for (String ex : excludes) {
      if (ex.isBlank()) {
        continue;
      }
      if (ex.contains("-")) {
        String[] sa = ex.split("-");
        int from = Integer.parseInt(sa[0]);
        int to = Integer.parseInt(sa[1]);
        for (int i = from; i <= to; i++) {
          excludeIndices.add(i - 1);
        }
      } else {
        excludeIndices.add(Integer.parseInt(ex) - 1);
      }
    }
    excludeIndices.sort(Comparator.reverseOrder());
    for (int ie : excludeIndices) {
      System.out.println(branches.remove(ie) + " removed.");
    }

    TreeMap<String, List<String>> commits = new TreeMap<>();
    for (String b : branches) {
      System.out.printf("Getting revs of %s ...%n", b);
      List<String> revs = exec(true, repo, "git", "rev-list", "--first-parent", b);
      revs.remove(0);
      commits.put(b, revs);
    }
    for (int i = 0; i < branches.size(); i++) {
      String b1 = branches.get(i);
      List<String> c1 = commits.get(b1);
      for (int j = i + 1; j < branches.size(); j++) {
        String b2 = branches.get(j);
        List<String> c2 = commits.get(b2);
        boolean isNewer = new HashSet<>(c1).containsAll(c2);
        if (isNewer) {
          c1.removeAll(c2);
        } else {
          c2.removeAll(c1);
        }
      }
    }

    HashMap<Map.Entry<String, String>, Boolean> ancestorMap = new HashMap<>();
    for (int i = 0; i < branches.size(); i++) {
      String b1 = branches.get(i);
      List<String> c1 = commits.get(b1);
      for (int j = i + 1; j < branches.size(); j++) {
        String b2 = branches.get(j);
        List<String> c2 = commits.get(b2);
        System.out.printf("Check %s and %s.%n", b1, b2);
        boolean a =
            "0"
                .equals(
                    exec(
                            false,
                            repo,
                            "git",
                            "merge-base",
                            "--is-ancestor",
                            c1.get(c1.size() - 1),
                            c2.get(0))
                        .get(0));
        boolean b =
            "0"
                .equals(
                    exec(
                            false,
                            repo,
                            "git",
                            "merge-base",
                            "--is-ancestor",
                            c2.get(c2.size() - 1),
                            c1.get(0))
                        .get(0));
        AbstractMap.SimpleEntry<String, String> e1 = new AbstractMap.SimpleEntry<>(b1, b2);
        AbstractMap.SimpleEntry<String, String> e2 = new AbstractMap.SimpleEntry<>(b2, b1);
        if (ancestorMap.containsKey(e1) || ancestorMap.containsKey(e2)) {
          throw new RuntimeException("Something went wrong.");
        }
        ancestorMap.put(e1, a);
        ancestorMap.put(e2, b);
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
          //          System.out.printf("%s is ancestor of %s.%n", b1, b2);
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
      if (amended) {
        System.out.printf("Checkout %s and amend last commit to now.%n", b2);
        if (!dryRun) {
          exec(true, repo, "git", "checkout", b2);
          List<String> amend =
              exec(
                  true,
                  repo,
                  "git",
                  "commit",
                  "--amend",
                  "--date=now",
                  "--no-edit",
                  "--allow-empty");
          System.out.println("amend = " + amend);
        }
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

  public static List<String> exec(boolean exitIfNoSuccess, File wd, String... cmd)
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
