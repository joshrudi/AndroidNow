package com.example.joshr.androidnow;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.telephony.PhoneStateListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.BatteryManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;
import android.os.StatFs;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.telephony.SignalStrength;
import android.os.Build;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //battery
    private TextView mBattery;
    private ImageView mBatteryCog;
    private TextView mChargeState;
    private ImageView mBatteryImg;

    //storage
    private ProgressBar mStorageBar;
    private ImageView mStorageCog;
    private TextView mStorageUsed;
    private TextView mStorageTotal;

    //network
    private ImageView mNetworkImg;
    private ImageView mNetworkCog;
    private TextView mNetworkOperator;
    private TextView mSignalStrength;
    private int mSignalStrengthLevel = 7;
    private TelephonyManager telephonyManager;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Battery
        mBattery = (TextView)view.findViewById(R.id.battery_level);
        mBatteryImg = (ImageView)view.findViewById(R.id.battery_level_img);
        mChargeState = (TextView)view.findViewById(R.id.charge_state);
        mBatteryCog = (ImageView)view.findViewById(R.id.battery_settings);

        //Storage
        mStorageCog = (ImageView)view.findViewById(R.id.storage_settings);
        mStorageBar = (ProgressBar)view.findViewById(R.id.storage_bar);
        mStorageUsed = (TextView)view.findViewById(R.id.storage_used);
        mStorageTotal = (TextView)view.findViewById(R.id.storage_total);

        //Network
        mNetworkCog = (ImageView)view.findViewById(R.id.network_settings);
        mSignalStrength = (TextView)view.findViewById(R.id.network_strength);
        mNetworkOperator = (TextView)view.findViewById(R.id.network_name);
        mNetworkImg = (ImageView)view.findViewById(R.id.network_level_img);


        //Implement

        mStorageBar.setProgress(82);

        mBatteryCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 0);
            }
        });

        mStorageCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS), 0);
            }
        });

        mNetworkCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
            }
        });


        //global manager to do network related tasks
        telephonyManager  =  ( TelephonyManager )getActivity().getSystemService( Context.TELEPHONY_SERVICE );

        //listen for changes in signal strength
        PhoneStateListener phoneStateListener = new PhoneStateListener() {

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {

                //if Marshmallow or higher, API supports calling .getLevel()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    mSignalStrengthLevel = signalStrength.getLevel();
                }
            }
        };

        // Register the listener for the telephony manager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        updateBatteryLevel();
        updateStorage();
        updateNetwork();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        updateBatteryLevel();
                        updateNetwork();
                    }
                },
                0, 5000
        );

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void updateNetwork() {

        String carrier = telephonyManager.getSimOperatorName();
        String networkOperator = "signal from " + carrier;


        if (carrier.equals("")) {  //if there is no carrier name

            mSignalStrengthLevel = 6;  //set strength to "Off"
            networkOperator = "carrier unavailable";  //tell user carrier is not available
        }


        mNetworkOperator.setText(networkOperator);  //show where signal is from (carrier name)


        //lollipop does not support getLevel(), so only check if active
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (!carrier.equals("")) {  //if there is a carrier name

                mSignalStrengthLevel = 5;  //set strength to "Active"
            }
        }

        String[] signalLevels = new String[] {"Bad", "Poor", "Fair", "Good", "Great", "Active", "Off", "Search"};
        mSignalStrength.setText(signalLevels[mSignalStrengthLevel]);

        //set image representation of signal strength
        if (mSignalStrengthLevel == 0) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_0_bar_24);
        }

        else if (mSignalStrengthLevel == 1) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_1_bar_24);
        }

        else if (mSignalStrengthLevel == 2) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_2_bar_24);
        }

        else if (mSignalStrengthLevel == 3) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_3_bar_24);
        }

        else if (mSignalStrengthLevel == 4) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_4_bar_24);
        }

        else if (mSignalStrengthLevel == 5) {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_4_bar_24);
        }

        else {

            mNetworkImg.setImageResource(R.drawable.round_signal_cellular_0_bar_24);
        }


        //set carrier color (Only supports verizon, t-mobile, sprint and at&t.  All others receive generic blue color
        if (carrier.toLowerCase().equals("verizon wireless") || carrier.toLowerCase().equals("verizon") || carrier.toLowerCase().equals("verizonwireless")) {

            mNetworkImg.setColorFilter(Color.argb(255, 214, 49, 45));
        }

        else if (carrier.toLowerCase().equals("tmobile") || carrier.toLowerCase().equals("t-mobile") || carrier.toLowerCase().equals("tmo") || carrier.toLowerCase().equals("t mobile")) {

            mNetworkImg.setColorFilter(Color.argb(255, 227, 60, 117));
        }

        else if (carrier.toLowerCase().equals("sprint")) {

            mNetworkImg.setColorFilter(Color.argb(255, 249, 206, 72));
        }

        else if (carrier.toLowerCase().equals("att") || carrier.toLowerCase().equals("at&t")) {

            mNetworkImg.setColorFilter(Color.argb(255, 59, 159, 219));
        }

        else {

            mNetworkImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

    }

    public void updateStorage() {

        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());

        long storageSizeBytes = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long storageSizeMegs = storageSizeBytes / 1048576;
        double storageSizeGigs = storageSizeMegs / 1024;

        long storageClearBytes = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long storageClearMegs = storageClearBytes / 1048576;
        double storageClearGigs = storageClearMegs / 1024;

        //Something I came up with to find out the actual "on system" storage

        //find the nearest power by taking log base 2 of the reported storage
        int power = (int) Math.ceil(Math.log(storageSizeGigs)/Math.log(2));

        //then raise 2 to said power (since FLASH storage (and storage in general) is in powers of 2
        int acutalSize = (int) Math.pow(2, power);

        String total = "used of " + String.valueOf(acutalSize) + " GB";
        double percent = (((double)acutalSize - storageClearGigs)/(double)acutalSize)*100;

        mStorageTotal.setText(total);
        mStorageUsed.setText(String.valueOf(acutalSize-storageClearGigs));
        mStorageBar.setProgress((int)percent);
    }

    public void updateBatteryLevel() {

        Context context = getContext();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;

        batteryPct = batteryPct*100;

        String batteryLvl = String.valueOf((int)batteryPct);

        batteryLvl += "%";

        mBattery.setText(batteryLvl);

        if (batteryPct == 100) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_full_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 90) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_90_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 80) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_80_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 60) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_60_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 50) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_50_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 30) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_30_24);
            mBatteryImg.setColorFilter(Color.argb(255, 28, 112, 228));
        }

        else if (batteryPct >= 0) {

            mBatteryImg.setImageResource(R.drawable.baseline_battery_20_24);
            mBatteryImg.setColorFilter(Color.argb(255, 255, 64, 129));
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

        if (isCharging) {

            mChargeState.setText("Phone is plugged in");
        }

        else {

            mChargeState.setText("Phone is unplugged");
        }
    }
}
