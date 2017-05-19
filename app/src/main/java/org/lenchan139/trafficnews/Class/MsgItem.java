package org.lenchan139.trafficnews.Class;

/**
 * Created by len on 19/5/2017.
 */

public class MsgItem {
    double lag;
    double lnt;
    String msg;
    public MsgItem(double g,double t,String m){
        lag = g;
        lnt = t;
        msg = m;
    }
    public double getLag() {
        return lag;
    }

    public void setLag(double lag) {
        this.lag = lag;
    }

    public double getLnt() {
        return lnt;
    }

    public void setLnt(double lnt) {
        this.lnt = lnt;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return lag + "," + lnt;
    }
}
