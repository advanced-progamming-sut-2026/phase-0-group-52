# راهنمای کامل دستورات PvZ2

> نقطه‌ی ورود: کلاس `Main` را Run کن. برنامه در منوی SignUp/Login شروع می‌شود.
> ناوبری بین منوها: `menu enter <menu_name>` و `menu show current`.

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

**نکته‌ها:** پسورد باید ≥۸ کاراکتر با حرف بزرگ، حرف کوچک، عدد و یک نماد خاص باشد. نمادهای مجاز: `! # $ % ^ & * ( ) = + { } [ ] | / : ; ' " , < > ? \` (علامت `@` مجاز **نیست**).

---

## منوی اصلی (Main Menu)

| دستور | کار |
|-------|-----|
| `menu show current` | نمایش منوی فعلی |
| `menu enter <menu_name>` | رفتن به منوی دیگر (مثل `settings`, `news`, `chapter_menu`, `greenhouse_menu`, `travel_log_menu`) |
| `menu leaderboard [-s <ستون>] [-o <asc\|desc>]` | جدول امتیازات؛ ستون: `score`, `level`, `minigames`, `daily`, `quests` |
| `menu logout` یا `logout` | خروج از حساب |

---

## منوی تنظیمات (Settings)

| دستور | کار |
|-------|-----|
| `menu settings change-difficulty -l <1-5>` | تغییر سطح سختی |
| `menu show current` / `menu enter <menu>` | ناوبری |

---

## منوی اخبار (News)

| دستور | کار |
|-------|-----|
| `menu news show-unread` | اعلان‌های خوانده‌نشده (بعد از نمایش، همه خوانده‌شده می‌شوند) |
| `menu news show-all` | همه‌ی اعلان‌ها |

---

## منوی چپتر (Chapter)

| دستور | کار |
|-------|-----|
| `menu enter chapter -c <chapterName>` | ورود به یک فصل. مقادیر: `ANCIENT_EGYPT`, `FROSTBITE_CAVES`, `BIG_WAVE_BEACH`, `DARK_AGES` |
| `menu coin-wallet` | موجودی سکه |
| `menu gem-wallet` | موجودی الماس |
| `menu cheat add <n> <coin\|diamond>` | اضافه کردن سکه/الماس |
| `menu leaderboard` | جدول امتیازات |

---

## داخل بازی (Game)

مختصات ۱-مبنا: `x` = ستون (۱ تا ۹)، `y` = ردیف (۱ تا ۵).

### کاشت و مدیریت گیاه
| دستور | کار |
|-------|-----|
| `plant plant -t <type> -l (<x>, <y>)` | کاشت گیاه (نام: `Peashooter` یا `PEASHOOTER`) |
| `pluck plant -l (<x>, <y>)` | برداشتن گیاه |
| `feed plant -l (<x>, <y>)` | دادن غذای گیاه |

### چیت‌ها
| دستور | کار |
|-------|-----|
| `cheat remove-cooldown` | حذف همه‌ی cooldownها |
| `cheat add-plant-food` | +۱ غذای گیاه |
| `cheat spawn-zombie -t <zombie-type> -l <x, y>` | ساخت زامبی (مثل `ZOMBIE_GARGANTUAR` یا `Gargantuar`) |

### نمایش وضعیت
| دستور | کار |
|-------|-----|
| `show map` | نقشه‌ی کامل (موج، خورشید، غذا، ماشین چمن‌زنی، زامبی‌ها) |
| `show plants status` | وضعیت گیاهان (هزینه، آماده/در حال شارژ) |
| `show tile status -l (<x>, <y>)` | جزئیات گیاهان و زامبی‌های یک خانه |
| `zombies info` | اطلاعات تمام زامبی‌های روی زمین |

### مراحل ویژه
| دستور | کار |
|-------|-----|
| `start zombie waves` | شروع موج‌ها (فقط در مرحله‌ی «هر چه رسد بکار» / Plant What You Get) |

---

## گلخانه و فروشگاه (Greenhouse / Shop)

### گلخانه
| دستور | کار |
|-------|-----|
| `show greenhouse` | جدول گلخانه (قفل/خالی/در حال رشد/آماده) |
| `plant pot at (<x>, <y>)` | کاشت گیاه تصادفی در گلدان (۵۰٪ Marigold، ۵۰٪ گیاه خاص) |
| `collect (<x>, <y>)` | برداشت (Marigold = ۵۰۰ سکه؛ گیاه خاص = بوست ذخیره) |
| `grow (<x>, <y>)` | تسریع رشد با الماس (۱ الماس به ازای هر ساعت باقی‌مانده) |

### فروشگاه (از داخل گلخانه)
| دستور | کار |
|-------|-----|
| `enter shop` | ورود به فروشگاه |
| `shop list` | کالاهای دائمی |
| `shop daily` | پیشنهاد روزانه |
| `shop buy -i <item_id> -n <count> [-t <plant_type>]` | خرید. کالاها: ۱=گلدان، ۲=غذای گیاه، ۳=بسته بذر تصادفی، ۴=بسته بذر انتخابی (`-t` اجباری)، ۵=تبدیل ارز، ۶=پیشنهاد روزانه |

### ارتقای گیاه
| دستور | کار |
|-------|-----|
| `upgrade plant -t <plant>` | ارتقای گیاه (هزینه‌ی پلکانی: سطح ۲ = ۵ بسته بذر + ۵۰۰ سکه، ...) |
| `show plant levels` | نمایش سطح گیاهان ارتقایافته |

---

## Travel Log و مینی‌گیم‌ها

| دستور | کار |
|-------|-----|
| `travel log page <page>` | نمایش یک صفحه؛ page: `daily`, `main`, `epic`, `minigame` |
| `show quests` | کوئست‌های صفحه‌ی فعلی |
| `play <minigame> <1-3>` | شروع مینی‌گیم. فعلاً فقط `beghouled` کامل قابل‌بازی است |
| `menu show current` | نمایش منوی فعلی |
| `menu exit` | برگشت به منوی اصلی |

### داخل Beghouled
| دستور | کار |
|-------|-----|
| `show` | نمایش صفحه |
| `swap <x1> <y1> <x2> <y2>` | جابه‌جایی دو گیاه مجاور (فقط اگر ترکیب ۳تایی بسازد) |
| `upgrades` | لیست ارتقاها |
| `upgrade <plant>` | ارتقای یک نوع گیاه با خورشید |
| `exit` | خروج از بازی |

---

## مثال کامل: از ثبت‌نام تا بازی

```
register -u player -p Test!1234 Test!1234 -n Player -e player@test.com -g male
pick question -q 1 -a blue -c blue
menu enter login
login -u player -p Test!1234
menu enter main
menu enter travel_log_menu
play beghouled 1
```
