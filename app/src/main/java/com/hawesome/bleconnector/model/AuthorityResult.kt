package com.hawesome.bleconnector.model

/*
* 设备操作权限
* */
enum class AuthorityResult {
    //用户锁，权限等级不够
    USER_LOCKED,
    //设备本地锁
    LOCAL_LOCKED,
    //用户已获得授权（不需二次验证）
    GRANTED,
    //验证中（输入验证密码）
    VERIFYING,
    //取消验证（密码
    CANCELED
}