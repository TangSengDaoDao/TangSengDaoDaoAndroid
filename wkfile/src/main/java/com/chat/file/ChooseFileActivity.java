package com.chat.file;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.chat.base.WKBaseApplication;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.utils.WKDialogUtils;
import com.chat.base.utils.WKFileUtils;
import com.chat.base.utils.WKPermissions;
import com.chat.base.utils.WKReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.file.databinding.ActChooseFileLayoutBinding;
import com.chat.file.msgitem.FileContent;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.message.type.WKMsgContentType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 2020-08-13 15:46
 * 选择文件
 */
public class ChooseFileActivity extends WKBaseActivity<ActChooseFileLayoutBinding> {
    private FileAdapter adapter;

    @Override
    protected ActChooseFileLayoutBinding getViewBinding() {
        return ActChooseFileLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.str_file_documents);
    }

    @Override
    protected String getRightTvText(TextView textView) {
        return getString(R.string.str_file_send);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        WKMsg msg = null;
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).checked) {
                msg = adapter.getData().get(i).msg;
                break;
            }
        }
        if (msg != null) {
            WKFileApplication.getInstance().sendMessage((FileContent) msg.baseContentMsgModel);
            finish();
        }
    }


    @Override
    protected void initView() {
        wkVBinding.refreshLayout.setEnableRefresh(false);
        wkVBinding.refreshLayout.setEnableLoadMore(false);
        wkVBinding.refreshLayout.setEnableNestedScroll(true);
        adapter = new FileAdapter();
        initAdapter(wkVBinding.recyclerView, adapter);
        getData();
    }

    @Override
    protected void initListener() {
        adapter.setOnItemClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view -> {
            ChooseFileEntity entity = (ChooseFileEntity) adapter1.getItem(position);
            if (entity != null) {
                Intent intent = new Intent(ChooseFileActivity.this, ChatFileActivity.class);
                intent.putExtra("clientMsgNo", entity.msg.clientMsgNO);
                startActivity(intent);
            }
        }));
        wkVBinding.choosePhoneTv.setOnClickListener(v -> {
            WKBaseApplication.getInstance().disconnect = false;
            checkManageAllPermission();

        });

    }

    private void checkStorage() {
        String desc = String.format(getString(R.string.file_permissions_des), getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        WKBaseApplication.getInstance().disconnect = false;
                        //  previewNewImgResultLac.launch("*/*");
                        try {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            if (Build.VERSION.SDK_INT >= 18) {
                                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            }
                            photoPickerIntent.setType("*/*");
                            previewNewImgResultLac.launch(photoPickerIntent);
                        } catch (Exception ignored) {
                        }
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            WKPermissions.getInstance().checkPermissions(new WKPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        WKBaseApplication.getInstance().disconnect = false;


                        try {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            if (Build.VERSION.SDK_INT >= 18) {
                                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            }
                            photoPickerIntent.setType("*/*");
                            previewNewImgResultLac.launch(photoPickerIntent);
                        } catch (Exception e) {
                        }

//                        Intent intent = new Intent();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                        }
//                        previewNewImgResultLac.launch("*/*");
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    ActivityResultLauncher<Intent> previewNewImgResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null && result.getData() != null) {
            if (result.getData().getData() != null) {
                String path = WKFileUtils.getInstance().getChooseFileResultPath(ChooseFileActivity.this, result.getData().getData());
                sendFile(path);
            } else if (result.getData().getClipData() != null) {
                ClipData clipData = result.getData().getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    String path = WKFileUtils.getInstance().getPath(clipData.getItemAt(i).getUri());
                    sendFile(path);
                }
            }
        }
    });

    private void checkManageAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // 有权限
                checkStorage();
            } else {
                WKDialogUtils.getInstance().showDialog(this, "", getString(R.string.manager_all_permission), true, "", getString(R.string.go_to_setting), 0, 0, index -> {
                    if (index == 1) {
                        // 无权限
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + ChooseFileActivity.this.getPackageName()));
                        launcher.launch(intent);
                    }

                });


            }
        } else {
            checkStorage();
        }
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> checkStorage());

    @Override
    protected void onResume() {
        super.onResume();
        WKBaseApplication.getInstance().disconnect = true;
    }

    private void getData() {
        List<ChooseFileEntity> tempList = new ArrayList<>();
        int oldLastClientSeq = 0;
        List<WKMsg> list = WKIM.getInstance().getMsgManager().getWithContentType(WKMsgContentType.WK_FILE, oldLastClientSeq, 20);
        if (WKReader.isNotEmpty(list)) {
            for (WKMsg msg : list) {
                FileContent fileContent = (FileContent) msg.baseContentMsgModel;
                if (!TextUtils.isEmpty(fileContent.url)) {
                    tempList.add(new ChooseFileEntity(msg));
                } else {
                    if (!TextUtils.isEmpty(fileContent.localPath)) {
                        File file = new File(fileContent.localPath);
                        if (file.exists()) {
                            tempList.add(new ChooseFileEntity(msg));
                        }
                    }
                }
            }
        }
        adapter.setList(tempList);
    }

    private void sendFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (WKFileUtils.getInstance().isFileOverSize(ChooseFileActivity.this, path)) {
                return;
            }
            File file = new File(path);
            FileContent fileContent = new FileContent();
            fileContent.localPath = path;
            fileContent.name = file.getName();
            fileContent.size = file.length();
            WKFileApplication.getInstance().sendMessage(fileContent);
            finish();
        }
    }
}
