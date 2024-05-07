package service.it.roberts.se.readsms_2;

/**
 * Created by robba on 2017-04-06.
 */

public interface InterfaceReadSMS {
     void smsReceived (CharSequence sender, CharSequence smsText);
}
