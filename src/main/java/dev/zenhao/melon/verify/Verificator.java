package dev.zenhao.melon.verify;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Verificator {
    public Verificator() {
        copyToClipboard(HWIDUtil.getEncryptedHWID("Melon"));
    }

    public static void copyToClipboard(String s) {
        StringSelection selection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

}

