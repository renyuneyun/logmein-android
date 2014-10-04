package in.shubhamchaudhary.logmein.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;
import in.shubhamchaudhary.logmein.UserStructure;

/**
 * Created by tanjot on 4/10/14.
 */
public class ManagerUserServices {

//    MainActivity context;
    Context context;
    DatabaseEngine databaseEngine;
    String username;
    Boolean add_update;
    View v;
    Button button_update, button_cancel;
    EditText textbox_username = null, textbox_password = null;
    CheckBox cb_show_pwd;


    ManagerUserServices(Context context){
        this.context = context;
        databaseEngine = DatabaseEngine.getInstance(this.context);
    }

    public void initialise(LayoutInflater inflater){
        v = inflater.inflate(R.layout.alert_dialog, null);

//        button_update = (Button) v.findViewById(R.id.button_edit_save);
//        button_cancel = (Button) v.findViewById(R.id.button_edit_cancel);
        textbox_username = (EditText) v.findViewById(R.id.edit_username);
        textbox_password = (EditText) v.findViewById(R.id.edit_password);
        cb_show_pwd = (CheckBox) v.findViewById(R.id.cb_show_password);

        cb_show_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_password();
            }
        });
    }

    public void add_update(String un,String pwd){

        if( un.isEmpty()){
            Toast.makeText(this.context,"Username cannot be an empty string",Toast.LENGTH_LONG).show();
            return;
        }
        if( pwd.isEmpty()){
            Toast.makeText(this.context,"Password cannot be an empty string",Toast.LENGTH_LONG).show();
            return;
        }
        UserStructure userStructure = new UserStructure();
        userStructure.setUsername(un);
        userStructure.setPassword(pwd);

        if(add_update){
            saveCredential(userStructure);
        }else{
            updateCredentials(userStructure);
        }
    }

    void saveCredential(UserStructure userStructure) {

        if(!databaseEngine.existsUser(userStructure.getUsername())){
            if(databaseEngine.insert(userStructure)){
                Toast.makeText(this.context, userStructure.getUsername() + " entered into your inventory", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.context," problem inserting record", Toast.LENGTH_SHORT).show();
            }

        } else{
            Toast.makeText(this.context,"Username already exists", Toast.LENGTH_SHORT).show();
        }

    }//end saveCredential

    public void updateCredentials(UserStructure userStructure){
        int i = databaseEngine.updateUser(userStructure, username);
        if (i == 1) {
            Log.e("Updated", "Updated user");
            Toast.makeText(this.context, "Updated account", Toast.LENGTH_SHORT).show();
        } else if (i == 0) {
            Toast.makeText(this.context, "Problem in updating account", Toast.LENGTH_SHORT).show();
            Log.e("Updated", "Error updating");
        } else {
            Toast.makeText(this.context, "Updated more than 1 records", Toast.LENGTH_SHORT).show();
            Log.e("Updated", "Updated more than 1 records");
        }

    }//end of updateCredentials

    public void update(String un,LayoutInflater inflater){
        this.username = un;
        initialise(inflater);
        textbox_username.setText(un);
        UserStructure us = databaseEngine.getUsernamePassword(un);
        textbox_password.setText(us.getPassword());


        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setView(v)
               .setTitle("Update user")
               .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       add_update = false;
                       add_update(textbox_username.getText().toString(), textbox_password.getText().toString());
                   }
               })
        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context, "Activity cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();

    }//end of edit

    public void add(LayoutInflater inflater){
        initialise(inflater);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setView(v)
               .setTitle("Add User")
               .setPositiveButton("SAVE",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                add_update = true;
                add_update(textbox_username.getText().toString(),textbox_password.getText().toString());
            }
        })
        .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context,"Activity cancelled",Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();

    }
    public void delete(String un) {
        this.username = un;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + username)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //String username = spinner_user_list.getSelectedItem().toString();
                        Boolean updated = databaseEngine.deleteUser(username);
                        if ( updated ){
                            Toast.makeText(context, "Successfully deleted user: " + username, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context,"Problem deleting user: "+username,Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context,"Cancelled",Toast.LENGTH_SHORT).show();
                    }
                });

        builder.create().show();
    }

    public void show_password() {
        if (cb_show_pwd.isChecked()) {
            textbox_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            return;
        }
        textbox_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }//end of show_password(View)

}

