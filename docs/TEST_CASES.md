# تست‌کیس‌های کامل PvZ2 (سناریو + دستور + خروجی مورد انتظار)

> نقطه‌ی ورود: کلاس `Main` را Run کن. برنامه در منوی SignUp/Login شروع می‌شود.
> مختصات همه‌جا **۱-مبنا** است: `x` = ستون (۱ تا ۹)، `y` = ردیف (۱ تا ۵).
> برای جلو بردن زمان از `tick <n>` استفاده کن (هر ۱۰ تیک = ۱ ثانیه).
> اولین خط ورودی کنسول خورده می‌شود؛ اگر خروجی را از فایل/اسکریپت می‌دهی، یک خط دور‌ریز (مثل `x`) اول بفرست.

**فهرست:**
1. آماده‌سازی (ثبت‌نام/ورود)
2. ناوبری منوها
3. پروفایل
4. کلکسیون
5. گلخانه و فروشگاه
6. Travel Log و کوئست‌ها
7. شروع مرحله‌ی عادی (انتخاب دک)
8. دستورهای داخل بازی
9. مکانیزم فصل‌ها
10. مراحل ویژه (Special Levels) — هر ۸ تا
11. مینی‌گیم‌ها — هر ۵ تا
12. افکت غذای گیاه (Plant Food)
13. برد/باخت، Meow Points، ریست دک، دراپ آیتم
14. Imitater
15. بلوک کامل نمونه

---

## ۰) بلوک ثبت‌نام/ورود (پیش‌نیاز همه تست‌ها)

```
register -u hero -p Test!1234 Test!1234 -n Hero -e hero@test.com -g male
pick question -q 1 -a blue -c blue
menu enter login
login -u hero -p Test!1234
```
**انتظار:** ثبت‌نام و ورود موفق، رفتن به منوی اصلی.
**نکته:** نیک‌نیم ۳ تا ۳۰ کاراکتر؛ پسورد ≥۸ با حرف بزرگ/کوچک/عدد/نماد (بدون `@`).

---

## ۱) ناوبری منوها

| دستور | انتظار |
|-------|--------|
| `menu show current` | نام منوی فعلی |
| `menu enter chapter_menu` | منوی چپتر |
| `menu enter collection_menu` | کلکسیون |
| `menu enter greenhouse_menu` | گلخانه |
| `menu enter travel_log_menu` | Travel Log |
| `menu enter choose_plant_menu` | انتخاب گیاه |
| `menu enter main` | منوی اصلی |
| `logout` | خروج از حساب |

---

## ۲) پروفایل

```
menu enter profile_menu
menu profile show-info
menu profile change-username -u newhero
menu profile change-nickname -u NewHero
menu profile change-email -e new@test.com
menu profile change-password -p New!2345 -o Test!1234
```
**انتظار:** نمایش اطلاعات شامل خط `Meow Points:`؛ هر تغییر پیام موفقیت.

---

## ۳) کلکسیون

```
menu enter collection_menu
menu collection show-plants
menu collection show-zombies
menu collection show-plant -p peashooter
menu collection show-zombie -z zombie_default
menu collection upgrade-plant -p peashooter
menu collection purchase-plant -p threepeater
```
**انتظار:** لیست گیاهان/زامبی‌ها؛ خرید هر گیاه **۲۰۰۰ سکه ثابت**؛ ارتقا سطح گیاه را بالا می‌برد.
اگر سکه کم بود: `menu cheat add 5000 coin`.

---

## ۴) گلخانه و فروشگاه

```
menu enter greenhouse_menu
show greenhouse
plant pot at (1, 1)
grow (1, 1)
collect (1, 1)
show plant levels
enter shop
shop list
shop daily
shop buy -i 1 -n 1
shop buy -i 3 -n 1
shop buy -i 4 -n 1 -t peashooter
```
**انتظار:** جدول گلخانه (قفل/خالی/در حال رشد/آماده)؛ `grow` با الماس رشد را تسریع می‌کند؛ `collect` روی Marigold=۵۰۰ سکه، روی گیاه خاص=بوست ذخیره. آیتم‌های فروشگاه: ۱=گلدان، ۲=غذای گیاه، ۳=بسته بذر تصادفی، ۴=بسته بذر انتخابی، ۵=تبدیل ارز، ۶=روزانه.

---

## ۵) Travel Log و کوئست‌ها

```
menu enter travel_log_menu
travel log page daily
show quests
travel log page main
travel log page epic
travel log page minigame
```
**انتظار:** هر صفحه کوئست‌های همان دسته را نشان می‌دهد؛ صفحه‌ی `minigame` لیست ۵ مینی‌گیم را می‌دهد.

---

## ۶) شروع مرحله‌ی عادی (فلوی انتخاب دک)

```
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT -l 1
menu enter choose_plant_menu
show plants
choose -t sunflower
choose -t peashooter
choose -t wall_nut
show selection
start
```
**انتظار:** بعد از `start` → `Level started in ANCIENT_EGYPT (level 1) with 3 plant(s)...`
**تست ارور دک خالی:** بدون انتخاب گیاه، در منوی چپتر `start level -l 2` →
`Error: Pick your plant deck first: 'menu enter choose_plant_menu', choose plants, then 'start'.`
**تست ارور بدون چپتر:** قبل از ورود به چپتر `start level -l 2` →
`Error: Enter a chapter first: menu enter chapter -c <chapter>`

---

## ۷) دستورهای داخل بازی

```
show map
show sun amount
show plants status
show tile status -l (1, 1)
zombies info
cheat sun 2000
cheat add-plant-food
cheat remove-cooldown
cheat spawn-zombie -t zombie_default -l 9, 3
plant plant -t peashooter -l (1, 3)
plant plant -t sunflower -l (1, 2)
feed plant -l (1, 3)
pluck plant -l (1, 2)
collect sun
tick 30
tick 200
```
**انتظار:**
- `show map`: فرمت خانه `<tile><plant><zombies>`؛ `Z`=یک زامبی، رقم=چند زامبی، `[M]`=ماشین چمن‌زنی. راهنمای تایل‌ها: `. normal  T tombstone  ~ water  # frozen  ^ slip-up  v slip-down  _ low-ground  N necromancy`.
- کاشت → `... planted at (x, y).`؛ `feed` → مصرف غذای گیاه؛ `pluck` → برداشت.
- `cheat spawn-zombie` یک زامبی می‌سازد؛ با گذر زمان موج‌ها/برد/باخت.

---

## ۸) مکانیزم فصل‌ها

### ۸.۱ مصر باستان (ANCIENT_EGYPT)
شروع مرحله در این فصل → چند خانه‌ی `T` (سنگ‌قبر) که مانع کاشت‌اند.

### ۸.۲ یخبندان (FROSTBITE_CAVES)
```
menu enter chapter -c FROSTBITE_CAVES -l 1
menu enter choose_plant_menu
choose -t peashooter
choose -t sunflower
start
tick 3
show map
```
**انتظار:**
- ابتدای بازی: `A zombie is frozen solid in row X as the level begins!` (۱ تا ۲ زامبیِ یخ‌زده، `x=8.00`، بی‌حرکت؛ بعد از ~۸ ثانیه آب می‌شوند).
- با موج: `An icy wind hits row X!` (گیاهان همان ردیف فریز می‌شوند مگر آتشین).
- گیاه آتشین کنار خانه‌ی یخی، یخ را آب می‌کند.

### ۸.۳ ساحل موج بزرگ (BIG_WAVE_BEACH)
```
menu enter chapter -c BIG_WAVE_BEACH -l 1
menu enter choose_plant_menu
choose -t lily_pad
choose -t sun_shroom
choose -t peashooter
start
show map
plant plant -t lily_pad -l (9, 1)
plant plant -t sun_shroom -l (9, 1)
tick 60
tick 60
tick 200
show map
```
**انتظار:**
- ستون راست از ابتدا آب `~`.
- **جزر و مد با گذر زمان:** `The tide rises - 2 column(s) underwater.` → `A big wave rolls in! The rightmost 3 columns are underwater.` → `The tide falls - 2 column(s) underwater.` → `The tide recedes back to the shoreline.` (نوسانی).
- **کاشت روی آب فقط با لیلی‌پد:** روی آب بدون لیلی‌پد → `Only water plants can be planted on water. Place a Lily Pad first.`
- **استکینگ:** `Lily Pad planted at (9, 1).` سپس `Sun-shroom planted at (9, 1).`
- **شسته‌شدن:** گیاه خاکی که زیر آب برود → `The tide washed away the plant at (x, y)!` (لیلی‌پد نگهش می‌دارد).
- **ساحل پست:** خانه‌های `_` (LOW_GROUND)؛ زامبی از زیرشان درمی‌آید → `A zombie surfaced from the low beach at (x, y)!`

### ۸.۴ عصر تاریکی (DARK_AGES)
```
menu enter chapter -c DARK_AGES -l 2
menu enter choose_plant_menu
choose -t peashooter
choose -t sunflower
start
tick 200
```
**انتظار:**
- روییدن قبر: `A grave rose at (x, y) containing <sun|plant food> [necromancy]!`
- **نکرومنسی:** `Necromancy! A zombie rose from the grave at (x, y)!`
- تخریب قبرِ حاوی جایزه: `The grave dropped 50 sun!` یا `The grave dropped a plant food; ...`

---

## ۹) مراحل ویژه (Special Levels)

دستور کلی: `start special -t <type> [-c <chapter>] [-l <n>]`
انواع: `conveyor`, `plant_what_you_get`, `locked_plants`, `save_our_seeds`, `deadline`, `love_your_plants`, `night_ops`, `timed_war`.

> **دو دسته:**
> - **دارای نوار نقاله (گیاه می‌دهند):** `conveyor` و `plant_what_you_get` → مستقیم بازی شروع می‌شود.
> - **بقیه:** بعد از `start special` **خودکار** وارد منوی انتخاب گیاه می‌شوی؛ دک را می‌چینی و `start` (یا `start level -l <n>`) می‌زنی.
>
> پیام شروع همیشه به‌صراحت می‌گوید: `Special level '<type>' started in <chapter> ...` — یعنی حتماً همان مرحله‌ی ویژه اجرا می‌شود، نه مرحله‌ی عادیِ فصل.

### ۹.۱ Conveyor
```
menu enter chapter -c ANCIENT_EGYPT -l 1
start special -t conveyor
show map
plant plant -t peashooter -l (1, 3)
tick 200
```
**انتظار:** بدون خورشید/ریچارج؛ `The conveyor belt delivered a <plant>.`؛ فقط گیاهِ روی نوار قابل کاشت.

### ۹.۲ Plant What You Get
```
menu enter chapter -c ANCIENT_EGYPT -l 2
start special -t plant_what_you_get
tick 40
show map
start zombie waves
show map
tick 160
```
**انتظار:**
- گیاه از نوار می‌آید؛ خورشید ۰؛ ریچارج ندارد.
- **قبل از کامند هیچ موجی نمی‌آید** (`Zombies: 0`).
- `start zombie waves` → `Zombie waves started! Wave 1 incoming! N zombie(s).`
- سپس موج‌های بعدی با گذر زمان: `Wave 2 incoming! ...`

### ۹.۳ Locked Plants
```
menu enter chapter -c ANCIENT_EGYPT -l 2
start special -t locked_plants
show plants
choose -t peashooter
choose -t cherry_bomb
start
```
**انتظار:** بعضی گیاهان جلوشان `[LOCKED]`؛ انتخاب قفل → `Error: <plant> is locked this level.`؛ بقیه‌ی بازی عادی.
پیام شروع: `Special level 'locked_plants' started ...`

### ۹.۴ Save Our Seeds
```
menu enter chapter -c ANCIENT_EGYPT -l 2
start special -t save_our_seeds
choose -t peashooter
choose -t wall_nut
start
show map
```
**انتظار:** چند گیاهِ محافظت‌شده از قبل در ستون اول؛ از دست رفتن یکی → `A protected plant was lost. You lose!`

### ۹.۵ Deadline
```
menu enter chapter -c ANCIENT_EGYPT -l 2
start special -t deadline
choose -t peashooter
choose -t sunflower
start
show map
```
**انتظار:** روی نقشه `[DEAD LINE]  Do not let a zombie cross the '|' at column N!` و علامت `|`؛ رد شدن زامبی → `A zombie crossed the dead line. You lose!`

### ۹.۶ Timed War
```
menu enter chapter -c ANCIENT_EGYPT -l 2
start special -t timed_war
choose -t peashooter
choose -t repeater
start
show map
tick 100
```
**انتظار:** بالای نقشه `[TIMED WAR]  Time left: X.Xs  |  Kills: N`؛ رسیدن به هدف در زمان → برد؛ اتمام زمان → `Time is up. You lose!`

### ۹.۷ Love Your Plants
```
menu enter chapter -c DARK_AGES -l 2
start special -t love_your_plants
choose -t peashooter
choose -t wall_nut
start
```
**انتظار:** از دست‌دادن بیش‌ازحد گیاه → `You lost N plants. You lose!`

### ۹.۸ Night Ops
```
menu enter chapter -c DARK_AGES -l 2
start special -t night_ops
choose -t puff_shroom
choose -t sun_shroom
start
```
**انتظار:** بازی شبانه (بدون خورشید آسمانی)؛ قارچ‌ها مناسب‌اند.

---

## ۱۰) مینی‌گیم‌ها

از منوی Travel Log: `play <name> <1-3>`
نام‌ها: `vase_breaker`, `wallnut_bowling`, `i_zombie`, `beghouled`, `zombotany`.

```
menu enter travel_log_menu
```

### ۱۰.۱ Vasebreaker
```
play vase_breaker 1
show
break (3, 2)
plant -t peashooter -l (1, 2)
tick 3
show
exit
```
**دستورها:** `break (x, y)` | `plant -t <type> -l (x, y)` | `tick [n]` | `show` | `exit`
**انتظار:** سه نوع کوزه: **سید‌پکت / زامبی / رندوم**؛ شکستن کوزه‌ی سید‌پکت یک بسته روی **زمین** می‌اندازد که بعد از مدتی ناپدید می‌شود؛ بازیکن با `plant -t` بسته‌ی هم‌نوعِ روی زمین را برمی‌دارد و **درجا می‌کارد** (نمی‌تواند نگه دارد)؛ کوزه‌ی خالی نمایش داده نمی‌شود.

### ۱۰.۲ Wallnut Bowling
```
play wallnut_bowling 1
show
plant -l (1, 3)
tick
show
exit
```
**دستورها:** `plant -l (x, y)` (گردو از نوار نقاله) | `tick` | `show` | `exit`
**انتظار:** گردو مثل توپ بولینگ قل می‌خورد و زامبی‌های مسیر را می‌زند.

### ۱۰.۳ I, Zombie
```
play i_zombie 1
show
place -t default -l (9, 2)
place -t gargantuar -l (9, 3)
tick
show
exit
```
**دستورها:** `place -t <default|armor1|armor2|imp|gargantuar> -l (x, y)` | `tick` | `show` | `exit`
**انتظار:** تو زامبی می‌گذاری تا از سد گیاهان رد شوند و به مغزها برسند.

### ۱۰.۴ Beghouled
```
play beghouled 1
show
swap 1 1 2 1
upgrades
upgrade peashooter
exit
```
**دستورها:** `show` | `swap <x1> <y1> <x2> <y2>` | `upgrades` | `upgrade <plant>` | `exit`
**انتظار:** جابه‌جایی دو گیاه مجاور که مچ ۳تایی بسازد؛ غیرمجاور → `Error: You can only swap adjacent plants.`؛ بدون مچ۳ → `Error: That swap does not create a match of 3.`

### ۱۰.۵ Zombotany
```
play zombotany 1
show
plant -t peashooter -l (1, 3)
plant -t wallnut -l (2, 3)
tick
show
exit
```
**دستورها:** `plant -t <peashooter|wallnut> -l (x, y)` | `tick` | `show` | `exit`
**انتظار:** زامبی‌هایی با سرِ گیاه؛ با پی‌شوتر و گردو دفاع می‌کنی.

---

## ۱۱) افکت غذای گیاه (Plant Food)

برای تست: `cheat add-plant-food` سپس `feed plant -l (x, y)`.

| گیاه | افکت مورد انتظار |
|------|------------------|
| Peashooter/Repeater | رگبار سریع نخود |
| Bonk Choy | ضربه به شعاع ۳×۳ اطراف |
| Chomper | بلعیدن تا ۳ زامبیِ جلوییِ همان ردیف |
| Squash | (خودکار) کوبیدن همه‌ی زامبی‌های آن ستون؛ `Squash crushed the zombie(s) at column X!` |
| Iceberg Lettuce | فریز ۱۰ ثانیه‌ای؛ `A zombie in row X was frozen solid for 10 seconds.` |
| Sunflower/Twin | تولید انفجاریِ خورشید |
| Wall-nut | ترمیم جان |

**تست فریز:** بعد از فریز، `zombies info` باید `frozen` را نشان دهد؛ زامبیِ فریز حرکت نمی‌کند.

---

## ۱۲) برد/باخت، Meow Points، ریست دک، دراپ آیتم

- **برد:** با نابودی همه‌ی موج‌ها → `Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.` + `Reward: 500 coins, score ...`
- **باخت:** رسیدن زامبی به خانه → `A zombie reached your house. You lose!` + `Use 'menu enter chapter_menu' to try again.`
- **Meow Points:** پس از پایان → `Meow points earned this level: N (best Meow Points: M).` (کشتار هم‌زمان، کشتار سریع، دفاع بی‌نقص، پاک‌سازی سریع موج).
- **ریست دک:** بعد از هر مرحله → `Your plant deck was reset. ...`
- **دراپ زامبی (۱۰٪):** `A zombie dropeed a <coin/diamond/pot>; you have <n> <coins/diamonds/pots> now.`
- **دراپ زامبیِ درخشان:** `The glowing zombie dropeed a plant food; you have N plant foods now.`

---

## ۱۳) Imitater (تقلیدگر)

در `choose_plant_menu`:
```
choose -t imitater
add plant -t peashooter
start
```
**انتظار:** بعد از انتخاب Imitater پیام «حالا گیاهی را برای کپی انتخاب کن»؛ سپس `The Imitater will copy Peashooter this level.`؛ داخل بازی `plant plant -t imitater -l (x, y)` یک پی‌شوتر می‌کارد.

---

## ۱۴) بلوک کامل نمونه (از صفر تا برد)

```
register -u demo -p Test!1234 Test!1234 -n Demo -e demo@test.com -g male
pick question -q 1 -a blue -c blue
menu enter login
login -u demo -p Test!1234
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT -l 1
menu enter choose_plant_menu
choose -t sunflower
choose -t peashooter
choose -t wall_nut
start
cheat sun 2000
plant plant -t sunflower -l (1, 2)
plant plant -t peashooter -l (2, 3)
plant plant -t wall_nut -l (4, 3)
tick 30
collect sun
show map
tick 300
```
**انتظار:** موج‌ها می‌آیند، دفاع می‌کنی، در پایان: پیام برد + جایزه + Meow Points + ریست دک.
