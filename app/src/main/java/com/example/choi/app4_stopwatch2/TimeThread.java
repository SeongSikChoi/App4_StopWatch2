package com.example.choi.app4_stopwatch2;
import android.os.Handler;

public class TimeThread extends Thread{
    Handler handler;
    boolean isRun = true;
    boolean isWait = false;
    //생성자
    public TimeThread(Handler handler){
        this.handler = handler;

    }
    //스레드 일시 정지 혹은 재시작 하는 메소드
    public void pauseNResume(boolean isWait){
        synchronized (this) {
            this.isWait=isWait;
            notify();
        }
    }
    //스레드 완전 정지 시키는 메소드
    public void stopForever(){
        synchronized (this) {
            isRun = false;
            notify();
        }
    }

    //스레드 본체(한번 끝난 thread는 다시 쓸수없다. 다시 객체를 재생성해서 사용해야함)
    public void run(){
        while(isRun){
            try{
                //매주기 10/1000 초씩 쉰다.
                Thread.sleep(10);

            }catch (Exception e) {
                // TODO: handle exception
            }
            if(isWait){//스탑일때 isWait를 true로 바꿔준다.
                try{
                    synchronized (this) {//스레드가 실행중에 값이 바뀌면 출동이 날 우려가 있으므로 동기화 블럭에서 해주는 것들이 있다.
                        wait(); //notify할 때까지 기다린다.
                    }
                }catch (Exception e) {
                    // TODO: handle exception
                }
            }

            //핸들러에 메세지를 보낸다
            handler.sendEmptyMessage(0);
        }
    }

}

