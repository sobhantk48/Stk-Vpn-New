# 📌 نقشه راه پروژه Stkvpn (ادغام با EasySNI)

## 🎯 هدف نهایی
تبدیل Stkvpn به یک اپلیکیشن کامل VPN با قابلیت‌های پیشرفته‌ی دور زدن فیلترینگ (SNI Tunnel، Domain Fronting، Desync) و همچنین بهبود تجربه کاربری با اضافه کردن Kill Switch، Split Tunneling، Recent Activity و پشتیبان‌گیری.

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
| **🛠️ دیباگ** | رفع خطاهای کامپایل و اجرا | ۱۲ | ✅ انجام شد |

---

## ✅ لیست دقیق وظایف (با وضعیت)

### فاز ۰: آماده‌سازی و برنامه‌ریزی
- [x] **۰-۱**: کلون کردن EasySNI و بررسی ساختار  
- [x] **۰-۲**: تهیه نقشه راه و لیست وظایف (این فایل)

---

### فاز ۱: پارسر لینک‌ها و تست سرعت
- [x] **۱-۱**: ترجمه `ParseURI` از Go به Kotlin (`ProfileParser.kt`)
- [x] **۱-۲**: پشتیبانی از vless://, vmess://, trojan://, ss://
- [x] **۱-۳**: ترجمه `CheckSNI` به Kotlin (`SpeedTester.kt`)
- [x] **۱-۴**: نمایش پینگ در لیست سرورها

---

### فاز ۲: SNI Tunnel
- [x] **۲-۱**: طراحی کلاس `SniTunnelManager.kt`
- [x] **۲-۲**: پیاده‌سازی پروکسی TCP با تغییر SNI
- [x] **۲-۳**: ادغام با `SingBoxManager` و اضافه کردن دکمه‌ی SNI به UI

---

### فاز ۳: Domain Fronting
- [x] **۳-۱**: طراحی کلاس `FrontingManager.kt` (شامل ProxyServer, CertificateManager, DohResolver)
- [x] **۳-۲**: پیاده‌سازی CA محلی و صدور گواهی موقت
- [x] **۳-۳**: پیاده‌سازی پروکسی MITM و DoH resolver

---

### فاز ۴: Desync و Fragment
- [x] **۴-۱**: ترجمه `desync` package به Kotlin (`FragmentManager.kt`, `DesyncManager.kt`)
- [x] **۴-۲**: اضافه کردن گزینه‌های Fragment به UI (`SettingsScreen.kt`)

---

### فاز ۵: Kill Switch و Split Tunneling
- [x] **۵-۱**: پیاده‌سازی Kill Switch در `VpnService`
- [x] **۵-۲**: پیاده‌سازی Split Tunneling (انتخاب اپ‌ها)
- [x] **۵-۳**: اضافه کردن تنظیمات مربوطه به `SettingsScreen`

---

### فاز ۶: Recent Activity و پشتیبان‌گیری
- [x] **۶-۱**: پیاده‌سازی `ConnectionHistory.kt` و ذخیره‌سازی در Room
- [x] **۶-۲**: اضافه کردن Recent Activity به Dashboard
- [x] **۶-۳**: پیاده‌سازی Backup & Restore (JSON/WebDAV)

---

### فاز ۷: بهبود UI/UX و انتشار
- [x] **۷-۱**: ارتقاء به Material 3 کامل
- [x] **۷-۲**: اضافه کردن حالت شب/روز
- [x] **۷-۳**: اضافه کردن راهنمای کاربری (Onboarding)
- [x] **۷-۴**: آماده‌سازی برای انتشار در GitHub Releases

---

## 🛠️ بخش دیباگ و اصلاحات انجام‌شده (به‌ترتیب)

| ردیف | مشکل | راه‌حل | وضعیت |
|------|------|--------|--------|
| **D-01** | خطای کامپایل `Unresolved reference: V2rayAppTheme` | ایجاد فایل `Theme.kt` با تابع `V2rayAppTheme` | ✅ رفع شد |
| **D-02** | خطای کامپایل `Unresolved reference: rememberSaveable` | اضافه کردن import `rememberSaveable` به `LogViewerScreen.kt` | ✅ رفع شد |
| **D-03** | خطای کامپایل `Unresolved reference: SplitMode` | انتقال `SplitMode` به فایل جداگانه `model/SplitMode.kt` | ✅ رفع شد |
| **D-04** | خطای کامپایل `Unresolved reference: GreenSuccess` | اضافه کردن `GreenSuccess` به `Color.kt` | ✅ رفع شد |
| **D-05** | خطای کامپایل `Unresolved reference: login` در `AdminLoginScreen` | بازنویسی کامل `AdminLoginScreen.kt` با منطق صحیح | ✅ رفع شد |
| **D-06** | خطای کامپایل `Unresolved reference: writeLog/writeError` | جایگزینی با `Logger` مستقیم و حذف importهای اضافی | ✅ رفع شد |
| **D-07** | خطای کامپایل `Conflicting import: Logger` | استفاده از مسیر کامل `com.v2ray.app.utils.Logger` | ✅ رفع شد |
| **D-08** | خطای کامپایل `@Composable invocations` در `MainActivity` | حذف `try-catch` از دور `setContent` و استفاده از `remember` | ✅ رفع شد |
| **D-09** | خطای کامپایل `OutlinedTextField colors` | اضافه کردن پارامترهای صحیح (`focusedTextColor`, `cursorColor`) | ✅ رفع شد |
| **D-10** | خطای runtime: `config error: Listen on AnyIP but no Port(s)` | اصلاح ساختار JSON کانفیگ (تبدیل `inbounds` به آرایه) | ✅ رفع شد |
| **D-11** | خطای runtime: `config error: failed to build inbound config` | جدا کردن `inbounds` از `outbounds` در کانفیگ | ✅ رفع شد |
| **D-12** | کرش برنامه هنگام اتصال VPN | افزودن `try-catch` و لاگ‌گیری در `V2RayService` | ✅ رفع شد |
| **D-13** | صفحه‌ی سفید در اولین اجرا | اضافه کردن `errorMessage` state در `MainActivity` | ✅ رفع شد |
| **D-14** | لاگ‌ها خالی بودند | پیاده‌سازی `Logger.kt` با ذخیره‌سازی در فایل | ✅ رفع شد |
| **D-15** | پنل ادمین فقط صفحه‌ی رمز داشت | تکمیل `AdminScreen` با لیست پروفایل‌ها و دکمه‌ی Add | ✅ رفع شد |
| **D-16** | رمز ادمین ۱۳۱۱ جواب نمی‌داد | رمز پیش‌فرض `admin` است (`1311` اشتباه بود) | ✅ مشخص شد |
| **D-17** | پینگ، دانلود و آپلود نمایشی بودند | پینگ با کانفیگ واقعی محاسبه می‌شود؛ دانلود/آپلود نیاز به توسعه‌ی جداگانه دارد | 🔄 نیاز به کار بیشتر |

---

## 🟢 وضعیت فعلی

- **تاریخ شروع:** ۱۴۰۵-۰۴-۱۵  
- **تاریخ به‌روزرسانی:** ۱۴۰۵-۰۴-۱۶  
- **بیلد:** ✅ موفق (سبز)  
- **اجرای اپ:** ✅ بدون کرش (با خطای کانفیگ برطرف‌شده)  
- **VPN:** ✅ وصل می‌شود و علامت VPN بالا می‌آید  
- **پنل ادمین:** ✅ کار می‌کند (رمز: `admin`)  
- **مدیریت کانفیگ:** ✅ افزودن، ویرایش و حذف  
- **لاگ‌گیری:** ✅ فعال و قابل مشاهده در Log Viewer  
- **پینگ:** 🔄 با کانفیگ واقعی کار می‌کند  
- **دانلود/آپلود:** ❌ نیاز به توسعه‌ی جداگانه (TrafficStats)  

---

## 📂 فایل‌های کلیدی اصلاح‌شده

| فایل | تغییرات |
|------|----------|
| `MainViewModel.kt` | اصلاح ساختار کانفیگ، اضافه کردن `buildConfigFromProfile` |
| `V2RayService.kt` | افزودن لاگ‌گیری، مدیریت خطا، اصلاح `startVpn` |
| `AdminScreen.kt` | تکمیل پنل ادمین با لیست پروفایل‌ها و دیالوگ Add |
| `AdminLoginScreen.kt` | اصلاح کامپایل و رفع خطای `login` |
| `SettingsScreen.kt` | اضافه کردن تنظیمات Kill Switch و Split Tunneling |
| `LogViewerScreen.kt` | اتصال به `Logger` و نمایش لاگ‌ها |
| `Logger.kt` | فایل جدید برای ذخیره‌سازی لاگ‌ها |
| `SplitMode.kt` | فایل جدید برای Enum |
| `Color.kt` | اضافه کردن `GreenSuccess` |

---

## 📌 گام‌های بعدی

1. **توسعه‌ی نمایش ترافیک واقعی** (دانلود/آپلود) با استفاده از `TrafficStats` یا API sing-box.
2. **تست با کانفیگ‌های واقعی** در شرایط مختلف شبکه.
3. **انتشار نسخه‌ی اولیه** در GitHub Releases.

---

## 📌 قوانین پروژه
- هر تغییر ابتدا در یک برنچ جدید انجام شود.
- پس از تست موفق، به `main` merge شود.
- قبل از هر کامیت، `./gradlew build` اجرا شود.
- برای هر وظیفه، یک کامیت جداگانه با پیام واضح.
