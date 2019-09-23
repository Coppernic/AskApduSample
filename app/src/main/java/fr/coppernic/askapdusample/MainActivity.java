package fr.coppernic.askapdusample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.coppernic.sdk.ask.Defines;
import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.ask.ReaderListener;
import fr.coppernic.sdk.ask.RfidTag;
import fr.coppernic.sdk.ask.SearchParameters;
import fr.coppernic.sdk.ask.sCARD_SearchExt;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;
import fr.coppernic.sdk.utils.io.InstanceListener;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements InstanceListener<Reader> {

    private Reader reader;
    private CommunicationExchangesAdapter adapter;
    private ArrayList<CommunicationExchanges> exchanges;
    @BindView(R.id.swPolling)
    SwitchCompat swPolling;
    @BindView(R.id.etApdu)
    EditText etApdu;
    @BindView(R.id.lvLogs)
    ListView lvLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        exchanges = new ArrayList<>();
        adapter = new CommunicationExchangesAdapter(this, R.layout.exchanges_row, exchanges);
        lvLogs.setAdapter(adapter);
        lvLogs.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_all) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Powers on RFID reader
        ConePeripheral.RFID_ASK_UCM108_GPIO
                .getDescriptor()
                .power(this, true)
                .subscribe(new SingleObserver<CpcResult.RESULT>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CpcResult.RESULT result) {
                        Reader.getInstance(MainActivity.this, MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    protected void onStop() {
        reader.cscClose();
        // Powers off RFID reader
        ConePeripheral.RFID_ASK_UCM108_GPIO
                .getDescriptor()
                .power(this, false);
        swPolling.setEnabled(false);
        super.onStop();
    }

    @Override
    public void onCreated(Reader reader) {
        this.reader = reader;
        int res = this.reader.cscOpen(CpcDefinitions.ASK_READER_PORT, 115200, false);

        if (res == Defines.RCSC_Ok) {
            // Initializes RFID reader
            StringBuilder sbVersion = new StringBuilder();
            this.reader.cscVersionCsc(sbVersion);
            Toast.makeText(this, sbVersion.toString(), Toast.LENGTH_SHORT).show();
            // Enables polling switch
            swPolling.setEnabled(true);
        } else {
            Toast.makeText(this, "Error opening serial port", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDisposed(Reader reader) {

    }

    @OnClick (R.id.swPolling)
    void togglePolling(View view) {
        if (swPolling.isChecked()) {
            startPolling(view);
            Snackbar.make(view, getString(R.string.polling_started), Snackbar.LENGTH_SHORT).show();
        } else {
            reader.stopDiscovery();
            Snackbar.make(view, getString(R.string.polling_stopped), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fab)
    void sendApdu() {
        String[] apdus = etApdu.getText().toString().split("\n");

        for(String apdu:apdus) {

            byte[] dataToSend = CpcBytes.parseHexStringToArray(apdu);
            byte[] dataReceived = new byte[256];
            int[] dataReceivedLength = new int[1];
            int res = reader.cscISOCommand(dataToSend, dataToSend.length, dataReceived, dataReceivedLength);

            byte[] status = null;

            if (res != Defines.RCSC_Ok) {
                status = new byte[1];
                status[0] = reader.getBufOut()[4];
            } else {
                if (dataReceivedLength[0] >= 2) {
                    status = new byte[2];
                    System.arraycopy(dataReceived, dataReceivedLength[0] - 2, status, 0, 2);
                }
            }

            byte[] data = null;

            if (dataReceivedLength[0] - 2 > 0) {
                data = new byte[dataReceivedLength[0] - 3];
                System.arraycopy(dataReceived, 1, data, 0, data.length);
            }

            exchanges.add(0, new CommunicationExchanges(dataToSend, data, status));
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Starts polling for cards
     * @param view View used to display Snackbars
     */
    private void startPolling(final View view) {
        // Sets the card detection
        reader.cscEnterHuntPhaseParameters((byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, null, (byte)0x00, (byte)0x00);
        sCARD_SearchExt search = new sCARD_SearchExt();
        search.OTH = 0;
        search.CONT = 0;
        search.INNO = 0;
        search.ISOA = 1;
        search.ISOB = 1;
        search.MIFARE = 0;
        search.MONO = 0;
        search.MV4k = 0;
        search.MV5k = 0;
        search.TICK = 0;
        int mask = Defines.SEARCH_MASK_ISOA | Defines.SEARCH_MASK_ISOB | Defines.SEARCH_MASK_MIFARE;
        SearchParameters parameters = new SearchParameters(search, mask, (byte) 0x01, (byte) 0x00);
        // Starts card detection
        reader.startDiscovery(parameters, new ReaderListener() {
            @Override
            public void onTagDiscovered(RfidTag rfidTag) {
                // Displays Tag data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, "Tag detected", Snackbar.LENGTH_SHORT).show();
                        swPolling.setChecked(false);
                    }
                });
            }

            @Override
            public void onDiscoveryStopped() {
                Snackbar.make(view, "Polling stopped", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
