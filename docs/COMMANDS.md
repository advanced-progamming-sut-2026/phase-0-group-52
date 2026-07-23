# راهنمای کامل دستورات PvZ2

> نقطه‌ی ورود: کلاس `Main` را Run کن. برنامه در منوی SignUp/Login شروع می‌شود.
> ناوبری بین منوها: `menu enter <menu_name>` و `menu show current`.
> نام منوها برای `menu enter`: `main`, `login`, `settings`, `news`, `chapter_menu`,
> `collection_menu`, `greenhouse_menu`, `travel_log_menu`, `choose_plant_menu`.

---

## احراز هویت (SignUp / Login)

| دستور | کار |
|-------|-----|
| `register -u <username> -p <password> <passwordConfirm> -n <nickname> -e <email> -g <male\|female>` | ثبت‌نام |
| `pick question -q <1-7> -a <answer> -c <answerConfirm>` | انتخاب سوال امنیتی (بعد از register) |
| `menu enter login` | رفتن به منوی ورود |
| `login -u <username> -p <password> [-stay-logged-in]` | ورود |
| `forget password -u <username> -e <email>` | فراموشی رمز |
| `answer -a <answer>` | پاسخ سوال امنیتی |
| `menu show current` | نمایش منوی فعلی |
| `menu exit` | خروج از برنامه (در منوی SignUp) |

**نکته:** پسورد ≥۸ کاراکتر با حرف بزرگ، کوچک، عدد و یک نماد خاص. نمادهای مجاز: `! # $ % ^ & * ( ) = + { } [ ] | / : ; ' " , < > ? \` (علامت `@` مجاز نیست).

---

## منوی اصلی (Main Menu)

| دستور | کار |
|-------|-----|
| `menu show current` | نمایش منوی فعلی |
| `menu enter <menu_name>` | رفتن به منوی دیگر |
| `menu leaderboard [-s <ستون>] [-o <asc\|desc>]` | جدول امتیازات؛ ستون: `score`, `level`, `minigames`, `daily`, `quests` |
| `menu logout` یا `logout` | خروج از حساب |

---

## منوی تنظیمات (Settings)

| دستور | کار |
|-------|-----|
| `menu settings change-difficulty -l <1-5>` | تغییر سطح سختی |

---

## منوی اخبار (News)

| دستور | کار |
|-------|-----|
| `menu news show-unread` | اعلان‌های خوانده‌نشده (بعد از نمایش، خوانده‌شده می‌شوند) |
| `menu news show-all` | همه‌ی اعلان‌ها |

---

## منوی کلکسیون (Collection)

| دستور | کار |
|-------|-----|
| `show plants` | لیست همه‌ی گیاهان با هزینه/جان/دمیج/دسته/سطح |
| `show zombies` | لیست همه‌ی زامبی‌ها با جان/سرعت/EatDPS/زره |
| `upgrade plant -t <plant>` | ارتقای یک گیاه |
| `buy plant -t <plant>` | خرید و آنلاک یک گیاه با سکه |

---

## منوی چپتر و شروع مرحله (Chapter)

| دستور | کار |
|-------|-----|
| `menu enter chapter -c <chapterName>` | ورود به فصل. مقادیر: `ANCIENT_EGYPT`, `FROSTBITE_CAVES`, `BIG_WAVE_BEACH`, `DARK_AGES` |
| `start level [-c <chapter>] [-l <number>]` | **شروع بازی** با فصل انتخاب‌شده (اگر `-c` ندهی از فصل فعلی) |
| `menu coin-wallet` | موجودی سکه |
| `menu gem-wallet` | موجودی الماس |
| `menu cheat add <n> <coin\|diamond>` | اضافه کردن سکه/الماس |
| `menu leaderboard` | جدول امتیازات |

---

## صفحه انتخاب گیاهان (Choose Plant)

| دستور | کار |
|-------|-----|
| `show plants` | لیست گیاهان قابل انتخاب |
| `choose -t <plant>` | افزودن به انتخاب (حداکثر ۷) |
| `remove -t <plant>` | حذف از انتخاب |
| `boost -t <plant>` | بوست یک گیاهِ انتخاب‌شده با ذخیره‌ی گلخانه |
| `show selection` | نمایش انتخاب فعلی |
| `clear selection` | پاک کردن انتخاب |

---

## داخل بازی (Game)

مختصات ۱-مبنا: `x` = ستون (۱ تا ۹)، `y` = ردیف (۱ تا ۵).

### چرخه‌ی بازی و خورشید
| دستور | کار |
|-------|-----|
| `advance time -t <count> ticks` | جلو بردن زمان (طبق داک). هر ۱۰ تیک = ۱ ثانیه |
| `tick [n]` | معادل کوتاه `advance time` (پیش‌فرض ۱): تولید خورشید، موج‌ها، حرکت زامبی، برد/باخت |
| `collect sun -l (<x>, <y>)` | جمع کردن خورشید در مختصات مشخص (رادیواکتیو در حال سقوط منفجر می‌شود) |
| `collect sun` | جمع کردن همه‌ی خورشیدهای روی زمین |

### کاشت و مدیریت گیاه
| دستور | کار |
|-------|-----|
| `plant plant -t <type> -l (<x>, <y>)` | کاشت گیاه (نام: `Peashooter` یا `PEASHOOTER`) |
| `pluck plant -l (<x>, <y>)` | برداشتن گیاه (بیل) |
| `feed plant -l (<x>, <y>)` | دادن غذای گیاه |

### چیت‌ها
| دستور | کار |
|-------|-----|
| `cheat add -n <count> suns` | +count خورشید (طبق داک) |
| `cheat sun [<n>]` | معادل کوتاه (+n، پیش‌فرض ۱۰۰) |
| `cheat remove-cooldown` | حذف همه‌ی cooldownها |
| `cheat add-plant-food` | +۱ غذای گیاه |
| `cheat spawn-zombie -t <zombie-type> -l <x, y>` | ساخت زامبی (مثل `ZOMBIE_GARGANTUAR` یا `Gargantuar`) |

### نمایش وضعیت
| دستور | کار |
|-------|-----|
| `show map` | نقشه‌ی کامل (موج، خورشید، غذا، ماشین چمن‌زنی، زامبی‌ها) |
| `show sun amount` | مقدار خورشید فعلی و تعداد غذای گیاه (`show sun` هم کار می‌کند) |
| `show plants status` | وضعیت گیاهان (هزینه، آماده/در حال شارژ) |
| `show tile status -l (<x>, <y>)` | جزئیات گیاهان و زامبی‌های یک خانه |
| `zombies info` | اطلاعات تمام زامبی‌های روی زمین |

### مراحل ویژه
| دستور | کار |
|-------|-----|
| `start zombie waves` | شروع موج‌ها (فقط در مرحله‌ی «هر چه رسد بکار») |

---

## گلخانه و فروشگاه (Greenhouse / Shop)

### گلخانه
| دستور | کار |
|-------|-----|
| `show greenhouse` | جدول گلخانه (قفل/خالی/در حال رشد/آماده) |
| `plant pot at (<x>, <y>)` | کاشت گیاه تصادفی در گلدان |
| `collect (<x>, <y>)` | برداشت (Marigold = ۵۰۰ سکه؛ گیاه خاص = بوست ذخیره) |
| `grow (<x>, <y>)` | تسریع رشد با الماس |
| `upgrade plant -t <plant>` | ارتقای گیاه |
| `show plant levels` | سطح گیاهان ارتقایافته |

### فروشگاه (از داخل گلخانه)
| دستور | کار |
|-------|-----|
| `enter shop` | ورود به فروشگاه |
| `shop list` | کالاهای دائمی |
| `shop daily` | پیشنهاد روزانه |
| `shop buy -i <item_id> -n <count> [-t <plant_type>]` | خرید (۱=گلدان، ۲=غذای گیاه، ۳=بسته بذر تصادفی، ۴=بسته بذر انتخابی، ۵=تبدیل ارز، ۶=روزانه) |

---

## Travel Log و مینی‌گیم‌ها

| دستور | کار |
|-------|-----|
| `travel log page <page>` | نمایش یک صفحه؛ page: `daily`, `main`, `epic`, `minigame` |
| `show quests` | کوئست‌های صفحه‌ی فعلی |
| `play <minigame> <1-3>` | شروع مینی‌گیم. minigame: `beghouled`, `vase_breaker`, `wallnut_bowling`, `izombie` |
| `menu show current` / `menu exit` | ناوبری |

### داخل Beghouled (ترکیب سه‌تایی)
`show` | `swap <x1> <y1> <x2> <y2>` | `upgrades` | `upgrade <plant>` | `exit`

### داخل Vasebreaker (کوزه‌شکن)
`break (x, y)` | `plant -t <type> -l (x, y)` | `tick` | `show` | `exit`

### داخل Wallnut Bowling (بولینگ گردویی)
`plant -l (x, y)` (از نوار نقاله) | `tick` | `show` | `exit`

### داخل I, Zombie (من‌زامبی)
`place -t <default|armor1|armor2|imp|gargantuar> -l (x, y)` | `tick` | `show` | `exit`

---

## مثال کامل: از ثبت‌نام تا برد یک مرحله

```
register -u hero -p Test!1234 Test!1234 -n Hero -e hero@test.com -g male
pick question -q 1 -a blue -c blue
menu enter login
login -u hero -p Test!1234
menu enter main
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT
start level -l 1
cheat sun 2000
plant plant -t Repeater -l (1, 3)
plant plant -t Sunflower -l (1, 2)
tick 30
collect sun
show map
tick 250
```

## مثال: بازی کردن یک مینی‌گیم

```
menu enter main
menu enter travel_log_menu
play beghouled 1
```
