package com.hawesome.bleconnector.model

/*
* 通信设备模型
* */
data class DeviceModel(val types: List<DeviceType>) {
    companion object {
        //items中item信息分隔：ON/AAAA/green
        const val ITEM_INFO_SEPARATOR = "/"

        //items中空item
        const val ITEM_NULL = ""

        //items[0]=step,表示等差范围序列
        const val ITEM_STEP_ARRAY = "step"

        //items[0]=accumulation,表示累加值
        const val ITEM_ACCUMULATION = "accumulation"

        //单位分隔符：100*In*Ir
        const val UNIT_INFO_SEPARATOR = "*"

        //控制模式：本地、远程
        const val AUTHORITY_MODE = "CtrlMode"
        const val LOCAL = "local"

        //控制密码
        const val AUTHORITY_CODE = "CtrlCode"
    }

}

/*
* 设备类型
* name:设备类型名称，e.g.XST-7T
* type:类型简称，e.g.7T
* address:地址片区列表
* authority:权限控制信息，e.g.["CtrlMode/9/0/local/1/remote","CtrlCode/%04X"]
*   -CtrlMode,本地/远程控制位，两种方式：
*       → -1/76/local/82/remote，整个参数值区分控制位
*       → n/0/local/1/remote,参数为开关位，第[n]位bit中0:local,1:remote,0≤n≤16
*   -CtrlCode，远程操作，控制密码
*       → %04X,4位16进制，高位补0
* pages:UI页面信息
* */
data class DeviceType(
    val name: String,
    val type: String,
    val address: List<Address>,
    val authority: List<String>,
    val pages: List<DevicePage>
) {}

/*
* 地址片区
* location:片区起始地址，16进制，e.g.0x0000
* items:参数列表
* log:记录片区，默认为null(普通片区）
*   -log>0时，为记录片区，第n条记录起始地址：location+N*items.size
* */
data class Address(val location: String, val items: List<String>, val log: Int = 0) {

    fun getLocation() = location.toInt(16)
}

/*
* 设备UI页面
* title:页面主旨
* addresses:涉及参数所属片区（起始地址），固定值，读取一次即可
* refresh:  涉及参数所属片区（起始地址），变化值，需实时刷新
* content: 页面参数列表
* */
data class DevicePage(
    val title: String, val content: List<DevicePageItem>,
    val addresses: List<String>? = null,
    val refresh: List<String>? = null
) {}

/*
* 页面参数列表项
* name：名称
* display: 列表样式
* tags：参数点列表，Address.items
* child: 详情——下一级参数点列表，Address.items，通常点击section进入下一级列表页面
* section: 区域标题
* items: 说明信息
*   - display：ATSStatus,bit
*       → 开关位，["value","valueOFF/valueON","valueON/valueOFF",""]
*           ☆一个值（value），开关位bit=1时有值
*           ☆二个值（valueOFF/valueON）,开关位bit=0:valueOFF,bit=1:valueON
*           ☆空值，此开关位无意义
*   - display: bar
*       → 范围柱状图，["0","0.1*In/yellow","5*In*Ir/red","10*In*Ir"
*           ☆量程：首尾是最小值、最大值
*           ☆值换算：double系数*引用值
*           ☆参考线：值/线条颜色
*   - display: range
*       → 范围长条，["0","green","0.3*In","yellow","0.7*In","red","In"]
*           ☆0←--绿--→30←--黄--→70←--红--→100
*   - display: list，item
*       → 固定值，["0/value1","10/value2","30/value3"]
*   - display: slider
*       → 滑块（多值选择），["step","10","1","100"]
*           ☆step/from/step/to
*   - display: segment
*       → 分段选择，[bit,length,num1/value1,num2/value2,,num3/value3……]
*           ☆bit,length:[-1,0],参数值；[1,3],开关位，起始bit=1,3个开关位
*
*   - display: onoff, 开关，[0],[bit/value1(bit=0)/value2(bit=1)]
*       → 开关位bit,后续开关位说明（可选）
*   - display: button,按键
*       → ["text1/value1/color1","text2/value2/color2"]
*   - display: log,记录条数
*       → ["0x6000"],关联记录片区起始地址
* unit: 单位,e.g.:0.1*In*Ir double系数*引用值1*引用值2
* secondary: 默认显示，在【详情·下一级】中的列表项设置初始状态为：不显示
* */
data class DevicePageItem(
    val name: String,
    val display: String,
    val tags: List<String>? = null,
    val section: String? = null,
    val items: ArrayList<String>? = null,
    val child: ArrayList<String>? = null,
    val unit: String? = null,
    val secondary: Boolean = false
) {
    fun getDisplay(): DeviceDisplay {// = DeviceDisplay.valueOf(display.toUpperCase())
        try {
            return DeviceDisplay.valueOf(display.toUpperCase())
        } catch (exp: Exception) {
            return DeviceDisplay.LIST
        }
    }
}