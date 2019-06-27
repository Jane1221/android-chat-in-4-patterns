package nju.androidchat.client.hw1;

import android.os.Handler;

import java.util.List;
import java.util.UUID;

import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.mvp0.BasePresenter;
import nju.androidchat.client.mvp0.BaseView;

public interface Mvp0Contract {
    interface View extends BaseView<Presenter> {
        void showMessageList(List<ClientMessage> messages);
    }

    interface Presenter extends nju.androidchat.client.mvp0.BasePresenter {
        void sendMessage(String content);

        void receiveMessage(ClientMessage content);

        String getUsername();

        //撤回消息mvp0不实现
        void recallMessage(int index0);
    }

    interface Model {
        ClientMessage sendInformation(String message);

        String getUsername();
    }

    interface ImagePresenter extends BasePresenter {
        void showImage(Handler handler, String url, UUID messageId, int type);
    }
}
