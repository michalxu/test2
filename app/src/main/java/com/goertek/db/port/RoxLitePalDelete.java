package com.goertek.db.port;

import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.HearingTable;
import com.goertek.rox2.ui.main.LogUtils;

import org.litepal.LitePal;

/**
 * 创建时间：2021/7/15
 *
 * @author michal.xu
 */
public class RoxLitePalDelete {
    public void deleteEq(String name){
        LitePal.deleteAll(EQTable.class,"name=?",name);
    }
    public void deleteHearing(String name){
        LogUtils.d("result34="+LitePal.deleteAll(HearingTable.class,"name=?",name));
    }
}
