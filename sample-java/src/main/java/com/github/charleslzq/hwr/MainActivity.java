package com.github.charleslzq.hwr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.text)
    TextView textView;
    @BindView(R.id.hwrView)
    HandWritingView handWritingView;
    @BindView(R.id.candidates)
    CandidateButtonBar candidateButtonBar;
    @BindView(R.id.undoButton)
    Button undoButton;
    @BindView(R.id.redoButton)
    Button redoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        candidateButtonBar.link(handWritingView);
        candidateButtonBar.onCandidateSelected(new CandidateButtonBar.CandidateHandler() {
            @Override
            public void selected(@NotNull String content) {
                textView.append(content);
                updateButtonState();
            }
        });
        handWritingView.onResult(new HandWritingView.ResultHandler() {
            @Override
            public void receive(@NotNull List<? extends Candidate> candidates) {
                updateButtonState();
            }
        });
    }

    @OnClick(R.id.undoButton)
    public void undo() {
        handWritingView.undo();
        updateButtonState();
    }

    @OnClick(R.id.redoButton)
    public void redo() {
        handWritingView.redo();
        updateButtonState();
    }

    @OnClick(R.id.clearButton)
    public void clear() {
        handWritingView.reset();
        updateButtonState();
    }

    @OnClick(R.id.resetButton)
    public void reset() {
        textView.setText("");
        candidateButtonBar.clear();
        clear();
    }

    private void updateButtonState() {
        undoButton.setEnabled(handWritingView.canUndo());
        redoButton.setEnabled(handWritingView.canRedo());
    }
}
