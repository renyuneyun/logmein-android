package in.shubhamchaudhary.logmein.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;

/**
 * Created by tanjot on 19/9/14.
 */
public class DialogAlert extends DialogFragment{
/*Interface has been used to return response from this dialog to the calling activity
* The Calling activity implements these functions */
    public interface ReturnDialogMessage{
        public void onClickPositive(String username,String password);
        public void onClickNegative();
    }
    ReturnDialogMessage returnDialogMessage;


    AlertDialog.Builder builder;
    String title = "Alert!!!";//""+R.string.alert_title;
    String message = "Do you want to proceed";//""+R.string.alert_message;
    String positive_message = "YES";//""+R.string.alert_positive_message;
    String negative_message = "NO";//""+R.string.alert_negative_message;
    Button button_update, button_cancel;
    EditText textbox_username = null, textbox_password = null;
    String username="",password="";
    View v;
    Boolean initialized_flag;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            returnDialogMessage = (ReturnDialogMessage) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ReturnDialogListener");
        }
    }

   //You can set title,message and positive and negative button strings as per your requirement
    public void setAlertStrings(String tit, String msg,String pos_msg, String neg_msg){
        this.title = tit;
        this.message = msg;
        this.positive_message = pos_msg;
        this.negative_message = neg_msg;
    }

    public void fill_textboxes(String username, String password){
        this.username = username;
        this.password = password;
    }//end of fill_textboxes

    public void initialize(){
        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.fragment_edit_layout, null);
        button_update = (Button) v.findViewById(R.id.button_edit_save);
        textbox_username = (EditText) v.findViewById(R.id.edit_username);
        textbox_password = (EditText) v.findViewById(R.id.edit_password);
        button_cancel = (Button) v.findViewById(R.id.button_edit_cancel);
        initialized_flag = true;

        textbox_username.setText(username);
        textbox_password.setText(password);

    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        initialize();
        builder.setView(v).setTitle(title).setMessage(message);

        button_update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String username = ""+textbox_username.getText();
                String password = ""+textbox_password.getText();
                returnDialogMessage.onClickPositive(username,password);
            }
        });
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnDialogMessage.onClickNegative();
            }
        });
//
//                    .setPositiveButton(positive_message,new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    returnDialogMessage.onClickPositive();
//                }
//            })
//            .setNegativeButton(negative_message, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    returnDialogMessage.onClickNegative();
//                }
//            });

            return builder.create();

        }

    }
