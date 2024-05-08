package com.example.convo_monitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;
import android.text.InputFilter;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@SuppressLint("SetTextI18n")
public class UiController {
    private ParticipantManager pm;
    private final VoskProvider vosk;
    private final VoskTranscriber vt;
    private Spinner userDD;
    private final Button recordButton;
    private final Button nConvoButton;
    private final Button resetConvoButton;
    private final Button addButton;
    private final Button saveButton;
    private final Button recordIdButton;
    private final TextView transTextView;
    private final TextView userTextView;
    private final TextInputEditText tagInputView;
    private final ConstraintLayout topLayout;
    private final FrameLayout midLayout;
    private final ConstraintLayout bottomLayout;
    private final TextInputLayout tagLayout;
    private final Activity act;
    private List<Integer> spinnerItems;
    private ArrayAdapter<Integer> adapter;
    private int pressed;

    public UiController(Activity a, VoskProvider vosk, VoskTranscriber vt) {
        this.act = a;
        this.vosk = vosk;
        this.vt = vt;
        AppUtils.isFirstTime = true;

        setupSpinner();

        this.recordButton = this.act.findViewById(R.id.recBtn);
        this.nConvoButton = this.act.findViewById(R.id.newConvoBtn);
        this.resetConvoButton = this.act.findViewById(R.id.resetConvoBtn);
        this.addButton = this.act.findViewById(R.id.addBtn);
        this.saveButton = this.act.findViewById(R.id.saveBtn);
        this.recordIdButton = this.act.findViewById(R.id.recIdBtn);

        this.transTextView = this.act.findViewById(R.id.TranscribeView);
        this.userTextView = this.act.findViewById(R.id.UserText);
        this.tagInputView = this.act.findViewById(R.id.tagInput);

        this.topLayout = this.act.findViewById(R.id.topLayout);
        this.midLayout = this.act.findViewById(R.id.midLayout);
        this.bottomLayout = this.act.findViewById(R.id.botLayout);
        this.tagLayout = this.act.findViewById(R.id.tagLayout);

        //apply alphanumeric filter to this.tagInputView
        applyAlphanumericFilter(this.tagInputView);

        // Hide all views
        hideAllViews();
    }
    /**
     * Sets up the ui when i is first opened
     */
    public void createUI() {
        // show bottom layout
        this.bottomLayout.setVisibility(View.VISIBLE);
        //show and enable the this.nConvoButton
        this.nConvoButton.setVisibility(View.VISIBLE);
        this.nConvoButton.setEnabled(true);
    }

    private void setNConvoListener() {
        // Set up this.nConvoButton listener
        this.nConvoButton.setOnClickListener(v -> {
            //disable this.nConvoButton
            this.nConvoButton.setEnabled(false);

            // when button clicked, show and enable: top layout, spinner, save button, and  UserText
            this.topLayout.setVisibility(View.VISIBLE);
            this.userDD.setVisibility(View.VISIBLE);
            this.userDD.setEnabled(true);
            this.saveButton.setVisibility(View.VISIBLE);
            this.saveButton.setEnabled(true);
            this.userTextView.setVisibility(View.VISIBLE);
            this.userTextView.setEnabled(true);
        });
    }

    private void setRecordIdButtonListener() {
        this.recordIdButton.setOnClickListener(v -> {
            //hide and disable tagLayout, tagInput, only disable this.recordIdButton
            this.tagInputView.setEnabled(false);
            this.recordIdButton.setEnabled(false);
            this.pm.recordParticipantId();
        });
    }

    private void setTagInputViewListener() {
        Button recIdBtn = this.recordIdButton;
        this.tagInputView.addTextChangedListener(new TextWatcher() {
            
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
                // Enable button only if the text length is exactly 3 characters and the tag is not the same as any of the participants
                if (pm != null) {
                    
                    boolean uniqueTag = true;
                    for (ParticipantManager.Participant participant : pm.participants) {
                        if (participant != null && participant.tagText().equals(s.toString())) {
                            uniqueTag = false;
                            break;
                        }
                    }
                    if (!uniqueTag) {
                        recIdBtn.setText("tag already used");
                    }
                    else {
                        recIdBtn.setText("Record ref audio(5secs)");
                    }
                    recIdBtn.setEnabled(uniqueTag && s.length() == 3);
                }
            }
        });
    }

    public void mainConvoUi(){
        // reset recognizer
        this.vosk.getRecognizer().reset();

        // hide and disable top layout and it's children
        this.topLayout.setVisibility(View.INVISIBLE);
        this.saveButton.setEnabled(false);
        this.saveButton.setVisibility(View.INVISIBLE);
        this.userDD.setVisibility(View.INVISIBLE);
        this.userDD.setEnabled(false);
        this.userTextView.setVisibility(View.INVISIBLE);
        this.userTextView.setEnabled(false);
        this.tagLayout.setVisibility(View.INVISIBLE);
        this.tagLayout.setEnabled(false);
        this.tagInputView.setVisibility(View.INVISIBLE);
        this.tagInputView.setEnabled(false);

        // show and enable bottom and mid layout and their children
        //Bottom layout
        this.bottomLayout.setVisibility(View.VISIBLE);
        this.nConvoButton.setVisibility(View.VISIBLE);
        this.nConvoButton.setEnabled(false);
        this.resetConvoButton.setVisibility(View.VISIBLE);
        this.resetConvoButton.setEnabled(true);
        this.addButton.setVisibility(View.VISIBLE);

        this.addButton.setEnabled(this.pm.pCount < 4);

        this.recordButton.setVisibility(View.VISIBLE);
        this.recordButton.setEnabled(true);

        this.recordIdButton.setVisibility(View.INVISIBLE);
        this.recordIdButton.setEnabled(false);
        this.transTextView.setVisibility(View.VISIBLE);

        //Mid layout
        this.midLayout.setVisibility(View.VISIBLE);
        this.midLayout.setEnabled(true);
        this.transTextView.setVisibility(View.VISIBLE);
        this.transTextView.setEnabled(true);
        this.transTextView.setText("Transcription will appear here");
    }

    private void setRecordButtonListener() {
        this.recordButton.setOnClickListener(v -> {
            if (AppUtils.isRecording) {
                // when recording other function buttons are disabled
                if (this.pm.pCount < 4){
                    this.addButton.setEnabled(true);
                }

                this.nConvoButton.setEnabled(true);
                this.resetConvoButton.setEnabled(true);

                this.recordButton.setText(R.string.startRecording);
                this.vt.stopRecording();
            } else {
                // when recording other function buttons are disabled
                this.addButton.setEnabled(false);
                this.nConvoButton.setEnabled(false);
                this.resetConvoButton.setEnabled(false);

                this.recordButton.setText(R.string.stopRecording);
                this.vt.startRecording(this.transTextView);
            }
        });
    }

    private void setAddButtonListener() {
        this.addButton.setOnClickListener(v -> {
            // disable add button record button and reset button
            if (this.pm != null) {
                if (this.pm.pCount < 4) {
                    this.addButton.setEnabled(false);
                    this.recordButton.setEnabled(false);
                    this.resetConvoButton.setEnabled(false);
                    this.nConvoButton.setEnabled(false);
                    addParticipant();
                }
            }
        });
    }
    public void addParticipant() {
        // when button clicked, show and enable: top layout, spinner, save button, and  UserText
        if (this.pm.pCount > 0) {
            this.topLayout.setVisibility(View.VISIBLE);
            try {
                updateSpinnerBasedOnCurrentUsers();// remove items back of spinner based off of pCount
            } catch (Exception e) {
                Log.e("ui", "Error: " + e.getMessage(), e);
            }
        }
        this.userDD.setVisibility(View.VISIBLE);
        this.userDD.setEnabled(true);
        this.saveButton.setVisibility(View.VISIBLE);
        this.userTextView.setText("how many participants do you want to add?");
        this.userTextView.setVisibility(View.VISIBLE);
        this.userTextView.setEnabled(true);
        this.saveButton.setEnabled(true);
        this.tagLayout.setVisibility(View.INVISIBLE);
        this.tagLayout.setEnabled(false);
        this.recordIdButton.setVisibility(View.INVISIBLE);
        this.recordIdButton.setEnabled(false);
    }
    public void addNextParticipant(){
        if (pm.pCount == 4) {
            this.addButton.setEnabled(false); // disable add button if pCount is 4
            this.addButton.setText("Max participants");
        }
        this.userTextView.setText("Participant: "+ (this.pm.pCount+1));

        // show and enable tagLayout, tagInput and RecordIdButton
        this.tagLayout.setVisibility(View.VISIBLE);
        // Clear the this.tagInputView
        this.tagInputView.setText("");
        this.tagInputView.setEnabled(true);
        this.tagInputView.setVisibility(View.VISIBLE);
        this.recordIdButton.setVisibility(View.VISIBLE);
        this.recordIdButton.setText("Record ref audio(5secs)");
    }

    public void setResetConvoListener() {
        this.pressed = 0;
        // Set up this.resetConvoButton listener
        this.resetConvoButton.setOnClickListener(v -> {
            if(this.pressed == 0) {
                this.resetConvoButton.setText("Press again to confirm");
                pressed++;
            }
            else if (this.pressed == 1) {
                //reset the participant manager
                this.pm = null;

                //reset Recognizer
                this.vosk.getRecognizer().reset();

                // reset first time
                AppUtils.isFirstTime = true;

                //reset spinner
                setupSpinner();

                //reset Ui
                hideAllViews();
                createUI();
                this.pressed = 0;
                this.resetConvoButton.setText("Reset Conversation");
            }
        });
    }

    private void setSaveListener() {
        // Set up this.saveButton listener
        this.saveButton.setOnClickListener(v -> {
            if (AppUtils.isFirstTime) { // if first time, init participantManager
                //hide and disable this.saveButton, spinner, and set userText
                this.userDD.setVisibility(View.INVISIBLE);
                this.saveButton.setVisibility(View.INVISIBLE);
                this.saveButton.setEnabled(false);
                // init participantManager
                this.pm = new ParticipantManager((Integer) this.userDD.getSelectedItem(), vosk, this);
                addNextParticipant();
            } else { // if not first time, add new participant
                this.saveButton.setVisibility(View.INVISIBLE);
                this.saveButton.setEnabled(false);
                this.userDD.setVisibility(View.INVISIBLE);
                this.pm.newPCount = (Integer) this.userDD.getSelectedItem();
                addNextParticipant();
            }
        });
    }

    public void setListeners() {
        setNConvoListener();
        setSaveListener();
        setTagInputViewListener();
        setRecordIdButtonListener();
        setRecordButtonListener();
        setAddButtonListener();
        setResetConvoListener();
    }
    public void hideAllViews() {
        this.userDD.setVisibility(View.INVISIBLE);
        this.recordButton.setVisibility(View.INVISIBLE);
        this.nConvoButton.setVisibility(View.INVISIBLE);
        this.resetConvoButton.setVisibility(View.INVISIBLE);
        this.addButton.setVisibility(View.INVISIBLE);
        this.saveButton.setVisibility(View.INVISIBLE);
        this.recordIdButton.setVisibility(View.INVISIBLE);
        this.transTextView.setVisibility(View.INVISIBLE);
        this.tagInputView.setVisibility(View.INVISIBLE);
        this.userTextView.setVisibility(View.INVISIBLE);

        // If you want to disable buttons:
        this.recordButton.setEnabled(false);
        this.nConvoButton.setEnabled(false);
        this.resetConvoButton.setEnabled(false);
        this.addButton.setEnabled(false);
        this.saveButton.setEnabled(false);
        this.recordIdButton.setEnabled(false);

        //hide layouts
        this.topLayout.setVisibility(View.INVISIBLE);
        this.midLayout.setVisibility(View.INVISIBLE);
        this.bottomLayout.setVisibility(View.INVISIBLE);
        this.tagLayout.setVisibility(View.INVISIBLE);
    }

    // Getters
    public Button getRecordIdButton() {
        return this.recordIdButton;
    }
    public TextView getTagInputView() {
        return this.tagInputView;
    }
    private void setupSpinner() {
        this.spinnerItems = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

        // array for user count spinner
        this.adapter = new ArrayAdapter<>(this.act, android.R.layout.simple_spinner_item, spinnerItems);
        this.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.userDD = this.act.findViewById(R.id.usersDD);
        this.userDD.setAdapter(adapter);
    }
    private void updateSpinnerBasedOnCurrentUsers() {
        int currentSize = this.spinnerItems.size(); // Get current number of items
        Log.i("ui", "currentSize: " + currentSize);
        int itemsToRemove =  currentSize - this.pm.pCount;// Number of items to remove from the end based on pCount
        Log.i("ui", "itemsToRemove: " + itemsToRemove);

        if (itemsToRemove > 0 && itemsToRemove < currentSize) {
            int cSize = currentSize -1;
            Log.i("ui", "cSize: " + cSize);
            // Remove the specified number of items from the end of the list

            for (int r = 0; r < itemsToRemove; r++) {
                this.spinnerItems.remove(cSize);// Remove last item each iteration
                cSize--;
                Log.i("ui", String.format("Removing Item at index: %d", cSize));
            }
            this.adapter.notifyDataSetChanged(); // Notify the adapter to update the view
            this.userDD.setSelection(0); // Adjust selection to last item
            Log.i("ui", "spinnerItems: " + spinnerItems);
        }
    }
    private void applyAlphanumericFilter(TextInputEditText editText) {
        InputFilter alphanumericFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";  // Block non-alphanumeric characters
                }
            }
            return null;  // Accept the original input
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