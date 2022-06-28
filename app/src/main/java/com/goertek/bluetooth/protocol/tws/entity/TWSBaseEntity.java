package com.goertek.bluetooth.protocol.tws.entity;

public class TWSBaseEntity {
    protected boolean isCommand;
    protected boolean needResponse;
    protected byte OpCode;
    protected byte OpCodeSn;
    private boolean isSuccess = false;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public void setIsCommand(boolean isCommand) {
        this.isCommand = isCommand;
    }

    public boolean isNeedResponse() {
        return needResponse;
    }

    public void setNeedResponse(boolean needResponse) {
        this.needResponse = needResponse;
    }

    public byte getOpCode() {
        return OpCode;
    }

    public void setOpCode(byte opCode) {
        OpCode = opCode;
    }

    public byte getOpCodeSn() {
        return OpCodeSn;
    }

    public void setOpCodeSn(byte opCodeSn) {
        OpCodeSn = opCodeSn;
    }
}
