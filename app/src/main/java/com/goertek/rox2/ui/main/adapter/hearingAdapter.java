package com.goertek.rox2.ui.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goertek.bluetooth.protocol.function.IRspListener;
import com.goertek.common.utils.Utils;
import com.goertek.db.bean.HearingTable;
import com.goertek.db.port.RoxLitePal;
import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.pop_dialog.SelectHearingModeDialog;
import com.goertek.rox2.ui.main.utils.SendWdrcData;
import com.goertek.rox2.ui.main.utils.WdrcDataListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2021/7/27
 *
 * @author michal.xu
 */
public class hearingAdapter extends RecyclerView.Adapter<hearingAdapter.MyViewHolder> {

    public List<String> itemNameList;
    public List<Boolean> selectedList = new ArrayList<>();
    private final Context mContext;
    private final SelectHearingModeDialog mDialog;
    public static Listener editListener;
    private RoxLitePal litePal;

    private String selecteedItem;
    public hearingAdapter(List<String> itemNameList,Context context,SelectHearingModeDialog dialog){
        mContext = context;
        this.itemNameList = itemNameList;
        mDialog = dialog;
        for (int i=0; i<itemNameList.size(); i++){
            selectedList.add(false);
        }

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        litePal = RoxLitePal.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hearing_mode_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String name = itemNameList.get(position);
        holder.hearingItemName.setText(name);
        holder.hearingItemSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("hearingItemSelect");
                List<HearingTable> hearingTableList = litePal.roxLitePalCheck.getHearingTableByName(name);
                byte[] wdrcData = null;
                if (hearingTableList.size()==1){
                    wdrcData = hearingTableList.get(0).getWdrcData();
                }
                if (wdrcData!=null){
                    LogUtils.d("发送数据");
                    SendWdrcData sendWdrcData = new SendWdrcData(wdrcData);
                    sendWdrcData.sendData(new WdrcDataListener() {
                        @Override
                        public void onFinish() {
                            for (int i=0; i<selectedList.size(); i++){
                                selectedList.set(i,false);
                            }
                            selectedList.set(position,true);
                            mDialog.setSelectItem(name);
                            LogUtils.d("name==="+name+"position=="+position);
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        selecteedItem = mDialog.getSelectItem();
        if (selecteedItem!=null&& !selecteedItem.equals("")){
            if (name.equals(selecteedItem)){
                for (int i=0; i<selectedList.size(); i++){
                    selectedList.set(i,false);
                }
                selectedList.set(position,true);
                selecteedItem = "";
            }
        }

        holder.heaingItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
                editListener.onEditClick(name,selecteedItem);
            }
        });
        if (selectedList.get(position)){
            holder.hearingItemSelect.setText("selected");
            holder.hearingItemSelect.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_gray,null));
        }else {
            holder.hearingItemSelect.setText("select");
            holder.hearingItemSelect.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.michal_bg_blue,null));
        }

    }

    @Override
    public int getItemCount() {
        return itemNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView hearingItemName;
        TextView hearingItemSelect;
        TextView heaingItemEdit;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            hearingItemName = itemView.findViewById(R.id.heaing_item_name);
            hearingItemSelect = itemView.findViewById(R.id.heaing_item_select);
            heaingItemEdit = itemView.findViewById(R.id.heaing_item_edit);

        }
    }
    public static void setListener(Listener listener){
        editListener = listener;
    }
     public  interface Listener{
        void onEditClick(String name,String selecteedItem);
     }
}
