package com.goertek.rox2.ui.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.bluetooth.protocol.tws.entity.TWSResult;
import com.goertek.common.utils.ProtocolUtils;
import com.goertek.common.utils.Utils;
import com.goertek.db.bean.EQTable;
import com.goertek.db.bean.FrequencyWithdB;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.MyApplication;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.hearing_test.activity.HearingTestDoneActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/26
 *
 * @author michal.xu
 */
public class eqAdapter extends RecyclerView.Adapter<eqAdapter.MyHolder> {

    private RoxLitePal litePal;
    private final List<String> mNameList;//数据源
    private final Context mContext;
    private boolean isClickable;
    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
    public void setSelectedUserName(String selectedUserName) {
        this.selectedUserName = selectedUserName;
    }

    private String selectedUserName = "";
    private final List<Boolean> isClickList;
    public eqAdapter(List<String> list, Context context) {
        mNameList = list;
        mContext = context;
        isClickList = new ArrayList<>();
        for(int i = 0;i<mNameList.size();i++){
            isClickList.add(false);
        }
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        litePal = RoxLitePal.getInstance();
        //将我们自定义的item布局R.layout.item_one转换为View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.eq_item_layout, parent, false);
        //将view传递给我们自定义的ViewHolder
//        MyHolder holder = new MyHolder(view);

        //返回这个MyHolder实体
        return new MyHolder(view);
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolder中
    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        String name = mNameList.get(position);
        LogUtils.d("name=-="+name);
        holder.nameTv.setText(mNameList.get(position));
        if (!selectedUserName.equals("")){
            if (name.equals(selectedUserName)){
                LogUtils.d("performClick");
                for(int i = 0; i <isClickList.size();i++){
                    isClickList.set(i,false);
                }
                isClickList.set(position,true);
                selectedUserName = "";
            }
        }
        // 如果设置了回调，则设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("click");
                if (isClickable){
                    for(int i = 0; i <isClickList.size();i++){
                        isClickList.set(i,false);
                    }
                    isClickList.set(position,true);
                    notifyDataSetChanged();
                }

            }
        });
        if(isClickList.get(position)){
            if (name.equals("Default mode")){
                LogUtils.d("Default click");
                List<EQTable> eqTableList =  litePal.roxLitePalCheck.getEqTableByName(name);
                String data = eqTableList.get(0).getFrequencyWithdB();
                LogUtils.d("michal,set eq data"+data);
                List<FrequencyWithdB> frequencyWithdBList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
                int[] tempDb = new int[frequencyWithdBList.size()];
                for (int j=0; j<frequencyWithdBList.size();j++){
                    tempDb[j] = (frequencyWithdBList.get(j).getdBValue()- MyApplication.STANDAR_EQ[j]);
                }

                Utils.setEq(tempDb, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        holder.layout.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_blue,null));
                        holder.nameTv.setTextColor(Color.WHITE);
                        holder.recommendTv.setTextColor(Color.WHITE);
                        holder.editTv.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_eq_default_on,null));

                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }else {
                LogUtils.d("other click");
                List<EQTable> eqTableList =  litePal.roxLitePalCheck.getEqTableByName(name);
                String data = eqTableList.get(0).getFrequencyWithdB();
                List<FrequencyWithdB> frequencyWithdBList = new Gson().fromJson(data,new TypeToken<List<FrequencyWithdB>>(){}.getType());
                int[] tempDb = new int[frequencyWithdBList.size()];
                for (int j=0; j<frequencyWithdBList.size();j++){
                    tempDb[j] = (frequencyWithdBList.get(j).getdBValue()-MyApplication.STANDAR_EQ[j]);
                }
                Utils.setEq(tempDb, new IRspListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] object) {
                        holder.layout.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_blue,null));
                        holder.nameTv.setTextColor(Color.WHITE);
                        holder.recommendTv.setTextColor(Color.WHITE);
                        holder.editTv.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_eq_edit_on,null));

                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        }else {
            if (name.equals("Default mode")){
                LogUtils.d("Default not click");
                holder.layout.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_white,null));
                holder.nameTv.setTextColor(mContext.getResources().getColor(R.color.text_color_black));
                holder.recommendTv.setTextColor(mContext.getResources().getColor(R.color.text_color_black));
                holder.editTv.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_eq_default_off,null));
            }else {
                LogUtils.d("other not click");
                holder.layout.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_white,null));
                holder.nameTv.setTextColor(mContext.getResources().getColor(R.color.text_color_black));
                holder.recommendTv.setTextColor(mContext.getResources().getColor(R.color.text_color_black));
                holder.editTv.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.ic_eq_edit_off,null));
            }
        }
        holder.editTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClickable){
                    Intent intent = new Intent(mContext, HearingTestDoneActivity.class);
                    String name = (String) mNameList.get(position);
                    if (!name.equals("Default mode")){
                        intent.putExtra("name",name);
                        intent.putExtra("comeFrom",1);
                        mContext.startActivity(intent);
                    }
                }

            }
        });

    }

    //获取数据源总的条数
    @Override
    public int getItemCount() {
        return mNameList.size();
    }
    public void addItem(String name){
        isClickList.add(false);
    }
    public void deleteItem(int position){
        isClickList.remove(position);
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView nameTv;
        TextView recommendTv;
        TextView editTv;
        LinearLayout layout;
        public MyHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.eq_item);
            nameTv = itemView.findViewById(R.id.eq_item_name);
            recommendTv = itemView.findViewById(R.id.eq_item_recommend);
            editTv = itemView.findViewById(R.id.eq_item_edit);
        }
    }


}
