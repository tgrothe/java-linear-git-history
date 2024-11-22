# java-linear-git-history

Create a linear history of all local branches through rebase.

No guarantee. Make backups, just in case.

## Example

Before:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* c47c35c 03_01 b
* c781435 03_01 a
| * dfe59e1 01_02 b
| * 52350b6 01_02 a
| | * 266bee8 03_02 b
| | * 65e4862 03_02 a
| |/
|/|
* | c55a33d 03 b
* | 5a1e37f 03 a
| | * ddd09e3 02_01 b
| | * ad5018b 02_01 a
| |/
|/|
| | * c5799ae 02_02 b
| | * 66a31b2 02_02 a
| |/
|/|
* | 41a2a87 02 b
* | 47faff0 02 a
|/
| * 7964bd8 01_01 b
| * 6bea479 01_01 a
|/
* f68c594 01 b
* aa55c79 01 a
* 85ed87e Init master
```

And afterwards:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* 20c0423 (HEAD -> 03_02) 03_02 b
* fea1ac8 03_02 a
* 1b16492 03 b
* f4aad2c 03 a
* 4fd3a30 02 b
* 97a5855 02 a
* 980f2fb 01 b
| * 5ba2ea5 (03_01) 03_01 b
| * 0c025d4 03_01 a
| * 0ccd871 03 b
| * cc255ff 03 a
| * 569c1cd 02 b
| * 8cf9d27 02 a
| * 9693f35 01 b
|/
* 29ad7aa (03) 03 b
* 1c75c36 03 a
* fd8031b 02 b
* b389eda 02 a
* 18b1e81 01 b
| * 3d350f6 (02_02) 02_02 b
| * 63c7b30 02_02 a
| * 9486188 02 b
| * fae4070 02 a
| * 12de050 01 b
|/
| * 2a98877 (02_01) 02_01 b
| * 2d7b94c 02_01 a
| * f0328fc 02 b
| * 99e19f0 02 a
| * 7f57b81 01 b
|/
* 4808d09 (02) 02 b
* 201bd8c 02 a
* bf671e3 01 b
| * 2966d15 (01_02) 01_02 b
| * a0ba9c8 01_02 a
| * 9580f78 01 b
|/
| * 8c83e51 (01_01) 01_01 b
| * f96ce95 01_01 a
| * dd2c63d 01 b
|/
* 3667623 (01) 01 b
* aa55c79 01 a
* 85ed87e (master) Init master
```
