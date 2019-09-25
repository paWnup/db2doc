package com.pa.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pa.plugin.ui.MainDialog;

public class MainAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        new MainDialog(e);
    }
}
