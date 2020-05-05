package com.resulozel.bluetooth_app;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // dinle button tanımlaması
    Button dinle_Butonu;

    // mesajı yolla buton tanımlaması
    Button mesajı_Yolla_Butonu;

    // cihazları listele buton tanımlaması
    Button cihazları_Listele_Butonu;

    // cihaz liste tanımı
    ListView cihaz_Listeleri;

    // textview mesaj_Alani tanımı
    TextView mesaj_Alani;

    // textview durum tanımlaması
    TextView cihaz_Durumu;

    // mesaj alanı edittext tanımı
    EditText Mesaj_Alani;

    // bluetoothadapter nesnesi tanımlaması
    BluetoothAdapter bluetoothAdapter;

    // BluetoothDeviceları tutacak array tanımlaması
    BluetoothDevice[] bluetoothCihazlari;

    // sendReceive değişken tanımlaması
    SendReceive sendReceive;


    // DURUM_DINLEME sabit değeri
    static final int DURUM_DINLEME = 1;

    // DURUM_BAGLANIYOR sabit değeri
    static final int DURUM_BAGLANIYOR = 2;

    // DURUM_BAGLANTI sabit değeri
    static final int DURUM_BAGLANTI = 3;

    // DURUM_HATASI sabit değeri
    static final int DURUM_HATASI = 4;

    // MESAJ_TESLIMI sabit değeri
    static final int MESAJ_TESLIMI = 5;

    // BAGLANTI_ACMA_BLUETOOTH değeri tanımı ve değeri
    int BAGLANTI_ACMA_BLUETOOTH = 1;

    // uygulama name değeri verildi.
    private static final String APP_NAME = "BluetoothChatApp";

    // cihazlarda bulunan uuid değerini yaz
    private static final UUID MY_UUID = UUID.fromString("318c6089-985c-4773-b7ca-4c6130e4209e");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* programın layout kısmını içinde göstermek  atadığım değerler */
        dinle_Butonu = findViewById(R.id.listen);
        mesajı_Yolla_Butonu = findViewById(R.id.send);
        cihazları_Listele_Butonu = findViewById(R.id.listDevices);
        cihaz_Listeleri = findViewById(R.id.listview);
        cihaz_Durumu = findViewById(R.id.status);
        mesaj_Alani = findViewById(R.id.msg);
        Mesaj_Alani = findViewById(R.id.writemsg);

        // adapter nesnesiyle bluetooth aygıtına erişim sağlamak için kullanıldı.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // bluetoothAdapter değeri açık değilse kullan

//Hocam burada  JAVA NULL POINTER yediğim için try-catch modülüne sokup istisna fırlattım.
/*
 try {
     if (!bluetoothAdapter.isEnabled()) {

         // bluetooth iznini kullanıcıdan istiyoruz.
         Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

         // intent işlemi
         startActivityForResult(intent, BAGLANTI_ACMA_BLUETOOTH);
     }
 }catch (Exception e)
 {


 }
        implementListeners();*/



// Burada JNPE döndüğünde try-catch yapılırsa bluetooth modülü patlıyor.

        if (!bluetoothAdapter.isEnabled()) {

         /*   bluetoothAdapter.enable();*/ //bluetooth aktifleştirildi.

            // bluetooth izinlerini kullanıcıdan istiyoruz.
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // Activity arası geçiş için intent işlemi
            startActivityForResult(intent, BAGLANTI_ACMA_BLUETOOTH);
        }


        // implementListeners metodun çağrılmasını yapıldığı alandır.
        // Bu alanda metot içindeki veriler okunur.
        implementListeners();
    }


    // implementListeners adında bir function oluşturduk.
    private void implementListeners() {


        // listelemeye tıklayınca neler olağını gösteriyor.
        cihazları_Listele_Butonu.setOnClickListener(new View.OnClickListener() {

            // button değerine tıklayınca hangi olayların olacağını gösteriyor.
            @Override
            public void onClick(View v) {

                // cihazların set ediliği ve güncellendiği kısımdır.
                Set<BluetoothDevice> cihazlar = bluetoothAdapter.getBondedDevices();

                // cihazların değerini alıp dizi içerisinde döndürmek için kullanılan koddur.
                String[] strings = new String[cihazlar.size()];

                // Cihazlar arasında ters olarak atama yapar.
                bluetoothCihazlari = new BluetoothDevice[cihazlar.size()];

                // index değeri 0 olarak verdik.
                int index = 0;

                // cihazların boyutu 0 dan büyükse
                if (cihazlar.size() > 0) {

                    // for döngüsü ile  cihazları döndür.
                    for (BluetoothDevice cihaz : cihazlar) {

                        // bluetoothCihazlari index değerine cihaz atanır.
                        bluetoothCihazlari[index] = cihaz;

                        // cihaz isimlerini getir ve index değerine koy.
                        strings[index] = cihaz.getName();

                        // index değerini bir artırır.
                        index++;
                    }

                    // adapter nesnesi tanımlandı. Gerekli feature kısımları verildi.
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);

                    // düzenlenen adapter nesnesine listview'i atama işlemini gerçekleştiriyor.
                    cihaz_Listeleri.setAdapter(arrayAdapter);
                }

            }
        });


        // Butona tıklayınca hangi olaylar olacak, o işlemleri gösteriyor.
        dinle_Butonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ServerClass dan bir serverClass nesnesi tanımlama alanı oluşturuyor.
                ServerClass serverClass = new ServerClass();

                // serverclass işlemini başladık.
                serverClass.start();
            }
        });


        // listviewe tıklanınca neler olacak onun incelendiği alandır.
        cihaz_Listeleri.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {

                // ClientClass sınıfından nesne oluşturma, ve içerisinde bluetoothCihazlari array kısmını atama yeridir.
                ClientClass clientClass = new ClientClass(bluetoothCihazlari[i]);

                // clientClass sınıfını başlatma işlemi uygulanır.
                clientClass.start();


                // cihaz_durumu text değerine "Bağlanıyor" değerini yazıncaya kadar devam eder.
                cihaz_Durumu.setText("Bağlanıyor");
            }
        });


        // mesajı_Yolla_Butonu butonuna basınca neler olacak
        mesajı_Yolla_Butonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Mesaj_Alani alanındaki mesaj değeri al
                String string = String.valueOf(Mesaj_Alani.getText());

                // mesajı bytes halinde yolla ve yaz
                sendReceive.write(string.getBytes());
            }
        });

    }

    // Handler sınıfından handler değişkeni tanımlama
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message mesaj) {

            // Handler : UI Thread ile haberleşmeyi sağlayan bir sınıftır.
            switch (mesaj.what) {

                // what : kullanıcı tanımlı mesaj kodudur.
                // Bu mesajın neyle ilgili olduğuna kullanıcı karar verebilir.
                // int tipinde tanımlanır.

                // DURUM_DINLEME değeriyse neler olacak
                case DURUM_DINLEME:

                    // cihaz_Durumu alanına "Dinleniyor" textini yaz
                    cihaz_Durumu.setText("Dinleniyor");
                    break;

                // DURUM_BAGLANIYOR değeriyse neler olacak
                case DURUM_BAGLANIYOR:

                    // cihaz_Durumu alanına "Bağlanıyor" textini yaz
                    cihaz_Durumu.setText("Bağlanıyor");
                    break;

                // DURUM_BAGLANTI değeriyse neler olacak
                case DURUM_BAGLANTI:

                    // cihaz_Durumu alanına "Bağlandı" textini yaz.
                    cihaz_Durumu.setText("Bağlandı");
                    break;

                // DURUM_HATASI değeriyse neler olacak
                case DURUM_HATASI:

                    // cihaz_Durumu alanına "Bağlantı Hatası" textini yaz
                    cihaz_Durumu.setText("Bağlantı Hatası");
                    break;

                // MESAJ_TESLIMI değeriyse neler olacak
                case MESAJ_TESLIMI:

                    // readBuffer değişkeniyle mesaj objesini al
                    byte[] readBuffer = (byte[]) mesaj.obj;

                    // tempMessage değişkenine ilgili değerler işleniyor
                    String tempMessage = new String(readBuffer, 0, mesaj.arg1);

                    // mesaj_Alani değişkeninin bulunduğu alana ilgili mesajları yaz
                    mesaj_Alani.setText(tempMessage);

                    // işlemi sonlandır.
                    break;
            }

            // geriye değeri true olarak döndür.
            return true;
        }
    });


    // her yerden erişebilmek için ServerClass sınıfı tanımlandı.
    public class ServerClass extends Thread {

        // BluetoothServerSocket sınıfından bir nesne tanımlama işlemi
        BluetoothServerSocket serverSocket;

        // ServerClass yapıcı fonksiyonu tanımı
        ServerClass() {

            try {
                // serverSocket değerine uuid değerini atama işlemi
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);

                // IOException hatasını yakalama
            } catch (IOException e) {
                // hatayı bastır ekrana bastırma işlemi
                e.printStackTrace();
            }
        }


        // run fonksiyonda yapılacak işlemler
        public void run() {

            // BluetoothSocket sınıfından socket adında nesne tanımlandı sonra nesneye null değeri verildi.
            BluetoothSocket socket = null;

            // socket değeri null değerine eşitse ise yani true olduğu sürece
            while (true) {

                // try-catch blokları arasına giriyor
                try {

                    // Alınan mesaja istediğimiz değerleri verebildiğimiz alan.
                    Message message = Message.obtain();

                    // bağlantı oluştuğunda bağlantının durumunun takibi - bağlanıyor verisi
                    message.what = DURUM_BAGLANIYOR;

                    // mesajı yollama işlemi, handler aracılığıyla
                    handler.sendMessage(message);

                    // socket işlemini kabul etme işlemi
                    socket = serverSocket.accept();

                    // hatayı yakalama alanı - IOException hata tipi
                } catch (IOException e) {

                    // hatayı ekrana ya da log kısmına bastırma işlemi
                    e.printStackTrace();

                    // mesaj örneğini alma işlemi - obtain() sayesinde
                    Message message = Message.obtain();

                    // DURUM_HATASI olayı olunca mesajı dinleme-türünü öğrenme
                    message.what = DURUM_HATASI;

                    // mesajı yollama işlemi
                    handler.sendMessage(message);
                }

                // socket değeri null değerine eşit değilse
                if (socket != null) {

                    // Alınan mesajı istediğimiz değerleri verebiliriz. Bu alan sizin yapacağınız işlere göre özelleştirilebilir.
                    Message message = Message.obtain();

                    // bağlantı oluştuğunu anlama kısmı - durum_baglantı
                    message.what = DURUM_BAGLANTI;

                    // mesajı yollama işini handler ile yapma işlemi
                    handler.sendMessage(message);


                    // sendReceive nesnesi tanımlama ve parametre olarak socket değerini yollama
                    sendReceive = new SendReceive(socket);

                    // sendReceive işlemini  başlatma
                    sendReceive.start();

                    // yapılan işlemlerden sonra olayları bitirme alanı
                    break;
                }

            }
        }
    }


    // ClientClass sınıfında yapılacak işlemler - Thread sınıfından kalıtma olayı
    public class ClientClass extends Thread {

        // BluetoothDevice sınıfından device nesnesi tanımlama
        BluetoothDevice device;

        // BluetoothSocket sınıfından socket nesnesi tanımlama
        BluetoothSocket socket;


        // ClientClass yapıcı sınıfından neler olacak
        ClientClass(BluetoothDevice device1) {

            // nesneye device değerine device1'i ata
            device = device1;

            try {
                // uuid değerine socket işlemini atama kısmı
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                // hatayı yakalama alanı
            } catch (IOException e) {

                // hatayı yazdır ekrana yazdırma işlemi
                e.printStackTrace();
            }
        }

        // run fonksiyonunda yapılacak işlemler-olaylar
        public void run() {

            try {

                // socket'i connect etme işlemi.
                socket.connect();

                // bir mesaj örneği alabilmek için bu metot kullanılır. Alınan mesaja istediğimiz değerler verilir.
                Message message = Message.obtain();

                // kullanıcı tanımlı mesaj kodudur. Int tipinde tanımlanır.
                message.what = DURUM_BAGLANTI;

                // mesaj kuyruğunun sonuna bir mesaj eklemeyi sağlar.
                handler.sendMessage(message);

                // sendReceive nesnesi işlemi
                sendReceive = new SendReceive(socket);

                // sendReceive başlat
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();

                // Alınan message istediğimiz değerler verilir.
                Message message = Message.obtain();

                // eğer fail olduysa değer
                message.what = DURUM_HATASI;

                // handler sendmessage işlemi
                handler.sendMessage(message);
            }
        }


    }


    // SendReceive sınıfında yapılacak işlemler - Thread sınıfındaki yapılara erişmek için extends kullanımı.
    private class SendReceive extends Thread {

        // BluetoothSocket sınıfından değişmez ve bu class özel bluetoothSocket nesnesi tanımlandı.
        private final BluetoothSocket bluetoothSocket;

        // InputStream sınıfından değeri değişmez ve bu sınıfa özgü inputStream değişkeni tanımlama kısmıdır.
        private final InputStream inputStream;

        // OutputStream sınıfından outputStream değişkeni tanımlama olayı. private-final değerinde oluşan kısımdır.
        private final OutputStream outputStream;

        // SendReceive constructor işlemleri
        SendReceive(BluetoothSocket socket) {

            // bluetoothSocket değerine socket'i atadık.
            bluetoothSocket = socket;

            // tempInput değişkenine null değeri verildi.
            InputStream tempInput = null;

            // tempOutput değişkenine null değeri verildi.
            OutputStream tempOutput = null;

            try {
                // tempInput getInputStream metodu
                tempInput = bluetoothSocket.getInputStream();

                // tempOutput getOutputStream metodu
                tempOutput = bluetoothSocket.getOutputStream();

                // hata yakalama kısmı - IOException hata tipi kısmıdır.
            } catch (IOException e) {

                // hatayı ekrana bastırma işlemi yeridir.
                e.printStackTrace();
            }

            // inputStream değerine tempInput değerini atamak için kullanılır.
            inputStream = tempInput;

            // outputStream değerine tempOutput değerini atamak için kullanılır.
            outputStream = tempOutput;
        }

        // run fonksiyonunda yapılacak işler gösterilir.
        public void run() {

            // buffer adında array oluşturma , boyutu 1024 olan dizidir.
            byte[] buffer = new byte[1024];

            // bytes değişkeni tanımlama
            int bytes;

            // true değeri döndükçe try içi çalıştırılır.
            while (true) {

                try {

                    // bytes değişkenine read buffer yapma olayıdır.
                    bytes = inputStream.read(buffer);

                    // obtainMessage parametreleri tutan bir mesaj oluşturmak için kullanılır.
                    handler.obtainMessage(MESAJ_TESLIMI, bytes, -1, buffer).sendToTarget();

                    // hatayı yakalama işlemidir.
                } catch (IOException e) {

                    // hatayı yazdır kısmıdır.
                    e.printStackTrace();
                }
            }
        }

        // yazma işleminin yapıldığı fonksiyondur.
        void write(byte[] bytes) {

            try {

                // outputStream değerini yaz
                outputStream.write(bytes);

                // hata yakalama alanı
            } catch (IOException e) {

                // hatayı ekrana yazdırma işlemi
                e.printStackTrace();
            }
        }

    }


}
