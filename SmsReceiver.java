package com.yitzhak.packtrackil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");
                if (pdus != null) {
                    StringBuilder fullText = new StringBuilder();
                    for (Object pdu : pdus) {
                        SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu, format);
                        if (msg != null && msg.getMessageBody() != null) {
                            fullText.append(msg.getMessageBody());
                        }
                    }
                    Intent local = new Intent("com.yitzhak.packtrackil.NEW_SMS");
                    local.putExtra("sms_body", fullText.toString());
                    context.sendBroadcast(local);
                }
            }
        }
    }
}
