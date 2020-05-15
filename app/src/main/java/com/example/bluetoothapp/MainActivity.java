package com.example.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{


    //Initialisation des paramètres

    //TAG pour les LOG.d
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter ;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>() ;
    public DeviceListAdapter mDeviceListAdapter ;
    ListView lvNewDevices ;
    private Context MainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        initActivity();
        // initialisation de notre adapteur bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }


    /**
     * Destructeur
     */
    @Override
    protected void onDestroy()
    {
        Log.d ( TAG,"onDestroy: called" );
        super.onDestroy ();
        unregisterReceiver ( mBroadcastReceiver1 );
        unregisterReceiver ( mBroadcastReceiver2 );
        unregisterReceiver ( mBroadcastReceiver3 );
    }


    /**
     * Initialisation des paramètres graphiques
     */
    private void initActivity()
    {
        Button btn_blt = (Button) findViewById ( R.id.btn_blt );
        lvNewDevices = (ListView) findViewById ( R.id.LvNewDevices );
        lvNewDevices.setOnItemClickListener ( (AdapterView.OnItemClickListener) MainActivity.this );
    }


    /*** SLOTS ***/
    /**
     * Cette fonction va permettre d'activer/désativer le bluetooth de votre téléphone
     *
     */
    public void OnOff(View v)
    {
        // trois possibilités

        // dans le cas où le téléphone n'a pas de bluetooth
        if(mBluetoothAdapter == null)
        {
            Log.d (TAG,"enableDisable : doesnt have BT compatobilities");
        }

        // Si le bluetooth n'est pas activé
        if(!mBluetoothAdapter.isEnabled ())
        {
            Log.d(TAG,"enableDisable : enabling BT");
            //Activation du bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity ( enableBTIntent );


            // Maintenant pour avoir une visibilité de ce qu'on fait nous allons attraper le flux c'est a dire appeler
            // broadcast1 permettant de catch les differents case de notre bluetooth afin d'aider au debug
            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver1,BTintent );
        }
        if(mBluetoothAdapter.isEnabled ())
        {
            Log.d(TAG,"enableDisable : disabling BT");

            // desactivation du bluetooth
            mBluetoothAdapter.disable ();


            // Maintenant pour avoir une visibilité de ce qu'on fait nous allons attraper le flux c'est a dire appeler
            // broadcast1 permettant de catch les differents case de notre bluetooth afin d'aider au debug
            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver1,BTintent );
        }
    }

    /*** SLOTS ***
     *
     * cette fonction permettra a lappareil d'etre visible pendant quelques secondes par d'autres appareils bluetooth
     *
     */
    public void btnEnableDisable_Discoverable(View v)
    {
        Log.d(TAG,"50 secondes pour être vu par d'autres périphériques");

        Intent discoverable = new Intent ( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
        discoverable.putExtra ( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,50 );
        startActivity ( discoverable );

        // Pour aider au debug nous allons intercepter le flux donc nous allons créer un broadcast2
        // afin de voir si des changements ont été appercus
        IntentFilter intentFilter = new IntentFilter ( mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED );
        registerReceiver ( mBroadcastReceiver2,intentFilter);
    }


    /*** SLOTS ***
     *
     * Le but va etre de détecter les appareils bluetooth
     *
     *
     */
    public void btnDiscover(View v)
    {
        Log.d(TAG , "btnDiscover : Looking for unpaired devices");
        if(mBluetoothAdapter.isDiscovering ())
        {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"btnDiscover : Cancelling discovery");

            //nous allons checker si il y a permission
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery ();

            // ici, cela ne va pas nous aider qu'à débuguer, nous allons aussi intégrer ce que nous avvons réceptionné dans une liste
            // généré par notre broadcast3
            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
            registerReceiver ( mBroadcastReceiver3 , discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering ())
        {
            //on va checker les permissions
            checkBTPermissions ();
            Toast.makeText (MainActivity.this,"Recherche d'appareils en cours ...",Toast.LENGTH_LONG).show ();

            // on lance la recherche
            mBluetoothAdapter.startDiscovery () ;
            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
            registerReceiver ( mBroadcastReceiver3 , discoverDevicesIntent);

        }
    }


    /**
     * Simple fonction permettant d'appeler la permission de localisation, cette permission est nécessaire pour certaines versions d'android
     * Sinon la connection bluetooth ne peut pas se faire
     */
    private void checkBTPermissions()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int permissionCheck = this.checkSelfPermission ( "Manifest.permission.ACCESS_FINE_LOCATION" );
                permissionCheck += this.checkSelfPermission ( "Manifest.permission.ACCESS_COARSE_LOCATION" );

                if(permissionCheck != 0)
                {
                    this.requestPermissions ( new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION} ,1001);
                }
                else
                {
                    Log.d ( TAG,"Pas besoin de checker les permissions" );
                }
            }
        }
    }


    /**
     * ici nous récupérons l'évènement lorsque l'utilisateur intéragis avec notre list dans le but de récupérer le nom du device et son adresse
     * dans un premier temps et pour finit par un apparaillage de cette deniere.
     *
     */
    @Override
    public  void onItemClick(AdapterView<?> adapterView,View v ,int i ,long l)
    {
        //nous devons tout dabord supprimer la recherche car cela demande beaucoup de memoire
        mBluetoothAdapter.cancelDiscovery ();

        Log.d(TAG , "onItemCLick : you clicked on a device");
        //nous allons recuperer les infos que vous avez selectionné
        String deviceName = mBTDevices.get(i).getName ();
        String deviceAdress = mBTDevices.get(i).getAddress ();

        Log.d(TAG , "onItemClick : deviceName = " + deviceName);
        Log.d(TAG , "onItemClick : deviceAdress = " + deviceAdress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG , "Tentative d'apparaillage " + deviceName);

            mBTDevices.get ( i ).createBond ();

            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver4 , discoverDevicesIntent);
        }

    }

    /*******************************************************************************************************************************/
    /*****                                                  LES BROADCASTS                                                  ********/
    /*******************************************************************************************************************************/


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver () {
        public void onReceive(Context context,Intent intent)
        {
            String action = intent.getAction();
            //evenement quand on decouvre un equipement
            if(action.equals ( mBluetoothAdapter.ACTION_STATE_CHANGED ))
            {
                final int state = intent.getIntExtra ( BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR );

                // application des differents etats du boutton
                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d ( TAG,"onReceive: STATE OFF" );
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d ( TAG,"mBroadcast: STATE_TURNING_ON" );
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d ( TAG,"mBroadcast: STATE_TURNING_OFF" );
                        break;

                }

            }
        }
    };



    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context,Intent intent)
        {
            String action = intent.getAction();
            //evenement quand on decouvre un equipement
            if(action.equals ( mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED ))
            {
                int mode = intent.getIntExtra ( BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR );

                // Receptions des differents etats du bouton
                switch(mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d ( TAG,"onReceive: Discoverable enabled" );
                        break;

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d ( TAG,"mBroadcast: tentative de connection" );
                        break;

                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d ( TAG,"mBroadcast: impossible detablir une connection" );
                        break;

                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d ( TAG,"mBroadcast: Connection en cours ..." );
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d ( TAG,"mBroadcast: Connection success" );
                        break;

                }

            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            Log.d(TAG , "onreceive : ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                Toast.makeText (MainActivity.this,"Appreil trouvé !",Toast.LENGTH_SHORT).show ();
                BluetoothDevice device = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );
                //ajout des devices trouvés dnas notre liste
                mBTDevices.add(device);
                // partie des elements qui ont deja ete rencontré
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if(pairedDevices.size () > 0 )
                {
                    for(BluetoothDevice periph : pairedDevices)
                    {
                        mBTDevices.add(periph);
                    }
                }
                Log.d(TAG , "onReceives " + device.getName () + " : " + device.getAddress());

                //appel du constructeur de la classe DeviceListAdapter
                mDeviceListAdapter = new DeviceListAdapter ( context , R.layout.device_blt , mBTDevices );
                //intégration de la vue mDeviceListAdapter dans notre ListView
                lvNewDevices.setAdapter ( mDeviceListAdapter );
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context , Intent intent)
        {
            final String action = intent.getAction () ;
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {


                BluetoothDevice mDevice = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );

                // on fait face à trois cas

                // si le composant est deja lié
                if(mDevice.getBondState () == BluetoothDevice.BOND_BONDED)
                {
                    //dans ce cas nous naurons qua nous logger directement
                    Toast.makeText (MainActivity.this,"Connexion success",Toast.LENGTH_SHORT).show ();
                    Log.d(TAG, "Broadcast : BOND_BOUNDED");


                }

                // si cest une nouvelle liaison
                if(mDevice.getBondState () == BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "Broadcast : BOND_BOUNDING");
                    Toast.makeText (MainActivity.this,"Tentative de Connexion avec l'appareil ...",Toast.LENGTH_SHORT).show ();

                }

                // si la liaison est cassé
                if(mDevice.getBondState () == BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "Broadcast : BOND_NONE");
                    Toast.makeText (MainActivity.this,"Impossible de se connecter avec l'appareil",Toast.LENGTH_SHORT).show ();

                }

            }
        }
    };


}
