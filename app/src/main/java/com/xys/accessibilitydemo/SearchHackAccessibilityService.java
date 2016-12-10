package com.xys.accessibilitydemo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xys.accessibilitydemo.utils.BaseAccessibilityService;

public class SearchHackAccessibilityService extends BaseAccessibilityService {

    private String mPackageName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mPackageName = event.getPackageName().toString();
        if ("com.hujiang.hjclass".equals(mPackageName)) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                goThrough(rootNode);
            }
        }
    }

    private boolean goThrough(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null && info.getText().toString().contains("搜索")) {
                if ("在沪江网校中搜索".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {
                    AccessibilityNodeInfo parent = info;
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }
                } else if ("输入关键字搜索".equals(info.getText().toString()) && "android.widget.EditText".equals(info.getClassName())) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("paste", "雅思英语");
                    clipboardManager.setPrimaryClip(clipData);
                    info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                } else if ("搜索".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {
                    AccessibilityNodeInfo parent = info;
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }
                    return true;
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    goThrough(info.getChild(i));
                }
            }
        }
        return false;
    }
}
