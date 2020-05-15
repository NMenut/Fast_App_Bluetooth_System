package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices ;
    private int mViewRessourceId ;


    public DeviceListAdapter(Context context , int tvRessourceId, ArrayList<BluetoothDevice> devices)
    {
        super(context , tvRessourceId,devices);
        this.mDevices = devices ;

        mLayoutInflater = (LayoutInflater) context.getSystemService ( Context.LAYOUT_INFLATER_SERVICE );
        mViewRessourceId = tvRessourceId ;
    }

    public View getView(int position,View convertView,ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate ( mViewRessourceId, null );

        BluetoothDevice device = mDevices.get(position);

        if(device != null)
        {
            TextView deviceName = (TextView) convertView.findViewById ( R.id.tvDevicename );
            TextView deviceAdress = (TextView) convertView.findViewById ( R.id.tvDeviceAdress );

            if (deviceName != null)
            {
                deviceName.setText(device.getName ());
            }

            if(deviceAdress != null)
            {
                deviceAdress.setText ( device.getAddress () );
            }

        }
        return convertView ;
    }
}
