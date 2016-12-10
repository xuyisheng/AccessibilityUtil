package com.xys.accessibilitydemo;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xys.accessibilitydemo.utils.BaseAccessibilityService;

import java.io.IOException;
import java.util.List;

public class AutoReplyAccessibilityService extends BaseAccessibilityService {

    private final static String MM_PNAME = "com.tencent.mm";
    boolean hasAction = false;
    boolean locked = false;
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;
    private KeyguardManager.KeyguardLock kl;
    private Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (!TextUtils.isEmpty(content)) {
                            locked = false;
                            background = true;
                            notifyWechat(event);
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (!hasAction) break;
                itemNodeinfo = null;
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    if (fill()) {
                        send();
                    }
                }
                hasAction = false;
                break;
        }
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }

            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
            pressBackButton();
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拉起微信界面
     *
     * @param event event
     */
    private void notifyWechat(AccessibilityEvent event) {
        hasAction = true;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            String[] cc = content.split(":");
            name = cc[0].trim();
            scontent = cc[1].trim();
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean fill() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findEditText(rootNode, "Auto Reply");
        }
        return false;
    }

    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                continue;
            }
            if (nodeInfo.getContentDescription() != null) {
                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);
                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
                if (nindex != -1) {
                    itemNodeinfo = nodeInfo;
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY, arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }
            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }
        return false;
    }
}
