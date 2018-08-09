package com.github.charleslzq.hwr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
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
    LinearLayout candidatesBar;
    @BindView(R.id.undoButton)
    Button undoButton;
    @BindView(R.id.redoButton)
    Button redoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        handWritingView.onCandidatesAvailable(new HandWritingView.ResultHandler() {
            @Override
            public void receive(@NotNull List<? extends Candidate> candidates) {
                updateButtonState();
                candidatesBar.removeAllViewsInLayout();
                for (Candidate candidate: candidates) {
                    Button button = new Button(candidatesBar.getContext());
                    candidate.bind(button);
                    candidatesBar.addView(button);
                }
            }
        });
        handWritingView.onCandidateSelected(new HandWritingView.CandidateHandler() {
            @Override
            public void selected(@NotNull String content) {
                textView.append(content);
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
        candidatesBar.removeAllViewsInLayout();
        updateButtonState();
    }

    @OnClick(R.id.resetButton)
    public void reset() {
        textView.setText("");
        clear();
    }

    private void updateButtonState() {
        undoButton.setEnabled(handWritingView.canUndo());
        redoButton.setEnabled(handWritingView.canRedo());
    }
}
