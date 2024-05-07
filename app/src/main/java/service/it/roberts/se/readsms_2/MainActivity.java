package service.it.roberts.se.readsms_2;


import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.content.pm.PackageManager.FLAG_PERMISSION_WHITELIST_INSTALLER;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static InterfaceReadSMS myListener;
    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    private static TextToSpeech mTts = null;
    private final int MY_DATA_CHECK_CODE = 2020;

    private Boolean checkOnlyConatcs;
    private Boolean checkSmsPermissions = true;
    private Boolean checkContacsPermissions = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // init TTS and read the default welcome message
        if (mTts == null) {
            checkTTS();
        }


        if (checkPermissions()) {
       // Checking runtime permissions
        }

        Switch sw1 = (Switch) findViewById(R.id.switch1);

        sw1.setChecked(false);
        checkOnlyConatcs = false;

        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    checkOnlyConatcs = true;
                } else {
                    checkOnlyConatcs = false;
                }
            }
        });


        SmsReceiver.bindListener(new InterfaceReadSMS() {
            @Override
            public void smsReceived(CharSequence sender, CharSequence smsText) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if(!checkSmsPermissions) {
                    Toast.makeText(getApplicationContext(), R.string.noSMS, Toast.LENGTH_LONG).show();
                } else if (checkContacsPermissions) {

                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender.toString()));
                    Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                    String contactName = "";

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            contactName = cursor.getString(0);

                            mTts.speak( " " + contactName + " " + smsText, TextToSpeech.QUEUE_FLUSH, null);

                        } else {
                            if (!checkOnlyConatcs) {
                                String tel = "";

                                for (int i = 0; i < sender.length(); i++) {
                                    tel += sender.charAt(i) + " ";
                                }

                                mTts.speak(" " + tel + " " + smsText, TextToSpeech.QUEUE_FLUSH, null);


                            }
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noContact, Toast.LENGTH_LONG).show();

                    String tel = "";

                    for (int i = 0; i < sender.length(); i++) {
                        tel += sender.charAt(i) + " ";
                    }
                    mTts.speak(" " + tel + " " + smsText, TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });

    }


    public static void bindListener(InterfaceReadSMS listener) {
        myListener = listener;
    }


    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            mTts.setLanguage(Locale.getDefault());

        } else {
            Toast.makeText(this, R.string.errorTTS, Toast.LENGTH_LONG).show();
        }
    }

    // send an intent (message) to an activity to check if TTS is available
    private void checkTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        } catch (Exception ActivityNotFoundException) {
            Toast.makeText(this, R.string.errorTTS, Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
            System.exit(0);
        }
        }

    // the TTS check result, if TTS is available

        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // sucess, create the TTS intance whick automatically call the
                // TextToSpeech.OnInitListener when the TextToSpeech engine has initialized.
                mTts = new TextToSpeech(this, this);
            } else {
                //missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.shutdown();
            super.onDestroy();
        }
    }

    public void onClickEnd(View view) {
        MainActivity.this.finish();
        System.exit(0);
    }


    private boolean checkPermissions() {
        int permissionReceiveSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS);
        int permissionReadSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);
        int permissionReadContacts = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);



        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionReceiveSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if (permissionReadSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (permissionReadContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_CODE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.RECEIVE_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for all permissions
                    if (perms.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                        // process the normal flow
                        //else any one or both the permissions are not granted
                        checkContacsPermissions = true;
                        checkSmsPermissions = true;
                    } else {


                        if (perms.get(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                                || perms.get(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                            checkSmsPermissions = false;
                        } else if (perms.get(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            checkContacsPermissions = false;
                        }


                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                            showDialogOK(R.string.permissionsInfo,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, R.string.permissionsInfo, Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(int message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, okListener)
                .create()
                .show();
    }


}
