# 📌 نقشه راه پروژه Stkvpn (ادغام با EasySNI)

## 🎯 هدف نهایی
تبدیل Stkvpn به یک اپلیکیشن کامل VPN با قابلیت‌های پیشرفته‌ی دور زدن فیلترینگ (SNI Tunnel، Domain Fronting، Desync) و همچنین بهبود تجربه کاربری با اضافه کردن Kill Switch، Split Tunneling، Recent Activity و پشتیبان‌گیری.

---

## 📋 قوانین پروژه (قوانین طلایی، نقره‌ای، برنزی و جدید)

| ردیف | عنوان قانون | توضیح |
|------|-------------|--------|
| ۱ | **قانون طلایی** | بررسی کامل ساختارها و در حافظه نگه داشتن فایل‌هایی که در این چت به من داده می‌شود. |
| ۲ | **قانون نقره‌ای** | بعد از هر تغییر، دستور کامیت و پوش روی برنچ main داده شود. |
| ۳ | **قانون برنزی** | همیشه به ریپو عمومی شما (`sobhantk48/Stkvpn`) نگاه کامل شود تا ببینم چه فایل‌هایی دارد. |
| ۴ | **قانون طلایی دوم** | من داخل Termux کد می‌زنم و داخل GitHub Actions بیلد می‌کنم. |
| ۵ | **قانون طلایی سوم** | بررسی کامل ساختارها و در حافظه نگه داشتن فایل‌هایی که به من داده می‌شود در این چت. |
| ۶ | **قانون دیباگ** | بعد از هر فاز، دیباگ انجام شود و تمام خطاها و لاگ‌ها به‌همراه راه‌حل‌هایشان در بخش دیباگ `ROADMAP.md` ثبت شوند. همچنین قبل از شروع فاز جدید، بیلد سبز تأیید شود. |
| ۷ | **قانون انتشار** | پس از اتمام فاز ۷ و تأیید بیلد سبز، نسخه‌ی اولیه در GitHub Releases منتشر شود. |
| ۸ | **قانون تست** | هر قابلیت جدید با حداقل یک کانفیگ واقعی تست شود. |
| ۹ | **قانون مستندسازی** | تمام تغییرات در `ROADMAP.md` و کامیت‌ها با پیام واضح ثبت شوند. |
| ۱۰ | **قانون امنیت** | اطلاعات حساس (مانند کلیدهای API و رمزها) در GitHub Secrets ذخیره شوند و در کد硬‌کد نشوند. |
| ۱۱ | **قانون لاگ‌گیری** | تمام خطاهای runtime و استثناها در `Logger` ثبت شوند تا در Log Viewer قابل مشاهده باشند. |
| ۱۲ | **قانون بازخورد** | پس از هر تغییر، منتظر تأیید کاربر برای ادامه باشم. |

---

## 📅 فازبندی کلی

| فاز | عنوان | تعداد وظایف | وضعیت |
|-----|-------|-------------|--------|
| **فاز ۰** | آماده‌سازی و برنامه‌ریزی | ۲ | ✅ انجام شد |
| **فاز ۱** | اضافه کردن پارسر لینک‌ها و تست سرعت | ۴ | ✅ انجام شد |
| **فاز ۲** | پیاده‌سازی SNI Tunnel | ۳ | ✅ انجام شد |
| **فاز ۳** | پیاده‌سازی Domain Fronting | ۳ | ✅ انجام شد |
| **فاز ۴** | اضافه کردن Desync و Fragment | ۲ | ✅ انجام شد |
| **فاز ۵** | Kill Switch و Split Tunneling | ۳ | ✅ انجام شد |
| **فاز ۶** | Recent Activity و پشتیبان‌گیری | ۳ | ✅ انجام شد |
| **فاز ۷** | بهبود UI/UX و انتشار | ۴ | ✅ انجام شد |
| **🛠️ دیباگ** | رفع خطاهای کامپایل و اجرا | ۲۰+ | ✅ انجام شد |
| **⚡ بهینه‌سازی** | کاهش حجم و مصرف حافظه | ۸ | ❌ انجام نشده |

---

## 🛠️ بخش دیباگ کامل (خطاها، راه‌حل‌ها و ایرادات)

### 📌 خطاهای کامپایل (Compilation Errors)

| ردیف | خطا | توضیح | راه‌حل | وضعیت |
|------|-----|-------|--------|--------|
| **D-01** | `Unresolved reference: V2rayAppTheme` | فایل Theme وجود نداشت | ایجاد `Theme.kt` با تابع `V2rayAppTheme` | ✅ |
| **D-02** | `Unresolved reference: rememberSaveable` | عدم import در `LogViewerScreen` | اضافه کردن import `rememberSaveable` | ✅ |
| **D-03** | `Unresolved reference: SplitMode` | Enum در جای اشتباه تعریف شده بود | انتقال به `model/SplitMode.kt` | ✅ |
| **D-04** | `Unresolved reference: GreenSuccess` | رنگ در `Color.kt` نبود | اضافه کردن `GreenSuccess` | ✅ |
| **D-05** | `Unresolved reference: login` در `AdminLoginScreen` | تابع `login` تعریف نشده بود | بازنویسی کامل `AdminLoginScreen` | ✅ |
| **D-06** | `Unresolved reference: writeLog/writeError` | توابع لاگ وجود نداشتند | جایگزینی با `Logger` مستقیم | ✅ |
| **D-07** | `Conflicting import: Logger` | import تکراری و مبهم | استفاده از مسیر کامل `com.v2ray.app.utils.Logger` | ✅ |
| **D-08** | `@Composable invocations` در `MainActivity` | استفاده از `try-catch` دور کامپوزیبل | حذف `try-catch` و استفاده از `remember` | ✅ |
| **D-09** | `OutlinedTextField colors` | پارامترهای رنگی منسوخ شده بودند | اضافه کردن پارامترهای جدید (`focusedTextColor`, `cursorColor`) | ✅ |

---

### 📌 خطاهای Runtime و کانفیگ (Configuration & Runtime Errors)

| ردیف | خطا | توضیح | راه‌حل | وضعیت |
|------|-----|-------|--------|--------|
| **D-10** | `config error: Listen on AnyIP but no Port(s) set` | JSON کانفیگ ساختار آرایه نداشت و `port` مشخص نشده بود | تبدیل `inbounds` و `outbounds` به آرایه، اضافه کردن `port: 0` | ✅ |
| **D-11** | `config error: failed to build inbound config` | `inbounds` و `outbounds` با هم تداخل داشتند | جدا کردن `inbounds` از `outbounds` در کانفیگ | ✅ |
| **D-12** | کرش برنامه هنگام اتصال VPN | خطاهای مدیریت نشده در `V2RayService` | افزودن `try-catch` و لاگ‌گیری کامل | ✅ |
| **D-13** | صفحه‌ی سفید در اولین اجرا | خطا در `setContent` دیده نمی‌شد | اضافه کردن `errorMessage` state در `MainActivity` | ✅ |
| **D-14** | لاگ‌ها خالی بودند | `Logger` پیاده‌سازی نشده بود | ایجاد `Logger.kt` با ذخیره‌سازی در فایل | ✅ |
| **D-15** | پنل ادمین فقط صفحه‌ی رمز داشت | `AdminScreen` کامل نبود | تکمیل `AdminScreen` با لیست پروفایل‌ها و دکمه‌ی Add | ✅ |
| **D-16** | رمز ادمین ۱۳۱۱ جواب نمی‌داد | رمز پیش‌فرض اشتباه بود | رمز پیش‌فرض `admin` است (نه `1311`) | ✅ |
| **D-17** | `config error: unknown config id` | کاما اضافی در JSON کانفیگ | حذف کاماهای اضافی و اعتبارسنجی JSON | ✅ |
| **D-18** | `config error: Listen on specific ip without port` | کتابخانه `libbox` از فرمت `sing-box` پشتیبانی نمی‌کرد | تشخیص اینکه `libbox.aar` برای Xray-core است و تغییر فرمت کانفیگ به Xray | ✅ |
| **D-19** | پینگ، دانلود و آپلود نمایشی بودند | داده‌های ساختگی در UI | پینگ با کانفیگ واقعی محاسبه می‌شود؛ دانلود/آپلود نیاز به توسعه‌ی جداگانه دارد | 🔄 |
| **D-20** | ویرایش کانفیگ در پنل ادمین کار نمی‌کرد | دیالوگ ویرایش کامل نبود | فیلدهای جدید (network, path, host) به دیالوگ ویرایش اضافه شدند | ✅ |
| **D-21** | کرش بعد از `V2Ray started successfully` | `port` و `server_port` به‌صورت رشته ارسال شده بودند | اصلاح نوع `port` به عدد در `buildXrayConfigFromProfile` | ✅ |

---

## 🔍 تشخیص و رفع مشکل اصلی: `libbox.aar` متعلق به Xray-core است

### علائم:
- خطاهای مکرر `Listen on AnyIP but no Port(s) set` و `Listen on specific ip without port`
- عدم تطابق فرمت کانفیگ `sing-box` با کتابخانه

### راه‌حل نهایی:
- تغییر فرمت کانفیگ از `sing-box` به `Xray-core`
- استفاده از ساختار `inbounds` با `port` در سطح اصلی (نه `listen_port`)
- تغییر `outbounds` به فرمت `Xray` با `vnext` و `streamSettings`

### کد اصلاح‌شده:
```kotlin
private fun buildXrayConfigFromProfile(profile: Profile): String {
    val inbound = buildJsonObject {
        put("port", 1080)
        put("protocol", "socks")
        put("settings", buildJsonObject {
            put("auth", "noauth")
            put("udp", true)
        })
    }
    // ... outbound با فرمت Xray
}
