# راهنمای Git تیم

> این فایل نسخه‌ی مکتوب همان قوانینی است که تیم روی آن توافق کرده. هدف این است که همه (نه فقط کسی که
> قوانین را نوشته) بتواند هر وقت لازم شد به آن مراجعه کند.

## Remote

از ابتدای پروژه دو remote داشتیم (`origin` روی هم‌گیت و `github` به عنوان نسخه پشتیبان). **هم‌گیت دیگر
استفاده نمی‌شود** و گیت‌هاب مخزن اصلی است. الان فقط یک remote داریم:

```
origin   # https://github.com/advanced-progamming-sut-2026/phase-0-group-52.git
```

برای دیدن remoteها:

```
git remote -v
```

## Branchهای اصلی

### `main`

- فقط برای نسخه‌های پایدار و تحویل فازهاست.
- مستقیم روی `main` کد نمی‌زنیم.
- فقط وقتی یک فاز کامل و تست شد، `develop` را با `main` ادغام می‌کنیم.
- روی نسخه نهایی هر فاز tag می‌زنیم.

### `develop`

- نسخه در حال توسعه پروژه است.
- featureها و bug fixها بعد از review وارد `develop` می‌شوند.
- قبل از شروع هر تسک، branch جدید را از `develop` می‌سازیم.
- مستقیم روی `develop` هم push نکنید، مگر در موارد خیلی ساده و با هماهنگی تیم.

## Branchهای موقت

برای هر تسک یک branch جدا بسازید. نام‌گذاری:

```
feature/<task-name>   # قابلیت جدید
fix/<task-name>        # رفع باگ
test/<task-name>       # اضافه کردن تست
docs/<task-name>       # مستندات و UML
chore/<task-name>      # تنظیمات پروژه
refactor/<task-name>   # تمیز کردن ساختار کد
```

مثال:

```
feature/user-registration
feature/login-menu
fix/email-validation
test/password-validator
docs/phase-0-uml
chore/configure-gradle
```

## شروع یک تسک جدید

```
git checkout develop
git pull origin develop
git checkout -b feature/user-registration
```

## Commit کردن تغییرات

تغییرات را در چند commit کوچک و معنادار ثبت کنید:

```
git add .
git commit -m "feat: add username validation"
git commit -m "feat: add password strength validation"
git commit -m "test: add registration validation tests"
```

از commitهای نامفهوم پرهیز کنید: `update`, `fix`, `final`, `done`, `changes`.

## Push کردن branch

```
git push -u origin feature/user-registration
```

بعد در گیت‌هاب یک Pull Request از branch خودتان به `develop` بسازید و از یکی از اعضای تیم بخواهید
آن را review کند.

## بعد از Merge

```
git checkout develop
git pull origin develop
git branch -d feature/user-registration
```

در صورت نیاز branch ریموت را هم حذف کنید:

```
git push origin --delete feature/user-registration
```

## تحویل هر فاز

وقتی یک فاز کامل و تست شد:

```
git checkout main
git pull origin main
git merge develop
git push origin main
```

سپس tag فاز را بسازید:

```
git tag phase-0
git push origin phase-0
```

برای فازهای بعدی: `git tag phase-1`, `git tag phase-2`, `git tag phase-3`, ...

## خلاصه قوانین تیم

- مستقیم روی `main` کد نزنید.
- هر تسک یک branch جدا داشته باشد.
- branchهای جدید را از `develop` بسازید.
- قبل از شروع کار، آخرین نسخه `develop` را pull کنید.
- commitها کوچک و معنادار باشند.
- قبل از merge شدن، حداقل یک نفر کد را review کند.
- `origin` تنها remote و مخزن اصلی (گیت‌هاب) است.
- در پایان هر فاز، `develop` به `main` merge شود و tag فاز ساخته شود.
