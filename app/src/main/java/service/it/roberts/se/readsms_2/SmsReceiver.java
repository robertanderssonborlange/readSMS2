package service.it.roberts.se.readsms_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    private static service.it.roberts.se.readsms_2.InterfaceReadSMS smsReader;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        SmsMessage smsMessage;
        CharSequence sender = "";
        StringBuilder messageBody = new StringBuilder();

        for (int i = 0; i < pdus.length; i++) {
             smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

               //smsMessage.

            sender = smsMessage.getDisplayOriginatingAddress();
            //Check the sender to filter messages which we require to read

            messageBody.append(smsMessage.getMessageBody());

        }
        //Pass the message text to interface
        smsReader.smsReceived(sender, messageBody.toString());
    }


    public static void bindListener(service.it.roberts.se.readsms_2.InterfaceReadSMS listener) {
        smsReader = listener;
    }

}
