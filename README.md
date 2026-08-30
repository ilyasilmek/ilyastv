# 📺 İlyasTV - Modern Android IPTV & Medya Oynatıcı

![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![ExoPlayer](https://img.shields.io/badge/Player-Media3%20ExoPlayer-E53935?style=for-the-badge)
![Room Database](https://img.shields.io/badge/Database-Room%20(KSP)-47A248?style=for-the-badge)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

**İlyasTV**, Canlı TV kanalları, VOD sinema filmleri ve diziler için geliştirilmiş, yüksek performanslı, zengin özellikli ve modern bir Android IPTV oynatıcı uygulamasıdır. Material Design 3 ilkeleriyle tamamen **Jetpack Compose** kullanılarak tasarlanmıştır.

---

## 🌟 Yeni Eklenen Premium Özellikler

### 1. 🔴 Canlı Yayın Kaydı (PVR - Personal Video Recorder)
* **Canlı Kayıt:** Canlı TV yayınlarını izlerken tek tıkla arka planda yerel depolamaya kaydetme (`.ts` / `.mp4`).
* **Canlı REC Paneli:** Oynatıcı ekranında anlık geçen süre (00:00:00) ve kaydedilen toplam dosya boyutu (MB/GB) göstergesi.
* **Kayıt Yönetimi & Çevrimdışı İzleme:** Tamamlanan PVR kayıtları otomatik olarak veritabanına işlenir ve *İndirilenler & Çevrimdışı* sekmesinden internetsiz oynatılabilir.

### 2. ⏪ Catch-Up TV & Timeshift (Geçmiş Yayınları İzleme)
* **Yayın Arşivi Desteği:** Xtream Codes ve HLS arşiv parametrelerini çözümleyerek geçmiş günlerin TV programlarını listeleme.
* **Geçmiş Program Rehberi:** Oynatıcı üzerinden veya kanal menüsünden geçmiş programların saat ve başlıklarını görüntüleyip tek dokunuşla başlatabilme.

### 3. 🌐 Çevrimiçi Altyazı Arama & İndirme (OpenSubtitles & SubDL)
* **Dahili Altyazı Arama:** Film veya dizi izlerken internet üzerindeki altyazı veritabanlarında (OpenSubtitles, SubDL) anlık arama yapabilme.
* **Çoklu Dil Desteği:** Türkçe, İngilizce, Almanca, Fransızca, İspanyolca ve onlarca dil seçeneği.
* **Anında Senkronize Oynatma:** İndirilen `.srt` altyazı dosyası doğrudan ExoPlayer'a harici altyazı katmanı olarak eklenir.

### 4. 🎬 TMDb Metadata & Film/Dizi Detayları
* **Zengin İçerik Bilgileri:** Film ve diziler için yüksek çözünürlüklü afişler, arka plan görselleri, TMDb / IMDb puanları, yapım yılı, türler ve Türkçe konu özetleri.
* **Detay Kartı:** Oynatıcı içindeki **Bilgi (ℹ️)** butonundan veya içerik ızgarasından detaylı yapım bilgilerine anında erişim.

### 5. 🍿 Binge-Watch Modu (Dizi Maratonu Desteği)
* **Jeneriği Atla (+85sn):** Dizi ve film başlangıçlarında beliren akıllı buton ile introyu tek tıkla atlama.
* **Sonraki Bölüm Uyarısı & Otomatik Geçiş:** Bölümün son 30 saniyesinde çıkan *"Sonraki Bölüm [Şimdi Geç]"* kartı ve bölüm bittiğinde sıradaki bölüme otomatik geçiş.

---

## ✨ Tüm Temel Özellikler

- **📥 VOD İndirme Yöneticisi & Çevrimdışı İzleme:**
  - Film ve dizi içeriklerini arka planda cihaza indirme (`Android DownloadManager`).
  - İnternet bağlantısı olmadan (çevrimdışı) depolamadan direkt oynatma.
  - Canlı indirme yüzdesi, hız, kalan süre ve cihaz depolama analizi grafiği.
  - Kategori filtreleri (Tümü, Filmler, Diziler, PVR Kayıtları, İnenler, Tamamlananlar).
- **💡 Ekran Uyanık Kalma Desteği (Keep-Screen-On):**
  - Yayın veya film izlerken ekranın zaman aşımına uğrayıp kararmasını önler.
- **🛠️ Gelişmiş Son İzlenenler & Geçmiş Yönetimi:**
  - *"En Başa Taşı"*, *"Listeden Sil"*, *"İlerlemeyi Sıfırla (00:00)"*, *"Cihaza İndir"* ve *"Favorilere Ekle"*.
- **📺 Canlı TV & EPG TV Rehberi:**
  - Kategori bazlı kanal listeleme, canlı yayın durumu ve program akış bilgisi.
- **🖼️ PiP (Picture-in-Picture) Modu:**
  - Başka bir uygulamaya geçtiğinizde yayının arka planda küçük pencerede kesintisiz devam etmesi.
- **🌐 Çoklu Playlist & Xtream Codes API Desteği:**
  - M3U / M3U8 URL'si veya Xtream Codes (Sunucu URL, Kullanıcı Adı, Şifre) ile hızlı giriş.
  - Birden fazla oynatma listesini hafızada tutma ve aralarında geçiş yapma.
  - Hesap bitiş tarihi geri sayımı ve üyelik durumu.
- **⏯️ Kaldığın Yerden Devam Et (Continue Watching):**
  - Yarım bırakılan filmlerin/bölümlerin süresini kaydeder, tek tıkla kaldığı dakikadan devam ettirir.
- **⚡ Gelişmiş Oynatıcı Kontrolleri (Media3 ExoPlayer):**
  - **Jest Kontrolleri:** Sol tarafta dikey kaydırma ile Parlaklık, sağ tarafta Ses kontrolü.
  - **Hızlı Sarma:** Ekrana çift dokunarak ±10 saniye sarma.
  - **Çoklu Ses & Altyazı:** Birden fazla ses dili ve altyazı izi seçimi.
  - **Oynatma Hızı:** 0.5x ile 2.0x arasında esnek hız kontrolü.
  - **Uyku Zamanlayıcısı:** 15dk - 120dk arası otomatik kapanma.
  - **En-Boy Oranı:** Fit, Fill, Zoom, Sabit Boyut geçişleri.
- **🎮 Android TV & D-Pad Kumanda Desteği:**
  - Android TV ve TV kutuları için tam D-Pad kumanda tuşları ve odaklama efektleri.
- **⭐ Favori Yönetimi:**
  - Kanalları ve içerikleri favorilere ekleme (Yerel Room veritabanı).
- **🔍 Akıllı Arama:**
  - Tüm kütüphanede başlık veya kategoriye göre anlık filtreleme.

---

## 📱 Ekranlar ve Menü Yapısı

| Ekran | Açıklama |
|---|---|
| **📺 Canlı TV (Live TV)** | Kategorili TV kanalları, EPG rehberi ve hızlı kanal değiştirme |
| **🎬 Filmler (Movies)** | Poster ızgarası, TMDb detayları ve VOD oynatma/indirme |
| **🍿 Diziler (Series)** | Sezon ve bölüm hiyerarşisi, Binge-Watch ve otomatik sonraki bölüm |
| **📥 İndirilenler (Downloads)** | İndirilen VOD filmler, dizi bölümleri ve PVR canlı kayıtları |
| **🔍 Arama (Search)** | Canlı arama ve kategori filtreleme |
| **⚙️ Hesap & Ayarlar (Account)** | Playlist yönetimi, Xtream girişi, arabellek ve tema ayarları |

---

## 🚀 APK İndirme ve Kurulum

GitHub Actions entegrasyonu sayesinde her kod güncellemesinde otomatik olarak yeni `.apk` dosyası üretilmektedir:

### 1. APK'yı İndirme:
1. GitHub reponuzda üst menüden **[Actions](../../actions)** sekmesine tıklayın.
2. Sol taraftan **"Build İlyasTV APK"** iş akışını seçin.
3. En son tamamlanan (yeşil onay işaretli **✓**) derlemeye tıklayın.
4. Sayfanın en altındaki **"Artifacts"** bölümünde yer alan **`IlyasTV-Debug-APK`** bağlantısına tıklayarak ZIP dosyasını indirin.

### 2. Telefona Yükleme:
1. İndirdiğiniz ZIP dosyasını açın ve içerisindeki `app-debug.apk` dosyasını Android cihazınıza aktarın.
2. APK dosyasına tıklayıp kurulumu başlatın. *(Gerekirse "Bilinmeyen kaynaklardan yüklemeye izin ver" ayarını açın)*.
3. Kurulum tamamlandıktan sonra **İlyasTV** uygulamasını açın!

---

## 🏗️ Teknik Mimari & Teknolojiler

- **Mimari:** MVVM (Model-View-ViewModel) + Temiz Mimari (Clean Architecture)
- **UI & Tasarım:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material Design 3
- **Medya Oynatıcı:** [AndroidX Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- **Veritabanı / Yerel Depolama:** [Room Database](https://developer.android.com/training/data-storage/room) & KSP
- **Ağ & API:** [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) & Kotlinx Serialization
- **Görsel Yükleme:** [Coil Compose](https://coil-kt.github.io/coil/compose/)
- **Asenkron Yapı:** Kotlin Coroutines & Flow
- **CI / CD:** GitHub Actions (Java 21, Gradle 9)

---

## 💻 Yerel Geliştirme (Local Build)

Projeyi yerel bilgisayarınızda derlemek için:

```bash
# Projeyi klonlayın
git clone https://github.com/kullaniciadi/ilyastv.git
cd ilyastv

# Debug APK derleyin
./gradlew assembleDebug

# Release AAB (Android App Bundle) derleyin
./gradlew bundleRelease
```

Derlenen APK çıktısı `app/build/outputs/apk/debug/app-debug.apk` konumunda oluşturulur.

---

## 📄 Lisans & Yasal Uyarı

- **İlyasTV**, kullanıcının kendi sağladığı yasal M3U / IPTV bağlantılarını ve yayın akışlarını oynatmak üzere geliştirilmiş açık kaynaklı bir medya arayüzüdür.
- Uygulama bünyesinde telif hakkına tabi hiçbir canlı yayın, film veya dizi barındırılmaz ya da sunulmaz.
