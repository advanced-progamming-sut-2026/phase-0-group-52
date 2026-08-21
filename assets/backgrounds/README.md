# Title backdrops

`backdrop_A.png` through `backdrop_J.png` are the game's ten title screens, shown
at random on every launch.

## How they were prepared

1. Source: `pvz-assets/ATLASES/TITLESCREEN1..10_768_00.PNG`, which map to
   `IMAGE_TITLEBACKGROUNDS_BACKDROP_A..J`. Each atlas holds one 1024x768 image
   padded out to 1024x1024.
2. Upscaled 3x with Upscayl (`realesrgan-x4plus-anime`), which removes the
   compression grain and sharpens the artwork noticeably.
3. Cropped to the region the game actually shows and saved at 1920x1080.

Step 3 matters: the window is 16:9 but the artwork is 4:3, so only a 868x488
window of the original was ever drawn - 54% of the pixels. Cropping to that
window cut the set from 73 MB to 21 MB with no visible difference, and means the
files are exactly 1080p, matching fullscreen on a typical monitor.

Because they are pre-cropped, `Backdrops` loads them whole and `TitleScreen`
draws them with `Scaling.fill` against a fill-parent image. No cropping or zoom
happens at runtime.

## Licence

These are EA/PopCap assets, committed so the whole team has them without each
person extracting their own copy. Keep that in mind if the repository is ever
made public or handed in outside the course.

Without them the title screen falls back to a plain gradient.
