#BluetoothLE DFU Demo

功能描述：硬件升级/空中升级/DFU

Demo运行环境：Android Studio 2.2.3

如果对你有帮助，欢迎star。

Demo中依赖我的另一个低功耗蓝牙库，同时也欢迎star。谢谢。

[https://github.com/qindachang/BluetoothLELibrary](https://github.com/qindachang/BluetoothLELibrary "https://github.com/qindachang/BluetoothLELibrary")

##教程

###Step1:准备及连接蓝牙

添加nordicsemi的DFU开源库依赖：

    compile 'no.nordicsemi.android:dfu:1.0.4'

在这里，笔者所使用来自于自己的低功耗蓝牙库：

    compile 'com.qindachang:BluetoothLELibrary:0.4.1'

连接蓝牙过程不再叙述。如果你想懒汉试开发蓝牙，欢迎使用我的蓝牙库。

###Step2:创建DFU的Service服务

【图】
![image](https://github.com/qindachang/DFUDemo/blob/master/image/20161223163555.png)

需要值得注意的是，AndroidManifest.xml文件中有Service标签

        <service
            android:name=".DfuService"
            android:enabled="true"
            android:exported="true">
        </service>

创建好后，添加以下代码：

    public class DfuService extends DfuBaseService {
        @Override
        protected Class<? extends Activity> getNotificationTarget() {

            return MainActivity.class;
        }
    }



###Step3:DfuProgressListener回调及启动升级

DfuProgressListener监听中，是升级过程关键信息的回调，例如开始升级、百分比、升级失败、升级成功等信息。

启动升级较为简单，直接使用DfuServiceInitiator类即可。


详情请看Demo:[链接][MainActivity.java](https://github.com/qindachang/DFUDemo/blob/master/app/src/main/java/com/qindachang/dfudemo/MainActivity.java "MainActivity.java")

###

