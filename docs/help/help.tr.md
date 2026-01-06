# Traditional T9
Bu kılavuz, Traditional T9'un farklı senaryolarda nasıl yapılandırılacağını ve kullanılacağını açıklar. Kurulum talimatları ve "lite" ve "full" sürümleri hakkında bilgi için lütfen GitHub'daki [Kurulum Kılavuzu'na](https://github.com/sspanak/tt9/blob/master/docs/installation.md) bakın. Son olarak, tüm kaynak kodları, bir geliştirici kılavuzunu, gizlilik politikasını ve ek belgeleri içeren [ana depo sayfasına](https://github.com/sspanak/tt9) göz atabilirsiniz.

## İlk Kurulum
Kurulumu yaptıktan sonra, ilk olarak Traditional T9'u bir Android klavyesi olarak etkinleştirmeniz gerekir. Bunu yapmak için başlatıcı simgesine tıklayın. Herhangi bir işlem yapmanız gerekiyorsa, İlk Kurulum dışındaki tüm seçenekler devre dışı bırakılacak ve bir etiket görünecektir: "TT9 devre dışı". İlk Kuruluma gidin ve etkinleştirin.

_Yüklemeden hemen sonra simgeyi görmüyorsanız, telefonunuzu yeniden başlatın, simge görünmelidir. Bu, Android'in yeni yüklenen uygulamalar listesini yenilemeyerek biraz pil tasarrufu sağlamaya çalışmasından kaynaklanmaktadır._

### Sadece Dokunmatik Ekranlı Telefonlarda Kullanım
Dokunmatik ekranlı cihazlarda, sistem yazım denetleyicisinin kapatılması da şiddetle tavsiye edilir. Etkin olduğunda, yazdığınız kelimelerin bir kısmını veya tamamını kırmızıyla altı çizili olarak gösterir.

Bir diğer sorun ise yazım denetleyicisinin kafa karıştırıcı bir “Kelime Ekle” açılır penceresi gösterebilmesidir. Bu pencere, yeni kelimeleri Traditional T9 sözlüğüne değil, varsayılan sistem klavyesine (genellikle Gboard) ekler.

Bu tür durumları önlemek için sistem yazım denetleyicisi devre dışı bırakılmalıdır.

Bu adımı gerçekleştirmeniz gerekiyorsa, İlk Kurulum ekranındaki “Sistem Yazım Denetleyicisi” öğesi etkin olacaktır. Sistem bileşenini kapatmak için üzerine dokunun. Öğe devre dışıysa, herhangi bir işlem yapmanıza gerek yoktur.

İlk kurulumu tamamladıktan sonra, daha fazla ipucu ve püf noktası için [Ekran Üzeri Tuş Takımı](#ekran-üzeri-tuş-takımı) bölümüne göz atın.

### Tahmin Modunu Etkinleştirme
Tahmin Modu, kelime önerileri sağlamak için bir dil sözlüğünün yüklenmesini gerektirir. Etkin dilleri değiştirebilir ve Ayarlar Ekranı → [Diller](#dil-seçenekleri) bölümünden sözlüklerini yükleyebilirsiniz. Sözlüklerden bazılarını yüklemeyi unutursanız, Traditional T9 yazmaya başladığınızda otomatik olarak yükleyecektir. Daha fazla bilgi için [aşağıya bakın](#dil-seçenekleri).

#### Düşük Donanımlı Telefonlar için Notlar
Sözlük yükleme, düşük donanımlı telefonları zorlayabilir. TT9 "lite" sürümü kullanılırken, bu durum Android'in işlemi sonlandırmasına neden olabilir. Yükleme 30 saniyeden fazla sürerse, şarj cihazını takın veya yükleme sırasında ekranın açık kalmasını sağlayın.

Yukarıdaki sorunu önlemek için "full" sürümünü kullanabilirsiniz.

#### Android 13 veya Üstü için Notlar
Varsayılan olarak, yeni yüklenen uygulamaların bildirimleri devre dışıdır. Bu bildirimleri etkinleştirmeniz önerilir. Bu sayede sözlük güncellemeleri olduğunda bilgilendirilirsiniz ve yüklemeyi seçtiğinizde TT9 yükleme ilerlemesini gösterir. Yeni güncellemeler en fazla ayda bir kez yayınlanır, bu yüzden aşırı bildirim almaktan endişelenmenize gerek yok.

Bildirimleri etkinleştirmek için Ayarlar → Diller'e gidin ve Sözlük Bildirimlerini açın.

_Bildirimleri kapalı tutmaya karar verirseniz, TT9 çalışmaya devam edecektir, ancak sözlükleri manuel olarak yönetmeniz gerekecektir._

## Ayarlar
Ayarlar ekranında, yazma dillerini seçebilir, tuș takımı kısayol tușlarını yapılandırabilir, uygulama görünümünü değiştirebilir veya telefonunuzla uyumluluğu geliştirebilirsiniz.

### Ayarlara Nasıl Erişilir?

#### Yöntem 1
Traditional T9 kısayol simgesine tıklayın.

#### Yöntem 2 (dokunmatik ekran kullanarak)
- TT9'u uyandırmak için bir metin veya sayı alanına dokunun.
- Ekrandaki ayar simgesine dokunun.

#### Yöntem 3 (fiziksel klavye kullanarak)
- Bir metin veya sayı alanında yazmaya başlayarak TT9'u uyandırın.
- Ekran üzerindeki araçlar tușuna veya atanmış kısayola basarak komut listesini açın [Varsayılan: ✱ basılı tutun].
- 2-tușuna basın.

### Ayarlarda Gezinme
Fiziksel tuş takımına sahip bir cihazınız varsa, Ayarlar’da gezinmenin iki yolu vardır.

1. Kaydırma için Yukarı/Așağı tușlarını kullanın ve bir seçenek açmak veya etkinleştirmek için Tamam tușuna basın.
2. İlgili seçeneği seçmek için 1-9 tușlarına basın ve açmak/etkinleştirmek için iki kez basın. Nerede olursanız olun çift basış işe yarayacaktır. Örneğin, ekranın en üstündeyken bile 3-tușuna iki kez basarsanız üçüncü seçenek etkinleșir. Son olarak, 0-tușu sona kaydırma için bir kısayol işlevi görür ancak son seçeneği açmaz.

### Dil Seçenekleri

#### Bir Sözlük Yüklemek
Bir veya daha fazla dili etkinleștirdikten sonra, Tahmin Modu için ilgili sözlükleri yüklemelisiniz. Bir kez yüklendiğinde, sözlük silme seçeneklerinden birini kullanmadığınız sürece orada kalacaktır. Bu, dilleri yeniden etkinleştirip devre dıșı bırakırken sözlükleri her seferinde yeniden yüklemenize gerek olmadığı anlamına gelir. Sadece bir kez yapın, yalnızca ilk defa.

Bu aynı zamanda, X dilini kullanmaya başlamanız gerektiğinde, diğer tüm dilleri güvenle devre dıșı bırakabileceğiniz anlamına gelir; yalnızca X dilini yükleyin (ve zamandan tasarruf edin!), ardından daha önce kullandığınız tüm dilleri yeniden etkinleştirin.

Bir sözlüğü yeniden yüklemek öneri popülerliğini fabrika ayarlarına sıfırlayacaktır. Ancak, endișe etmenizi gerektiren bir durum yoktur. Çoğunlukla seçtiğiniz kelime tam olarak önerilecektir.

#### Otomatik Sözlük Yükleme

Ayarlar ekranından bir sözlük yüklemeyi atlarsanız veya unutursanız, yazı yazabileceğiniz bir uygulamaya gidip Tahmin Moduna geçtiğinizde otomatik olarak yüklenecektir. Yükleme tamamlanana kadar beklemeniz istenecek ve ardından yazmaya başlayabilirsiniz.

Bir veya daha fazla sözlüğü sildiğinizde, bunlar otomatik olarak yeniden yüklenmez. Bunu manuel olarak yapmanız gerekecektir. Sadece yeni etkinleştirilen dillerin sözlükleri otomatik olarak yüklenecektir.

#### Bir Sözlüğü Silme
X veya Y dillerini kullanmayı bıraktıysanız, bunları devre dışı bırakabilir ve depolama alanını boşaltmak için "Seçilmeyenleri Sil" seçeneğini kullanabilirsiniz.

Her şeyi seçiminize bakılmaksızın silmek için "Hepsini Sil" seçeneğini kullanın.

Her durumda, eklediğiniz özel kelimeler korunacak ve ilgili sözlüğü yeniden yüklediğinizde geri yüklenecektir.

#### Eklenen Kelimeler
"Dışa Aktar" seçeneği, tüm diller için eklenen tüm kelimelerle bir CSV dosyası oluşturmanıza olanak tanır. Ardından bu CSV dosyasını Traditional T9'u iyileştirmek için kullanabilirsiniz! GitHub'a gidip kelimeleri [yeni bir konu](https://github.com/sspanak/tt9/issues) veya [pull request](https://github.com/sspanak/tt9/pulls) açarak paylaşabilirsiniz. Gözden geçirildikten ve onaylandıktan sonra bir sonraki sürüme ekleneceklerdir.

"Import" ile daha önce dışa aktardığınız bir CSV'yi içe aktarabilirsiniz. Ancak bazı kısıtlamalar vardır:
- Yalnızca harflerden oluşan kelimeleri içe aktarabilirsiniz. Kesme işaretleri, kısa çizgiler, diğer noktalama işaretleri veya özel karakterler kabul edilmez.
- Emojiler kabul edilmez.
- Bir CSV dosyası en fazla 250 kelime içerebilir.
- En fazla 1000 kelime içe aktarabilirsiniz; yani en fazla 4 dosya X 250 kelime içe aktarabilirsiniz. Bu sınırdan sonra, yazarken yine de kelime ekleyebilirsiniz.

"Delete" seçeneğini kullanarak yanlış yazılmış veya sözlükte bulunmasını istemediğiniz kelimeleri arayıp silebilirsiniz.

## Fiziksel kısayol tuşları

Tüm kısayol tuşları, Ayarlar → Tuş Takımı → Kısayol Tuşlarını Seç bölümünden yeniden yapılandırılabilir veya devre dışı bırakılabilir.

### Yazı Tuşları

#### Önceki Öneri Tuşu (Varsayılan: D-pad Sol):
Önceki kelime/harf önerisini seçin.

#### Sonraki Öneri Tuşu (Varsayılan: D-pad Sağ):
Sonraki kelime/harf önerisini seçin.

#### Öneri Filtreleme Tuşu (Varsayılan: D-pad Yukarı):
_Tahmin modu yalnızca._

- **Tek basış:** Öneri listesini filtreleyin, yalnızca geçerli kelimeyle başlayanları bırakın. Bu tam bir kelime olup olmaması fark etmez. Örneğin, "remin" yazın ve Filtreye basın. "remin" ile başlayan tüm kelimeler kalacaktır: "remin", "remind", "reminds", "reminded", "reminding" vb.
- **Çift basış:** Filtreyi tam öneriye genişletin. Örneğin, "remin" yazın ve Filtreye iki kez basın. Önce "remin"e göre filtreleyecek, ardından filtreyi "remind"a genişletecek. En uzun sözlük kelimesine ulaşana kadar filtreyi genişletmeye devam edebilirsiniz.

Filtreleme, bilinmeyen kelimeleri yazmak için de faydalıdır. Örneğin, sözlükte olmayan "Anakin" kelimesini yazmak istediğinizi varsayalım. "A" ile başlayın, ardından "B" ve "C"yi gizlemek için Filtre'ye basın. Şimdi 6-tușuna basın. Filtre açık olduğundan, gerçek sözlük kelimelerinin yanı sıra 1+6 için tüm olası kombinasyonları sunacaktır: "A..." + "m", "n", "o". "n"i seçin ve seçimizi onaylamak için Filtreye basın, "An" üretilir. Şimdi 2-tușuna basarak "An..." + "a", "b", ve "c" elde edebilirsiniz. "a" seçin ve "Anakin" elde edene kadar devam edin.

Filtreleme etkinleştirildiğinde, temel metin kalın ve italik hale gelir.

#### Filtreyi Temizleme Tușu (Varsayılan: D-pad Așağı):
_Tahmin modu yalnızca._

Uygulanan öneri filtresini temizleyin.

#### D-pad Merkezi (OK veya ENTER):
- Öneriler görüntüleniyorsa, şu anki seçili öneriyi yazın.
- Aksi halde, mevcut uygulama için varsayılan işlemi gerçekleştirin (örneğin, bir mesaj gönderin, bir URL'ye gidin veya yeni satıra geçin).

_**Not:** Her uygulama, OK tuşuna basıldığında ne yapılacağını kendisi belirler ve TT9'un üzerinde kontrolü yoktur._

_**Not 2:** Mesajlaşma uygulamalarında OK ile mesaj göndermek için, uygulamanın "ENTER ile Gönder" veya benzer isimli ayarını etkinleştirmeniz gerekir. Uygulamada böyle bir ayar yoksa, muhtemelen bu şekilde mesaj göndermeyi desteklemiyordur. Bu durumda, [Play Store](https://play.google.com/store/apps/details?id=io.github.sds100.keymapper) veya [F-droid](https://f-droid.org/packages/io.github.sds100.keymapper/) üzerinden KeyMapper uygulamasını kullanın. KeyMapper, sohbet uygulamalarını algılayabilir ve bir fiziksel tuşa basıldığında veya basılı tutulduğunda mesaj gönderme butonuna dokunmayı simüle edebilir. Daha fazla bilgi için [hızlı başlangıç kılavuzuna](https://docs.keymapper.club/quick-start/) göz atın._

#### 0-tușu:
- **123 modunda:**
  - **Bas:** "0" yaz.
  - **Basılı tut:** özel/matematik karakterleri yaz.
- **ABC modunda:**
  - **Bas:** boşluk, yeni satır veya özel/matematik karakterleri yaz.
  - **Basılı tut:** "0" yaz.
- **Tahmin modunda:**
  - **Bas:** boşluk, yeni satır veya özel/matematik karakterleri yaz.
  - **Çift bas:** Tahmin modunda ayarlanan karakteri yaz. (Varsayılan: ".")
  - **Basılı tut:** "0" yaz.
- **Cheonjiin modunda (Korece):**
  - **Bas:** "ㅇ" ve "ㅁ" yaz.
  - **Basılı tut:** boşluk, yeni satır, "0" veya özel/matematiksel karakterler yaz.

#### 1-tușu:
- **123 modunda:**
  - **Bas:** "1" yaz.
  - **Basılı tut:** noktalama işaretleri yaz.
- **ABC modunda:**
  - **Bas:** noktalama işaretleri yaz.
  - **Basılı tut:** "1" yaz.
- **Tahmin modunda:**
  - **Bas:** noktalama işaretleri yaz.
  - **Çoklu basış:** emoji yaz.
  - **Basılı tut:** "1" yaz.
- **Cheonjiin modunda (Korece):**
  - **Bas:** "ㅣ" ünlüsünü yaz.
  - **Basılı tut:** noktalama işaretleri yaz.
  - **Basılı tut, ardından bas:** emoji yaz.

#### 2-9 tușu:
- **123 modunda:** ilgili sayıyı yaz.
- **ABC ve Tahmin modunda:** bir harf yazın veya basılı tutarak ilgili sayıyı yazın.

### Fonksiyon Tușları

#### Kelime Ekle Tușu:
Mevcut dil için sözlüğe yeni bir kelime ekleyin.

#### Silme Tușu (Geri, Sil veya Backspace):
Sadece metni siler.

Telefonunuzda özel bir "Sil" veya "Temizle" tuşu varsa, Ayarlarda bir şey ayarlamanıza gerek yoktur, başka bir Backspace'e sahip olmak istemiyorsanız otomatik olarak boş seçenek: "--" önceden seçilecektir.

"Kombine Geri/Sil" tuşuna sahip telefonlarda bu tuş otomatik olarak seçilecektir. Ancak, "Geri" yalnızca geri gitmek için kullanılması adına başka bir tușu seçebilirsiniz.

_**Not:** "Geri"yi backspace olarak kullanmak tüm uygulamalarda çalışmaz, özellikle Firefox, Spotify ve Termux gibi. Bu uygulamalar tuşun işlevini tamamen yeniden tanımlayabilirler. Maalesef "Geri" Android'de özel bir rol oynar ve sistemi tarafından kullanımı sınırlıdır._

_**Not 2:** "Geri" tuşunu basılı tutmak her zaman varsayılan sistem işlemini tetikleyecektir (örneğin, çalışan uygulamalar listesini gösterir)._

_Bu durumlarda, başka bir tuş atayabilir (tüm diğer tuşlar tam anlamıyla kullanılabilir) veya ekran üzerindeki silme tuşunu kullanabilirsiniz._

#### Sonraki Giriş Modu Tușu (Varsayılan: # bas):
Giriş modlarını değiştirir (abc → Tahmin → 123).

_Tahmin modu parola alanlarında kullanılamaz._

_Sadece sayı alanlarında, mod değiştirme mümkün değildir. Böyle durumlarda, tuş varsayılan işlevine (örneğin "#" yaz) geri döner._

#### Panoya Araçları tuşu:
Pano Araçları paneli açılır ve bu panel metin seçme, kesme, kopyalama ve yapıştırma işlemlerini yapmanıza olanak tanır. Paneli kapatmak için yeniden "✱" tuşuna veya çoğu uygulamada Geri tuşuna basabilirsiniz. Ayrıntılar için [aşağıda](#pano-araçları) bakın.

#### Sonraki Dil Tușu (Varsayılan: # basılı tut):
Birden fazla dil Ayarlardan etkinleştirildiğinde yazma dilini değiştirmek.

#### Klavye Seç Tușu:
Android Klavye Değiştirme iletişim kutusunu açarak yüklü tüm klavyeler arasında seçim yapabilirsiniz.

#### Shift Tușu (Varsayılan: ✱ bas):
- **Metin yazarken:** Büyük ve küçük harfler arasında geçiş yapın.
- **Özel karakterler yazarken:** 0-tușu ile sonraki karakter grubunu gösterin.

#### Ayarlar Tușu:
Ayarlar yapılandırma ekranını açar. Bu, yazma dillerini seçebileceğiniz, tuş takımı kısayol tușlarını yapılandırabileceğiniz, uygulama görünümünü değiştirebileceğiniz veya telefonunuzla uyumluluğu geliştirebileceğiniz yerdir.

#### Geri Alma Tuşu:
Son işlemi geri alır. Bilgisayarda Ctrl+Z veya Mac’te Cmd+Z tuşlarına basmakla aynıdır.

_Geri alma geçmişi uygulamalar tarafından yönetilir, Traditional T9 tarafından değil. Bu nedenle, her uygulamada geri alma mümkün olmayabilir._

#### Yinele Tuşu:
Geri alınan son işlemi tekrarlar. Bilgisayarda Ctrl+Y veya Ctrl+Shift+Z, Mac’te Cmd+Y tuşlarına basmakla aynıdır.

_Geri alma gibi, yineleme komutu da her uygulamada mevcut olmayabilir._

#### Sesli Giriş Tușu:
Desteklenen telefonlarda sesli giriși etkinleştirir. Daha fazla bilgi için [așağıya bakın](#sesli-giriş).

#### Komut Listesi Tușu / Komut Paleti / (Varsayılan: ✱ basılı tut):
Tüm komutları (veya işlevleri) içeren bir liste gösterir.

Birçok telefonda kısayol tuşları için yalnızca iki veya üç "boș" tuş bulunur. Ancak, Traditional T9 daha fazla işlev sunar ve bu, tüm işlevler için tuș takımında yeterli yer olmadığı anlamına gelir. Komut Paleti bu sorunu çözer. Ek işlevleri (veya komutları) tuş kombinasyonlarıyla çağırmanızı sağlar.

İşte olası komutların listesi:
- **Ayarlar Ekranını Göster (Varsayılan Kombinasyon: ✱ basılı tut, 1-tușu).** [Ayarlar Tușu](#ayarlar-tușu)'na basmakla aynıdır.
- **Kelime Ekle (Varsayılan Kombinasyon: ✱ basılı tut, 2-tușu).** [Kelime Ekle Tușu](#kelime-ekle-tușu)'na basmakla aynıdır.
- **Sesli Giriş (Varsayılan Kombinasyon: ✱ basılı tut, 3-tușu).** [Sesli Giriş Tușu](#sesli-giriş-tușu)'na basmakla aynıdır.
- **Geri al (Varsayılan Kombinasyon: ✱ basılı tut, 4-tușu).** [Geri Alma Tuşu](#geri-alma-tuşu)'na basmakla aynıdır.
- **Pano araçları (Varsayılan Kombinasyon: ✱ basılı tut, 5-tușu).** [Panoya Araçları tuşu](#panoya-araçları-tuşu)'na basmakla aynıdır.
- **Yinele (Varsayılan Kombinasyon: ✱ basılı tut, 6-tușu).** [Yinele Tuşu](#yinele-tuşu)'na basmakla aynıdır.
- **Farklı bir Klavye Seç (Varsayılan Kombinasyon: ✱ basılı tut, 8-tușu).** [Klavye Seç Tușu](#klavye-seç-tușu)'na basmakla aynıdır.

_Ekran Düzeni "Sanal Tuș Takımı" olarak ayarlandığında bu tuș hiçbir şey yapmaz çünkü tüm işlevler için tüm tușlar zaten ekranda mevcuttur._

## Ekran Üzeri Tuş Takımı
Yalnızca dokunmatik ekrana sahip cihazlarda, tam işlevli bir ekran üzeri tuş takımı mevcuttur ve otomatik olarak etkinleştirilir. Cihaz dokunmatik ekranlı olarak algılanmazsa, Ayarlar → Görünüm → Ekran Üzeri Düzen bölümünden “Sanal Sayısal Tuş Takımı” seçilerek manuel olarak etkinleştirin.

Hem dokunmatik ekrana hem de fiziksel tuş takımına sahip cihazlarda, ekran üzeri tuşlar ekran alanını boşaltmak için devre dışı bırakılabilir. Bu seçenek Ayarlar → Görünüm altında bulunur.

Ayrıca, “Geri” tuşunun “Backspace” olarak eşlenmesine neden olan özel davranışın devre dışı bırakılması önerilir; bu davranış yalnızca fiziksel tuş takımı kullanıldığında faydalıdır. Bu işlem genellikle otomatik olarak yapılır. Aksi takdirde Ayarlar → Tuş Takımı → Kısayol Tuşlarını Seç → Backspace Tuşu yolunu izleyin ve “--” seçeneğini seçin.

### Retro ve Modern Ekran Üzeri Düzenler
İki sanal tuş takımı düzeni mevcuttur: Retro ve Modern.

Retro düzen, üst kısımda ortasında OK tuşu bulunan bir yön tuşu (D-pad) ve altında sayısal tuşlar içerir. Bu düzen, 2000’li yılların başındaki telefon tuş takımlarına oldukça benzer. Geleneksel bir deneyim arayan kullanıcılar, küçük ekranlı cihazlar ve daha büyük başparmaklara sahip kişiler için uygundur. Ayrıca Old Keyboard veya Big Old Keyboard gibi artık kullanılmayan eski T9 klavye uygulamalarına aşina olanlara da hitap edebilir.

Modern düzen, 12 tuşlu bir yazım düzeni kullanırken Android’in standart görünümünü ve hissini korur. Metin girişi için ortada 0–9 rakam tuşlarından oluşan bir blok bulunur; Shift, Backspace, dil değiştirme ve OK (Enter) gibi işlev tuşları ise sol ve sağ sütunlara yerleştirilmiştir.

### Sanal Tuşlara Genel Bakış
Ekran üzeri tuş takımı, fiziksel telefon tuş takımıyla aynı şekilde çalışır. Tek işlevli tuşlar ortada bir etiket veya simge gösterir. Uzun basma ile ek işlevi olan tuşlar, sağ üst köşede ikincil bir etiket veya simge gösterir.

#### 0–9 Tuşları
Sayı tuşları kelime yazmak ve rakam girmek için kullanılır. Retro düzende bazı tuşlarda sola ve sağa kaydırma hareketleri de kullanılabilir. Bu işlevler mevcut olduğunda, tuşun sol alt veya sağ alt köşesindeki simgelerle belirtilir.

Google Play sürümünde, kaydırma hareketleri hem Retro hem de Modern düzen için özelleştirilebilir veya devre dışı bırakılabilir. Bu ayarlar Ayarlar → Tuş Takımı → Tuş İşlevleri bölümünden yapılabilir.

#### Özel Metin Tuşları ("!" ve "?")
Varsayılan olarak bu tuşlar kendi noktalama işaretlerini ekler. Sayısal veya telefon giriş alanlarında, yıldız, diyez veya ondalık ayırıcı gibi alternatif karakterler ekleyebilirler.

Google Play sürümünde bu tuşlar özelleştirilebilir. Varsayılan karakter değiştirilebilir ve yukarı, aşağı, sola ve sağa kaydırma eylemleri atanabilir. Bu ayarlar Ayarlar → Tuş Takımı → Tuş İşlevleri bölümünden yapılandırılabilir.

#### Giriş Modu Tuşu
- **Bas:** Giriş modları arasında geçiş yapar (abc → Öngörülü → 123).
- **Basılı tut:** Ayarlardan birden fazla dil etkinleştirildiğinde yazım dilini değiştirir.
- **Yatay kaydırma:** TT9 dışındaki en son kullanılan klavyeye geçer.
- **Dikey kaydırma:** Yüklü tüm klavyeler arasında seçim yapabileceğiniz Android Klavye Değiştir iletişim kutusunu açar.

Ayarlar → Diller bölümünden birden fazla dil etkinleştirildiğinde, tuş küçük bir küre simgesi gösterir. Bu simge, tuşa basılı tutarak dil değiştirilebileceğini belirtir.

_Retro düzende, sağ alt köşedeki tuştur._

_Modern düzende, sol alt köşedeki tuştur._

#### Backspace
Basılıp bırakıldığında karakterleri siler. Ayarlar → Tuş Takımı → Hızlı Silme etkinleştirildiğinde, geri kaydırarak önceki kelime silinebilir.

#### Filtre Tuşu
- **Bas:** Öneri listesini filtreler. Kelime filtrelemenin nasıl çalıştığı için [yukarıya](#öneri-filtreleme-tuşu-varsayılan-d-pad-yukarı) bakın.
- **Basılı tut:** Etkinse filtreyi temizler.

_Tuş yalnızca Modern düzende mevcuttur. Konum: üstten ikinci tuş._

_Filtreleme yalnızca Öngörülü modda mümkündür._

#### Pano Araçları / Sesli Giriş Tuşu
- **Bas:** Kopyalama, yapıştırma ve metin düzenleme seçeneklerini açar.
- **Basılı tut:** Sesli girişi etkinleştirir.

_Tuş yalnızca Modern düzende mevcuttur. Konum: üstten üçüncü tuş._

#### OK Tuşu
- **Bas:** Diğer klavyelerde ENTER tuşuna basmakla aynıdır.

Retro düzende ayrıca Ayarlar → Görünüm → Tuşlar bölümünden kaydırma hareketleri etkinleştirilebilir.

- **Öneri yokken yukarı kaydırma:** İmleci yukarı taşır (D-PAD yukarı ile aynı).
- **Öneri yokken aşağı kaydırma:** İmleci aşağı taşır (D-PAD aşağı ile aynı).
- **Öneri varken yukarı kaydırma:** Öneri listesini filtreler. [Yukarıya](#öneri-filtreleme-tuşu-varsayılan-d-pad-yukarı) bakın.
- **Öneri varken aşağı kaydırma:** Öneri filtresini temizler.

### Yazarken Klavye Panelini Yeniden Boyutlandırma
Bazı durumlarda, Sanal Tuș Takımının çok fazla ekran alanı kapladığını, yazdıklarınızı veya bazı uygulama öğelerini göremediğinizi fark edebilirsiniz. Bu durumda, Ayarlar/Komut Paleti tușunu basılı tutarak veya Durum Çubuğunu (mevcut dil veya yazma modunun gösterildiği alan) sürükleyerek yeniden boyutlandırabilirsiniz. Yükseklik çok küçük hale geldiğinde, düzen otomatik olarak "İşlev tușları" veya yalnızca "Öneri listesi" olarak değișecektir. Yukarı doğru yeniden boyutlandırdığınızda ise düzen "Sanal Tuș Takımı"na geçer. Durum çubuğuna iki kez dokunarak hızlıca minimize veya maximize edebilirsiniz.

_Çift dokunarak yeniden boyutlandırma varsayılan olarak kapalıdır. Bu özelliği şu bölümden etkinleştirebilirsiniz: Ayarlar → Görünüm._

_Traditional T9'u yeniden boyutlandırmak, aynı zamanda mevcut uygulamanın yeniden boyutlandırılmasına neden olur. Her ikisini birden yapmak hesaplama açısından oldukça maliyetlidir. Birçok telefonda, hatta üst düzey olanlarda bile titreme veya takılma olabilir._

### Tuș Yüksekliğini Değiștirme
Ekran üzerindeki tuș yüksekliğini de değiștirmek mümkündür. Bunu yapmak için Ayarlar → Görünüm → Ekran Üzeri Tuș Yüksekliği bölümüne gidin ve istediğiniz gibi ayarlayın.

Varsayılan ayar olan %100, kullanılabilir tuș boyutu ve kapladığı ekran alanı arasında iyi bir denge sağlar. Ancak, büyük parmaklarınız varsa ayarı biraz artırmak isteyebilir veya TT9'u daha büyük ekranlarda (örneğin bir tablette) kullanıyorsanız ayarı azaltmak isteyebilirsiniz.

_Eğer mevcut ekran alanı sınırlıysa, TT9 bu ayarı göz ardı edecek ve yüksekliğini otomatik olarak azaltarak mevcut uygulama için yeterli alan bırakacaktır._

## Pano araçları
Pano Araçları panelinde, bilgisayar klavyesinde yapıldığı gibi metin seçebilir, kesebilir, kopyalayabilir ve yapıştırabilirsiniz. Panelden çıkmak için "✱" tuşuna veya Geri tuşuna basabilirsiniz (web tarayıcıları, Spotify ve bazı diğer uygulamalar hariç). Alternatif olarak, ekran klavyesinde herhangi bir harf tuşuna basabilirsiniz.

Așağıda olası metin komutlarının bir listesi bulunmaktadır:
1. Önceki karakteri seç (bilgisayar klavyesinde Shift+Sol gibi)
2. Seçimi iptal et
3. Sonraki karakteri seç (Shift+Sağ gibi)
4. Önceki kelimeyi seç (Ctrl+Shift+Sol gibi)
5. Tümünü seç
6. Sonraki kelimeyi seç (Ctrl+Shift+Sağ gibi)
7. Kes
8. Kopyala
9. Yapıștır

Daha kolay düzenleme için geri sil, boşluk ve Tamam tușları da aktiftir.

## Sesli Giriş
Sesli giriş özelliği, Gboard’a benzer şekilde konuşmayı metne dönüştürmenizi sağlar. Diğer tüm klavyeler gibi Traditional T9 da kendi başına ses tanıma yapmaz; bunun yerine telefonunuza bu görevi verir.

_Sesli Giriş düğmesi, bu özelliği desteklemeyen cihazlarda gizlenir._

### Google'lı Cihazlar
Google Servisleri olan cihazlarda, TT9 kelimelerinizi metne dönüştürmek için Google altyapısını kullanır. Android 12 veya daha önceki sürümlerde bu yöntemin çalışması için Wi-Fi bağlantınızın olması veya mobil verinin açık olması gerekir. Android 13 veya üzeri sürümlerde TT9, cihazın dil paketlerini kullanarak hem çevrimiçi hem de çevrimdışı ses tanıma gerçekleştirebilir. Çevrimdışı kullanım için, şu adımlardan gerekli dilleri indirdiğinizden emin olun: Android Ayarları → Sistem → Cihaz üzerindeki tanıma → Dil Ekle.

_Google Voice, diğer sesli asistanlar veya klavyeler için yüklenen dil paketleri Traditional T9 ile uyumlu çalışmayabilir. "Cihaz üzerindeki tanıma" ekranından global paketleri yüklemeniz tavsiye edilir._

### Google Olmayan Cihazlar
Google olmayan cihazlarda, eğer bir sesli asistan uygulaması varsa veya yerel klavye sesli girişi destekliyorsa, ses tanıma için mevcut olan bu seçenekler kullanılır. Ancak bu yöntem, Google kadar güçlü değildir. Gürültülü ortamlarda çalışmaz ve genellikle yalnızca “takvimi aç” veya “müzik çal” gibi basit ifadeleri tanır.

### Diğer Cihazlar
Google olmayan diğer telefonlar genellikle sesli girişi desteklemez. Çinli telefonlar, Çin güvenlik politikaları nedeniyle ses tanıma özelliğine sahip değildir. Bu cihazlarda, "com.google.android.googlequicksearchbox" paket adına sahip Google uygulamasını yükleyerek sesli girişi etkinleştirmek mümkün olabilir. Alternatif olarak, "com.google.android.apps.searchlite" uygulaması olan Google Go’yu da deneyebilirsiniz.

## Sorun giderme
Bazı uygulama veya cihazlar için, Traditional T9'un bunlarla daha iyi çalışmasını sağlayacak özel seçenekleri etkinleştirmek mümkündür. Her ayar ekranının sonunda Uyumluluk bölümünde bulabilirsiniz.

### Alternatif öneri kaydırma yöntemi
_Ayarlar → Görünüm altında._

Bazı cihazlarda, Tahmin Modunda, listeyi sonuna kadar kaydıramayabilir veya son öneri görünene kadar birkaç kez ileri geri kaydırmanız gerekebilir. Bu sorun bazen Android 9 veya daha eski sürümlerde meydana gelir. Bu sorunu yaşıyorsanız, bu seçeneği etkinleştirin.

### Her Zaman Üstte
_Ayarlar → Görünüm altında._

Bazı telefonlarda, özellikle Sonim XP3plus (XP3900) modelinde, Traditional T9 yazmaya başladığınızda görünmeyebilir veya yumuşak tuşlarla kısmen örtülebilir. Diğer durumlarda, etrafında beyaz çubuklar olabilir. Sorunu önlemek için "Her Zaman Üstte" seçeneğini etkinleştirin.

### Alt boşluk (dikey yönlendirme)
_Şurada: Ayarlar → Görünüm._

Android 15 veya üzeri sürümlere sahip Samsung cihazlarda Traditional T9 ekranda çok aşağıda görünebilir. Bu durumda sistem Gezinti Çubuğu klavyenin son satırını kapatarak tuşların kullanılamaz hâle gelmesine neden olur. Boşluk tuşuna basmaya, OK tuşuna basmaya veya giriş modunu değiştirmeye çalışmak klavyenin kapanmasına yol açar. “Alt boşluk” değerinin 48 dp’ye yükseltilmesi sorunu çözecektir.

Diğer durumlarda, tuş bloğunun altında gereksiz bir boş alan görünebilir. “Alt boşluk” değerinin 0 dp’ye düşürülmesi bu alanı ortadan kaldırır.

_Daha fazla bilgi için [#950](https://github.com/sspanak/tt9/issues/950) numaralı hataya bakınız._

_Çok nadir durumlarda, Samsung olmayan cihazlarda da aynı sorunlar görülebilir. Bkz. [#755](https://github.com/sspanak/tt9/issues/755)._

### Tuş tekrarı koruması
_Ayarlar → Tuş Takımı altında._

CAT S22 Flip ve Qin F21 telefonları, zamanla hızla bozulup tek bir tuşa basış için birden çok tıklama kaydeden düşük kaliteli tuş takımlarına sahip olmalarıyla bilinir. Bu, yazarken veya telefon menülerinde gezinirken fark edilebilir.

CAT telefonlar için önerilen ayar 50-75 ms’dir. Qin F21 için 20-30 ms ile başlayın. Sorun devam ediyorsa değeri biraz artırın, ancak genel olarak mümkün olduğunca düşük tutmaya çalışın.

_**Not:** Değer ne kadar yüksek olursa, yazmanız o kadar yavaş olur. TT9 çok hızlı tuş basışlarını görmezden gelecektir._

_**Not 2:** Yukarıdakilere ek olarak, Qin telefonları bazen uzun basışları algılayamayabilir. Bu durumda maalesef yapılabilecek bir şey yoktur._

### Yazılan metni göster
_Ayarlar → Tuş Takımı altında._

Eğer Deezer veya Smouldering Durtles uygulamalarında yazı yazarken öneriler çok hızlı kayboluyorsa ve göremiyorsanız, bu seçeneği devre dışı bırakın. Bu, mevcut kelimenin OK veya Boşluk tuşuna basana ya da öneri listesine dokunana kadar gizli kalmasını sağlar.

Bu sorun, Deezer ve Smouldering Durtles uygulamalarının bazen yazdığınız metni değiştirmesi nedeniyle TT9’un düzgün çalışmamasından kaynaklanmaktadır.

### Telegram/Snapchat sticker ve emoji panelleri açılmıyor
Bu, küçük boyutlu düzenlerden birini kullanıyorsanız gerçekleşir. Şu an için kalıcı bir çözüm yok, ancak şu geçici çözümü kullanabilirsiniz:
- Ayarlar → Görünüm yoluna gidin ve Ekran Üzeri Numpad'i etkinleştirin.
- SoHBETe geri dönün ve emoji veya sticker butonuna tıklayın. Artık görünecekler.
- Ayarlara geri dönüp ekran üzeri numpadi devre dışı bırakabilirsiniz. Emoji ve sticker panelleri, uygulamayı veya telefonu yeniden başlatana kadar erişilebilir durumda kalacaktır.

### Traditional T9 bazı uygulamalarda hemen görünmüyor (yalnızca dokunmatik ekranı olmayan telefonlar için)
Yazabileceğiniz bir uygulamayı açtıysanız ancak TT9 otomatik olarak görünmüyorsa, yazmaya başlayın, ortaya çıkacaktır. Alternatif olarak, [giriş modunu değiştirmek](#sonraki-giriş-modu-tușu-varsayılan-bas) veya [dil değiştirmek](#sonraki-dil-tușu-varsayılan-basılı-tut) için kısayol tuşlarına basmak TT9'u ortaya çıkarmaya yardımcı olabilir.

Bazı cihazlarda TT9 görünmez kalabilir, ne yaparsanız yapın. Bu durumda, [Her Zaman Üstte](#her-zaman-üstte) seçeneğini etkinleştirmeniz gerekir.

**Uzun açıklama:** Bu sorunun nedeni, Android'in öncelikle dokunmatik ekranlı cihazlar için tasarlanmış olmasıdır. Bu nedenle, klavyeyi göstermek için metin/sayı alanına dokunmanızı bekler. TT9'u bu onayı almadan görünür hale getirmek mümkün, ancak o zaman Android bazen gizlemeyi unutabilir. Örneğin, bir telefon numarası çevirdiğinizde veya bir arama alanına metin girdikten sonra klavye açık kalabilir.

Bu nedenlerden dolayı, Android standartlarına uygun kalmak için kontrol sizin elinizde. Ekrana "dokunmak" için bir tuşa basın ve yazmaya devam edin.

### Qin F21 Pro'da 2 veya 8 tuşuna basılı tutmak, sayı yazmak yerine sesi açıyor veya kısıyor
Bu sorunu hafifletmek için Ayarlar → Görünüm yoluna gidin ve "Durum Simgesi"ni etkinleştirin. TT9, Qin F21'i otomatik olarak algılamalı ve ayarları etkinleştirmelidir, ancak otomatik algılama başarısız olursa veya simgeyi bir nedenle devre dışı bıraktıysanız, tüm tuşların düzgün çalışması için simgeyi etkinleştirmeniz gerekir.

**Uzun açıklama:** Qin F21 Pro (ve muhtemelen F22), numara tuşlarına Ses Aç ve Ses Kıs işlevlerini atama olanağı sunan bir kısayol uygulamasına sahiptir. Varsayılan olarak, kısayol yöneticisi etkinleştirilmiştir ve 2 tuşuna basılı tutmak sesi artırır, 8 tuşuna basılı tutmak ise sesi kısar. Ancak, durum simgesi yoksa, yönetici etkin bir klavye olmadığını varsayar ve ses seviyesini ayarlar; bu durumda, Traditional T9 tuşu kullanamaz ve bir sayı yazamaz. Simgeyi etkinleştirmek, kısayol yöneticisini atlayarak her şeyin sorunsuz çalışmasını sağlar.

### Xiaomi telefonlarda genel sorunlar
Xiaomi, telefonlarında Traditional T9'un sanal ekran klavyesinin düzgün çalışmasını engelleyen bir dizi standart dışı izin tanıtmıştır. Daha spesifik olarak, "Ayarları Göster" ve "Kelime Ekle" tuşları, ilgili işlevlerini yerine getiremeyebilir. Bunu düzeltmek için TT9'a telefonunuzun ayarlarından "Ekran pop-up penceresi göster" ve "Arka planda çalışırken ekran pop-up penceresi göster" izinlerini vermelisiniz. [Bu rehber](https://parental-control.flashget.com/how-to-enable-display-pop-up-windows-while-running-in-the-background-on-flashget-kids-on-xiaomi), başka bir uygulama için nasıl yapılacağını anlatıyor.

"Açık bildirim" iznini vermek de şiddetle tavsiye edilir. Bu, Android 13'te tanıtılan "Bildirimler" iznine benzer. Neden gerektiği konusunda daha fazla bilgi için [yukarıya bakın](#android-13-veya-üstü-için-notlar).

_Xiaomi sorunları [bu GitHub sorununda](https://github.com/sspanak/tt9/issues/490) tartışılmıştır._

### Sesli Giriş'in durması çok uzun sürüyor
Bu, Android 10'da Google'ın asla düzeltmediği [bilinen bir sorun](https://issuetracker.google.com/issues/158198432). TT9 tarafında hafifletmek mümkün değildir. Sesli Giriş işlemini durdurmak için birkaç saniye sessiz kalın. Android, herhangi bir konuşma algılamadığında mikrofonu otomatik olarak kapatır.

### Banka uygulamam Traditional T9’u kabul etmiyor
Bu durum TT9 ile ilgili bir sorun değildir. Bankalar, risk almak istemedikleri ve bu tür klavyeleri güvensiz varsaydıkları için genellikle standart dışı veya açık kaynaklı klavyeleri kısıtlamaktadır. Bazıları daha da ileri giderek kendi klavyelerini sunmakta ve hatta Google’ın standart klavyesi olan Gboard’u engellemektedir. Ne yazık ki bu durumda tek seçenek, cihazın orijinal klavyesine geçmektir.

### Titreşim çalışmıyor (yalnızca dokunmatik ekran cihazları)
Pil tasarrufu, optimizasyon seçenekleri ve "Rahatsız Etmeyin" modu titreşimi engelleyebilir. Cihazınızın Sistem Ayarlarında bu seçeneklerden herhangi birinin açık olup olmadığını kontrol edin. Bazı cihazlarda, pil optimizasyonunu her uygulama için ayrı ayrı ayarlamak mümkündür. Bunu yapmak için Sistem Ayarları → Uygulamalar bölümüne gidin. Eğer cihazınız destekliyorsa, TT9 için optimizasyonu kapatın.

Titreşimin çalışmamasının bir diğer nedeni, sistem düzeyinde devre dışı bırakılmış olması olabilir. Cihazınızın Sistem Ayarları → Erişilebilirlik bölümünde "Dokunmada titreşim" veya "Tuş basışında titreşim" seçenekleri olup olmadığını kontrol edin ve etkinleştirin. Xiaomi ve OnePlus cihazları titreşim üzerinde daha ayrıntılı kontrol imkanı sunar. Tüm ilgili ayarların açık olduğundan emin olun.

Son olarak, bazı cihazlarda titreşim güvenilir şekilde çalışmayabilir. Bunu düzeltmek için daha fazla izin ve cihaz fonksiyonlarına erişim gereklidir. Ancak, TT9 gizliliği ön planda tutan bir klavye olduğu için bu tür erişimleri talep etmeyecektir.

## Sıkça Sorulan Sorular

### Neden X dilini eklemiyorsunuz?
Eklemeyi çok isterim, ancak yardımına ihtiyacım var. 40’tan fazla dili tek başıma desteklemek benim için imkânsız. Senin dilini konuşmadığım için internette doğru kaynakları bulmak zor oluyor. İşte bu noktada, senin gibi ana dil konuşurları çok yardımcı olabiliyor.
Aslında mevcut dillerin %90’ından fazlası kullanıcıların katkılarıyla eklendi.

Yeni bir dil eklemek için, yazım hatası olmayan bir kelime listesine ihtiyacım var. Tercihen, saygın bir üniversite veya dil enstitüsü tarafından hazırlanmış resmi bir sözlükten alınmış olmalı (örneğin, “X Dilinin Büyük Sözlüğü”). Böyle listeler, yazarken en kaliteli önerileri sağlar.

Böyle bir sözlük yoksa, ücretsiz indirilebilen bir kelime listesi de kabul edilir. Liste genellikle 300.000–500.000 kelime içermelidir, ancak dilde çok fazla çekim varsa (zaman, cinsiyet, sayı vb.), 1 milyon kelimeye kadar gerekebilir.

### XYZ dilinde hatalı yazılmış veya eksik kelimeler var. Bunları neden düzeltmiyorsunuz?
Yukarıda belirttiğim gibi, senin dilini konuşmadığım için bu hataları fark etmem zor. Ama senin yardımınla bunları düzeltebilir ve sözlüğü daha iyi hâle getirebiliriz.

### X özelliğini ekleyemez misiniz?
Hayır.

Herkesin kendi tercihleri vardır. Kimisi daha büyük tuşlar ister, kimisi farklı bir düzen, kimisi ".com" yazmak için bir kısayol tuşu ister ve kimisi de eski telefonunu veya klavyesini özler. Ancak lütfen anlayın ki, bu projeyi boş zamanlarımda gönüllü olarak yürütüyorum. Birbirleriyle çelişen binlerce farklı isteği yerine getirmek imkansızdır.

Henry Ford bir keresinde şöyle demiştir: "Müşteri istediği herhangi bir rengi seçebilir, yeter ki siyah olsun." Benzer şekilde, Traditional T9 sade, etkili ve ücretsizdir; ne alıyorsanız onu kullanırsınız.

### Uygulamayı favori cihazıma (ör. Sony Ericsson, Xperia, Nokia C2, Samsung) veya favori klavye uygulamama daha benzer hâle getiremez misiniz?
Hayır.

Traditional T9, bir kopya uygulama ya da eski bir klavyenin birebir ikamesi olmak için tasarlanmadı. Kendine özgü bir tasarımı vardır ve temel olarak Nokia 3310 ve 6303i modellerinden ilham almıştır. Klasik telefonların hissini verse de, tam olarak hiçbir cihazı taklit etmez.

### TouchPal’i kopyalamalısınız; en iyi klavyeydi!
Hayır. Önceki maddelere bakınız.

TouchPal, kapsamlı tema seçenekleri, özelleştirme olanakları ve çok dilli destek sunan hızlı ve duyarlı bir klavyeydi. Rekabetin sınırlı olduğu 2015 civarında popülerdi. Ancak hiçbir zaman gerçek bir T9 klavyesi olmadı: 12 tuşlu düzen yalnızca bazı dillerde mevcuttu ve yalnızca dokunmatik ekranlar için tasarlanmıştı.

Zamanla en önemli unsur olan yazma deneyiminden uzaklaşmaya başladı. Reklamlar eklendi, izin talepleri giderek daha agresif hale geldi ve hassas kullanıcı verileri toplanmaya başlandı. Sonunda Play Store’dan kaldırıldı.

Buna karşılık, TT9’un [felsefesi](https://github.com/sspanak/tt9/?tab=readme-ov-file#-philosophy) açık kaynak ilkelerine dayanmaktadır. Kaynak kodu ve sözlükleri herkese açıktır ve incelenebilir. Kullanıcı gizliliği tasarım aşamasından itibaren gözetilmektedir. Topluluk katkıları, hata düzeltmeleri, yeni diller ve çeviriler dahil olmak üzere projenin gelişmesine yardımcı olmuştur. Kullanıcılar ayrıca kendi değiştirilmiş sürümlerini oluşturmakta özgürdür.

TT9, özelleştirilebilir tuş şekilleri gibi özellikler sunmaz; bunun yerine verimli yazmaya odaklanan temiz ve okunabilir bir düzen sağlar. TouchPal’in görsel stilini taklit etmez, ancak Android 16 çalıştıran modern akıllı telefonlarda, Qin F21, Cat S22 Flip ve Sonim XP3800 gibi fiziksel tuş takımlı nostaljik cihazlarda ve hatta TV uzaktan kumandalarında çalışır.

Katılmıyorsanız veya görüşünüzü açıklamak istiyorsanız, GitHub’daki [açık tartışmaya](https://github.com/sspanak/tt9/issues/647) katılabilirsiniz. Lütfen diğer kullanıcılara karşı saygılı olun. Nefret içerikli paylaşımlara izin verilmez.

### Android, klavyenin kredi kartı numaraları ve parolalar dahil olmak üzere kişisel verilerimi toplayabileceği konusunda beni uyardı
Bu, yalnızca Traditional T9 için değil, herhangi bir klavye yüklendiğinde ve etkinleştirildiğinde görüntülenen standart bir Android uyarısıdır. Yazdığınız her şeyin cihazınızda kaldığından emin olabilirsiniz. Yazma motoru tamamen açık kaynaklıdır; bu nedenle kodunu GitHub üzerinden inceleyebilir ve gizliliğinizin korunduğunu doğrulayabilirsiniz.

_Hâlâ herhangi bir endişeniz varsa, lütfen uygulamanın Gizlilik Politikasını inceleyiniz._

### QWERTY Düzeni Kullanmak İstiyorum (yalnızca dokunmatik ekran cihazları)
Traditional T9, bir T9 klavyesidir ve bu nedenle QWERTY benzeri bir düzen sağlamaz.

T9 kullanmayı öğreniyorsanız ve ara sıra geri dönmeniz gerekiyorsa veya yeni kelimeleri QWERTY ile yazmak daha uygun geliyorsa, farklı bir klavyeye geçmek için giriş modu tuşuna yukarı kaydırabilirsiniz. Daha fazla bilgi için [sanal tuşlar genel bakışına](#sanal-tuşlara-genel-bakış) bakın.

Çoğu diğer klavye, space tuşuna veya "dil değiştirme" tuşuna basılı tutarak tekrar Traditional T9'a geçiş yapmanıza izin verir. İlgili kılavuz veya kullanım kılavuzuna göz atın.

### Dokunmatik ekranlı bir telefonda dili değiştiremiyorum
Öncelikle Ayarlar → Diller kısmından tüm istediğiniz dilleri etkinleştirdiğinizden emin olun. Ardından, dili değiştirmek için [giriş modu tuşunu](#giriş-modu-tuşu) basılı tutun.

### “I've” veya “don't” gibi kısaltmalar sözlüğe nasıl eklenir?
Tüm dillerdeki kısaltmalar zaten ayrı kelimeler olarak mevcut, bu yüzden herhangi bir şey eklemenize gerek yok. Bu maksimum esneklik sağlar — istediğiniz herhangi bir kelimeyi herhangi bir kısaltma ile birleştirebilir ve aynı zamanda önemli miktarda depolama alanı tasarrufu elde edersiniz.

Örneğin, 've yazmak için: 183 tuşlayın; veya 'll için: 155. Bu, "I'll" = 4155 ve "we've" = 93183 anlamına gelir. Ayrıca "google.com" yazmak için: 466453 (google) 1266 (.com) tuşlayabilirsiniz.

Fransızca daha karmaşık bir örnek: "Qu'est-ce que c'est" = 781 (qu'), 378123 (est-ce), 783 (que), 21378 (c'est).

_Bu kuralın dikkate değer istisnaları İngilizcedeki "can't" ve "don't" kelimeleridir. Burada 't ayrı bir kelime değildir, ancak yukarıda açıklandığı şekilde yazabilirsiniz._