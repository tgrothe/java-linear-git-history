# java-linear-git-history

Create a linear history of all local branches through rebase.

No guarantee. Make backups, just in case.

## Example

Before:

```
git log --graph --pretty=oneline --abbrev-commit --all
* 33d9179 (HEAD -> 03) 03 new
| * 4327237 (02) 02
| | * ad680c5 (04) 04
| |/
|/|
| | * 78174a0 (03_a) 03 a
| |/
|/|
* | 6e0ccae 03
|/
* a8618b5 (01) 01
* 72fe483 (master) init
```

And afterwards:

```
git log --graph --pretty=oneline --abbrev-commit --all
* 974185e (HEAD -> 04) 04
* 13fbd01 03
| * c3d9053 (03_a) 03 a
| * f24a908 03
|/
| * 3f81066 (03) 03 new
| * 97b712f 03
|/
| * 7db0e56 (02) 02
|/
* 1dff6bd (01) 01
* 72fe483 (master) init
```

(This is a bit confusing yet because "03" appears here multiple times.)
