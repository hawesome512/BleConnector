package com.hawesome.bleconnector.model

/*
*   设备页面子项显示类型
* */
enum class DeviceDisplay {

    //柱状图
    BAR,

    //按键
    BUTTON,

    //开关
    ONOFF,

    //范围条
    RANGE,

    //大文本
    TEXT,

    //可调参数(5~10)：旋钮
    ITEM,

    //可调参数(>10)：滑块
    SLIDER,

    //可调参数(<5)：分段控件
    SEGMENT,

    //记录类
    LOG,

    //位状态
    BIT,

    //默认序列
    LIST,

    //BleConnector特殊定制
    //软件版本
    VERSION,

    //在线升级
    OTA,

    //时钟
    TIME,

    //ATS特殊定制
    ATSSTATUS,
    ATSMODE
}