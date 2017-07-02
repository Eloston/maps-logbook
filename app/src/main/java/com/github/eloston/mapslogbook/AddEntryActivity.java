package com.github.eloston.mapslogbook;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddEntryActivity extends AppCompatActivity {
    private EditText entryNameEditText;
    private Entry userEntry;
    private String enteredName;
    private int userColor;

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)this.getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void makeResult(int result) {
        // TODO: Refactor to consider case when activity isn't started from MainActivity
        Intent i = new Intent();
        if (result == this.RESULT_OK) {
            this.userEntry.setName(this.enteredName);
            this.userEntry.setColor(this.userColor);
            i.putExtra(MainActivity.ENTRY_EXTRA, this.userEntry);
        }
        this.setResult(result, i);
    }

    /**
     * Checks to see if any of the fields have been modified by the user since
     * the activity's instantiation.
     *
     * @return true if any one field has been modified; false otherwise
     */
    private boolean areFieldsModified() {
        // TODO
        return true;
    }

    /**
     * Saves the current values of all fields
     */
    private void saveChanges() {
        // TODO
    }

    /**
     * Presents a dialog asking if the user wants to confirm exiting, and
     * carries out exiting if the user does want to.
     *
     * @param exitCallback is used to perform the exit if the user chooses
     */
    private void confirmExit(DialogInterface.OnClickListener exitCallback) {
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.changes_made_title)
            .setMessage(R.string.changes_made_message)
            .setPositiveButton(R.string.yes, exitCallback)
            .setNegativeButton(R.string.no, null)
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String theme = this.getSharedPreferences(MainActivity.THEME_PREFERENCES, this.MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
        if (theme.equals(MainActivity.LIGHTTHEME)) {
            this.setTheme(R.style.CustomStyle_LightTheme);
        } else {
            this.setTheme(R.style.CustomStyle_DarkTheme);
        }

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_entry);

        // Show an X in place of <-
        final Drawable cross = this.getResources().getDrawable(R.drawable.ic_clear_white_24dp);
        if (cross != null) {
            cross.setColorFilter(this.getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_ATOP);
        }

        this.setSupportActionBar((Toolbar)this.findViewById(R.id.toolbar));

        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setElevation(0);
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            this.getSupportActionBar().setHomeAsUpIndicator(cross);
        }

        this.userEntry = (Entry)this.getIntent().getSerializableExtra(MainActivity.ENTRY_EXTRA);
        if ((Boolean)this.getIntent().getSerializableExtra(MainActivity.ENTRY_NEW_EXTRA)) {
            this.getSupportActionBar().setTitle(this.getResources().getString(R.string.add_new_entry));
        } else {
            this.getSupportActionBar().setTitle(this.getResources().getString(R.string.edit_entry));
        }

        this.enteredName = this.userEntry.getName();
        this.userColor = this.userEntry.getColor();

        this.entryNameEditText = (EditText)findViewById(R.id.entryNameEditText);

        // Setup name field content and hooks
        this.entryNameEditText.setText(this.enteredName);
        this.entryNameEditText.setSelection(this.entryNameEditText.length());

        this.entryNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AddEntryActivity.this.enteredName = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Setup entry type spinner
        Spinner entryTypeSpinner = (Spinner) this.findViewById(R.id.entryTypeSpinner);
        //ArrayAdapter<CharSequence> entryTypeAdapter = ArrayAdapter.createFromResource(
        //    this,
        //    R.array.url_type_array,
        //    android.R.layout.simple_spinner_item
        //);
        //entryTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //entryTypeSpinner.setAdapter(entryTypeAdapter);

        ((FloatingActionButton)this.findViewById(R.id.saveEntryFloatingActionButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (AddEntryActivity.this.entryNameEditText.length() <= 0){
                    AddEntryActivity.this.entryNameEditText.setError(getString(R.string.field_required_error));
                } // TODO: Check length of URL field, and then check if it's a valid URL
                else{
                    AddEntryActivity.this.makeResult(RESULT_OK);
                    AddEntryActivity.this.finish();
                }
                AddEntryActivity.this.hideKeyboard();
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.confirmExit(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddEntryActivity.this.makeResult(AddEntryActivity.this.RESULT_CANCELED);
                AddEntryActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.confirmExit(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (NavUtils.getParentActivityName(AddEntryActivity.this) != null) {
                            AddEntryActivity.this.makeResult(AddEntryActivity.this.RESULT_CANCELED);
                            NavUtils.navigateUpFromSameTask(AddEntryActivity.this);
                        }
                        AddEntryActivity.this.hideKeyboard();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

