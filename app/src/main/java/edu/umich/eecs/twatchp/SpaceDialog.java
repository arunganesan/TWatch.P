package edu.umich.eecs.twatchp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Wifi address chooser dialog
 */
public class SpaceDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Please set the space");
        final EditText textView = new EditText(getActivity());
        textView.setInputType(InputType.TYPE_CLASS_NUMBER);
        textView.setText("" + C.CHIRPSPACE);
        textView.setGravity(Gravity.CENTER);
        builder.setView(textView);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                C.CHIRPSPACE = new Integer(textView.getText().toString());
                SharedPreferences sp = getActivity().getSharedPreferences("twatch", Context.MODE_PRIVATE);
                sp.edit().putInt(C.SPACE_KEY, C.CHIRPSPACE).commit();
                Toast.makeText(getActivity(), "Setting space to " + C.CHIRPSPACE, Toast.LENGTH_SHORT).show();
                mListener.doneSettingSpace();
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }


    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SpaceDialogListener {
        void doneSettingSpace();
    }


    // Use this instance of the interface to deliver action events
    SpaceDialogListener mListener;



    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SpaceDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

}
