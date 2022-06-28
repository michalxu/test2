package com.goertek.rox2.common.entity;

import com.goertek.rox2.common.Const;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;
/**
 * 文件名：EQConfig
 * 描述：EQ配置 默认EQ + 用户EQ
 * 创建时间：2020/9/7
 * @author jochen.zhang
 */
@XStreamAlias("EQConfig")
public class EQConfig {
    /** 默认EQ列表 */
    private List<EQModel> mDefaultList;
    /** 用户EQ列表 */
    @XStreamImplicit(itemFieldName = "userEQ")
    private List<EQModel> mUserList;

    /** @return 默认EQ列表 */
    private List<EQModel> getDefaultList() {
        if (mDefaultList == null) {
            mDefaultList = new ArrayList<>();
            EQModel defaultModel1 = new EQModel();
            defaultModel1.setName("Bass Mode");
            defaultModel1.setDefault(true);
            for (int i = 0; i < Const.DEFAULT_EQ_1.length; i++) {
                defaultModel1.setLeftEq(i, Const.DEFAULT_EQ_1[i]);
                defaultModel1.setRightEq(i, Const.DEFAULT_EQ_1[i]);
            }
            EQModel defaultModel2 = new EQModel();
            defaultModel2.setName("Balance Mode");
            defaultModel2.setDefault(true);
            for (int i = 0; i < Const.DEFAULT_EQ_2.length; i++) {
                defaultModel2.setLeftEq(i, Const.DEFAULT_EQ_2[i]);
                defaultModel2.setRightEq(i, Const.DEFAULT_EQ_2[i]);
            }
            EQModel defaultModel3 = new EQModel();
            defaultModel3.setName("Treble Mode");
            defaultModel3.setDefault(true);
            for (int i = 0; i < Const.DEFAULT_EQ_3.length; i++) {
                defaultModel3.setLeftEq(i, Const.DEFAULT_EQ_3[i]);
                defaultModel3.setRightEq(i, Const.DEFAULT_EQ_3[i]);
            }

            mDefaultList.add(defaultModel1);
            mDefaultList.add(defaultModel2);
            mDefaultList.add(defaultModel3);
        }
        return mDefaultList;
    }

    /** @return 用户EQ列表 */
    public List<EQModel> getUserList() {
        return mUserList;
    }

    /** @return 默认EQ Size + 用户EQ Size */
    public int size() {
        // 用户EQ
        int userSize = 0;
        if (mUserList != null) {
            userSize = mUserList.size();
        }
        return userSize + getDefaultList().size();
    }

    /** @return (默认EQ列表 + 用户EQ列表) */
    public EQModel get(int index) {
        if (index < mDefaultList.size()) {
            return mDefaultList.get(index);
        } else {
            return mUserList.get(index - mDefaultList.size());
        }
    }

    public void add(EQModel model) {
        if (mUserList == null) {
            mUserList = new ArrayList<>();
        }
        mUserList.add(model);
    }

    public void remove(EQModel model) {
        if (mUserList != null) {
            mUserList.remove(model);
        }
    }

    /** @param index 包含default */
    public void remove(int index) {
        if (mUserList != null) {
            mUserList.remove(index - getDefaultList().size());
        }
    }
}
