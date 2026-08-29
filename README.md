# 📺 İlyasTV - Modern Android IPTV & Medya Oynatıcı

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![ExoPlayer](https://img.shields.io/badge/Player-Media3%20ExoPlayer-E53935?style=for-the-badge)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

**İlyasTV**, Canlı TV kanalları, VOD filmler ve diziler için tasarlanmış yüksek performanslı, modern ve kullanıcı dostu bir Android IPTV oynatıcı uygulamasıdır. Material Design 3 ilkeleriyle tamamen **Jetpack Compose** kullanılarak geliştirilmiştir.

---

## ✨ Öne Çıkan Özellikler

- **📥 VOD İndirme Yöneticisi & Çevrimdışı İzleme (Offline Download):**
  - Film ve Dizi içeriklerini arka planda cihazınıza indirebilme (`Android DownloadManager`).
  - İnternet bağlantınız olmadığında (uçakta, seyahatte, kotasızken) indirilen içerikleri çevrimdışı oynatma.
  - Canlı indirme yüzdesi, indirilen/toplam MB göstergesi ve cihaz depolama alanı grafiği.
  - İndirmeleri filtreleme (Tümü, Filmler, Diziler, İnenler, Tamamlananlar) ve tek tıkla silme/iptal etme.
- **💡 Ekran Uyanık Kalma Desteği (Keep-Screen-On):**
  - Canlı TV kanalı veya film/dizi izlenirken cihaz ekranının uyku moduna geçip kararması engellenir; kesintisiz seyir keyfi sunulur.
- **🛠️ Gelişmiş Son İzlenenler & Geçmiş Yönetimi:**
  - Son izlenen her kanal veya VOD için seçenekler menüsü: **"En Başa Taşı"**, **"Listeden Sil"**, **"İlerlemeyi Sıfırla (00:00)"**, **"Cihaza İndir"** ve **"Favorilere Ekle"**.
- **📺 Canlı TV (Live TV):** EPG TV rehberi, kategori filtreleme ve kesintisiz canlı yayın deneyimi.
- **🎬 Film & VOD Kütüphanesi:** Afişler, açıklamalar, süre göstergeleri ve kategori filtreleri.
- **🍿 Dizi (Series) Desteği:** Sezon ve bölümlere göre organize edilmiş geniş dizi arşivi.
- **🖼️ PiP (Picture-in-Picture) Modu:** Başka bir uygulamaya geçtiğinizde veya ana ekrana döndüğünüzde yayının arka planda küçük pencerede kesintisiz devam etmesi.
- **🌐 Çoklu Playlist & Xtream Codes API Desteği:** 
  - İster doğrudan M3U linki / dosyası ekleyin, ister Xtream Sunucu, Kullanıcı Adı ve Şifre ile giriş yapın.
  - Birden fazla oynatma listesi ekleyip aralarında anında geçiş yapabilme.
  - Hesap bitiş tarihi geri sayımı ve kullanıcı bilgisi gösterimi.
- **⏯️ Geçmiş & "Kaldığın Yerden Devam Et" (Continue Watching):** 
  - İzlemeyi yarıda bıraktığınız film ve dizilerin süresini otomatik kaydetme ve tek dokunuşla kaldığı dakikadan devam ettirme.
  - Son izlenen canlı TV kanalları çubuğu.
- **⚡ Gelişmiş Oynatıcı Kontrolleri (Media3 ExoPlayer):** 
  - **Jest Kontrolleri:** Sol tarafta dikey kaydırma ile Parlaklık, sağ tarafta Ses kontrolü.
  - **Hızlı İleri/Geri:** Ekrana çift dokunarak ±10 saniye sarma.
  - **Çoklu Ses & Altyazı:** Birden fazla ses dili ve altyazı desteği.
  - **Oynatma Hızı:** 0.5x ile 2.0x arasında hız seçimi.
  - **Uyku Zamanlayıcısı:** 15dk ile 120dk arası otomatik kapanma zamanlayıcısı.
  - **En-Boy Oranı:** Fit, Fill, Zoom, Sabit Boyut geçişleri.
  - **Cihaza İndir Butonu:** Oynatıcı içinden tek dokunuşla indirme başlatma.
- **🎮 Android TV & D-Pad Kumanda Desteği:** Android TV ve TV kutuları için tam kumanda tuşları (Yukarı/Aşağı kanal değiştirme, Sağ/Sol sarma, Ortadaki tuşla oynatma/duraklatma) ve görsel odak efektleri.
- **⭐ Favori Yönetimi:** Hızlı erişim için beğendiğiniz kanalları veya yayınları tek tıkla favorilere ekleme (Yerel Room veritabanında saklanır).
- **🔍 Akıllı Arama & Filtreleme:** Yüzlerce kanal ve içerik arasından anlık arama yapabilme.
- **🌓 Modern Arayüz (Material 3):** Göz yormayan şık karanlık/aydınlık tema, EPG Rehber, Afiş Izgarası ve Kompakt Liste görünüm modları.

---

## 📱 Ekranlar ve Uygulama Yapısı

1. **Canlı TV (Live TV):** Tüm TV kanallarının kategori bazlı listesi, yayın durumu ve canlı önizleme.
2. **Filmler (Movies):** Popüler ve kategoriye ayrılmış sinema filmleri.
3. **Diziler (Series):** Dizi bölümlerini takip edebileceğiniz içerik ekranı.
4. **İndirilenler (Downloads):** İndirilen ve indirilmekte olan VOD içeriklerini çevrimdışı izleme ve yönetme ekranı.
5. **Arama (Search):** Tüm kütüphanede başlık veya kategori bazlı filtreleme.
6. **Hesap / Oynatma Listesi (Account):** Aktif playlist yönetimi, yeni URL/Xtream ekleme, tema ve arabellek ayarları.

---

## 🚀 APK İndirme ve Kurulum

GitHub Actions entegrasyonu sayesinde her güncellemede otomatik olarak yeni `.apk` dosyası üretilmektedir:

### 1. APK'yı İndirme:
1. GitHub reponuzda üst menüden **[Actions](../../actions)** sekmesine tıklayın.
2. Sol taraftan **"Build İlyasTV APK"** iş akışını seçin.
3. En son tamamlanan (yeşil onay işaretli **✓**) derlemeye tıklayın.
4. Sayfanın en altındaki **"Artifacts"** bölümünde yer alan **`IlyasTV-Debug-APK`** bağlantısına tıklayarak ZIP dosyasını indirin.

### 2. Telefona Yükleme:
1. İndirdiğiniz ZIP dosyasını açın ve içerisindeki `app-debug.apk` dosyasını telefonunuza aktarın.
2. APK dosyasına tıklayıp kurulumu başlatın. *(Gerekirse tarayıcınız/dosya yöneticiniz için "Bilinmeyen kaynaklardan yüklemeye izin ver" ayarını açın)*.
3. Kurulum tamamlandıktan sonra **İlyasTV** uygulamasını açıp izlemeye başlayabilirsiniz!

---

## 🛠️ Nasıl Kullanılır?

1. **Uygulamayı Açın:** İlk açılışta yerel demo listesi veya boş liste karşılar.
2. **Playlist Ekleme:** 
   - **Hesap** veya **Ayarlar** sekmesine gidin.
   - IPTV sağlayıcınızdan aldığınız **M3U / M3U8 Linkini** yapıştırın ve **"Kaydet / İçe Aktar"** butonuna basın.
3. **Kanal Seçimi ve İzleme:** 
   - Üst kısımdaki kategorilerden (Örn: *Ulusal, Spor, Sinema, Belgesel, Haber*) dilediğiniz grubu seçin.
   - Kanala tıklayarak oynatıcıyı başlatın.
4. **Favorilere Ekleme:** Kanal kartının üzerindeki **Kalp (❤️)** simgesine tıklayarak kanalı favorilerinize ekleyin.

---

## 🏗️ Teknik Mimari & Teknolojiler

- **Mimari:** MVVM (Model-View-ViewModel) + Temiz Mimari (Clean Architecture)
- **UI & Tasarım:** [Jetpack Compose](https://developer.android.com/jetpack/compose), Material Design 3
- **Medya Oynatıcı:** [AndroidX Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- **Veritabanı / Kalıcılık:** [Room Database](https://developer.android.com/training/data-storage/room) & KSP
- **Görsel Yükleme:** [Coil Compose](https://coil-kt.github.io/coil/compose/)
- **Asenkron Yapı:** Kotlin Coroutines & Flow
- **CI / CD:** GitHub Actions (Java 21, Gradle 9)

---

## 💻 Yerel Geliştirme (Local Build)

Projeyi yerel bilgisayarınızda derlemek isterseniz:

```bash
# Projeyi klonlayın
git clone https://github.com/kullaniciadi/ilyastv.git
cd ilyastv

# Debug APK derleyin
./gradlew assembleDebug
```

Derlenen APK `app/build/outputs/apk/debug/app-debug.apk` dizininde oluşacaktır.

---

## 📄 Lisans & Yasal Uyarı

- **İlyasTV**, kullanıcının kendi sağladığı yasal M3U / IPTV bağlantılarını oynatmak üzere tasarlanmış açık kaynaklı bir medya arayüzüdür.
- Uygulama bünyesinde telif hakkına tabi hiçbir canlı yayın, film veya dizi barındırılmaz ya da sunulmaz.
