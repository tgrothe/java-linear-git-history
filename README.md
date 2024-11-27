# java-linear-git-history

Create a linear history of all local branches through rebase.

No guarantee. Make backups, just in case.

## Example

Before:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* c18b859 04_01 b
* 8d2b8ce 04_01 a
| * 54e2f28 03_02 b
| * a8d5d2f 03_02 a
| | * bd9910c 02_02 b
| | * fa8608e 02_02 a
| | | * 3f97093 04_02 b
| | | * ab1d132 04_02 a
| |_|/
|/| |
* | | b193e57 04 b
* | | 812ac52 04 a
|/ /
| | * cdd25ee 03_01 b
| | * 0436480 03_01 a
| |/
|/|
* | 8481d3e 03 b
* | 4055f86 03 a
|/
| * 2a296b0 02_01 b
| * fe2ec34 02_01 a
|/
* fe570a4 02 b
* 421bfd4 02 a
| * b1b71a8 01_01 b
| * 73af8a1 01_01 a
|/
| * d7d25d9 01_02 b
| * 67587d2 01_02 a
|/
* 6b7509a 01 b
* cf85257 01 a
* aec5c62 Init master
```

and after:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* 2dea885 04_02 b
* 5097d6b 04_02 a
| * d82e9ff 04_01 b
| * 01e8a68 04_01 a
|/
* eb26ea4 04 b
* 480e870 04 a
| * 6e64ece 03_02 b
| * 018cd20 03_02 a
|/
| * 5234c59 03_01 b
| * 03abd82 03_01 a
|/
* 68ffe51 03 b
* da0da7a 03 a
| * 89a32af 02_02 b
| * c0ea807 02_02 a
|/
| * 4d6e82e 02_01 b
| * 15803bd 02_01 a
|/
* c7e856a 02 b
* 40b0f21 02 a
| * 9b4238b 01_02 b
| * a5b1083 01_02 a
|/
| * 1fce315 01_01 b
| * b53d682 01_01 a
|/
* 75c7670 01 b
* cf85257 01 a
* aec5c62 Init master
```

In this example, each last commits on branches are amended. As you can see, there are no more crossing paths, the commits should be untangled and linear. =)
