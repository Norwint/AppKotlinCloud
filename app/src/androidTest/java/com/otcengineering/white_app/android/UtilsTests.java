package com.otcengineering.white_app.android;

import androidx.test.runner.AndroidJUnit4;

import com.otcengineering.white_app.utils.Utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UtilsTests {
    @Test
    public void isImage() throws Exception {
        Assert.assertTrue(Utils.isImage("a.jpg"));
        Assert.assertTrue(Utils.isImage("a.jpeg"));
        Assert.assertTrue(Utils.isImage("a.png"));
        Assert.assertFalse(Utils.isImage("a"));
        Assert.assertFalse(Utils.isImage("a.mp4"));
        Assert.assertFalse(Utils.isImage("a.mp3"));
        Assert.assertFalse(Utils.isImage("a.html"));
        Assert.assertFalse(Utils.isImage("a.bin"));
    }
}
