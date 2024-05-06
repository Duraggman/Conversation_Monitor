package com.example.convo_monitor;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;
import android.text.InputFilter;
import android.text.Spanned;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class UiController {
    private ParticipantManager pm;
    private VoskProvider vosk;
    private AppUtils utils;


    private final Spinner userDD;
    private final Button recordButton;
    private final Button nConvoButton;
    private final Button resetConvoButton;
    private final Button addButton;
    private final Button saveButton;
    private final Button recordIdButton;

    private final TextView transTextView;
    private final TextView UserTextView;
    private final TextInputEditText tagInputView;
    private ConstraintLayout topLayout;
    private FrameLayout midLayout;
    private ConstraintLayout bottomLayout;
    private TextInputLayout tagLayout;
    private int participantCount;
    
    private final Activity act;
    

    public UiController(Activity a, VoskProvider vosk, AppUtils utils) {
        this.act = a;
        this.vosk = vosk;
        this.utils = utils;


        // array for user count spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this.act, android.R.layout.simple_spinner_item, new Integer[]{1, 2, 3, 4});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userDD = this.act.findViewById(R.id.usersDD);
        userDD.setAdapter(adapter);

        recordButton = this.act.findViewById(R.id.recBtn);
        nConvoButton = this.act.findViewById(R.id.newConvoBtn);
        resetConvoButton = this.act.findViewById(R.id.resetConvoBtn);
        addButton = this.act.findViewById(R.id.addBtn);
        saveButton = this.act.findViewById(R.id.saveBtn);
        recordIdButton = this.act.findViewById(R.id.recIdBtn);

        transTextView = this.act.findViewById(R.id.TranscribeView);
        UserTextView = this.act.findViewById(R.id.UserText);
        tagInputView = this.act.findViewById(R.id.tagInput);

        topLayout = this.act.findViewById(R.id.topLayout);
        midLayout = this.act.findViewById(R.id.midLayout);
        bottomLayout = this.act.findViewById(R.id.botLayout);
        tagLayout = this.act.findViewById(R.id.tagLayout);

        //apply alphanumeric filter to tagInputView
        applyAlphanumericFilter(tagInputView);

        // Set Listener
        setListeners();

        // Hide all views
        hideAllViews();
    }
    /**
     * Sets up the ui when i is first opened
     */
    public void createUI() {
        // show bottom layout
        bottomLayout.setVisibility(View.VISIBLE);
        //show and enable the nConvoButton
        nConvoButton.setVisibility(View.VISIBLE);
        nConvoButton.setEnabled(true);
    }

    public void setNConvoListener() {
        // Set up nConvoButton listener
        nConvoButton.setOnClickListener(v -> {
            //disable nConvoButton
            nConvoButton.setEnabled(false);

            // when button clicked, show and enable: top layout, spinner, save button, and  UserText
            topLayout.setVisibility(View.VISIBLE);
            userDD.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            UserTextView.setVisibility(View.VISIBLE);
            saveButton.setEnabled(true);
        });
    }

    public void setSaveListener() {
        // Set up saveButton listener
        saveButton.setOnClickListener(v -> {
            //hide and disable saveButton, spinner, and set userText
            userDD.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setEnabled(false);

            // init participantManager
            pm = new ParticipantManager((Integer)userDD.getSelectedItem(), vosk, this);

            UserTextView.setText("Participant:" + userDD.getSelectedItem().toString()); // add participant logic later

            // show and enable tagLayout, tagInput and RecordIdButton
            tagLayout.setVisibility(View.VISIBLE);
            tagInputView.setVisibility(View.VISIBLE);
            recordIdButton.setVisibility(View.VISIBLE);
        });
    }

    public void setTagInputViewListener() {
        tagInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used here
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Enable button only if the text length is exactly 3 characters
                recordIdButton.setEnabled(s.length() == 3);
            }
        });
    }

    public void setRecordIdButtonListener() {
        recordIdButton.setOnClickListener(v -> {
            //hide and disable tagLayout, tagInput, only disable recordIdButton
            tagInputView.setEnabled(false);
            recordIdButton.setEnabled(false);
            pm.recordParticipantID();



        });
    }


    public void setListeners() {
        setNConvoListener();
        setSaveListener();
        setTagInputViewListener();
        setRecordIdButtonListener();

    }

    public void hideAllViews() {
        userDD.setVisibility(View.INVISIBLE);
        recordButton.setVisibility(View.INVISIBLE);
        nConvoButton.setVisibility(View.INVISIBLE);
        resetConvoButton.setVisibility(View.INVISIBLE);
        addButton.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);
        recordIdButton.setVisibility(View.INVISIBLE);
        transTextView.setVisibility(View.INVISIBLE);
        tagInputView.setVisibility(View.INVISIBLE);
        UserTextView.setVisibility(View.INVISIBLE);

        // If you want to disable buttons:
        recordButton.setEnabled(false);
        nConvoButton.setEnabled(false);
        resetConvoButton.setEnabled(false);
        addButton.setEnabled(false);
        saveButton.setEnabled(false);
        recordIdButton.setEnabled(false);

        //hide layouts
        topLayout.setVisibility(View.INVISIBLE);
        midLayout.setVisibility(View.INVISIBLE);
        bottomLayout.setVisibility(View.INVISIBLE);
        tagLayout.setVisibility(View.INVISIBLE);
    }


    // Getter methods
    public Spinner getUserDD() {
        return userDD;
    }

    public Button getRecButton() {
        return recordButton;
    }

    public Button getNConvoButton() {
        return nConvoButton;
    }

    public Button getResetConvoButton() {
        return resetConvoButton;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getRecordIdButton() {
        return recordIdButton;
    }

    public TextView getTransTextView() {
        return transTextView;
    }

    public TextView getTagInputView() {
        return tagInputView;
    }

    public TextView getUserTextView() {
        return UserTextView;
    }

    public ConstraintLayout getTopLayout() {
        return topLayout;
    }

    public FrameLayout getMidLayout() {
        return midLayout;
    }

    public ConstraintLayout getBottomLayout() {
        return bottomLayout;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    // Update text methods
    public void updateTransTextView(String text) {
        transTextView.setText(text);
    }

    public void updateUserTextView(String text) {
        UserTextView.setText(text);
    }

    public void applyAlphanumericFilter(TextInputEditText editText) {
        InputFilter alphanumericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";  // Block non-alphanumeric characters
                    }
                }
                return null;  // Accept the original input
            }
        };

        // Retrieve the current set of filters
        InputFilter[] currentFilters = editText.getFilters();
        InputFilter[] newFilters;

        // Check if there are any existing filters
        if (currentFilters != null) {
            newFilters = new InputFilter[currentFilters.length + 1];
            System.arraycopy(currentFilters, 0, newFilters, 0, currentFilters.length);
            newFilters[currentFilters.length] = alphanumericFilter;
        } else {
            newFilters = new InputFilter[] { alphanumericFilter };
        }

        // Set the new array of filters to the editText which includes both existing and new filters
        editText.setFilters(newFilters);
    }
}