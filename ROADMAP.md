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
| **فاز ۵** | Kill Switch و Split Tunneling | ۳ | ❌ انجام نشده |
| **فاز ۶** | Recent Activity و پشتیبان‌گیری | ۳ | ❌ انجام نشده |
| **فاز ۷** | بهبود UI/UX و انتشار | ۴ | ❌ انجام نشده |

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
- [ ] **۵-۱**: پیاده‌سازی Kill Switch در `VpnService`
- [ ] **۵-۲**: پیاده‌سازی Split Tunneling (انتخاب اپ‌ها)
- [ ] **۵-۳**: اضافه کردن تنظیمات مربوطه به `SettingsScreen`

---

### فاز ۶: Recent Activity و پشتیبان‌گیری
- [ ] **۶-۱**: پیاده‌سازی `ConnectionHistory.kt` و ذخیره‌سازی در Room
- [ ] **۶-۲**: اضافه کردن Recent Activity به Dashboard
- [ ] **۶-۳**: پیاده‌سازی Backup & Restore (JSON/WebDAV)

---

### فاز ۷: بهبود UI/UX و انتشار
- [ ] **۷-۱**: ارتقاء به Material 3 کامل
- [ ] **۷-۲**: اضافه کردن حالت شب/روز
- [ ] **۷-۳**: اضافه کردن راهنمای کاربری (Onboarding)
- [ ] **۷-۴**: آماده‌سازی برای انتشار در GitHub Releases

---

## 📂 فایل‌های مرتبط با هر وظیفه

| وظیفه | فایل‌های جدید | فایل‌های موجود برای ویرایش |
|-------|---------------|----------------------------|
| ۱-۱ تا ۱-۲ | `ProfileParser.kt` | – |
| ۱-۳ تا ۱-۴ | `SpeedTester.kt` | `DashboardScreen.kt`, `MainViewModel.kt` |
| ۲-۱ تا ۲-۳ | `SniTunnelManager.kt` | `SingBoxManager.kt`, `DashboardScreen.kt` |
| ۳-۱ تا ۳-۳ | `CertificateManager.kt`, `DohResolver.kt`, `ProxyServer.kt` | `DashboardScreen.kt`, `MainViewModel.kt`, `build.gradle` |
| ۴-۱ تا ۴-۲ | `FragmentManager.kt`, `DesyncManager.kt` | `SettingsScreen.kt` |
| ۵-۱ تا ۵-۳ | – | `VpnService.kt`, `SettingsScreen.kt` |
| ۶-۱ تا ۶-۳ | `ConnectionHistory.kt` | `DashboardScreen.kt` |
| ۷-۱ تا ۷-۴ | – | همه فایل‌های UI |

---

## 📌 قوانین پروژه
- هر تغییر ابتدا در یک برنچ جدید انجام شود.
- پس از تست موفق، به `main` merge شود.
- قبل از هر کامیت، `./gradlew build` اجرا شود.
- برای هر وظیفه، یک کامیت جداگانه با پیام واضح.

---

## 🟢 وضعیت فعلی
- **تاریخ شروع:** ۱۴۰۵-۰۴-۱۵
- **فاز فعال:** فاز ۵ (Kill Switch و Split Tunneling)
- **وظیفه فعال:** ۵-۱ (پیاده‌سازی Kill Switch)
