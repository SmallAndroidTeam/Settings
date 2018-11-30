package of.settings.bq;

import android.app.Application;
import android.content.Intent;

import of.settings.bq.service.BluetoothService;
import of.settings.bq.service.WiFiService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /* Start WiFiService and BluetoothService */
        Intent service = new Intent(this, WiFiService.class);
        startService(service);

        service = new Intent(this, BluetoothService.class);
        startService(service);
    }
}
