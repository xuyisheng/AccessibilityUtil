package com.xys.accessibilitydemo;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xys.accessibilitydemo.utils.BaseAccessibilityService;

/**
 * 自动安装
 * <p>
 * Created by xuyisheng on 16/12/10.
 */

public class AutoInstallAccessibilityService extends BaseAccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getPackageName().equals("com.android.packageinstaller")) {
            AccessibilityNodeInfo nodeInfo = findViewByText("安装", true);
            if (nodeInfo != null) {
                performViewClick(nodeInfo);
            }
        }
    }
}
