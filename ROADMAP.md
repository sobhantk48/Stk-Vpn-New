# نقشه‌راه پروژه V2Ray-STK

## فاز ۱: راه‌اندازی اولیه و زیرساخت ✅ کامل
- [x] ایجاد ساختار پایه پروژه
- [x] پیاده‌سازی Room Database
- [x] پیاده‌سازی Dagger-Hilt برای تزریق وابستگی‌ها
- [x] پیاده‌سازی Sing-Box Core

---

## فاز ۲: رابط کاربری و مدیریت پروفایل ✅ کامل
- [x] صفحه‌ی داشبورد (Dashboard)
- [x] صفحه‌ی تنظیمات (Settings)
- [x] صفحه‌ی مدیریت پروفایل‌ها (Admin)
- [x] صفحه‌ی لیست لوکیشن‌ها (Locations)
- [x] صفحه‌ی About
- [x] صفحه‌ی Log Viewer

---

## فاز ۳: Smart Paste و ConfigBuilder پیشرفته ✅ کامل
- [x] اضافه شدن `ConfigBuilder.kt` برای ساخت کانفیگ V2Ray/Xray
- [x] اضافه شدن `SmartParser.kt` برای تشخیص و پارس انواع لینک‌ها
- [x] پشتیبانی از لینک‌های VLESS, VMess, Trojan, Shadowsocks
- [x] پشتیبانی از Clash YAML
- [x] پشتیبانی از v2rayN JSON
- [x] اضافه شدن قابلیت Smart Paste در Admin Screen

---

## فاز ۴: پروتکل‌های جدید (Hysteria2, WireGuard, TUIC, AWG) ✅ کامل

### ۴-۱: Hysteria2 ✅ کامل
- [x] اضافه شدن فیلدهای `hysteriaProtocolVersion`, `hysteriaAuthPayload`, `hysteriaObfs`, `hysteriaSni`, `hysteriaAllowInsecure`, `hysteriaUploadMbps`, `hysteriaDownloadMbps` به `Profile.kt`
- [x] اضافه شدن تابع `parseHysteria2` برای پارس لینک‌های `hysteria2://` و `hy2://`
- [x] به‌روزرسانی `SmartParser.kt` برای تشخیص لینک‌های Hysteria2
- [x] اضافه شدن `buildHysteria2Json` برای ساخت کانفیگ Sing-box
- **خطاها:** هیچ خطایی ثبت نشد

### ۴-۲: WireGuard ✅ کامل
- [x] اضافه شدن `wgLocalAddress`, `wgPrivateKey`, `wgPeerPublicKey`, `wgPreSharedKey`, `wgMtu`, `wgReserved` به `Profile.kt`
- [x] اضافه شدن `parseWireGuard` برای پارس لینک‌های `wireguard://`
- [x] به‌روزرسانی `SmartParser.kt` برای تشخیص لینک‌های WireGuard
- [x] اضافه شدن `buildWireGuardJson` برای ساخت کانفیگ Sing-box
- [x] تست موفق با لینک نمونه
- **خطاها:** هیچ خطایی ثبت نشد

### ۴-۳: TUIC v5 ✅ کامل
- [x] اضافه شدن `tuicToken`, `tuicUuid`, `tuicCongestionController`, `tuicUdpRelayMode`, `tuicReduceRTT`, `tuicDisableSNI` به `Profile.kt`
- [x] اضافه شدن `parseTuic` برای پارس لینک‌های `tuic://`
- [x] به‌روزرسانی `SmartParser.kt` برای تشخیص لینک‌های TUIC
- [x] اضافه شدن `buildTuicJson` برای ساخت کانفیگ Sing-box
- **خطاها:** هیچ خطایی ثبت نشد

### ۴-۴: AWG (AmneziaWG) ✅ کامل
- [x] اضافه شدن پارامترهای AWG (`awgJc`, `awgJmin`, `awgJmax`, `awgS1`..`awgS4`, `awgH1`..`awgH4`, `awgI1`..`awgI5`) به `Profile.kt`
- [x] اضافه شدن `parseAwg` برای پارس لینک‌های `awg://`
- [x] اضافه شدن `parseAmneziaLink` برای پارس لینک‌های `vpn://` (Amnezia VPN)
- [x] اضافه شدن `parseAwgIni` برای پارس فایل‌های INI AWG
- [x] به‌روزرسانی `SmartParser.kt` برای تشخیص لینک‌های AWG
- [x] اضافه شدن `buildAwgJson` برای ساخت کانفیگ Sing-box با پارامترهای AWG
- **خطاها:** هیچ خطایی ثبت نشد

### جمع‌بندی فاز ۴:
- **وضعیت کلی:** ✅ کامل
- **پروتکل‌های پشتیبانی‌شده:** VLESS, VMess, Trojan, Shadowsocks, Hysteria2, WireGuard, TUIC, AWG
- **بیلد:** سبز ✅
- **تست‌های دستی:** WireGuard و AWG تست شدند و کار می‌کنند.

---

## فاز ۵: قابلیت‌های پیشرفته و بهینه‌سازی

### ۵-۱: Domain Fronting ✅ ناقص
- [x] اضافه شدن فیلد `frontingDomain` به `Profile.kt`
- [x] اضافه شدن منطق Domain Fronting در `SingBoxManager.kt`
- [x] اضافه شدن دکمه‌ی Front در `DashboardScreen.kt`
- [x] اضافه شدن متدهای `startFronting` و `stopFronting` در `MainViewModel.kt`
- [ ] تست عملی Domain Fronting
- **خطاها:** نیاز به تست عملی دارد

### ۵-۲: Split Tunneling ⏳ در حال انجام
- [ ] تکمیل منطق Split Tunneling در `V2RayService.kt`
- [ ] تکمیل رابط کاربری در `SettingsScreen.kt`
- [ ] تست عملی

### ۵-۳: Kill Switch ⏳ در حال انجام
- [ ] تکمیل منطق Kill Switch در `V2RayService.kt`
- [ ] تکمیل رابط کاربری در `SettingsScreen.kt`
- [ ] تست عملی

### ۵-۴: Warp (Cloudflare) ⏳ برنامه‌ریزی‌شده
- [ ] اضافه کردن پشتیبانی از پروتکل Warp
- [ ] اضافه کردن پارس لینک‌های Warp
- [ ] تست عملی

### ۵-۵: بهبود Log Viewer ⏳ برنامه‌ریزی‌شده
- [ ] نمایش لاگ‌های Sing-box در Log Viewer
- [ ] فیلتر کردن لاگ‌ها بر اساس سطح
- [ ] ذخیره‌سازی و مدیریت فایل‌های لاگ

---

## مشکلات فعلی (نیاز به بررسی)

| مشکل | وضعیت | اولویت |
|------|--------|--------|
| اتصال VPN برقرار نمی‌شود (آیکون VPN ظاهر نمی‌شود) | ❌ | بالا |
| ذخیره‌سازی تنظیمات کار نمی‌کند | ❌ | بالا |
| لاگ‌ها در Log Viewer نمایش داده نمی‌شوند | ❌ | متوسط |
| پروفایل‌ها ذخیره و نمایش داده می‌شوند | ✅ | - |

---

## دیباگ

### فاز ۴-۱: Hysteria2
- **وضعیت:** ✅ کامل
- **خطاها:** هیچ خطایی ثبت نشد

### فاز ۴-۲: WireGuard
- **وضعیت:** ✅ کامل
- **خطاها:** هیچ خطایی ثبت نشد

### فاز ۴-۳: TUIC v5
- **وضعیت:** ✅ کامل
- **خطاها:** هیچ خطایی ثبت نشد

### فاز ۴-۴: AWG (AmneziaWG)
- **وضعیت:** ✅ کامل
- **خطاها:** هیچ خطایی ثبت نشد

### فاز ۵: Domain Fronting
- **وضعیت:** ⏳ ناقص (نیاز به تست)
- **خطاها:** نیاز به تست عملی دارد

---

## یادداشت‌ها
- بیلد در تاریخ ۲۰۲۶-۰۷-۰۷ سبز شد ✅
- پروتکل‌های جدید (Hysteria2, WireGuard, TUIC, AWG) به‌طور کامل پیاده‌سازی شدند
- مشکلات اتصال VPN، ذخیره‌سازی تنظیمات و نمایش لاگ‌ها باید در اولویت بعدی رفع شوند
- فاز ۵ باید پس از رفع مشکلات فعلی شروع شود

