# java-linear-git-history

Create a linear history of all local branches through rebase.

No guarantee. Make backups, just in case.

## Example

Before:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* af3cf1d 02_02 b
* 71f9c52 02_02 a
| * 826e3e7 02_03 b
| * 8c4c4dc 02_03 a
|/
| * 6f803e7 03_02 b
| * 952eb60 03_02 a
| | * f246cc5 03_01 b
| | * 488c8a5 03_01 a
| |/
| | * 926f31c 03_03 b
| | * e0a8b37 03_03 a
| |/
| * c048b74 03 b
| * e145af1 03 a
|/
| * f5a535e 01_01 b
| * da7274f 01_01 a
| | * 6a186e9 02_01 b
| | * b637c6b 02_01 a
| |/
|/|
* | 9e10352 02 b
* | 7097703 02 a
|/
| * d9eda91 01_02 b
| * e244295 01_02 a
|/
| * 554534d 01_03 b
| * 936029a 01_03 a
|/
* e8bc96c 01 b
* 79de9d0 01 a
* e61a5be Init master
```

and after:

```plain
git log --graph --pretty=oneline --abbrev-commit --all
* ae908a6 03_02 b
* 623915d 03_02 a
| * 8fd9fd9 03_03 b
| * 7ee399d 03_03 a
|/
* b4dde2c 03 b
* a134c86 03 a
* ca4173e 02 b
* c9de5a1 02 a
* f797a5c 01 b
| * ace917d 03_01 b
| * 812cc26 03_01 a
| * 078c1aa 03 b
| * 5f87000 03 a
| * 14f442f 02 b
| * 6bfb782 02 a
| * 7ae9593 01 b
|/
* 315ddd1 03 b
* 92e84e6 03 a
| * 6fc13cc 02_03 b
| * 77915d5 02_03 a
|/
* cc14fab 02 b
* 25f5070 02 a
* b0747c8 01 b
| * 28cf001 02_01 b
| * a78255c 02_01 a
| | * 4ea45b9 02_02 b
| | * eda1cae 02_02 a
| |/
| * 2692caf 02 b
| * 4403ddc 02 a
| * 005f18b 01 b
|/
* 63b9183 02 b
* 329565c 02 a
| * ad153d4 01_03 b
| * 3a5cdfe 01_03 a
|/
* 7ccdec6 01 b
| * 15d223c 01_01 b
| * f970c6e 01_01 a
| | * b2d199d 01_02 b
| | * 923c9ac 01_02 a
| |/
| * 7510a59 01 b
|/
* 0931c8b 01 b
* 79de9d0 01 a
* e61a5be Init master
```

In this example, each last commits on branches are amended. As you can see, there are no more crossing paths.
