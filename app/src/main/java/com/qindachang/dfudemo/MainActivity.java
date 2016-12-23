package com.qindachang.dfudemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qindachang.bluetoothle.BluetoothLe;
import com.qindachang.bluetoothle.OnLeConnectListener;
import com.qindachang.bluetoothle.OnLeScanListener;

import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class MainActivity extends AppCompatActivity {
    private static final String SERVICE_UUID = "6E401001-B5A3-F393-E0A9-E50E24DCCA0E";
    private static final String WRITE_UUID = "6E401003-B5A3-F393-E0A9-E50E24DCCA0E";
    private static final String DEVICE_NAME = "heatclothes";

    private boolean isDisvocerService = false;

    private TextView tv_text,mTvOtaUploadNotice;
    private ProgressBar mProgressBarOtaUpload;

    private StringBuilder mStringBuilder;
    private BluetoothLe mBluetoothLe;
    private BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_connect = (Button) findViewById(R.id.btn_connect);
        Button btn_update = (Button) findViewById(R.id.btn_update);
        tv_text = (TextView) findViewById(R.id.tv_text);
        mProgressBarOtaUpload = (ProgressBar) findViewById(R.id.progressBar_dfu);
        mTvOtaUploadNotice = (TextView) findViewById(R.id.tv_update_notice);

        mBluetoothLe = BluetoothLe.getDefault();
        mBluetoothLe.init(this);
        mStringBuilder = new StringBuilder();

        if (!mBluetoothLe.isBluetoothOpen()) {
            mBluetoothLe.enableBluetooth(this);
        }

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStringBuilder.append("开始扫描\n");
                showLog();
                scan();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvOtaUploadNotice.setText("正在升级，请等待升级成功");
                mBluetoothLe.writeDataToCharacteristic(new byte[]{-1, -2, -3}, SERVICE_UUID, WRITE_UUID);
                startDFU(mBluetoothDevice, true, false, true, 0, "");

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLe.getConnected()) {
            mBluetoothLe.destroy();
            mBluetoothLe.close();
        }
    }

    private void scan() {
        mBluetoothLe.setScanPeriod(15000)
                .setScanWithServiceUUID(SERVICE_UUID)
                .setScanWithDeviceName(DEVICE_NAME)
                .setReportDelay(0)
                .startScan(this, new OnLeScanListener() {
                    @Override
                    public void onScanResult(BluetoothDevice bluetoothDevice, int rssi, ScanRecord scanRecord) {
                        mStringBuilder.append("发现设备，开始连接\n");
                        showLog();
                        mBluetoothDevice = bluetoothDevice;
                        mBluetoothLe.stopScan();
                        connect();
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {

                    }

                    @Override
                    public void onScanCompleted() {
                        mStringBuilder.append("停止扫描\n");
                        showLog();
                        if (mBluetoothDevice == null) {
                            mStringBuilder.append("没有发现设备\n");
                            showLog();
                        }
                    }

                    @Override
                    public void onScanFailed(int code) {
                        mStringBuilder.append("扫描错误");
                        showLog();
                    }
                });
    }

    private void connect() {
        mBluetoothLe.startConnect(mBluetoothDevice, new OnLeConnectListener() {
            @Override
            public void onDeviceConnecting() {

            }

            @Override
            public void onDeviceConnected() {
                mStringBuilder.append("连接成功，正在发现服务\n");
                showLog();
            }

            @Override
            public void onDeviceDisconnected() {
                mStringBuilder.append("断开连接\n");
                showLog();
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt) {
                mStringBuilder.append("已发现服务，可以升级了\n");
                showLog();
                isDisvocerService = true;
            }

            @Override
            public void onDeviceConnectFail() {
                mStringBuilder.append("连接失败\n");
                showLog();
            }
        });
    }


    DfuProgressListener mDfuProgressListener = new DfuProgressListener() {

        @Override
        public void onDeviceConnecting(String deviceAddress) {
            //当DFU服务开始与DFU目标连接时调用的方法
            Log.d("debug", "DFU服务开始与DFU目标连接," + deviceAddress);
            mStringBuilder.append("升级服务开始与硬件设备连接.\n");
            showLog();
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            //方法在服务成功连接时调用，发现服务并在DFU目标上找到DFU服务。
            Log.d("debug", "服务成功连接,发现服务并在DFU目标上找到DFU服务." + deviceAddress);
            mStringBuilder.append("升级服务连接成功.\n");
            showLog();
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            //当DFU进程启动时调用的方法。 这包括读取DFU版本特性，发送DFU START命令以及Init数据包（如果设置）。
            Log.d("debug", "DFU进程启动," + deviceAddress);
            mStringBuilder.append("升级进程启动.\n");
            showLog();
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            //当DFU进程启动和要发送的字节时调用的方法。
            Log.d("debug", "DFU进程启动和要发送的字节," + deviceAddress);
        }

        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            //当服务发现DFU目标处于应用程序模式并且必须切换到DFU模式时调用的方法。 将发送开关命令，并且DFU过程应该再次开始。 此调用后不会有onDeviceDisconnected（String）事件。
            Log.d("debug", "当服务发现DFU目标处于应用程序模式并且必须切换到DFU模式时调用的方");
            mStringBuilder.append("硬件设备切换到升级模式.\n");
            showLog();
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            //在上传固件期间调用的方法。 它不会使用相同的百分比值调用两次，但是在小型固件文件的情况下，可能会省略一些值。\
            mProgressBarOtaUpload.setProgress(percent);
            mTvOtaUploadNotice.setText("进度：" + percent + "%");
            Log.d("debug", "percent:" + percent + " partsTotal:" + partsTotal);
            mStringBuilder.append("状态：升级中...");
            showLog();
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            //在目标设备上验证新固件时调用的方法。
            Log.d("debug", "目标设备上验证新固件时调用的方法");
            mStringBuilder.append("硬件设备正在验证新固件.\n");
            showLog();
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            //服务开始断开与目标设备的连接时调用的方法。
            Log.d("debug", "服务开始断开与目标设备的连接时调用的方法");
            mStringBuilder.append("服务开始断开设备连接.\n");
            showLog();
        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            //当服务从设备断开连接时调用的方法。 设备已重置。
            Log.d("debug", "当服务从设备断开连接时调用的方法。 设备已重置。");
            mStringBuilder.append("硬件设备已重置.\n");
            showLog();
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            //Method called when the DFU process succeeded.
            Log.d("debug", "DFU已完成");
            mStringBuilder.append("升级成功！\n");
            showLog();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            //当DFU进程已中止时调用的方法。
            Log.d("debug", "当DFU进程已中止时调用的方法。");
            mStringBuilder.append("升级进程已中止.\n");
            showLog();
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            //发生错误时调用的方法。
            Log.d("debug", "发生错误时调用的方法onError");
            mStringBuilder.append("升级发生错误.\n");
            showLog();
        }
    };

    /**
     * 启动DFU升级服务
     *
     * @param bluetoothDevice 蓝牙设备
     * @param keepBond        升级后是否保持连接
     * @param force           将DFU设置为true将防止跳转到DFU Bootloader引导加载程序模式
     * @param PacketsReceipt  启用或禁用数据包接收通知（PRN）过程。
     *                        默认情况下，在使用Android Marshmallow或更高版本的设备上禁用PEN，并在旧设备上启用。
     * @param numberOfPackets 如果启用分组接收通知过程，则此方法设置在接收PEN之前要发送的分组数。 PEN用于同步发射器和接收器。
     * @param filePath        约定匹配的ZIP文件的路径。
     */
    private void startDFU(BluetoothDevice bluetoothDevice, boolean keepBond, boolean force,
                          boolean PacketsReceipt, int numberOfPackets, String filePath) {
        final DfuServiceInitiator stater = new DfuServiceInitiator(bluetoothDevice.getAddress())
                .setDeviceName(bluetoothDevice.getName())
                .setKeepBond(keepBond)
                .setForceDfu(force)
                .setPacketsReceiptNotificationsEnabled(PacketsReceipt)
                .setPacketsReceiptNotificationsValue(numberOfPackets);
        stater.setZip(R.raw.update);//这个方法可以传入raw文件夹中的文件、也可以是文件的string或者url路径。
        stater.start(this, DfuService.class);
    }

    private void showLog() {
        tv_text.setText(mStringBuilder.toString());
    }
}
