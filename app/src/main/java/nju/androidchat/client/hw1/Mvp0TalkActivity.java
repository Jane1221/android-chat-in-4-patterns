package nju.androidchat.client.hw1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.java.Log;
import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.R;
import nju.androidchat.client.Utils;
import nju.androidchat.client.component.ItemTextReceive;
import nju.androidchat.client.component.ItemTextSend;
import nju.androidchat.client.component.OnRecallMessageRequested;

@Log
public class Mvp0TalkActivity extends AppCompatActivity implements Mvp0Contract.View, TextView.OnEditorActionListener, OnRecallMessageRequested {
    private Mvp0Contract.Presenter presenter;
    private Mvp0Contract.ImagePresenter imagePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mvp0TalkModel mvp0TalkModel = new Mvp0TalkModel();

        // Create the presenter
        this.presenter = new Mvp0TalkPresenter(mvp0TalkModel, this, new ArrayList<>());
        this.imagePresenter=new Mvp0TalkPresenter();
        mvp0TalkModel.setIMvp0TalkPresenter(this.presenter);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            UUID mID;
            mID = UUID.fromString(message.getData().getString("mID"));
            Bitmap bm;
            bm = (Bitmap) message.obj;
            if (message.what==0){
                sendImage(bm,mID);
            }else{
                receiveImage(bm, mID);
            }
        }
    };

    private void sendImage(Bitmap bitmap, UUID messageID){
        LinearLayout content=findViewById(R.id.chat_content);
        content.addView(new ItemTextSend(this,toSpanString(bitmap),messageID,this));
        Utils.scrollListToBottom(this);
    }
    private void receiveImage(Bitmap bitmap, UUID messageID){
        LinearLayout content=findViewById(R.id.chat_content);
        content.addView(new ItemTextReceive(this,toSpanString(bitmap),messageID));
        Utils.scrollListToBottom(this);
    }

    private SpannableString toSpanString(Bitmap bitmap){
        ImageSpan imageSpan=new ImageSpan(this, bitmap);
        SpannableString spannableString=new SpannableString("0");
        spannableString.setSpan(imageSpan,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showMessageList(List<ClientMessage> messages) {
        runOnUiThread(() -> {
                    LinearLayout content = findViewById(R.id.chat_content);

                    // 删除所有已有的ItemText
                    content.removeAllViews();

                    // 增加ItemText
                    for (ClientMessage message : messages) {
                        String text = String.format("%s", message.getMessage());
                        if (text.substring(0,5).equals("![]({") && text.substring(text.length()-2).equals("})")){
                            text=text.substring(5,text.length()-2);
                            if (text.substring(0,4).equals("http")){
                                //如果是自己发的,显示图片
                                if (message.getSenderUsername().equals(this.presenter.getUsername())){
                                    imagePresenter.showImage(handler, text,message.getMessageId(),0);
                                }else{
                                    imagePresenter.showImage(handler, text,message.getMessageId(),1);
                                }
                            }else{
                                text="加载失败！";
                                if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                                    content.addView(new ItemTextSend(this, text, message.getMessageId(), this));
                                } else {
                                    content.addView(new ItemTextReceive(this, text, message.getMessageId()));
                                }
                            }
                        } else{
                            // 如果是自己发的，增加ItemTextSend
                            if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                                content.addView(new ItemTextSend(this, text, message.getMessageId(), this));
                            } else {
                                content.addView(new ItemTextReceive(this, text, message.getMessageId()));
                            }
                        }
                    }
                    Utils.scrollListToBottom(this);
                }
        );
    }

    @Override
    public void setPresenter(Mvp0Contract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            return hideKeyboard();
        }
        return super.onTouchEvent(event);
    }

    private boolean hideKeyboard() {
        return Utils.hideKeyboard(this);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (Utils.send(actionId, event)) {
            hideKeyboard();
            // 异步地让Controller处理事件
            sendText();
        }
        return false;
    }

    private void sendText() {
        EditText text = findViewById(R.id.et_content);
        AsyncTask.execute(() -> {
            this.presenter.sendMessage(text.getText().toString());
        });
    }

    public void onBtnSendClicked(View v) {
        hideKeyboard();
        sendText();
    }

    // 当用户长按消息，并选择撤回消息时做什么，MVP-0不实现
    @Override
    public void onRecallMessageRequested(UUID messageId) {

    }
}
