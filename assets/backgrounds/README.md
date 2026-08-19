# Title backdrops

`backdrop_A.png` through `backdrop_J.png` are the game's ten title screens, taken
from `pvz-assets/ATLASES/TITLESCREEN1..10_768_00.PNG`. Each atlas holds a single
1024x768 image, so the files are used directly rather than through an atlas.

`Backdrops.java` picks one at random on every launch and crops to the top
1024x768 (the source PNGs are padded to 1024x1024).

## Licence

These are EA/PopCap assets, committed here so the whole team has them without
each person extracting their own copy. Keep that in mind if the repository is
ever made public or handed in outside the course.

Without them the title screen falls back to a plain gradient.
