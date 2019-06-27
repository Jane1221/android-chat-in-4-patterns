package nju.androidchat.client.hw1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nju.androidchat.client.ClientMessage;

@NoArgsConstructor
@AllArgsConstructor
public class Mvp0TalkPresenter implements Mvp0Contract.Presenter,Mvp0Contract.ImagePresenter {

    private Mvp0Contract.Model mvp0TalkModel;
    private Mvp0Contract.View iMvp0TalkView;

    @Getter
    private List<ClientMessage> clientMessages;

    @Override
    public void sendMessage(String content) {
        ClientMessage clientMessage = mvp0TalkModel.sendInformation(content);
        refreshMessageList(clientMessage);
    }

    @Override
    public void receiveMessage(ClientMessage clientMessage) {
        refreshMessageList(clientMessage);
    }

    @Override
    public String getUsername() {
        return mvp0TalkModel.getUsername();
    }

    private void refreshMessageList(ClientMessage clientMessage) {
        clientMessages.add(clientMessage);
        iMvp0TalkView.showMessageList(clientMessages);
    }

    //撤回消息，Mvp0暂不实现
    @Override
    public void recallMessage(int index0) {

    }

    @Override
    public void start() {

    }

    @Override
    public void showImage(Handler handler, String url, UUID messageId, int type) {
        new Thread(()->{
            try {
                URL imageUrl;
                imageUrl = new URL(url);
                HttpURLConnection conn;
                conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                conn.setConnectTimeout(8000);
                conn.setRequestMethod("GET");
                Bitmap bm=null;
                InputStream inputStream=null;
                if (conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    bm = BitmapFactory.decodeStream(inputStream);
                }
                Bundle bundle=new Bundle();
                bundle.putString("mID",messageId.toString());
                Message message=handler.obtainMessage(type);
                message.obj=bm;
                message.setData(bundle);
                handler.sendMessage(message);
                assert inputStream != null;
                inputStream.close();
                conn.disconnect();
            }catch(MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
